package com.johnsimon.payback.util;

import android.os.AsyncTask;
import java.util.ArrayList;

public class ContactLoader extends AsyncTask<Void, Void, ArrayList<AppData>> {

    @Override
    protected ArrayList<AppData> doInBackground(Void... params) {

        return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(ArrayList<AppData> result) {}

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}
}