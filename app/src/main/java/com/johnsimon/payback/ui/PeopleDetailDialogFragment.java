package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.adapter.PeopleListAdapter;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;
import com.makeramen.RoundedImageView;

import java.util.Random;
import java.util.UUID;

public class PeopleDetailDialogFragment extends DataDialogFragment {
	public Person person;

	private AlertDialog alertDialog;

	private TextView personDetailTitle;

	private RoundedImageView avatar;

	private TextView avatarLetter;

	private final static String ARG_PERSON_ID = "PERSON_ID";

	public static PeopleDetailDialogFragment newInstance(Person person) {
		PeopleDetailDialogFragment instance = new PeopleDetailDialogFragment();

		Bundle bundle = new Bundle();
		bundle.putString(ARG_PERSON_ID, person.id.toString());
		instance.setArguments(bundle);

		return instance;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View rootView = inflater.inflate(R.layout.person_detail_dialog, null);

        personDetailTitle = (TextView) rootView.findViewById(R.id.person_detail_title);

		avatar = (RoundedImageView) rootView.findViewById(R.id.person_detail_dialog_avatar);
		avatarLetter = (TextView) rootView.findViewById(R.id.person_detail_dialog_avatar_letter);

		Button personRename = (Button) rootView.findViewById(R.id.person_detail_dialog_rename);
		Button personMerge = (Button) rootView.findViewById(R.id.person_detail_dialog_merge);
		Button personDelete = (Button) rootView.findViewById(R.id.person_detail_dialog_delete);

		personRename.setOnClickListener(clickListener);
		personMerge.setOnClickListener(clickListener);
		personDelete.setOnClickListener(clickListener);

		builder.setView(rootView);

		alertDialog = builder.create();

		return alertDialog;
	}

	@Override
	protected void onDataReceived() {
		person = data.findPerson(UUID.fromString(getArguments().getString(ARG_PERSON_ID)));

		personDetailTitle.setText(person.getName());

		Resource.createProfileImage(getDataActivity(), person, avatar, avatarLetter);
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
					final PersonPickerDialogFragment personPickerDialogFragmentMerge = new PersonPickerDialogFragment();

					Bundle argsMerge = new Bundle();
					argsMerge.putString(PersonPickerDialogFragment.TITLE_KEY, PersonPickerDialogFragment.USE_DEFAULT_TITLE);
					argsMerge.putBoolean(PersonPickerDialogFragment.PEOPLE_KEY, true);
                    argsMerge.putString(PersonPickerDialogFragment.BLACKLIST_KEY, person.getName());
					personPickerDialogFragmentMerge.setArguments(argsMerge);

					personPickerDialogFragmentMerge.show(getFragmentManager(), "people_detail_dialog_merge");
					personPickerDialogFragmentMerge.completeCallback = mergeCallback;
					break;
				case R.id.person_detail_dialog_delete:
					new MaterialDialog.Builder(getActivity())
                            .content(R.string.delete_person_text)
                            .positiveText(R.string.delete)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);

                                    final int listIndex = PeopleListAdapter.people.indexOf(person);

									Undo.executeAction(getActivity(), R.string.deleted_person, new Undo.UndoableAction() {
										@Override
										public void onDisplay() {
                                            PeopleListAdapter.people.remove(listIndex);
                                            PeopleManagerActivity.adapter.notifyDataSetChanged();
                                            PeopleListAdapter.updateEmptyViewVisibility();

											alertDialog.dismiss();
										}

										@Override
										public void onRevert() {
											PeopleListAdapter.people.add(listIndex, person);
                                            PeopleManagerActivity.adapter.notifyDataSetChanged();
                                            PeopleListAdapter.updateEmptyViewVisibility();
										}

										@Override
										public void onCommit() {
											data.delete(person);
											storage.commit();
										}
									});

									dialog.dismiss();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .show();

					break;
			}
		}
	};

	public PersonPickerDialogFragment.PersonSelectedCallback renameCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(final String name) {

			personDetailTitle.setText(name);

            final String oldName = person.getName();

			Undo.executeAction(getActivity(), R.string.renamed_person, new Undo.UndoableAction() {
				@Override
				public void onDisplay() {
					person.setName(name);

					alertDialog.dismiss();
				}

				@Override
				public void onRevert() {
					person.setName(oldName);
					PeopleManagerActivity.adapter.notifyDataSetChanged();
				}

				@Override
				public void onCommit() {
					storage.commit();
				}
			});
		}
	};

	public PersonPickerDialogFragment.PersonSelectedCallback mergeCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(String name) {
			Person other = data.findPersonByName(name);
			int index = data.people.indexOf(person);

			PeopleManagerActivity.adapter.people.remove(index);
			PeopleManagerActivity.adapter.notifyItemRemoved(index);
			PeopleManagerActivity.adapter.updateEmptyViewVisibility();

			alertDialog.dismiss();

			data.merge(person, other);
			storage.commit();

		}
	};

}