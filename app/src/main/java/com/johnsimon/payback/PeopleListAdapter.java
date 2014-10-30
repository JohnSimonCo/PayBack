package com.johnsimon.payback;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

public class PeopleListAdapter extends ArrayAdapter<Person> {
	private final Activity context;
	private final ArrayList<Person> list;

	public PeopleListAdapter(Activity context, ArrayList<Person> list) {
		super(context, R.layout.feed_list_item, list);
		this.context = context;
		this.list = list;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(R.layout.people_list_item, null);

			holder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.people_list_item_name),
					(ImageView) convertView.findViewById(R.id.people_list_item_avatar),
                    (TextView) convertView.findViewById(R.id.people_list_item_avatar_letter)
			);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Person person = list.get(position);

		holder.name.setText(person.name);

        if(person.color != null) {
            holder.avatar.setImageDrawable(new RoundedAvatarDrawable(new AvatarPlaceholderDrawable(person.color).toBitmap(Resource.getPx(36, context), Resource.getPx(36, context))));
            holder.avatarLetter.setVisibility(View.VISIBLE);
            holder.avatarLetter.setText(person.getAvatarLetter());
            //Set avatar as image like some stupid faggot
        } else {
            holder.avatarLetter.setVisibility(View.GONE);

			final ViewHolder finalHolder = holder;
			ThumbnailLoader.getInstance().load(person.photoURI, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					finalHolder.avatar.setImageDrawable(new RoundedAvatarDrawable(loadedImage));
				}
			});
        }

		return convertView;
	}

	static class ViewHolder {
		public TextView name;
		public ImageView avatar;
        public TextView avatarLetter;

		ViewHolder(TextView name, ImageView avatar, TextView avatarLetter) {
			this.name = name;
			this.avatar = avatar;
            this.avatarLetter = avatarLetter;
		}
	}
}