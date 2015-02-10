package com.johnsimon.payback.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.johnsimon.payback.R;

import java.util.ArrayList;

public class CreateSpinnerAdapter extends ArrayAdapter<CreateSpinnerAdapter.CalendarOptionItem> {

	private ArrayList<CalendarOptionItem> list;
	private Context context;

	public CreateSpinnerAdapter(Context context, int resource, ArrayList<CalendarOptionItem> list) {
		super(context, resource);
		this.context = context;
		this.list = list;
	}

	@Override
	public View getDropDownView(int position, View convertView,ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	private View getCustomView(int position, View convertView, ViewGroup parent) {
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
		holder.secondaryTextView.setText(calendarOptionItem.secondaryText);

		if (calendarOptionItem.calendarFlag == CalendarOptionItem.FLAG_CALENDAR_CUSTOM) {
			holder.masterLayout.setBackgroundColor(context.getResources().getColor(R.color.gray_oncolor_very_light));
		} else {
			holder.masterLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white));
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

		public CalendarOptionItem(String text, String secondaryText, int calendarFlag) {
			this.text = text;
			this.secondaryText = secondaryText;
			this.calendarFlag = calendarFlag;
		}

	}
}