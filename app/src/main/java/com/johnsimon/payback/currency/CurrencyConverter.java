package com.johnsimon.payback.currency;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.johnsimon.payback.util.Resource;

public class CurrencyConverter {

    public CurrencyConverter(Context context, String fromCountryCode, String toCountryCode, final ConvertCallback convertCallback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://rate-exchange.appspot.com/currency?from=" + fromCountryCode + "&to=" + toCountryCode;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("{\"err\": \"failed to parse response from xe.com.\"}")) {
                    convertCallback.onCurrencyConverted(0, false);
                    return;
                }
                ConvertResult convertResult = Resource.gson().fromJson(response, ConvertResult.class);
                convertCallback.onCurrencyConverted(convertResult.getRate().doubleValue(), true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                convertCallback.onCurrencyConverted(0, false);
            }
        });

        queue.add(stringRequest);
    }

    public interface ConvertCallback {
        void onCurrencyConverted(double amount, boolean success);
    }

}
