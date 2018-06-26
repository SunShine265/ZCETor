package com.goodproductssoft.flypoolmonitor.activitys;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applovin.mediation.ApplovinAdapter;
import com.goodproductssoft.flypoolmonitor.CustomApp;
import com.goodproductssoft.flypoolmonitor.MyPreferences;
import com.goodproductssoft.flypoolmonitor.OnBroadcastService;
import com.goodproductssoft.flypoolmonitor.R;
import com.goodproductssoft.flypoolmonitor.models.Miner;
import com.goodproductssoft.flypoolmonitor.util.IabHelper;
import com.goodproductssoft.flypoolmonitor.util.IabResult;
import com.goodproductssoft.flypoolmonitor.util.Inventory;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.mopub.mobileads.dfp.adapters.MoPubAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.google.android.gms.ads.AdSize.BANNER;
import static com.google.android.gms.ads.AdSize.FULL_BANNER;
import static com.google.android.gms.ads.AdSize.LARGE_BANNER;
import static com.google.android.gms.ads.AdSize.LEADERBOARD;
import static com.google.android.gms.ads.AdSize.SMART_BANNER;

public class MainActivity extends AppCompatActivity implements FragmentMiner.ProgressDisplay,
        FragmentWorker.ProgressDisplay, FragmentPayouts.ProgressDisplay, FragmentPoolSettings.IProgressDisplay {
    //SharedPreferences pref;
    ImageView btnWorker, btnMiner, btnPayouts, btnSettings, icon_app;
    TextView title_app;
    RelativeLayout progressbar;
    MyPreferences myPreferences;
    ArrayList<Miner> miners;
    LinearLayout tab_settings, tab_payouts, tab_workers, tab_miner, id_ads_app;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd rewardedVideoAd;

    private static final String ADMOB_AD_UNIT_ID_BANNER = "ca-app-pub-1827062885697339/7957798806";
    private static final String ADMOB_AD_UNIT_ID_LEADERBOARD = "ca-app-pub-1827062885697339/2834569257";
    private static final String ADMOB_AD_UNIT_ID_INTERSTITIAL = "ca-app-pub-1827062885697339/2922176114";
    private static final String ADMOB_AD_UNIT_ID_REWARDEDVIDEO = "ca-app-pub-1827062885697339/4737573246";

    public final static int MAX_SHOW_ADS_REMAIN_TIMES = 20;
    public final static int MIN_SHOW_ADS_REMAIN_TIMES = 15;
    public final static boolean IS_SHOW_ADS = true;
    public final static int SHOW_RATE_REMAIN_TIMES = 20;

    Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIHandler = new Handler();

        setContentView(R.layout.activity_main);

        final IabHelper mHelper = CustomApp.getInstance().createIaHelper();

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    try {
                        ArrayList<String> items = new ArrayList<>();
                        items.add(CustomApp.ADS_ITEM_SKU);
                        mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                            @Override
                            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                                try {
                                    if (inventory.hasPurchase(CustomApp.ADS_ITEM_SKU)) {
//                                                   //Remove purchase for test devices
//                                                   Purchase premiumPurchase = inventory.getPurchase(CustomApp.ADS_ITEM_SKU);
//                                                   mHelper.consumeAsync(premiumPurchase, new IabHelper.OnConsumeFinishedListener() {
//                                                       @Override
//                                                       public void onConsumeFinished(Purchase purchase, IabResult result) {
//                                                       }
//                                                   });
                                        MyPreferences myPreferences = MyPreferences.getInstance();
                                        myPreferences.setRemoveAds(true);
                                        hideBannerAds();
                                    } else {
                                        MyPreferences myPreferences = MyPreferences.getInstance();
                                        myPreferences.setRemoveAds(false);
                                    }
                                }
                                catch (Exception ex){}
                            }
                        });
                    }
                    catch (Exception ex){
                    }
                }
            }
        });

        btnWorker = (ImageView) findViewById(R.id.btn_worker);
        btnMiner = (ImageView)findViewById(R.id.btn_miner);
        btnSettings = (ImageView)findViewById(R.id.btn_pool_settings);
        btnPayouts = (ImageView) findViewById(R.id.btn_payouts);
        progressbar = (RelativeLayout) findViewById(R.id.progressbar);
        tab_settings = (LinearLayout) findViewById(R.id.tab_settings);
        tab_payouts = (LinearLayout) findViewById(R.id.tab_payouts);
        tab_workers = (LinearLayout) findViewById(R.id.tab_workers);
        tab_miner = (LinearLayout) findViewById(R.id.tab_miner);
