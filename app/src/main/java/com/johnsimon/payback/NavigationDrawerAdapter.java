package com.johnsimon.payback;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.UUID;

public class NavigationDrawerAdapter extends BaseAdapter {
	private final static NavigationDrawerItem allItem = new NavigationDrawerItem(NavigationDrawerItem.Type.All);
	private final ArrayList<NavigationDrawerItem> items = new ArrayList<NavigationDrawerItem>();

	private final Activity context;

	NavigationDrawerAdapter(Activity context, ArrayList<Person> people) {
		this.context = context;

		items.add(allItem);
		for(Person person : people) {
			items.add(new NavigationDrawerItem(person.toString(), person.id, null, person));
		}
	}

	public void selectPerson(Person person) {
		for(int i = 0, l = items.size(); i < l; i++) {
			NavigationDrawerItem item = items.get(i);
			if(person == null && item == allItem || person != null && item.personId == person.id) {
				NavigationDrawerFragment.mCurrentSelectedPosition = i;
				break;
			}
		}
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public NavigationDrawerItem getItem(int i) {
		return items.get(i);
	}

	@Override
	public long getItemId(int i) {
		return -1;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder holder;
		NavigationDrawerItem item = getItem(i);
		boolean isSelected = i == NavigationDrawerFragment.mCurrentSelectedPosition;

		if(item == allItem) {
			return getAllView(view, isSelected);
		}

		if (view == null) {
			view = context.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
				(TextView) view.findViewById(R.id.navigation_drawer_list_item_text),
				(ImageView) view.findViewById(R.id.navigation_drawer_list_item_avatar),
				(TextView) view.findViewById(R.id.navigation_drawer_list_item_avatar_letter)
			);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

        if (item.image == null) {
			holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_placeholder)));
        } else {
            holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_772b5027830c46519a7fd8bccf4c2c94)));
        }

		Person owner = item.owner;

		if(owner.color != null) {
			holder.avatar.setImageDrawable(new RoundedAvatarDrawable(new AvatarPlaceholderDrawable(owner.color).toBitmap(Resource.getPx(36, context), Resource.getPx(36, context))));
			holder.avatarLetter.setVisibility(View.VISIBLE);
			holder.avatarLetter.setText(owner.name.substring(0, 1).toUpperCase());
			//Set avatar as image like some stupid faggot
		} else {
			holder.avatarLetter.setVisibility(View.GONE);

			final ViewHolder finalHolder = holder;
			ImageLoader.getInstance().loadImage(owner.photoURI.replaceAll("/photo$", ""), new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					finalHolder.avatar.setImageDrawable(new RoundedAvatarDrawable(loadedImage));
				}
			});
			Resource.toast(context, owner.photoURI.replaceAll("/photo$", ""));

		}

		holder.title.setText(item.title);

		if(isSelected) {
			holder.title.setTypeface(null, Typeface.BOLD);
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
		}

		return view;
	}

	public View getAllView(View view, boolean isSelected) {
		ViewHolder holder;
		if (view == null) {
			view = context.getLayoutInflater().inflate(R.layout.navigation_drawer_list_item, null);

			holder = new ViewHolder(
					(TextView) view.findViewById(R.id.navigation_drawer_list_item_text),
					(ImageView) view.findViewById(R.id.navigation_drawer_list_item_avatar),
					null
			);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.avatar.setImageDrawable(new RoundedAvatarDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_people_placeholder)));

		holder.title.setText(R.string.all);

		if(isSelected) {
			holder.title.setTypeface(null, Typeface.BOLD);
		} else {
			holder.title.setTypeface(null, Typeface.NORMAL);
		}

		return view;
	}

	static class ViewHolder {
		public TextView title;
        public ImageView avatar;
		public TextView avatarLetter;

		ViewHolder(TextView title, ImageView avatar, TextView avatarLetter) {
			this.title = title;
            this.avatar = avatar;
			this.avatarLetter = avatarLetter;
		}
	}
}
