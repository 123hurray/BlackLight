/* 
 * Copyright (C) 2014 Peter Cai
 *
 * This file is part of BlackLight
 *
 * BlackLight is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlackLight is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlackLight.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.shandian.blacklight.ui.directmessage;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import android.support.v4.widget.SwipeRefreshLayout;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.directmessages.DirectMessagesApi;
import us.shandian.blacklight.model.DirectMessageListModel;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.adapter.DirectMessageAdapter;
import us.shandian.blacklight.ui.common.SwipeUpAndDownRefreshLayout;

public class DirectMessageConversationActivity extends SwipeBackActivity implements SwipeRefreshLayout.OnRefreshListener
{
	private UserModel mUser;
	private DirectMessageListModel mMsgList = new DirectMessageListModel();
	private int mPage = 0;
	private boolean mRefreshing = false;
	
	private ListView mList;
	private DirectMessageAdapter mAdapter;
	private SwipeUpAndDownRefreshLayout mSwipeRefresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.direct_message_conversation);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Argument
		mUser = getIntent().getParcelableExtra("user");
		getActionBar().setTitle(mUser.getName());
		
		// View
		mSwipeRefresh = (SwipeUpAndDownRefreshLayout) findViewById(R.id.direct_message_refresh);
		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
									 android.R.color.holo_orange_dark, android.R.color.holo_red_dark);
		
		mList = (ListView) findViewById(R.id.direct_message_conversation);
		mList.setStackFromBottom(true);
		mAdapter = new DirectMessageAdapter(this, mMsgList, mUser.id);
		mList.setAdapter(mAdapter);
		
		new Refresher().execute(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(mSwipeRefresh.isDown());
		}
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Boolean> {
		@Override
		public void onPreExecute() {
			super.onPreExecute();
			
			mRefreshing = true;
			mSwipeRefresh.setRefreshing(true);
		}
		
		@Override
		public Boolean doInBackground(Boolean... params) {
			if (params[0]) {
				mPage = 0;
				mMsgList.getList().clear();
			}
			
			DirectMessageListModel list = DirectMessagesApi.getConversation(mUser.id, 10, ++mPage);
			
			mMsgList.addAll(params[0], list);
			
			return params[0];
		}
		
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if (result) {
				mAdapter.notifyDataSetChangedAndClear();
			} else {
				mAdapter.notifyDataSetChanged();
			}
			
			mRefreshing = false;
			mSwipeRefresh.setRefreshing(false);
		}
	}
}
