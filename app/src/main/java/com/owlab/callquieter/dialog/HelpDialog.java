package com.owlab.callquieter.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.owlab.callquieter.FUNS;
import com.owlab.callquieter.R;

/**
 * Created by ernest on 7/9/16.
 */
public class HelpDialog extends AppCompatDialog {
    private static final String TAG = HelpDialog.class.getSimpleName();

    private Context context;
    //private int layoutId;
    public HelpDialog(Context context) {
        //This is critical!
        //super(context, android.R.style.Theme_DeviceDefault_Dialog);
        //super(context, android.R.style.Theme_Material_Dialog);
        super(context);
        this.context = context;
    }


    //public HelpDialog(Context context, int layoutId) {
    //    super(context, layoutId);
    //    this.context = context;
    //    this.layoutId = layoutId;
    //}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.help_dialog_layout);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        WebView helpView = (WebView) findViewById(R.id.helpWebView);
        //helpView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        final WebSettings webViewSettings = helpView.getSettings();
        int fontSize = (int) context.getResources().getDimension(R.dimen.webViewTextSize);
        webViewSettings.setDefaultFontSize(fontSize);

        //webViewSettings.setJavaScriptEnabled(false);
        //helpView.setVerticalScrollBarEnabled(true);
        //helpView.setVerticalScrollBarEnabled(false);
        //webViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //if (Build.VERSION.SDK_INT >= 19) {
        //    helpView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //}
        //else {
        //    helpView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //}

        helpView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if(url.startsWith("mailto:")) {
                    MailTo mailTo = MailTo.parse(url);
                    //Intent intent = new Intent(Intent.ACTION_SEND);
                    //intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo.getTo()});
                    //Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mailTo.getTo(), null));
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    String uriText = "mailto:" + mailTo.getTo() +
                            "?subject=" + Uri.encode("CallQuieter feedback");
                    //intent.putExtra(Intent.EXTRA_SUBJECT, "CallQuieter feedback");
                    Uri uri = Uri.parse(uriText);
                    intent.setData(uri);
                    //To detect if email client exists
                    ComponentName emailApp = intent.resolveActivity(getContext().getPackageManager());
                    ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
                    boolean hasEmailApp = emailApp != null && !emailApp.equals(unsupportedAction);

                    if(hasEmailApp) {
                        context.startActivity(Intent.createChooser(intent, "Send feedback email"));
                    }
                    return true;
                } else {
                    webView.loadUrl(url);
                }

                return false;
            }
        });

        //String helpText = "haha";
        //long startTime = System.currentTimeMillis();
        String helpText = FUNS.readRawTextFile(getContext(), R.raw.help_contents);
        helpView.loadData(helpText, "text/html; charset=utf-8", "utf-8");
        //long endTime = System.currentTimeMillis();
        //////Log.d(TAG, ">>>>> time to load: " + (endTime - startTime));

        //// No effect
        //TextView titleTV = (TextView) findViewById(android.R.id.title);
        //////Log.d(TAG, ">>>>> titleTV: " + titleTV);

        //if(titleTV != null) {
        //    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) titleTV.getLayoutParams();
        //    layoutParams.gravity = Gravity.CENTER;
        //    titleTV.setLayoutParams(layoutParams);
        //}
    }

    /*
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
            buffer.close();
        } catch(IOException e) {
            return null;
        }
        return stringBuilder.toString();
    }
    */
}
