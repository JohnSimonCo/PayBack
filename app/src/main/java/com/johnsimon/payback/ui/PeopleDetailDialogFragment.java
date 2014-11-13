package com.johnsimon.payback.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.johnsimon.payback.R;
import com.johnsimon.payback.core.Debt;
import com.johnsimon.payback.core.Person;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.RobotoMediumTextView;
import com.johnsimon.payback.util.FontCache;
import com.makeramen.RoundedImageView;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class PeopleDetailDialogFragment extends DialogFragment {
	public static Person person;

	public EditPersonCallback editPersonCallback = null;
	private AlertDialog alertDialog;

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

		alertDialog = builder.create();

		return alertDialog;
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
					argsMerge.putBoolean(PersonPickerDialogFragment.PEOPLE_KEY, true);
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

                            final int restorePersonIndex = Resource.data.people.indexOf(person);

                            Snackbar.with(getActivity())
                                    .text(getString(R.string.sort_list))
                                    .actionLabel(getString(R.string.undo))
									.actionColor(Color.WHITE)
                                    .actionListener(new Snackbar.ActionClickListener() {
                                        @Override
                                        public void onActionClicked() {
                                            //TODO make sure this works
                                            Resource.data.people.add(restorePersonIndex, person);
                                            editPersonCallback.onEdit();
                                        }
                                    })
                                    .show(getActivity());

							Resource.data.delete(person);
							cancel();
						}
					};

					break;
			}
		}
	};

	public PersonPickerDialogFragment.PersonSelectedCallback renameCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(String name) {

            final String oldName = person.name;

			Resource.data.rename(person, name);

            Snackbar.with(getActivity())
                    .text(getString(R.string.renamed_person))
                    .actionLabel(getString(R.string.undo))
                    .actionColor(Color.WHITE)
                    .actionListener(new Snackbar.ActionClickListener() {
                        @Override
                        public void onActionClicked() {
                            //TODO make sure this works
                            Resource.data.rename(person, oldName);
                            editPersonCallback.onEdit();
                        }
                    })
                    .show(getActivity());

			cancel();
		}
	};

	public PersonPickerDialogFragment.PersonSelectedCallback mergeCallback = new PersonPickerDialogFragment.PersonSelectedCallback() {
		@Override
		public void onSelected(String name) {
			final Person other = Resource.data.findPersonByName(name);

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

                    final int index = Resource.people.indexOf(person);
					final ArrayList<Debt> debts = new ArrayList<Debt>();
					for(Debt debt : Resource.debts) {
						if(debt.owner == person) {
							debts.add(debt);
						}
					}

                   // final int otherIndex = Resource.people.indexOf(other);

                    Snackbar.with(getActivity())
                            .text(getString(R.string.merged_people))
                            .actionLabel(getString(R.string.undo))
                            .actionColor(Color.WHITE)
                            .actionListener(new Snackbar.ActionClickListener() {
                                @Override
                                public void onActionClicked() {
                                    //TODO make sure this works
                                    Resource.data.unmerge(person, debts, index);

                                    editPersonCallback.onEdit();
                                }
                            })
                            .show(getActivity());


                    //from, to
					Resource.data.merge(person, other);
					cancel();
				}
			};
		}
	};

	private void cancel() {
		editPersonCallback.onEdit();
		alertDialog.cancel();
	}

	public interface EditPersonCallback {
		public void onEdit();
	}
}