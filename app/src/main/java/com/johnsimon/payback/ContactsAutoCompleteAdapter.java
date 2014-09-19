package com.johnsimon.payback;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactsAutoCompleteAdapter extends ArrayAdapter<Person> {

    private final Activity context;
    private final ArrayList<Person> list;

    public ContactsAutoCompleteAdapter(Activity context, ArrayList<Person> list) {
        super(context, R.layout.autocomplete_list_item, list);
        this.context = context;
        this.list = list;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.autocomplete_list_item, null);

            holder = new ViewHolder((TextView) convertView.findViewById(R.id.autocomplete_list_item_title)

            );
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(list.get(position).name);
        Toast.makeText(context, list.get(position).name, Toast.LENGTH_LONG).show();

        return convertView;
    }

    static class ViewHolder {
        public TextView name;

        ViewHolder(TextView name) {
            this.name = name;
        }
    }

}
