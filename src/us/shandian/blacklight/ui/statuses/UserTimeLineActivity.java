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

package us.shandian.blacklight.ui.statuses;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.BaseApi;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.ui.friendships.FriendsActivity;

public class UserTimeLineActivity extends SwipeBackActivity implements View.OnClickListener
{
	private UserTimeLineFragment mFragment;
	private UserModel mModel;
	
	private TextView mName;
	private TextView mFollowState;
	private TextView mDes;
	private TextView mFollowers;
	private TextView mFollowing;
	private TextView mMsgs;
	private TextView mLikes;
	private TextView mGeo;
	private ImageView mAvatar;
	private View mCover;
	private View mFollowingContainer;
	
	private UserApiCache mCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_timeline_activity);
		
		mCache = new UserApiCache(this);
		
		// Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);
		
		// Arguments
		mModel = getIntent().getParcelableExtra("user");
		
		// Views
		mName = (TextView) findViewById(R.id.user_name);
		mFollowState = (TextView) findViewById(R.id.user_follow_state);
		mDes = (TextView) findViewById(R.id.user_des);
		mFollowers = (TextView) findViewById(R.id.user_followers);
		mFollowing = (TextView) findViewById(R.id.user_following);
		mMsgs = (TextView) findViewById(R.id.user_msgs);
		mLikes = (TextView) findViewById(R.id.user_like);
		mGeo = (TextView) findViewById(R.id.user_geo);
		mAvatar = (ImageView) findViewById(R.id.user_avatar);
		mCover = findViewById(R.id.user_cover);
		mFollowingContainer = findViewById(R.id.user_following_container);
		
		mFollowingContainer.setOnClickListener(this);
		
		// View values
		mName.setText(mModel.getName());
		
		// Follower state (following/followed/each other)
		if (mModel.follow_me && mModel.following) {
			mFollowState.setText(R.string.following_each_other);
		} else if (mModel.follow_me) {
			mFollowState.setText(R.string.following_me);
		} else if (mModel.following) {
			mFollowState.setText(R.string.i_am_following);
		} else {
			mFollowState.setText(R.string.no_following);
		}
		
		// Also view values
		mDes.setText(mModel.description);
		mFollowers.setText(String.valueOf(mModel.followers_count));
		mFollowing.setText(String.valueOf(mModel.friends_count));
		mMsgs.setText(String.valueOf(mModel.statuses_count));
		mLikes.setText(String.valueOf(mModel.favourites_count));
		mGeo.setText(mModel.location);
		
		new Downloader().execute();
		
		// Init
		mFragment = new UserTimeLineFragment(mModel.id);
		getFragmentManager().beginTransaction().replace(R.id.user_timeline_container, mFragment).commit();
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
	public void onClick(View v) {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.putExtra("uid", mModel.id);
		
		if (v == mFollowingContainer) {
			i.setClass(this, FriendsActivity.class);
		}
		
		startActivity(i);
	}
	
	private class Downloader extends AsyncTask<Void, Object, Void> {

		@Override
		protected Void doInBackground(Void[] params) {
			// Avatar
			Bitmap avatar = mCache.getLargeAvatar(mModel);
			publishProgress(new Object[]{0, avatar});
			
			// Cover
			if (!mModel.cover_image.trim().equals("")) {
				Bitmap cover = mCache.getCover(mModel);
				if (cover != null) {
					publishProgress(new Object[]{1, cover});
				}
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Object[] values) {
			super.onProgressUpdate(values);
			
			switch ((int) values[0]) {
				case 0:
					if (mAvatar != null) {
						mAvatar.setImageBitmap((Bitmap) values[1]);
					}
					break;
				case 1:
					if (mCover != null) {
						mCover.setBackground(new BitmapDrawable((Bitmap) values[1]));
					}
					break;
			}
		}
	}
}
