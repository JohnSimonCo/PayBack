package com.johnsimon.payback;

import android.content.Context;
import android.support.v7.internal.widget.TintSpinner;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;
import java.lang.reflect.Field;

/** Spinner extension that calls onItemSelected even when the selection is the same as       its previous value.
 *   ie This is extended "Customized class of Spinner" to  get the "onItemSelected"      event even if the item in the
 *  Spinner is already  selected by the user*/
public class NDSpinner extends Spinner {

	public NDSpinner(Context context)
	{ super(context); }

	public NDSpinner(Context context, AttributeSet attrs)
	{ super(context, attrs); }

	public NDSpinner(Context context, AttributeSet attrs, int defStyle)
	{ super(context, attrs, defStyle); }


	private void ignoreOldSelectionByReflection() {
		try {
			Class<?> c = this.getClass().getSuperclass().getSuperclass().getSuperclass();
			Field reqField = c.getDeclaredField("mOldSelectedPosition");
			reqField.setAccessible(true);
			reqField.setInt(this, -1);
		} catch (Exception e) {
			Log.d("Exception Private", "ex", e);
			// TODO: handle exception
		}
	}

	@Override
	public void  setSelection(int position, boolean animate)
	{
		boolean sameSelected = position == getSelectedItemPosition();
		ignoreOldSelectionByReflection();
		super.setSelection(position, animate);
		if (sameSelected) {
			// Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
			getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());


		}
	}
	@Override
	public void setSelection(int position) {
		ignoreOldSelectionByReflection();
		super.setSelection(position);
	}

}