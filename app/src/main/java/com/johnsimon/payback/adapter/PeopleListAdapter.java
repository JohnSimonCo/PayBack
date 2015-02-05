package com.johnsimon.payback.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataActivity;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.data.AppData;
import com.johnsimon.payback.util.Resource;
import com.makeramen.RoundedImageView;

import java.util.ArrayList;

public class PeopleListAdapter extends RecyclerView.Adapter<PeopleListAdapter.ViewHolder> {

	private DataActivity activity;
	private View emptyView;
    private AppData data;
    private TextView managerTitle;
    public ArrayList<Person> people;

	public PeopleListClickListener clickListener = null;

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView name;
		public RoundedImageView avatar;
		public TextView avatarLetter;
		public TextView debtsCount;
		public View itemView;

		public ViewHolder(View _itemView) {
			super(_itemView);

			this.name =(TextView) _itemView.findViewById(R.id.people_list_item_name);
			this.avatar =(RoundedImageView) _itemView.findViewById(R.id.people_list_item_avatar);
			this.avatarLetter =(TextView) _itemView.findViewById(R.id.people_list_item_avatar_letter);
			this.debtsCount =(TextView) _itemView.findViewById(R.id.people_list_item_debts);
			this.itemView = _itemView;

		}
	}

	public PeopleListAdapter(DataActivity _activity, View emptyView, AppData data, TextView managerTitle, ArrayList<Person> _people) {
		this.activity = _activity;
		this.emptyView = emptyView;
        this.data = data;
        this.managerTitle = managerTitle;
		this.people = _people;
	}

	@Override
	public PeopleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.people_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(PeopleListAdapter.ViewHolder holder, final int position) {

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickListener.onListItemClick(position);
			}
		});

		Person person = people.get(position);
		holder.name.setText(person.getName());
		int debts = data.feed(person).size();
		if (debts == 1) {
			holder.debtsCount.setText(debts +  " " + activity.getString(R.string.debt_single));
		} else {
			holder.debtsCount.setText(debts +  " " + activity.getString(R.string.debt_plural));
		}
		Resource.createProfileImage(activity, person, holder.avatar, holder.avatarLetter);
	}

	@Override
	public int getItemCount() {
		return people.size();
	}

	public void updateEmptyViewVisibility() {
		if (people.size() == 1) {
			managerTitle.setText("1 " + activity.getString(R.string.person));
		} else {
			managerTitle.setText(people.size() + " " + activity.getString(R.string.people));
		}

		if (people.size() == 0) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}

	public void clear() {
		people.clear();
	}

	public void addAll(ArrayList<Person> newPeopleList) {
		people.addAll(newPeopleList);
	}

	public void remove(int index) {
		people.remove(index);
	}

	public Person getItem(int index) {
		return people.get(index);
	}

	public void insert(Person person, int index) {
		people.add(index, person);
	}

	public interface PeopleListClickListener {
		public void onListItemClick(int position);
	}

}