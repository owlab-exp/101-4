package com.owlab.callquieter.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.owlab.callquieter.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ernest on 7/9/16.
 */
public class HelpDialog extends Dialog {
    private static final String TAG = HelpDialog.class.getSimpleName();

    private Context context = null;
    public HelpDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.help_dialog_layout);

        WebView helpView = (WebView) findViewById(R.id.helpWebView);
        final WebSettings webViewSettings = helpView.getSettings();
        int fontSize = (int) context.getResources().getDimension(R.dimen.webViewTextSize);
        webViewSettings.setDefaultFontSize(fontSize);

        helpView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if(url.startsWith("mailto:")) {
                    MailTo mailTo = MailTo.parse(url);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo.getTo()});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "CallQuieter feedback");
                    context.startActivity(Intent.createChooser(intent, "Send beedback ..."));
                } else {
                    webView.loadUrl(url);
                }

                return true;
            }
        });

        String helpText = readRawTextFile(R.raw.help_contents);
        helpView.loadData(helpText, "text/html; charset=utf-8", "utf-8");

        // No effect
        TextView titleTV = (TextView) findViewById(android.R.id.title);
        Log.d(TAG, ">>>>> titleTV: " + titleTV);

        if(titleTV != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) titleTV.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER;
            titleTV.setLayoutParams(layoutParams);
        }
    }

    private String readRawTextFile(int id) {
        InputStream in = context.getResources().openRawResource(id);
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader buffer = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            while((line = buffer.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch(IOException e) {
            return null;
        }
        return stringBuilder.toString();
    }
}
