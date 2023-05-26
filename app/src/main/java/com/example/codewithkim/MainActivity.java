package com.example.codewithkim;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout noInternetLayout;
    private final int INTERVAL = 3600000; // 1 hour
    private Handler mHandler;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_info) {
            // Display app information or perform any desired action
            showAppInfoDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAppInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App Information")
                .setMessage("Twitter @codewithkim")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove the title bar
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressbar);
        noInternetLayout = findViewById(R.id.noInternetLayout);

        webView = findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());

        checkInternet();
        mHandler = new Handler();
        startRepeatingTask();

        Button refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternet();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if (uri.getHost() != null && uri.getHost().contains("bugdevs.com")) {
                view.loadUrl(url);
            } else {
                openLink(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            if (noInternetLayout.getVisibility() == View.VISIBLE) {
                noInternetLayout.setVisibility(View.GONE);
            }
        }
    }

    private void openLink(String url) {
        if (url.startsWith("mailto:")) { // check if the link is a mail link
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        } else if (url.startsWith("tel:")) { // check if the link is a tel link
            Intent telIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(telIntent);
        } else { // handle other types of links
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    private boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            webView.loadUrl("https://bugdevs.com/index.html");
            noInternetLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            return true;
        } else {
            noInternetLayout.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            return false;
        }
    }

    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            checkInternet();
            mHandler.postDelayed(mStatusChecker, INTERVAL);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
