package com.johnsimon.payback.currency;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class CurrencyConverter {

    public ConvertCallback callback;

    public void convert(Context context, String fromCountryCode, String toCountryCode) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "http://rate-exchange.appspot.com/currency?from=" + fromCountryCode + "&to=" + toCountryCode;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener() {

                    @Override
                    public void onResponse(Object response) {
                        
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public interface ConvertCallback {
        public void onCurrencyConverted(double amount);
    }

}
