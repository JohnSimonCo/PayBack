package com.johnsimon.payback.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.johnsimon.payback.R;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.drawable.AvatarPlaceholderDrawable;
import com.johnsimon.payback.util.ColorPalette;
import com.johnsimon.payback.util.Resource;

public class WidgetViewsFactory extends DataWidgetViewsFactory {

    public WidgetViewsFactory(Context context, Intent intent) {
        super(context);
    }

    @Override
    protected void onDataReceived() {

    }

    @Override
    protected void onDataLinked() {
    }

    @Override
    public void onCreate() {
        Toast.makeText(context, data.debts.size() + " start", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        Toast.makeText(context, data.debts.size() + " hejhopp size", Toast.LENGTH_SHORT).show();
        return data.debts.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.feed_list_item);

        Toast.makeText(context, position + " pos", Toast.LENGTH_SHORT).show();
        Toast.makeText(context, data.debts.size() + " size", Toast.LENGTH_SHORT).show();

        Debt debt = data.debts.get(position);

        if (debt.getOwner().hasImage()) {
            row.setImageViewUri(R.id.list_item_avatar, Uri.parse(debt.getOwner().link.photoURI));
        } else {
            row.setImageViewBitmap(R.id.list_item_avatar, Resource.drawableToBitmap(new AvatarPlaceholderDrawable(ColorPalette.getInstance(this), debt.getOwner().paletteIndex)));
            row.setViewVisibility(R.id.list_item_avatar_letter, View.VISIBLE);
            row.setTextViewText(R.id.list_item_avatar_letter, debt.getOwner().getAvatarLetter());
        }

        row.setTextViewText(R.id.list_item_person, debt.getOwner().getName());
        row.setTextViewText(R.id.list_item_note, debt.getNote() == null ? context.getResources().getString(R.string.cash) : debt.getNote());
        row.setTextViewText(R.id.list_item_date, " - " + Resource.getRelativeTimeString(context, debt.timestamp));
		row.setTextViewText(R.id.list_item_amount, data.preferences.getCurrency().render(debt));

        if (debt.isPaidBack()) {
            row.setTextColor(R.id.list_item_person, context.getResources().getColor(R.color.gray_text_very_light));
            row.setTextColor(R.id.list_item_note, context.getResources().getColor(R.color.gray_oncolor_light));
            row.setTextColor(R.id.list_item_amount, context.getResources().getColor(debt.getDisabledColor()));
        } else {
            row.setTextColor(R.id.list_item_person, context.getResources().getColor(R.color.gray_text_normal));
            row.setTextColor(R.id.list_item_note, context.getResources().getColor(R.color.gray_text_light));
            row.setTextColor(R.id.list_item_amount, context.getResources().getColor(debt.getColor()));
        }

        Intent intent = new Intent();
        Bundle extras = new Bundle();

        //TODO nämen, här ståre bajs!
        extras.putString(ListWidgetService.EXTRA_SEND, "bajs");
        intent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.feed_list_item_master, intent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {

    }
}