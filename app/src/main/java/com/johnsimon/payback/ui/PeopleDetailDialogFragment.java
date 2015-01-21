package com.johnsimon.payback.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;
import com.johnsimon.payback.core.DataDialogFragment;
import com.johnsimon.payback.data.Debt;
import com.johnsimon.payback.data.Person;
import com.johnsimon.payback.util.Resource;
import com.johnsimon.payback.util.Undo;
import com.makeramen.RoundedImageView;
import com.williammora.snackbar.Snackbar;

import java.util.ArrayList;

public class PeopleDetailDialogFragment extends DataDialogFragment {
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

        TextView personDetailTitle = (TextView) rootView.findViewById(R.id.person_detail_title);
		personDetailTitle.setText(person.getName());

		RoundedImageView avatar = (RoundedImageView) rootView.findViewById(R.id.person_detail_dialog_avatar);
		TextView avatarLetter = (TextView) rootView.findViewById(R.id.person_detail_dialog_avatar_letter);

		Resource.createProfileImage(getDataActivity(), person, avatar, avatarLetter);

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
                    argsMerge.putString(PersonPickerDialogFragment.BLACKLIST_KEY, person.getName());
					personPickerDialogFragmentMerge.setArguments(argsMerge);

					personPickerDialogFragmentMerge.show(getFragmentManager(), "people_detail_dialog_merge");
					personPickerDialogFragmentMerge.completeCallback = mergeCallback;
					break;
				case R.id.person_detail_dialog_delete:

					//TODO ALLA UNDOS
					new MaterialDialog.Builder(getActivity())
                            .content(R.string.delete_person_text)
                            .positiveText(R.string.delete)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);

                                    final int restorePersonIndex = data.people.indexOf(person);

									Activity activity = getActivity();
									if (activity != null) {
										Snackbar.with(getActivity().getApplicationContext())
												.text(getString(R.string.deleted_person))
												.actionLabel(getString(R.string.undo))
												.actionColor(getResources().getColor(R.color.green))
												.actionListener(new Snackbar.ActionClickListener() {
													@Override
													public void onActionClicked() {
														//TODO ALLA UNDOS
														//data.people.add(restorePersonIndex, person);
														editPersonCallback.onEdit();
													}
												})
												.show(getActivity());
									}


                                    data.delete(person);
                                    cancel();

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

            final String oldName = person.getName();

			Undo.executeAction(getActivity(), R.string.renamed_person, new Undo.UndoableAction() {
				@Override
				public void onDisplay() {
					data.rename(person, name);
					editPersonCallback.onEdit();
				}

				@Override
				public void onRevert() {
					data.rename(person, oldName);
					editPersonCallback.onEdit();
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
			final Person other = data.findPersonByName(name);

            new MaterialDialog.Builder(getActivity())
                    .content(String.format(getString(R.string.merge_confirm_text_format), person.getName(), other.getName()))
                    .positiveText(R.string.merge)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog dialog) {
							super.onPositive(dialog);

							final int index = data.people.indexOf(person);
							final ArrayList<Debt> debts = new ArrayList<Debt>();
							for (Debt debt : data.debts) {
								if (debt.getOwner() == person) {
									debts.add(debt);
								}
							}

							Snackbar.with(getActivity().getApplicationContext())
									.text(getString(R.string.merged_people))
									.actionLabel(getString(R.string.undo))
									.actionColor(getResources().getColor(R.color.green))
									.actionListener(new Snackbar.ActionClickListener() {
										@Override
										public void onActionClicked() {
											data.unmerge(person, debts, index);

											editPersonCallback.onEdit();
										}
									})
									.eventListener(new Snackbar.EventListener() {
										@Override public void onShow(int i) {}

										@Override
										public void onDismiss(int i) {

										}
									})
									.show(getActivity());


							//from, to
							data.merge(person, other);
							cancel();

							dialog.dismiss();
						}

						@Override
						public void onNegative(MaterialDialog dialog) {
							super.onNegative(dialog);
						}
					})
                    .show();

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