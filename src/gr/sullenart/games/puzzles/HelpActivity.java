package gr.sullenart.games.puzzles;

import gr.sullenart.ads.AdsManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class HelpActivity extends Activity {

	private WebView webview;

	private AdsManager adsManager = new AdsManager();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.web_view);

        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);

        final Context myApp = this;

        /* WebChromeClient must be set BEFORE calling loadUrl! */
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
            {
                new AlertDialog.Builder(myApp)
                    .setTitle(HelpActivity.this.getResources().getString(R.string.tip))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();

                return true;
            };
        });


		webview.loadUrl("file:///android_asset/" +
			  	getResources().getString(R.string.help_file_name));

        LinearLayout layout = (LinearLayout)findViewById(R.id.banner_layout_web);
        adsManager.addAdsView(this, layout);
	}
}
