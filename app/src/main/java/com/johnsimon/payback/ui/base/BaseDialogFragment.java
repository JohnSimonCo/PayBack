package com.johnsimon.payback.ui.base;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;

public class BaseDialogFragment extends DialogFragment {

    public Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
