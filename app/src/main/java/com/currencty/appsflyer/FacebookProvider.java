package com.currencty.appsflyer;

import android.content.Context;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FacebookProvider {
    private Context mContext;
    private String fb_data = "";
    private CountDownLatch countDownLatch;

    public FacebookProvider(Context context){

        mContext = context;
    }

    public String getFacebookLink() throws InterruptedException, TimeoutException, ExecutionException {
        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.fullyInitialize();
        countDownLatch = new CountDownLatch(1);
        Runnable runnable = () -> AppLinkData.fetchDeferredAppLinkData(mContext,
                new AppLinkData.CompletionHandler() {
                    @Override
                    public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {

                        Log.d("main_deep", "targetUri: " + appLinkData.getTargetUri());
                        String[] deepLink = Objects.requireNonNull(appLinkData.getTargetUri()).toString().split("//", 2);
                        fb_data = deepLink[1];
                    }
                }
        );

        Executors.newFixedThreadPool(1).submit(runnable).get(5, TimeUnit.SECONDS);
        countDownLatch.await(5, TimeUnit.SECONDS);
        return fb_data;
    }

}
