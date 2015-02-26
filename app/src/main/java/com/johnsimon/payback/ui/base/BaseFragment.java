package com.johnsimon.payback.ui.base;

import android.app.Fragment;

public class BaseFragment extends Fragment {

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

}
