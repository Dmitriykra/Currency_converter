package com.currencty;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.currencty.appsflyer.AppsFlyes;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebViewActivity extends AppCompatActivity {

    private final Intent intent = getIntent();
    private Map<String, Object> eventValue = new HashMap<>();
    JSONObject jsonObject;
    Map<String, String> params = new HashMap<>();
    private static SharedPreferences sharedPreferences;
    private static String idfa;
    private WebView webView;
    private final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private WebViewClient webViewClient = new ChromeClient();

    private String AF_KEY;
    private Context mContext;
    private String newCampaign;
    private String af_data;
    private CountDownLatch countDownLatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        httpRequest(this);

        //mContext = context;
        //this.AF_KEY = AF_KEY;

        webView = findViewById(R.id.webview);

        Thread intThread =  new Thread(() -> {
            if(!isNetworkAvailable() || !isInternetAvailable())
            {
                Log.d("main_internet", "off");
                Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        intThread.start();

        Thread addThready = new Thread(() -> getIDFA(WebViewActivity.this));
        addThready.start();

        //Запускаем метод в побочном потоке
        Thread myThready = new Thread(new Runnable() {
            public void run() {
                AppsFlyes appsFlyes = new AppsFlyes(WebViewActivity.this);
                try {
                    Log.d("main_aps", "aps: " + appsFlyes.getAppsflyer());
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        myThready.start();

    }

    public JSONObject httpRequest(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://gist.githubusercontent.com/dimaskravt/bd294120e886c5aad2e506640667c979/raw/New%2520project";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        jsonObject = new JSONObject(response);
                        jsonObject.getString("url");
                        jsonObject.getString("show_ad");
                        startApplication(jsonObject);
                        Log.d("show_ad", jsonObject.getString("show_ad"));



                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(this, "Something go wrong", Toast.LENGTH_SHORT).show();
        });
        queue.add(stringRequest);
        return jsonObject;

    }

    public void startApplication(JSONObject jsonObject) throws JSONException, ExecutionException, InterruptedException, TimeoutException {

        
    }


    //AdvertisingID
    /*public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;



        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }


    }

    public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
        if(Looper.myLooper() == Looper.getMainLooper()) throw new IllegalStateException("Cannot be called from the main thread");

        try { PackageManager pm = context.getPackageManager(); pm.getPackageInfo("com.android.vending", 0); }
        catch (Exception e) { throw e; }

        AdvertisingConnection connection = new AdvertisingConnection();
        Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        if(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            try {
                AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());
                AdInfo adInfo = new AdInfo(adInterface.getId(), adInterface.isLimitAdTrackingEnabled(true));
                return adInfo;
            } catch (Exception exception) {
                throw exception;
            } finally {
                context.unbindService(connection);
            }
        }
        throw new IOException("Google Play connection failed");
    }

    private static final class AdvertisingConnection implements ServiceConnection {
        boolean retrieved = false;
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);

        public void onServiceConnected(ComponentName name, IBinder service) {
            try { this.queue.put(service); }
            catch (InterruptedException localInterruptedException){}
        }

        public void onServiceDisconnected(ComponentName name){}

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved) throw new IllegalStateException();
            this.retrieved = true;
            return (IBinder)this.queue.take();
        }
    }


    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }

    }*/
    public static String getIDFA(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);

        if (sharedPreferences.getString("device_id", " ").equals(" ")) {
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                if (adInfo != null & adInfo.getId().length() > 0) {
                    idfa = adInfo.getId();
                    sharedPreferences.edit().putString("device_id", adInfo.getId()).apply();
                } else {
                    throw new IOException();
                }
            } catch (IOException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {
                idfa = UUID.randomUUID().toString();
                sharedPreferences.edit().putString("device_id", idfa).apply();
            }
        }
        Log.d("main", "idfa: " + idfa);
        return idfa;
    }

    public void setupWebView(String link) {
        webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString() + " MobileAppClient/Android/0.9");
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setEnableSmoothTransition(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new ChromeClient());

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setDatabaseEnabled(true);
        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setSaveEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setMixedContentMode(0);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        webView.loadUrl(link);

    }

    public void openOtherApp(String url_intent) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url_intent)));
        } catch (Exception ex) {
        }
    }

    public void openDeepLink(WebView view_data) {
        try {
            WebView.HitTestResult result = view_data.getHitTestResult();
            String data = result.getExtra();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
            view_data.getContext().startActivity(intent);
        } catch (Exception ex) {
        }
    }


    public class ChromeClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("main_url", url);
            if (!url.isEmpty() || !url.equals("")) {
                sharedPreferences.edit().putString("url", url).apply();
            }
            super.onPageFinished(view, url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            Log.d("main_test", request.getUrl().toString());
            return true;
        }

        private boolean handleUri(WebView view, final String url) {
            if (url.startsWith("mailto:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(i);
                return true;
            } else if (url.startsWith("whatsapp://")) {
                openDeepLink(view);
                return true;
            } else if (url.startsWith("tel:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception ex) {
                }
                return true;
            } else if (url.contains("youtube.com")) {
                openOtherApp(url);
                return true;
            } else if (url.contains("play.google.com/store/apps")) {
                openOtherApp(url);
                return true;
            } else if (url.startsWith("samsungpay://")) {
                openOtherApp(url);
                return true;
            } else if (url.startsWith("viber://")) {
                openDeepLink(view);
                return true;
            } else if (url.startsWith("tg://")) {
                openDeepLink(view);
                return true;
            } else if (url.startsWith("intent://")) {
                openOtherApp(url);
                return true;
            } else {
                return false;
            }
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (url.getHost() != null) {
                String hosting = url.getHost();
                if (hosting.equals("localhost")) {
                    if ((url.getPath() == null || url.getPath().length() <= 1)) {
                        Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public boolean isInternetAvailable() {
        try {
            InetAddress ipAdress = InetAddress.getByName("google.com");

            //You can replace it with your name
            return !ipAdress.equals("");

        } catch (Exception e) {
            return false;
        }
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

                            /*Bundle bundle = getIntent().getExtras();
                            String key = bundle.getString("key");*/
                            String newUrl = null;
                            try {
                                newUrl = jsonObject.getString("url");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            assert newUrl != null;
                            if (newUrl.contains("campaign"))
                            {
                                //Log.d("main_camp", "url: " + newUrl + " camp: " + key);
                                newUrl.replace("{campaign}", newCampaign);
                            }

                            setupWebView(newUrl);
                            
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