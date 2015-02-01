package com.johnsimon.payback.util;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CurrencyUtils {

    // http://www.xe.com/symbols.php
    public final static List<String> simpleCurrencies = Arrays.asList("$", "€", "£", "₪", "₫", "₩", "¥", "฿");
    public final static List<String> simpleCurrenciesAsCountries = Arrays.asList("USD", "EUR", "GBP", "ILS", "VND", "KRW", "JPY", "THB");

    public static Set<Currency> getAllCurrencies() {
        Set<Currency> toret = new HashSet<>();
        Locale[] locs = Locale.getAvailableLocales();

        for(Locale loc : locs) {
            try {
                toret.add( Currency.getInstance( loc ) );
            } catch(Exception exc){
            }
        }

        return toret;
    }

    public static String getCurrencyCodeBySymbol(String symbol) {
        return simpleCurrenciesAsCountries.get(simpleCurrencies.indexOf(symbol));
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
        }
    }

}
