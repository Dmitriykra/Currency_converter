package com.currencty.appsflyer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppsFlyes {
    private String AF_KEY;
    private Context mContext;
    private String newCampaign;
    private String af_data;
    private CountDownLatch countDownLatch;


    public AppsFlyes(Context context) {
        mContext = context;
        //this.AF_KEY = AF_KEY;
    }

    public String getAppsflyer() throws TimeoutException, InterruptedException, ExecutionException {
        countDownLatch = new CountDownLatch(1);
        Runnable runnable = () -> {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {

                    StringBuilder params = new StringBuilder();
                    for (String attrName : conversionData.keySet()) {
                        if(attrName.equals("campaign") && conversionData.get(attrName) != null) {
                            //Log.d("main_camp", Objects.requireNonNull(conversionData.get(attrName)).toString());
                            newCampaign = (conversionData.get(attrName)).toString();
                            //newCampaign = Objects.requireNonNull(conversionData.get(attrName)).toString();
                            Intent intent = new Intent(String.valueOf(this));
                            Bundle b = new Bundle();
                            b.putString("key", newCampaign);

                            intent.putExtras(b);
                        }
                    }
                    af_data = params.toString().replace(" ", "_");
                    countDownLatch.countDown();

                }


                @Override
                public void onConversionDataFail(String s) {
                    Log.d("main_error", "error data conversion Appsflyer   \n Error is: " + s);
                    countDownLatch.countDown();
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> map) {

                }

                @Override
                public void onAttributionFailure(String s) {
                    Log.d("main_error", "attribution failure " + s);
                }
            };
            Log.d("main_key", "af: " + AF_KEY);
            AppsFlyerLib.getInstance().init("DERWbnag49uxrG5wcHPxL7", conversionListener, mContext);
            AppsFlyerLib.getInstance().start(mContext);
        };

        Executors.newFixedThreadPool(1).submit(runnable).get(5, TimeUnit.SECONDS);
        countDownLatch.await(10, TimeUnit.SECONDS);
        return newCampaign;
    }
}


