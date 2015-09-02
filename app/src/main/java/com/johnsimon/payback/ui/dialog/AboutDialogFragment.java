package com.johnsimon.payback.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.johnsimon.payback.R;

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.about_dialog_fragment, null);

		TextView versionText = (TextView) rootView.findViewById(R.id.about_dialog_version);
		try {
			String version = getActivity().getPackageManager()
					.getPackageInfo(getActivity().getPackageName(), 0).versionName;
			versionText.setText(version);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			versionText.setText("");
		};

		LinearLayout about_dialog_list_parent = (LinearLayout) rootView.findViewById(R.id.about_dialog_list_parent);
		for (int i = 0, l = about_dialog_list_parent.getChildCount(); i < l; i++) {
			View childView = about_dialog_list_parent.getChildAt(i);

			childView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (view.getTag().equals("devdesign")) {
						try {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:John+Simon+Co")));
						} catch (android.content.ActivityNotFoundException anfe) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:John+Simon+Co")));
						}
					} else if (view.getTag().equals("icon")) {
						new MaterialDialog.Builder(getActivity())
								.title(R.string.author_icon)
								.items(new CharSequence[]{"Google+", "Dribbble", "Behance"})
								.itemsCallback(new MaterialDialog.ListCallback() {
									@Override
									public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
										Intent intent = new Intent(Intent.ACTION_VIEW);
										intent.setData(Uri.parse(new CharSequence[]{
												"https://plus.google.com/+JovieBrettBardoles/posts",
												"https://dribbble.com/bretbardolees",
												"https://www.behance.net/bretbardolees"}
												[which].toString()));
										startActivity(intent);
									}
								})
								.show();
					} else {
						if (!TextUtils.isEmpty(view.getTag().toString())) {
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(view.getTag().toString()));
							startActivity(intent);
						}
					}
				}
			});
		}

        builder.setView(rootView);

        final AlertDialog ad = builder.create();

        return ad;
    }

}