package com.dimaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.trusted.sharing.ShareTarget;

import android.app.Dialog;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;

/*import okhttp3.Request;
import okhttp3.Response;*/
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.material.internal.TextWatcherAdapter;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String ONESIGNAL_APP_ID = "b6d68acb-4d69-4520-b212-829fe7684728";
    String LOG_TAG;
    TextView conversionRateText, calendarDate;
    EditText amount_to_convert;
    Spinner spinnerFrom, spinnerTo;
    Button conversation_button;
    String convertFromValue, convertToValue, conversionValue;
    ArrayList currencyArrayList;
    ArrayAdapter currencyAdapter;
    String currencyNameFrom, currencyNameTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar calendar = Calendar.getInstance();
        String thisDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        calendarDate = findViewById(R.id.calendar);
        calendarDate.setText(thisDate);

        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        currencyArrayList= new ArrayList();

        currencyArrayList.add("USD");
        currencyArrayList.add("EUR");
        currencyArrayList.add("UAH");
        currencyArrayList.add("CAD");

        currencyAdapter = new ArrayAdapter(this,  android.R.layout.simple_spinner_item, currencyArrayList);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(currencyAdapter);
        spinnerTo.setAdapter(currencyAdapter);

        //OneSignal method
        oneSignal();
        appsFlyer();
        //httpRequest();

        conversation_button = findViewById(R.id.conversion_button);
        conversionRateText = findViewById(R.id.conversion_rate_text);
        amount_to_convert = findViewById(R.id.amount_to_convert_value_edit_text);

        /*Runnable runnable =  new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://currency-converter5.p.rapidapi.com/currency/convert?format=json&from=AUD&to=CAD&amount=1")
                        .get()
                        .addHeader("x-rapidapi-host", "currency-converter5.p.rapidapi.com")
                        .addHeader("x-rapidapi-key", "61f2ad6c62msh6288a8eeff5c23fp192f5ajsnd60fa6384344")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    Log.d("main", "test:" + response);
                    conversionRateText.post(new Runnable() {
                        public void run() {
                            conversionRateText.setText(conversionValue);
                        }
                    });
                } catch (IOException e) {
                    Log.d("main", "error");
                    e.printStackTrace();


                }
            }


        };*/

        conversation_button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                try{
                    Double amountToConvert = Double.valueOf(MainActivity.this.amount_to_convert.getText().toString());
                    getConversionRate(convertFromValue, convertToValue, amountToConvert);


                }
                catch (Exception e){

                }
            }
        });


        super.onStart();
    }


    /*private void httpRequest() {
        //final TextView textView = (TextView) findViewById(R.id.hello);
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://gist.githubusercontent.com/dimaskravt/f4e3f725fd2d4a063d63462e425c1ebf/raw/Dimaster1";


// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;

                        try {
                            jsonObject = new JSONObject(response);
                            //Log.d("Main", jsonObject.toString());
                            jsonObject.getString("url");
                            jsonObject.getString("show_ad");

                            Log.d("show_ad", jsonObject.getString("show_ad"));


                        }catch (JSONException e){
                            e.printStackTrace();

                        }

                        // Display the first 500 characters of the response string.
                        //textView.setText("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //textView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }*/

    private void appsFlyer() {
        AppsFlyerLib.getInstance().init("zky33TNFo27BGW44UCiFof", null, this);
        AppsFlyerLib.getInstance().start(this);
    }

    private void oneSignal() {
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }

    AppsFlyerConversionListener conversionListener =  new AppsFlyerConversionListener() {
        @Override
        public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {

            for (String attrName : conversionDataMap.keySet())
                Log.d(LOG_TAG, "Conversion attribute: " + attrName + " = " + conversionDataMap.get(attrName));
            String status = Objects.requireNonNull(conversionDataMap.get("af_status")).toString();
            if(status.equals("Organic")){
                // Business logic for Organic conversion goes here.
            }
            else {
                // Business logic for Non-organic conversion goes here.
            }
        }

        @Override
        public void onConversionDataFail(String errorMessage) {
            Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
        }

        @Override
        public void onAppOpenAttribution(Map<String, String> attributionData) {
            // Must be overriden to satisfy the AppsFlyerConversionListener interface.
            // Business logic goes here when UDL is not implemented.
        }

        @Override
        public void onAttributionFailure(String errorMessage) {
            // Must be overriden to satisfy the AppsFlyerConversionListener interface.
            // Business logic goes here when UDL is not implemented.
            Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
        }

    };


    public String getConversionRate(String from, String to, Double amountToConvert){
        from = spinnerFrom.getSelectedItem().toString();
        to = spinnerTo.getSelectedItem().toString();
        String finalFrom = from;
        String finalTo = to;

        RequestQueue queue = Volley.newRequestQueue(this);
        String uri = "https://free.currconv.com/api/v7/convert?q=" + from + "_" + to + "&compact=ultra&apiKey=" + "b4ed6614c4c9817457b6";
        StringRequest stringRequest =  new StringRequest(Request.Method.GET, uri,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;

                if (!finalFrom.equals(finalTo)) {
                    try {
                        jsonObject = new JSONObject(response);
                        Double conversionRateValue = round(((Double) jsonObject.get(finalFrom + "_" + finalTo)), 2);
                        conversionValue = "" + round((conversionRateValue * amountToConvert), 2);
                        conversionRateText.setText(conversionValue);


                    } catch (JSONException e) {

                        e.printStackTrace();


                    }
                } else if (finalFrom.equals(finalTo)) {
                    try {
                        jsonObject = new JSONObject(response);
                        double conversionRateValue = round(((Integer) jsonObject.get(finalFrom + "_" + finalTo)), 2);
                        conversionValue = "" + round((conversionRateValue * amountToConvert), 2);
                        conversionRateText.setText(conversionValue);

                    } catch (JSONException e) {

                        e.printStackTrace();


                    }
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }

        });
        queue.add(stringRequest);
        return null;
    }

    public static double round(double aDouble, int i) {
        if(i<0) throw new IllegalArgumentException();
        BigDecimal bg = BigDecimal.valueOf(aDouble);
        bg = bg.setScale(i, RoundingMode.HALF_UP);
        return bg.doubleValue();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currencyNameFrom = spinnerFrom.getSelectedItem().toString();
        currencyNameTo = spinnerTo.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}