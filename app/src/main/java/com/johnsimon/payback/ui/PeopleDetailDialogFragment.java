package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.RobotoMediumTextView;
import com.johnsimon.payback.util.FontCache;
import com.makeramen.RoundedImageView;

public class PeopleDetailDialogFragment extends DialogFragment {
	public static Person person;

	public EditPersonCallback confirm = null;

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
					PersonPickerDialogFragment personPickerDialogFragmentRename = new PersonPickerDialogFragment();

					Bundle argsRename = new Bundle();
					argsRename.putString(PersonPickerDialogFragment.TITLE_KEY, getResources().getString(R.string.rename));
					personPickerDialogFragmentRename.setArguments(argsRename);

					personPickerDialogFragmentRename.show(getFragmentManager(), "people_detail_dialog_rename");
					personPickerDialogFragmentRename.completeCallback = renameCallback;
					break;
				case R.id.person_detail_dialog_merge:
					PersonPickerDialogFragment personPickerDialogFragmentMerge = new PersonPickerDialogFragment();

					Bundle argsMerge = new Bundle();
					argsMerge.putString(PersonPickerDialogFragment.TITLE_KEY, PersonPickerDialogFragment.USE_DEFAULT_TITLE);
					personPickerDialogFragmentMerge.setArguments(argsMerge);

					personPickerDialogFragmentMerge.show(getFragmentManager(), "people_detail_dialog_merge");
					personPickerDialogFragmentMerge.completeCallback = mergeCallback;
					break;
				case R.id.person_detail_dialog_delete:
					ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();

					Bundle argsDelete = new Bundle();
					argsDelete.putString(ConfirmDialogFragment.INFO_TEXT, getResources().getString(R.string.delete_person_text));
					argsDelete.putString(ConfirmDialogFragment.CONFIRM_TEXT, getResources().getString(R.string.delete));
					confirmDialogFragment.setArguments(argsDelete);

					confirmDialogFragment.show(getFragmentManager(), "people_detail_dialog_delete");

					confirmDialogFragment.confirm = new ConfirmDialogFragment.ConfirmCallback() {
						@Override
						public void onConfirm() {
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
			Person other = Resource.data.findPersonByName(name);

			ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();

			Bundle argsDelete = new Bundle();
			String confirmFormat = getString(R.string.merge_confirm_text_format);
			argsDelete.putString(ConfirmDialogFragment.INFO_TEXT, String.format(confirmFormat, person.name, other.name));
			argsDelete.putString(ConfirmDialogFragment.CONFIRM_TEXT, getResources().getString(R.string.merge));
			confirmDialogFragment.setArguments(argsDelete);

			confirmDialogFragment.show(getFragmentManager(), "people_detail_dialog_delete");

			confirmDialogFragment.confirm = new ConfirmDialogFragment.ConfirmCallback() {
				@Override
				public void onConfirm() {
					Resource.data.delete(person);
				}
			};

			Resource.data.merge(person, other);
		}
	};

	public interface EditPersonCallback {
		public void onConfirm();
	}
}