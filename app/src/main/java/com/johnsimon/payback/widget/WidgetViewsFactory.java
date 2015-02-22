package com.johnsimon.payback.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

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
        onDataSetChanged();
    }

    @Override
    protected void onDataLinked() {
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.debts.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

        Debt debt = data.debts.get(position);

        if (debt.getOwner().hasImage()) {
            row.setImageViewUri(R.id.list_widget_avatar, Uri.parse(debt.getOwner().link.photoURI));
        } else {
        //    row.setImageViewBitmap(R.id.list_widget_avatar, Resource.drawableToBitmap(new AvatarPlaceholderDrawable(ColorPalette.getInstance(this), debt.getOwner().paletteIndex)));
            row.setViewVisibility(R.id.list_widget_avatar_letter, View.VISIBLE);
            row.setTextViewText(R.id.list_widget_avatar_letter, debt.getOwner().getAvatarLetter());
        }

        row.setTextViewText(R.id.list_widget_person, debt.getOwner().getName());
        row.setTextViewText(R.id.list_widget_note, debt.getNote() == null ? context.getResources().getString(R.string.cash) : debt.getNote());
		row.setTextViewText(R.id.list_widget_amount, data.preferences.getCurrency().render(debt));

        if (debt.isPaidBack()) {
            row.setTextColor(R.id.list_widget_person, context.getResources().getColor(R.color.gray_text_very_light));
            row.setTextColor(R.id.list_widget_note, context.getResources().getColor(R.color.gray_oncolor_light));
            row.setTextColor(R.id.list_widget_amount, context.getResources().getColor(debt.getDisabledColor()));
        } else {
            row.setTextColor(R.id.list_widget_person, context.getResources().getColor(R.color.gray_text_normal));
            row.setTextColor(R.id.list_widget_note, context.getResources().getColor(R.color.gray_text_light));
            row.setTextColor(R.id.list_widget_amount, context.getResources().getColor(debt.getColor()));
        }

        Intent intent = new Intent();

        intent.putExtra(ListWidgetService.EXTRA_OPEN, debt.getId());
        row.setOnClickFillInIntent(R.id.feed_list_item_master, intent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {

    }
}