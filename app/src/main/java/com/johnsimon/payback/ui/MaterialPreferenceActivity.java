package com.johnsimon.payback.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataPreferenceActivity;
import com.johnsimon.payback.util.Resource;

public abstract class MaterialPreferenceActivity extends DataPreferenceActivity
{
	private static int getResIdFromAttribute(final Activity activity,final int attr)
	{
		if(attr==0)
			return 0;
		final TypedValue typedvalueattr=new TypedValue();
		activity.getTheme().resolveAttribute(attr,typedvalueattr,true);
		return typedvalueattr.resourceId;
	}
	protected Toolbar _toolbar;

	private View    _shadowView;
	public View masterView;

	protected abstract int getPreferencesXmlId();

	public Toolbar getToolbar()
	{
		return _toolbar;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msl__activity_preference);
		addPreferencesFromResource(getPreferencesXmlId());

		masterView = findViewById(R.id.preference_master_view);

		_toolbar = (Toolbar)findViewById(R.id.msl__toolbar);
		_toolbar.setClickable(true);
		_toolbar.setTitle(getTitle());
		_toolbar.setNavigationIcon(getResIdFromAttribute(this,R.attr.homeAsUpIndicator));
		_toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				finish();
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void setEnabledActionBarShadow(final boolean enable)
	{
        if (Resource.isLOrAbove()) {
            _toolbar.setElevation(4);
        } else {
            if(_shadowView==null)
                _shadowView=findViewById(R.id.msl__shadowView);
            _shadowView.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
	}
}
