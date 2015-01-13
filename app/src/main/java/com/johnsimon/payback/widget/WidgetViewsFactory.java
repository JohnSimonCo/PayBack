package com.johnsimon.payback.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Callback;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.drawable.AvatarPlaceholderDrawable;
import com.johnsimon.payback.storage.LocalStorage;
import com.johnsimon.payback.storage.Storage;
import com.johnsimon.payback.util.AppData;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.ThumbnailLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory, Callback<AppData> {

    private Context ctx = null;
    private AppData data;
    private Storage storage;

    public WidgetViewsFactory(Context ctx, Intent intent) {
        this.ctx = ctx;
        storage = new LocalStorage(ctx);
        storage.subscription.listen(this);
    }

    @Override
    public void onCreate() {
        // no-op
    }

    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        return data.debts.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews row = new RemoteViews(ctx.getPackageName(), R.layout.feed_list_item);

        Debt debt = data.debts.get(position);

        if (debt.getOwner().hasImage()) {
            ThumbnailLoader.getInstance().load(debt.getOwner().link.photoURI, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    row.setImageViewBitmap(R.id.list_item_avatar, loadedImage);
                }
            });
        } else {
            row.setImageViewBitmap(R.id.list_item_avatar, Resource.drawableToBitmap(new AvatarPlaceholderDrawable(debt.getOwner().color)));
            row.setViewVisibility(R.id.list_item_avatar_letter, View.VISIBLE);
            row.setTextViewText(R.id.list_item_avatar_letter, debt.getOwner().getAvatarLetter());
        }

        row.setTextViewText(R.id.list_item_person, debt.getOwner().getName());
        row.setTextViewText(R.id.list_item_note, debt.getNote() == null ? ctx.getResources().getString(R.string.cash) : debt.getNote());
        row.setTextViewText(R.id.list_item_date, " - " + Resource.getRelativeTimeString(ctx, debt.timestamp));
        row.setTextViewText(R.id.list_item_amount, debt.amountString());
        row.setTextColor(R.id.list_item_amount, ctx.getResources().getColor(debt.getColor()));

        if (debt.isPaidBack()) {
            row.setTextColor(R.id.list_item_person, ctx.getResources().getColor(R.color.gray_text_very_light));
            row.setTextColor(R.id.list_item_note, ctx.getResources().getColor(R.color.gray_oncolor_light));
            row.setTextColor(R.id.list_item_amount, ctx.getResources().getColor(debt.getDisabledColor()));
        } else {
            row.setTextColor(R.id.list_item_person, ctx.getResources().getColor(R.color.gray_text_normal));
            row.setTextColor(R.id.list_item_note, ctx.getResources().getColor(R.color.gray_text_light));
            row.setTextColor(R.id.list_item_amount, ctx.getResources().getColor(debt.getColor()));
        }

        Intent intent = new Intent();
        Bundle extras = new Bundle();

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

    @Override
    public void onCalled(AppData data) {
        this.data = data;
    }
}