//        mAdView = findViewById(R.id.adView);
        id_ads_app = findViewById(R.id.id_ads_app);
        icon_app = findViewById(R.id.icon_app);
        title_app = findViewById(R.id.title_app);

        myPreferences = MyPreferences.getInstance();
        miners = myPreferences.GetIdMiners();
        Miner minerActive = GetMinerIdActive();
        if(minerActive != null ) {
            TabMinerSelected();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment fragment = new FragmentMiner();
            fragmentTransaction.replace(R.id.fragment_content, fragment);
            fragmentTransaction.commitAllowingStateLoss();
            UnlockItemMenu();

        } else {
            TabSettingsSelected();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment fragment = new FragmentPoolSettings();
            fragmentTransaction.replace(R.id.fragment_content, fragment);
            fragmentTransaction.commitAllowingStateLoss();
            LockItemMenu();
        }

        id_ads_app.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    title_app.setTextColor(getResources().getColor(R.color.colorWhite));
                    icon_app.setImageResource(R.drawable.menu_press);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    title_app.setTextColor(getResources().getColor(R.color.color_txt_average_hashrate));
                    icon_app.setImageResource(R.drawable.menu);

                    try {
                        //Close keyBoard in transition
                        InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    catch (Exception ex){}

                    TabAdsAppSelected();
                    FragmentAdsApp fragment = new FragmentAdsApp();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();

                }
                return true;
            }
        });

        final View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                //Fragment fragment = null;

                try {
                    //Close keyBoard in transition
                    InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                catch (Exception ex){}


                if(view == findViewById(R.id.btn_worker)){
                    TabWorkersSelected();
                    FragmentWorker fragment = new FragmentWorker();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                } else if(view == findViewById(R.id.btn_miner)){
                    TabMinerSelected();
                    FragmentMiner fragment = new FragmentMiner();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                }
                else if(view == findViewById(R.id.btn_payouts)){
                    TabPayoutsSelected();
                    FragmentPayouts fragment = new FragmentPayouts();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                }
                else {
                    TabSettingsSelected();
                    FragmentPoolSettings fragment = new FragmentPoolSettings();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_content, fragment);
                    transaction.commitAllowingStateLoss();
                }
            }
        };

        btnWorker.setOnClickListener(listener);
        btnMiner.setOnClickListener(listener);
        btnSettings.setOnClickListener(listener);
        btnPayouts.setOnClickListener(listener);

        int time_for_repeate = 1000 * 120;
        AlarmManager processTimer = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, OnBroadcastService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1012,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
        processTimer.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),time_for_repeate, pendingIntent);

        setViewMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (IS_SHOW_ADS && !MyPreferences.getInstance().getRemoveAds()) {
                if (timeBanner == null || timeBanner.getTime() + 1000 * 3600 < Calendar.getInstance().getTime().getTime()
                        ||  mAdView == null || mAdView.getVisibility() == View.GONE || mAdView.getHeight() == 0) {
                    showBannerAds();
                }
            }
        }
        catch (Exception ex){}
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof FragmentMiner
                || fragment instanceof FragmentPayouts
                || fragment instanceof FragmentWorker
                || fragment instanceof FragmentPoolSettings) {
            try {
                MyPreferences myPreferences = MyPreferences.getInstance();
                long remainTimes = Math.min(SHOW_RATE_REMAIN_TIMES, myPreferences.getShowRateRemainTimes());
                if (remainTimes > 0) {
                    remainTimes--;
                    myPreferences.setShowRateRemainTimes(remainTimes);
                    if(remainTimes == 0) {
                        long remainAdsTimes = myPreferences.getShowAdsRemainTimes();
                        myPreferences.setShowAdsRemainTimes(Math.max(remainAdsTimes, 6));

                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Rate us!");
                        alertDialog.setMessage("Could you please rate us 5 stars? Thank you.");
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Not now ",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, rate now",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                        try {
                                            startActivity(goToMarket);
                                        } catch (Exception e) {
                                            startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                                        }
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            } catch (Exception ex) {}

            if(IS_SHOW_ADS) {
                showAds();
            }
        }
    }

    @Override
    public void onAttachFragment(android.support.v4.app.Fragment fragment) {
        super.onAttachFragment(fragment);
        if(IS_SHOW_ADS) {
//        if(fragment instanceof FragmentMiner
//                || fragment instanceof FragmentPayouts
//                || fragment instanceof FragmentWorker
//                || fragment instanceof FragmentPoolSettings) {
            showAds();
//        }
        }
    }

    private Miner GetMinerIdActive(){
        if(miners != null && !miners.isEmpty()) {
            for (int i = 0; i < miners.size(); i++) {
                if (miners.get(i).isActive()) {
                    return miners.get(i);
                }
            }
        }
        return null;
    }

    private void TabAdsAppSelected(){
        id_ads_app.setBackgroundResource(R.color.background_selected);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    private void TabSettingsSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_selected);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    private void TabPayoutsSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_selected);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    private void TabWorkersSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_selected);
        tab_miner.setBackgroundResource(R.color.background_header);
    }

    @Override
    public void TabMinerSelected(){
        id_ads_app.setBackgroundResource(R.color.background_header);
        tab_settings.setBackgroundResource(R.color.background_header);
        tab_payouts.setBackgroundResource(R.color.background_header);
        tab_workers.setBackgroundResource(R.color.background_header);
        tab_miner.setBackgroundResource(R.color.background_selected);
    }

    @Override
    public void showProgress(){
        progressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress(){
        progressbar.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mAdView != null && mAdView.getParent() != null) {
                mAdView.setVisibility(View.GONE);
                ((ViewGroup)(mAdView.getParent())).removeView(mAdView);
                mAdView.destroy();
            }
        }
        catch (Exception e){}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(IS_SHOW_ADS && !MyPreferences.getInstance().getRemoveAds()){
            ViewGroup adViews = (ViewGroup)findViewById(R.id.adView);
            try {
                for (int i = 0; i < adViews.getChildCount(); i++) {
                    AdView adView = (AdView) adViews.getChildAt(i);
                    adView.setVisibility(View.GONE);
                    adView.destroy();
                }
            } catch (Exception ex){}
            adViews.removeAllViews();
            showBannerAds();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
//            if (mAdView != null && mAdView.getParent() != null) {
//                mAdView.setVisibility(View.GONE);
//                ((ViewGroup)(mAdView.getParent())).removeView(mAdView);
//                mAdView.destroy();
//            }
        }
        catch (Exception e){}
    }

    public void hideBannerAds() {
        if(mAdView != null) {
            mAdView.setVisibility(View.GONE);
        }
    }

    private void showBannerAds() {
        showBannerAds(SMART_BANNER);
    }

    private void showBannerAds(final AdSize adSize) {
        showBannerAds(adSize, false);
    }

    private AdSize bannerSizeLoading = null;
    private Date timeBanner = Calendar.getInstance().getTime();
    private long refreshTimes = 30 * 1000;

    private void showBannerAds(final AdSize adSize, boolean useBannerId){
        //TODO: Ads primary
        try {
//            if (mAdView != null) {
//                mAdView.destroy();
//                mAdView.setVisibility(View.GONE);
//                mAdView = null;
//            }
        }
        catch (Exception ex){}
        timeBanner = Calendar.getInstance().getTime();

        mAdView = new AdView(this);
        final AdView adViewFinal = mAdView;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        adViewFinal.setLayoutParams(layoutParams);
        ((ViewGroup)findViewById(R.id.adView)).addView(adViewFinal);
        adViewFinal.setVisibility(View.GONE);
        adViewFinal.setAdSize(adSize);

        float uiScreenWidth = getResources().getDisplayMetrics().widthPixels;
        final float uiScreenWidthDp = uiScreenWidth / getResources().getDisplayMetrics().density;
        float uiScreenHeight = getResources().getDisplayMetrics().heightPixels;
        final float uiScreenHeightDp = uiScreenHeight / getResources().getDisplayMetrics().density;

        if (useBannerId){
            adViewFinal.setAdUnitId(ADMOB_AD_UNIT_ID_BANNER);
        }
        else {
            if(adSize == SMART_BANNER) {
                if (uiScreenWidthDp >= 730 && uiScreenHeightDp > 720) {
                    adViewFinal.setAdUnitId(ADMOB_AD_UNIT_ID_LEADERBOARD);
                } else {
                    adViewFinal.setAdUnitId(ADMOB_AD_UNIT_ID_BANNER);
                }
            } else if (adSize == LEADERBOARD) {
                adViewFinal.setAdUnitId(ADMOB_AD_UNIT_ID_LEADERBOARD);
            } else {
                adViewFinal.setAdUnitId(ADMOB_AD_UNIT_ID_BANNER);
            }
        }


//
        //TODO: bundle for AppLovin banners
        Bundle bundleAppLovin = new Bundle();

//        TODO: bundle for Mopub
        Bundle bundleMopub = new MoPubAdapter.BundleBuilder()
                .build();
//
//        //TODO: bundle for Ads facebook
////        Bundle extras = new FacebookAdapter.FacebookExtrasBundleBuilder()
////                .setNativeAdChoicesIconExpandable(false)
////                .build();
//
        AdRequest adRequest = new AdRequest.Builder()
                //.addNetworkExtrasBundle(ApplovinAdapter.class, bundleAppLovin)
                //.addNetworkExtrasBundle(MoPubAdapter.class, bundleMopub)
                .build();

        bannerSizeLoading = adSize;

        adViewFinal.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerSizeLoading = null;
                if (!MyPreferences.getInstance().getRemoveAds()) {
                    ViewGroup adViews = (ViewGroup)findViewById(R.id.adView);
                    try {
                        for (int i = 0; i < adViews.getChildCount(); i++) {
                            AdView adView = (AdView)adViews.getChildAt(i);
                            if(adViewFinal != adView) {
                                adView.setVisibility(View.GONE);
                                adView.destroy();
                            }
                        }
                    } catch (Exception ex){}
                    adViews.removeAllViews();
                    adViews.addView(adViewFinal);
                    adViewFinal.setVisibility(View.VISIBLE);

                } else {
                    adViewFinal.setVisibility(View.GONE);
                    ((ViewGroup)findViewById(R.id.adView)).removeAllViews();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                try {
                    if (adViewFinal.getParent() != null) {
                        try {
                            if(((ViewGroup) findViewById(R.id.adView)).getChildCount() > 1 &&
                                    (adViewFinal.getVisibility() == View.GONE || adViewFinal.getHeight() == 0)) {
                                adViewFinal.setVisibility(View.GONE);
                                ((ViewGroup) findViewById(R.id.adView)).removeView(adViewFinal);
                                adViewFinal.destroy();
                            }
                        } catch (Exception ex) {
                        }

                        if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                            if (bannerSizeLoading == null) {
                                showBannerAds(SMART_BANNER);
                            } else if (bannerSizeLoading == SMART_BANNER) {
                                if (ADMOB_AD_UNIT_ID_LEADERBOARD.equals(adViewFinal.getAdUnitId())) {
                                    showBannerAds(SMART_BANNER, true);
                                } else {
                                    if (uiScreenWidthDp >= 730) {
                                        showBannerAds(LEADERBOARD);
                                    } else {
                                        showBannerAds(BANNER);
                                    }
                                }
                            } else if (bannerSizeLoading == LEADERBOARD) {
                                showBannerAds(BANNER);
                            } else if (bannerSizeLoading == BANNER) {
                                if (uiScreenWidthDp >= 470) {
                                    showBannerAds(FULL_BANNER);
                                } else {
                                    if (uiScreenHeightDp >= 680) {
                                        showBannerAds(LARGE_BANNER);
                                    } else {
                                        UIHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                refreshBannerAds(adViewFinal);
                                            }
                                        }, refreshTimes);
                                    }
                                }
                            } else if (bannerSizeLoading == FULL_BANNER) {
                                if (uiScreenHeightDp >= 680) {
                                    showBannerAds(LARGE_BANNER);
                                } else {
                                    UIHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            refreshBannerAds(adViewFinal);
                                        }
                                    }, refreshTimes);
                                }
                            } else {
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshBannerAds(adViewFinal);
                                    }
                                }, refreshTimes);
                            }
                        }
                    } else {
                        UIHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refreshBannerAds(adViewFinal);
                            }
                        }, refreshTimes);
                    }
                }
                catch(Exception ex) {
                    UIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshBannerAds(adViewFinal);
                        }
                    }, refreshTimes);
                }
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
//                if(adViewFinal != null){
//                    adViewFinal.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
//                if(adViewFinal != null){
//                    adViewFinal.setVisibility(View.GONE);
//                }
            }
        });
        adViewFinal.loadAd(adRequest);
    }

    public  void refreshBannerAds(AdView adView){
        try {

            boolean isShowing = findViewById(R.id.adView).getHeight() > 0;
            if (IS_SHOW_ADS && !MyPreferences.getInstance().getRemoveAds()) {
                if(!isShowing) {
                    ((ViewGroup)findViewById(R.id.adView)).removeAllViews();
                    showBannerAds();
                }
                else if(adView != null && adView.getParent() != null
//                    && adView.getVisibility() == View.GONE
//                    && !adView.isLoading()
//                    && adView == mAdView)
                        ) {
                    showBannerAds();
                }

            }
        }
        catch (Exception ex){}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager != null) {
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_content);
            if (fragment instanceof FragmentAdsApp) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private boolean lockedAds = false;
    private void showAds() {
        try {
            MyPreferences myPreferences = MyPreferences.getInstance();
            if (!myPreferences.getRemoveAds()) {
                long remainTimes = myPreferences.getShowAdsRemainTimes();
                if (remainTimes > 0) {
                    myPreferences.setShowAdsRemainTimes(remainTimes - 1);
                } else if (!lockedAds && UIHandler != null) {
                    long remainAdsTimes = myPreferences.getShowRateRemainTimes();
                    if (remainAdsTimes > 0) {
                        myPreferences.setShowRateRemainTimes(Math.max(remainAdsTimes, 6));
                    }

                    lockedAds = true;
                    UIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getIntersitialAds();
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        lockedAds = false;
                                    }
                                }, 6000);
                            } catch (Exception ex) {
                                lockedAds = false;
                            }
                        }
                    }, 2000);
                }
            }
        }
        catch (Exception ex){
            lockedAds = false;
        }
    }

    /**
     * Add Intersitial Ads
     * */
    public void getIntersitialAds(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(ADMOB_AD_UNIT_ID_INTERSTITIAL);

        // Set an AdListener.
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    MyPreferences myPreferences = MyPreferences.getInstance();
                    myPreferences.setShowAdsRemainTimes(new Random().nextInt(MAX_SHOW_ADS_REMAIN_TIMES - MIN_SHOW_ADS_REMAIN_TIMES + 1) + MIN_SHOW_ADS_REMAIN_TIMES);
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (i == 3) {
                    getRewardedVideoAds();
                }
            }

            @Override
            public void onAdClosed() {
            }
        });

        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    /**
     * ADd RewardedVideo Ads
     * */
    private void getRewardedVideoAds() {
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener()
        {
            @Override
            public void onRewardedVideoAdLeftApplication() {
            }

            @Override
            public void onRewardedVideoAdClosed() {
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
            }

            @Override
            public void onRewardedVideoAdLoaded() {
                showRewardedVideo();
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewarded(RewardItem reward) {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoCompleted() {
            }
        });

        loadRewardedVideoAd();
        showRewardedVideo();
    }

    private void LockItemMenu(){
        btnPayouts.setEnabled(false);
        btnWorker.setEnabled(false);
        btnMiner.setEnabled(false);
        id_ads_app.setEnabled(false);
        btnMiner.setAlpha(0.4f);
        btnWorker.setAlpha(0.4f);
        btnPayouts.setAlpha(0.4f);
        id_ads_app.setAlpha(0.4f);
    }

    @Override
    public void UnlockItemMenu(){
        btnPayouts.setEnabled(true);
        btnWorker.setEnabled(true);
        btnMiner.setEnabled(true);
        id_ads_app.setEnabled(true);

        btnMiner.setAlpha(1f);
        btnWorker.setAlpha(1f);
        btnPayouts.setAlpha(1f);
        id_ads_app.setAlpha(1f);
    }

    private void loadRewardedVideoAd() {
        if (!rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.loadAd(ADMOB_AD_UNIT_ID_REWARDEDVIDEO, new AdRequest.Builder().build());
        }
    }

    private void showRewardedVideo() {
        if (rewardedVideoAd.isLoaded()) {
            MyPreferences myPreferences = MyPreferences.getInstance();
            myPreferences.setShowAdsRemainTimes(new Random().nextInt(MAX_SHOW_ADS_REMAIN_TIMES - MIN_SHOW_ADS_REMAIN_TIMES + 1) + MIN_SHOW_ADS_REMAIN_TIMES);
            rewardedVideoAd.show();
        }
    }

    private void setViewMode(){
        int mode = MyPreferences.getInstance().getViewModes();
        switch (mode){
            case 0:
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                break;
            case 1:
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 2:
                MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }
}
