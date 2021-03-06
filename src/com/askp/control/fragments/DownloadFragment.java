/*
 * Copyright (C) 2014 AmperificSuperKANG Project
 *
 * This file is part of ASKP Control.
 *
 * ASKP Control is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ASKP Control is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASKP Control.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.askp.control.fragments;

import java.util.ArrayList;
import java.util.List;

import com.askp.control.R;
import com.askp.control.activities.DownloadActivity;
import com.askp.control.utils.GetConnection;
import com.askp.control.utils.LayoutStyle;
import com.askp.control.utils.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadFragment extends Fragment implements OnItemClickListener,
		OnClickListener {

	private static Context context;

	public static final String mLink = "https://raw.github.com/AmperificSuperKANG/ASKP-Support/master/";
	private static LinearLayout mLayout;
	private static ProgressBar mProgress;
	private static Button mRefresh;
	private static ListView mListView;

	private static List<String> valueLinkList = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		View rootView = inflater.inflate(R.layout.download, container, false);

		mLayout = (LinearLayout) rootView.findViewById(R.id.layout);

		mRefresh = (Button) rootView.findViewById(R.id.refresh);
		LayoutStyle.setButton(mRefresh, getString(R.string.refresh),
				getActivity());
		mRefresh.setOnClickListener(this);

		mListView = (ListView) rootView.findViewById(R.id.listView);
		mListView.setOnItemClickListener(this);

		refresh();
		return rootView;
	}

	public static void refresh() {
		GetConnection.mHtmlstring = "";
		mLayout.removeAllViews();
		mProgress = new ProgressBar(context);
		mRefresh.setVisibility(View.GONE);
		mListView.setVisibility(View.GONE);
		mLayout.addView(mProgress);
		GetConnection.getconnection(mLink
				+ InformationFragment.mModel.replace(" ", "").toLowerCase());
		new DisplayString().execute();
	}

	private static class DisplayString extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			return GetConnection.mHtmlstring;
		}

		@Override
		protected void onPostExecute(String result) {
			mRefresh.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.GONE);

			TextView mError = new TextView(context);
			mError.setTextSize(20);
			LayoutStyle.setCenterText(mError,
					context.getString(R.string.nointernet));

			if (GetConnection.mHtmlstring.isEmpty())
				mLayout.addView(mError);
			else if (GetConnection.mHtmlstring.contains("Contact Support")) {
				mError.setText(context.getString(R.string.nosupport));
				mLayout.addView(mError);
			} else {
				mListView.setVisibility(View.VISIBLE);
				String[] resultRaw = GetConnection.mHtmlstring.split(System
						.getProperty("line.separator"));

				List<String> valueNameList = new ArrayList<String>();
				for (int i = 0; i < resultRaw.length; i++)
					valueNameList.add(resultRaw[i].split(": ")[0]);

				valueLinkList.clear();
				for (int i = 0; i < resultRaw.length; i++)
					valueLinkList.add(resultRaw[i].split(": ")[1]);

				ListAdapter adapter = new ArrayAdapter<String>(context,
						android.R.layout.simple_list_item_1, valueNameList);

				mListView.setAdapter(adapter);
			}
		}
	}

	private static void confirmDownload(final Context context) {
		AlertDialog.Builder mConfirm = new AlertDialog.Builder(context);
		mConfirm.setTitle(context.getString(R.string.download))
				.setMessage(
						context.getString(R.string.doyouwantdownload,
								DownloadActivity.mDownloadname))
				.setNegativeButton(context.getString(android.R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						})
				.setPositiveButton(context.getString(android.R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (Utils.existFile(Environment
										.getExternalStorageDirectory()
										.toString()
										+ "/askp-kernel/"
										+ DownloadActivity.mDownloadname))
									deleteFile(context);
								else
									startDownload(context);
							}
						}).show();
	}

	private static void deleteFile(final Context context) {
		AlertDialog.Builder mDelete = new AlertDialog.Builder(context);
		mDelete.setTitle(context.getString(R.string.delete))
				.setMessage(context.getString(R.string.filealreadyexist))
				.setNegativeButton(context.getString(android.R.string.no),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						})
				.setPositiveButton(context.getString(android.R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Utils.runCommand("rm -f "
										+ Environment
												.getExternalStorageDirectory()
												.toString() + "/askp-kernel/"
										+ DownloadActivity.mDownloadname);
								startDownload(context);
							}
						}).show();
	}

	private static void startDownload(Context context) {
		context.startActivity(new Intent(context, DownloadActivity.class));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg0.equals(mListView)) {
			DownloadActivity.mDownloadlink = valueLinkList.get(arg2);
			DownloadActivity.mDownloadname = mListView.getAdapter()
					.getItem(arg2).toString().replace(" ", "")
					+ ".zip";
			confirmDownload(context);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mRefresh))
			refresh();
	}
}
