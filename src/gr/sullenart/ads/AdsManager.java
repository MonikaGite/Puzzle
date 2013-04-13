package gr.sullenart.ads;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AdsManager implements AdListener {
	private String publisherId = "Your publisher Id here";

	public void addAdsView(Activity activity, LinearLayout layout) {
        AdView adView;
        int screenLayout = activity.getResources().getConfiguration().screenLayout;
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= 3) {
        	adView = new AdView(activity, AdSize.IAB_LEADERBOARD, publisherId);
        }
        else {
        	adView = new AdView(activity, AdSize.BANNER, publisherId);
        }
        layout.addView(adView);
        AdRequest request = new AdRequest();
        request.addTestDevice(AdRequest.TEST_EMULATOR);
        request.addTestDevice("Your test device id here");
        adView.loadAd(request);

        //Log.i(AdsManager.class.getName(), "Adding ads view");
	}

	@Override
	public void onDismissScreen(Ad arg0) {


	}

	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) {
		Log.e(AdsManager.class.getName(), "Failed to receive ad (" + errorCode + ")");
	}

	@Override
	public void onLeaveApplication(Ad arg0) {


	}

	@Override
	public void onPresentScreen(Ad arg0) {


	}

	@Override
	public void onReceiveAd(Ad arg0) {


	}

}