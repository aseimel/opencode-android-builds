package ai.opencode.mobile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String USERNAME = "armin";

    private SharedPreferences prefs;
    private FrameLayout root;
    private WebView webView;
    private String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("opencode-web", MODE_PRIVATE);
        root = new FrameLayout(this);
        setContentView(root);

        String savedUrl = prefs.getString("server_url", "");
        String savedPassword = prefs.getString("server_password", "");
        if (!savedUrl.isEmpty() && !savedPassword.isEmpty()) {
            password = savedPassword;
            showWebView(savedUrl);
        } else {
            showConnectScreen(savedUrl, savedPassword);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void showConnectScreen(String initialUrl, String initialPassword) {
        root.removeAllViews();
        webView = null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(28), dp(28), dp(28), dp(28));
        layout.setBackgroundColor(Color.rgb(10, 10, 10));

        TextView title = new TextView(this);
        title.setText("OpenCode Web");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        layout.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Connects as Basic Auth user: " + USERNAME);
        subtitle.setTextColor(Color.rgb(150, 150, 150));
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.setMargins(0, dp(10), 0, dp(34));
        layout.addView(subtitle, subtitleParams);

        EditText urlInput = input("Server URL, e.g. http://157.180.5.117:4096", false);
        urlInput.setText(initialUrl);
        layout.addView(urlInput, inputParams());

        EditText passwordInput = input("Password", true);
        passwordInput.setText(initialPassword);
        layout.addView(passwordInput, inputParams());

        Button connect = new Button(this);
        connect.setText("Connect");
        connect.setTextColor(Color.WHITE);
        connect.setTextSize(16);
        connect.setBackgroundColor(Color.rgb(99, 102, 241));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(58)
        );
        buttonParams.setMargins(0, dp(16), 0, 0);
        layout.addView(connect, buttonParams);

        TextView hint = new TextView(this);
        hint.setText("Start the server with: opencode web --hostname 0.0.0.0 --port 4096");
        hint.setTextColor(Color.rgb(115, 115, 115));
        hint.setTextSize(14);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = matchWrap();
        hintParams.setMargins(0, dp(28), 0, 0);
        layout.addView(hint, hintParams);

        connect.setOnClickListener(v -> {
            String url = normalizeUrl(urlInput.getText().toString());
            password = passwordInput.getText().toString();
            if (url.isEmpty()) {
                Toast.makeText(this, "Enter the OpenCode web URL", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit()
                .putString("server_url", url)
                .putString("server_password", password)
                .apply();
            showWebView(url);
        });

        root.addView(layout, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void showWebView(String url) {
        root.removeAllViews();

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.rgb(10, 10, 10));

        Button reset = new Button(this);
        reset.setText("Change Server");
        reset.setTextColor(Color.WHITE);
        reset.setBackgroundColor(Color.rgb(35, 35, 35));
        reset.setOnClickListener(v -> {
            String oldUrl = prefs.getString("server_url", "");
            String oldPassword = prefs.getString("server_password", "");
            prefs.edit().clear().apply();
            showConnectScreen(oldUrl, oldPassword);
        });
        container.addView(reset, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(48)
        ));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed(USERNAME, password);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                Toast.makeText(MainActivity.this, "SSL error loading OpenCode", Toast.LENGTH_LONG).show();
            }
        });

        container.addView(webView, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1
        ));
        root.addView(container);
        webView.loadUrl(url);
    }

    private EditText input(String hint, boolean passwordField) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setHintTextColor(Color.rgb(120, 120, 120));
        input.setTextColor(Color.WHITE);
        input.setTextSize(17);
        input.setSingleLine(true);
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setBackgroundColor(Color.rgb(28, 28, 28));
        if (passwordField) {
            input.setInputType(0x00000081);
        }
        return input;
    }

    private String normalizeUrl(String value) {
        String url = value.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private LinearLayout.LayoutParams inputParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(64)
        );
        params.setMargins(0, 0, 0, dp(14));
        return params;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
