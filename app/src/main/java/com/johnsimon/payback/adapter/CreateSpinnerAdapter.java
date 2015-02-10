package com.johnsimon.payback.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.johnsimon.payback.R;

import java.util.ArrayList;

public class CreateSpinnerAdapter extends ArrayAdapter<CreateSpinnerAdapter.CalendarOptionItem> {

	private ArrayList<CreateSpinnerAdapter.CalendarOptionItem> list;
	private Context context;

	public CreateSpinnerAdapter(Context context, int resourceId, ArrayList<CreateSpinnerAdapter.CalendarOptionItem> list) {
		super(context, resourceId, list);
		this.list = list;
		this.context = context;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, false);
	}

	public View getCustomView(int position, View convertView, boolean dropdown) {
		ViewHolder holder;
		CalendarOptionItem calendarOptionItem = list.get(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.create_spinner_item, null);

			holder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.create_spinner_text),
					(TextView) convertView.findViewById(R.id.create_spinner_secondary_text),
					(LinearLayout) convertView.findViewById(R.id.create_spinner_master));

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.textView.setText(calendarOptionItem.text);
		if (calendarOptionItem.secondaryText != null && dropdown) {
			holder.secondaryTextView.setText(calendarOptionItem.secondaryText);
			holder.secondaryTextView.setVisibility(View.VISIBLE);
		} else {
			holder.secondaryTextView.setVisibility(View.GONE);
		}

		if (dropdown) {
			if (calendarOptionItem.calendarFlag == CalendarOptionItem.FLAG_CALENDAR_CUSTOM) {
				holder.masterLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_oncolor_extremely_light));
			} else {
				holder.masterLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
			}

			holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		} else {
			holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			holder.textView.setText(calendarOptionItem.selectedString);
		}

		return convertView;
	}

	static class ViewHolder {
		public TextView textView;
		public TextView secondaryTextView;
		public LinearLayout masterLayout;

		ViewHolder(TextView textView, TextView secondaryTextView, LinearLayout masterLayout) {
			this.textView = textView;
			this.secondaryTextView = secondaryTextView;
			this.masterLayout = masterLayout;
		}
	}

	public static class CalendarOptionItem {

		public final static int FLAG_CALENDAR_CUSTOM = -1;
		public final static int FLAG_CALENDAR_TODAY = 1;
		public final static int FLAG_CALENDAR_TOMORROW = 2;
		public final static int FLAG_CALENDAR_MORNING = 3;
		public final static int FLAG_CALENDAR_AFTERNOON = 4;
		public final static int FLAG_CALENDAR_EVENING = 5;
		public final static int FLAG_CALENDAR_NIGHT = 6;

		public String text;
		public String secondaryText;
		public int calendarFlag;
		public String selectedString;

		public CalendarOptionItem(String text, String secondaryText, int calendarFlag, String selectedString) {
			this.text = text;
			this.secondaryText = secondaryText;
			this.calendarFlag = calendarFlag;
			if (selectedString == null) {
				this.selectedString = text;
			} else {
				this.selectedString = selectedString;
			}
		}

	}
}