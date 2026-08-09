package com.projectmayhem.pdf;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.webkit.WebViewAssetLoader;

import org.json.JSONObject;

import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single-purpose, offline WebView host for Project Mayhem.
 *
 * PDF bytes are produced by the bundled editor and transferred through the narrow
 * Javascript interface below. Android's Storage Access Framework writes them to a
 * user-selected document tree. The persisted URI grant means the directory is
 * selected once and remains usable after restarts without broad storage access.
 */
public final class MainActivity extends ComponentActivity {
    private static final String APP_URL =
            "https://appassets.androidplatform.net/assets/index.html";
    private static final String PREFS = "project_mayhem_preferences";
    private static final String KEY_OUTPUT_TREE = "output_tree_uri";
    private static final int MAX_BASE64_CHARS = 140_000_000; // ~100 MiB PDF ceiling.

    private WebView webView;
    private SharedPreferences preferences;
    private ActivityResultLauncher<Intent> folderPickerLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ValueCallback<Uri[]> filePathCallback;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private boolean pageReady;
    private boolean automaticFolderPromptShown;
    @Nullable private String pendingPdfBase64;
    @Nullable private String pendingPdfName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        registerActivityResultLaunchers();
        createAndConfigureWebView();

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            webView.loadUrl(APP_URL);
        }
    }

    private void registerActivityResultLaunchers() {
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleFolderPickerResult);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    ValueCallback<Uri[]> callback = filePathCallback;
                    filePathCallback = null;
                    if (callback == null) return;
                    callback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(
                            result.getResultCode(), result.getData()));
                });
    }

    private void createAndConfigureWebView() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(10, 10, 10));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(10, 10, 10));
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        ViewCompat.setOnApplyWindowInsetsListener(webView, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            return windowInsets;
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Required by the local PDF editor.
        settings.setDomStorageEnabled(false);
        settings.setDatabaseEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true); // Required only for the image picker.
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSafeBrowsingEnabled(true);

        CookieManager.getInstance().setAcceptCookie(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        WebView.setWebContentsDebuggingEnabled(false);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(
                    @NonNull WebView view,
                    @NonNull WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    @NonNull WebView view,
                    @NonNull WebResourceRequest request) {
                Uri uri = request.getUrl();
                return !("https".equals(uri.getScheme())
                        && "appassets.androidplatform.net".equals(uri.getHost()));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> newCallback,
                    FileChooserParams fileChooserParams) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = newCallback;
                try {
                    Intent picker = fileChooserParams.createIntent();
                    picker.addCategory(Intent.CATEGORY_OPENABLE);
                    filePickerLauncher.launch(picker);
                    return true;
                } catch (RuntimeException error) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this,
                            R.string.image_picker_unavailable, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        // Exposed only to the app's bundled, CSP-restricted page at APP_URL.
        webView.addJavascriptInterface(new NativeBridge(), "ProjectMayhem");
    }

    private void handleFolderPickerResult(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() != Activity.RESULT_OK || data == null
                || data.getData() == null) {
            notifyFolderStatus();
            if (pendingPdfBase64 != null) {
                notifyPdfResult(false, getString(R.string.folder_required));
                clearPendingPdf();
            }
            return;
        }

        Uri selectedTree = data.getData();
        // ACTION_OPEN_DOCUMENT_TREE providers grant both flags; pass the explicit
        // documented mask so the persisted grant remains readable and writable.
        int grantFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(selectedTree, grantFlags);
        } catch (SecurityException error) {
            notifyPdfResult(false, getString(R.string.folder_permission_failed));
            return;
        }

        Uri previousTree = readSavedTreeUriWithoutValidation();
        preferences.edit().putString(KEY_OUTPUT_TREE, selectedTree.toString()).apply();
        if (previousTree != null && !previousTree.equals(selectedTree)) {
            try {
                getContentResolver().releasePersistableUriPermission(
                        previousTree,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // The provider may already have revoked the previous grant.
            }
        }

        notifyFolderStatus();
        if (pendingPdfBase64 != null && pendingPdfName != null) {
            String base64 = pendingPdfBase64;
            String name = pendingPdfName;
            clearPendingPdf();
            queuePdfWrite(selectedTree, base64, name);
        }
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        Uri currentTree = getValidSavedTreeUri();
        if (currentTree != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, currentTree);
        }
        folderPickerLauncher.launch(intent);
    }

    @Nullable
    private Uri readSavedTreeUriWithoutValidation() {
        String value = preferences.getString(KEY_OUTPUT_TREE, null);
        if (value == null || value.isEmpty()) return null;
        try {
            return Uri.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Nullable
    private Uri getValidSavedTreeUri() {
        Uri saved = readSavedTreeUriWithoutValidation();
        if (saved == null) return null;
        boolean canWrite = getContentResolver().getPersistedUriPermissions().stream()
                .anyMatch(permission -> permission.getUri().equals(saved)
                        && permission.isWritePermission());
        if (!canWrite) {
            preferences.edit().remove(KEY_OUTPUT_TREE).apply();
            return null;
        }
        return saved;
    }

    private String getFolderDisplayName(@Nullable Uri treeUri) {
        if (treeUri == null) return getString(R.string.no_folder_selected);
        try {
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(
                    treeUri, DocumentsContract.getTreeDocumentId(treeUri));
            try (Cursor cursor = getContentResolver().query(
                    documentUri,
                    new String[]{OpenableColumns.DISPLAY_NAME},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                    return cursor.getString(0);
                }
            }
        } catch (RuntimeException ignored) {
            // Fall back to a provider-neutral label below.
        }
        return getString(R.string.selected_folder);
    }

    private void notifyFolderStatus() {
        runOnUiThread(() -> {
            if (!pageReady || webView == null) return;
            Uri treeUri = getValidSavedTreeUri();
            String script = "window.onNativeFolderChanged && window.onNativeFolderChanged("
                    + JSONObject.quote(getFolderDisplayName(treeUri)) + ","
                    + (treeUri != null) + ");";
            webView.evaluateJavascript(script, null);
        });
    }

    private void queuePdfWrite(Uri treeUri, String base64, String requestedName) {
        ioExecutor.execute(() -> writePdf(treeUri, base64, requestedName));
    }

    private void writePdf(Uri treeUri, String base64, String requestedName) {
        try {
            byte[] pdfBytes = Base64.decode(base64, Base64.DEFAULT);
            if (pdfBytes.length < 5
                    || pdfBytes[0] != '%'
                    || pdfBytes[1] != 'P'
                    || pdfBytes[2] != 'D'
                    || pdfBytes[3] != 'F'
                    || pdfBytes[4] != '-') {
                throw new IllegalArgumentException(getString(R.string.invalid_pdf));
            }

            ContentResolver resolver = getContentResolver();
            Uri parent = DocumentsContract.buildDocumentUriUsingTree(
                    treeUri, DocumentsContract.getTreeDocumentId(treeUri));
            String safeName = sanitizeFileName(requestedName);
            Uri created = DocumentsContract.createDocument(
                    resolver, parent, "application/pdf", safeName);
            if (created == null) throw new IllegalStateException(getString(R.string.create_failed));

            try (OutputStream output = resolver.openOutputStream(created, "w")) {
                if (output == null) throw new IllegalStateException(getString(R.string.open_failed));
                output.write(pdfBytes);
                output.flush();
            }

            String actualName = queryDocumentName(created, safeName);
            notifyPdfResult(true, getString(
                    R.string.saved_to,
                    getFolderDisplayName(treeUri),
                    actualName));
        } catch (SecurityException error) {
            preferences.edit().remove(KEY_OUTPUT_TREE).apply();
            notifyPdfResult(false, getString(R.string.folder_access_lost));
            notifyFolderStatus();
        } catch (Exception error) {
            String detail = error.getMessage();
            if (detail == null || detail.trim().isEmpty()) {
                detail = error.getClass().getSimpleName();
            }
            notifyPdfResult(false, getString(R.string.save_failed, detail));
        }
    }

    private String queryDocumentName(Uri documentUri, String fallback) {
        try (Cursor cursor = getContentResolver().query(
                documentUri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0);
            }
        } catch (RuntimeException ignored) {
            // Some providers do not expose DISPLAY_NAME after creation.
        }
        return fallback;
    }

    private String sanitizeFileName(@Nullable String requestedName) {
        String name = requestedName == null ? "PROJECT_MAYHEM.pdf" : requestedName.trim();
        name = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_")
                .replaceAll("\\s+", " ");
        if (name.length() > 96) name = name.substring(0, 96);
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) name += ".pdf";
        if (name.equalsIgnoreCase(".pdf")) name = "PROJECT_MAYHEM.pdf";
        return name;
    }

    private void notifyPdfResult(boolean success, String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            if (!pageReady || webView == null) return;
            String script = "window.onNativePdfSaved && window.onNativePdfSaved("
                    + success + "," + JSONObject.quote(message) + ");";
            webView.evaluateJavascript(script, null);
        });
    }

    private void clearPendingPdf() {
        pendingPdfBase64 = null;
        pendingPdfName = null;
    }

    private final class NativeBridge {
        @JavascriptInterface
        public void onAppReady() {
            runOnUiThread(() -> {
                pageReady = true;
                notifyFolderStatus();
                if (getValidSavedTreeUri() == null && !automaticFolderPromptShown) {
                    automaticFolderPromptShown = true;
                    openFolderPicker();
                }
            });
        }

        @JavascriptInterface
        public void chooseOutputFolder() {
            runOnUiThread(MainActivity.this::openFolderPicker);
        }

        @JavascriptInterface
        public void savePdf(String base64, String requestedName) {
            if (base64 == null || base64.isEmpty()) {
                notifyPdfResult(false, getString(R.string.empty_pdf));
                return;
            }
            if (base64.length() > MAX_BASE64_CHARS) {
                notifyPdfResult(false, getString(R.string.pdf_too_large));
                return;
            }

            Uri treeUri = getValidSavedTreeUri();
            if (treeUri == null) {
                pendingPdfBase64 = base64;
                pendingPdfName = requestedName;
                runOnUiThread(MainActivity.this::openFolderPicker);
                return;
            }
            queuePdfWrite(treeUri, base64, requestedName);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (webView != null) {
            ((View) webView.getParent()).setVisibility(View.GONE);
            webView.removeJavascriptInterface("ProjectMayhem");
            webView.destroy();
            webView = null;
        }
        if (isFinishing()) ioExecutor.shutdownNow();
        super.onDestroy();
    }
}
