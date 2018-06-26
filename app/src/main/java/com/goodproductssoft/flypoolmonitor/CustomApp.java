package com.goodproductssoft.flypoolmonitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.applovin.sdk.AppLovinSdk;
import com.goodproductssoft.flypoolmonitor.util.IabHelper;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by user on 5/2/2018.
 */

public class CustomApp extends MultiDexApplication {
    private static CustomApp instance = null;

    final static String ADMOB_APP_ID = "ca-app-pub-1827062885697339~2552223604";
    public static final String ADS_ITEM_SKU = "com.goodproductssoft.flypoolmonitor.removeads";
    String base64EncodedPublicKey =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAikFtAZ5Dp2JNgJDATDjtl9sUQlh5cA6B+5fHOUyQl27LHYgBjvhUIGeddqTQt0/ImtoacdwZjnFkpsyMGdfxRdsw8XSGhRRwkY3RocrScgcIRVqV01jmxRpbHCBwIDl2vEmFrkrzDAMY0GgHFSIyEJzakhb7eQGH2wZCksDNnLq9sEtBKUY4suBiZWhIfW1tvdx/SD11Xc4wtX1scb9pAz3gB58posYhTIgsxUqbTGECFNGElXps87MtS2MYr3IKCrS3ThVepEC5JyrJRtd+QRgK5HuBE9t9/VWaCTafaBKjShR1vEvV+0yVyLPIUnlZsVwJfFfaUc7yVMx+V4EY4QIDAQAB";

    public final static CustomApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        MobileAds.initialize(this, ADMOB_APP_ID);
        AppLovinSdk.initializeSdk(this);

    }

    public IabHelper createIaHelper(){
        return new IabHelper(CustomApp.getInstance(), base64EncodedPublicKey);
    }

    public static void showToast(String message){
        showToast(Toast.LENGTH_LONG, message);
    }

    public static void showToast(int length, String message){
        if(instance != null) {
            Toast.makeText(instance,
                    message,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    public static OkHttpClient SetConnectTimeOut(){
        //setup cache
        File httpCacheDirectory = new File(instance.getCacheDir(), "responses");
        okhttp3.Cache cache = new okhttp3.Cache(httpCacheDirectory, 20 * 1024 * 1024);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_NETWORK_INTERCEPTOR)
                .addInterceptor(REWRITE_CACHE_CONTROL_NETWORK_INTERCEPTOR)
                .build();
        return okHttpClient;
    }

    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    private static final Interceptor REWRITE_CACHE_CONTROL_NETWORK_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            String cacheHeaderValue = isNetworkAvailable(instance)
                    ? "public, max-age=100"
                    : "public, only-if-cached" ;
            Request request = originalRequest.newBuilder().build();
            Response response = chain.proceed(request);
            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheHeaderValue)
                    .build();
        }
    };
}
