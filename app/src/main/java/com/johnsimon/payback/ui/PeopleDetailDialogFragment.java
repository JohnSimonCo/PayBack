package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.internal.widget.TintEditText;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.RequiredValidator;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.RobotoMediumTextView;
import com.johnsimon.payback.util.ValidatorListener;
import com.johnsimon.payback.util.FontCache;
import com.makeramen.RoundedImageView;

public class PeopleDetailDialogFragment extends DialogFragment {
	public static Person person;

	public static PeopleDetailDialogFragment newInstance(Person person) {
		PeopleDetailDialogFragment.person = person;
		return new PeopleDetailDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.person_detail_dialog, null);

		RobotoMediumTextView personDetailTitle = (RobotoMediumTextView) rootView.findViewById(R.id.person_detail_title);
		personDetailTitle.setText(person.name);

		RoundedImageView avatar = (RoundedImageView) rootView.findViewById(R.id.person_detail_dialog_avatar);
		TextView avatarLetter = (TextView) rootView.findViewById(R.id.person_detail_dialog_avatar_letter);

		Resource.createProfileImage(person, avatar, avatarLetter);

		Button personRename = (Button) rootView.findViewById(R.id.person_detail_dialog_rename);
		Button personMerge = (Button) rootView.findViewById(R.id.person_detail_dialog_merge);
		Button personDelete = (Button) rootView.findViewById(R.id.person_detail_dialog_delete);

		personRename.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));
		personMerge.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));
		personDelete.setTypeface(FontCache.get(getActivity(), FontCache.RobotoMedium));

		personRename.setOnClickListener(clickListener);
		personMerge.setOnClickListener(clickListener);
		personDelete.setOnClickListener(clickListener);

		builder.setView(rootView);

		return builder.create();
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.person_detail_dialog_rename:
					PersonPickerDialogFragment personPickerDialogFragmentRename = PersonPickerDialogFragment.newInstance(getResources().getString(R.string.rename));
					personPickerDialogFragmentRename.show(getFragmentManager(), "people_detail_dialog_rename");
					personPickerDialogFragmentRename.completeCallback = renameCallback;
					break;
				case R.id.person_detail_dialog_merge:
					PersonPickerDialogFragment personPickerDialogFragmentMerge = PersonPickerDialogFragment.newInstance(PersonPickerDialogFragment.USE_DEFAULT_TITLE);
					personPickerDialogFragmentMerge.show(getFragmentManager(), "people_detail_dialog_merge");
					personPickerDialogFragmentMerge.completeCallback = mergeCallback;
					break;
				case R.id.person_detail_dialog_delete:
					ConfirmDeleteDialogFragment confirmDeleteDialogFragment = new ConfirmDeleteDialogFragment();
					Bundle args = new Bundle();
					args.putString(ConfirmDeleteDialogFragment.DELTE_TEXT, getResources().getString(R.string.delete_person_text));
					confirmDeleteDialogFragment.show(getFragmentManager(), "people_detail_dialog_delete");

					confirmDeleteDialogFragment.confirmDelete = new ConfirmDeleteDialogFragment.ConfirmDeleteCallback() {
						@Override
						public void onDelete() {
							Resource.data.delete(person);
						}
					};

					break;
			}
		}
	};

	public PersonPickerDialogFragment.PersonSelectedCallback renameCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(String name) {
			person.name = name;
		}
	};

	public PersonPickerDialogFragment.PersonSelectedCallback mergeCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(String name) {
			Resource.data.merge(person, Resource.data.findPerson(name));
		}
	};
}