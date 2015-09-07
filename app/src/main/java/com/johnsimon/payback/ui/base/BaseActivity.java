package com.johnsimon.payback.ui.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import com.johnsimon.payback.util.Undo;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    public ArrayList<Undo.QueuedAction> queuedActions = new ArrayList<>();
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Undo.QueuedAction action : queuedActions) {
            action.handler.removeCallbacksAndMessages(null);
        }

        handler.removeCallbacksAndMessages(null);
    }
}
