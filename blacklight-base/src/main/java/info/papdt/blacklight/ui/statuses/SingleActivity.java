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

package info.papdt.blacklight.ui.statuses;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcel;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import info.papdt.blacklight.R;
import info.papdt.blacklight.api.attitudes.AttitudesApi;
import info.papdt.blacklight.api.statuses.PostApi;
import info.papdt.blacklight.cache.login.LoginApiCache;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.model.MessageListModel;
import info.papdt.blacklight.model.MessageModel;
import info.papdt.blacklight.support.AsyncTask;
import info.papdt.blacklight.support.Utility;
import info.papdt.blacklight.ui.comments.CommentOnActivity;
import info.papdt.blacklight.ui.comments.StatusCommentFragment;
import info.papdt.blacklight.ui.common.AbsActivity;
import info.papdt.blacklight.ui.common.LinearViewPagerIndicator;

public class SingleActivity extends AbsActivity
{
	private MessageModel mMsg;
	
	private Fragment mMsgFragment;
	private Fragment mCommentFragment;
	private Fragment mRepostFragment;
	
	private ViewPager mPager;
	private SlidingUpPanelLayout mRoot;
	private View mDragger;
	private View mContent;
	
	private LinearViewPagerIndicator mIndicator;
	private ImageView mCollapse;
	private ImageView[] mIcons = new ImageView[2];
	
	private MenuItem mFav, mLike;

	private boolean mIsMine = false;
	private boolean mFavourited = false;
	private boolean mLiked = false;
	private boolean mFavTaskRunning = false;
	private boolean mLikeTaskRunning = false;
	private boolean mDark = false;

	private int mActionBarColor, mDragBackgroundColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLayout = R.layout.single;
		super.onCreate(savedInstanceState);
		
		mActionBarColor = getResources().getColor(R.color.action_gray);
		mDragBackgroundColor = getResources().getColor(R.color.light_gray);
		mDark = Utility.isDarkMode(this);

		// Arguments
		mMsg = getIntent().getParcelableExtra("msg");
		mFavourited = mMsg.favorited;
		mLiked = mMsg.liked;
		if (mMsg.user != null && mMsg.user.id != null) {
			mIsMine = new LoginApiCache(this).getUid().equals(mMsg.user.id);
		}
		
		// Initialize views
		mPager = Utility.findViewById(this, R.id.single_pager);
		mRoot = Utility.findViewById(this, R.id.single_root);
		mDragger = Utility.findViewById(this, R.id.single_dragger);
		mContent = Utility.findViewById(this, R.id.single_content);
		mIndicator = Utility.findViewById(this, R.id.single_indicator);
		mCollapse = Utility.findViewById(this, R.id.iv_collapse);
		mIcons[0] = Utility.findViewById(this, R.id.single_comment_img);
		mIcons[1] = Utility.findViewById(this, R.id.single_repost_img);
		
		View comment = Utility.findViewById(this, R.id.single_comment);
		View repost = Utility.findViewById(this, R.id.single_repost);
		
		// Bind onClick events
		Utility.bindOnClick(this, comment, "commentOn");
		Utility.bindOnClick(this, repost, "repost");

		// Dark
		if (mDark) {
			for (ImageView v : mIcons) {
				v.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
			}
		}
		
		mMsgFragment = new HackyFragment();
		Bundle b = new Bundle();
		b.putParcelable("msg",mMsg);
		mMsgFragment.setArguments(b);

		mCommentFragment = new StatusCommentFragment(mMsg.id);
		mRepostFragment = new RepostTimeLineFragment(mMsg.id);
		getFragmentManager().beginTransaction().replace(R.id.single_content, mMsgFragment).commit();
		ViewCompat.setTransitionName(findViewById(R.id.single_content), "msg");
		
		mPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Fragment getItem(int position) {
				switch (position) {
					case 0:
						return mCommentFragment;
					case 1:
						return mRepostFragment;
					default:
						return null;
				}
			}
		});

		mRoot.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener(){

			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				//Utility.setActionBarTranslation(SingleActivity.this, mRoot.getCurrentParalaxOffset());
				
				// Gradient color if in light mode
				if (!mDark) {
					float gradientFactor = 1 - slideOffset;
					mDragger.setBackgroundColor(Utility.getGradientColor(mDragBackgroundColor,
							mActionBarColor,gradientFactor));
					int foreground = Utility.getGradientColor(mActionBarColor, mDragBackgroundColor, gradientFactor);
					mIndicator.setForeground(foreground);
					mCollapse.setColorFilter(foreground, PorterDuff.Mode.SRC_IN);
				}

				mCollapse.setRotation((1 - slideOffset) * -180);
				/*mTabWidget.setLeftStripDrawable(new ColorDrawable(Utility
						.getGradientColor(mActionBarColor,mDragBackgroundColor,gradientFactor)));
				mTabWidget.setLeftStripDrawable(new ColorDrawable(Utility
						.getGradientColor(mActionBarColor,mDragBackgroundColor,gradientFactor)));
						*/
			}

			@Override
			public void onPanelCollapsed(View panel) {
			}

			@Override
			public void onPanelExpanded(View panel) {
			}

			@Override
			public void onPanelAnchored(View panel) {
				
			}
			
		});

		// Indicator
		mIndicator.setViewPager(mPager);
		mIndicator.addTab(getResources().getString(R.string.comment) + " " + Utility.addUnitToInt(this, mMsg.comments_count));
		mIndicator.addTab(getResources().getString(R.string.retweet) + " " + Utility.addUnitToInt(this, mMsg.reposts_count));
		
		/*mTabs.setup();

		final String comment = getResources().getString(R.string.comment);
		TabHost.TabSpec tab1 = mTabs.newTabSpec(comment);
		tab1.setIndicator(comment);
		tab1.setContent(android.R.id.tabcontent);
		mTabs.addTab(tab1);
		
		final String repost = getResources().getString(R.string.retweet);
		TabHost.TabSpec tab2 = mTabs.newTabSpec(repost);
		tab2.setIndicator(repost);
		tab2.setContent(android.R.id.tabcontent);
		mTabs.addTab(tab2);

		mTabWidget = mTabs.getTabWidget();
		mTabWidget.setLeftStripDrawable(new ColorDrawable(mActionBarColor));
		mTabWidget.setRightStripDrawable(new ColorDrawable(mActionBarColor));
		mTabWidget.setStripEnabled(false);

		mTabs.setCurrentTab(0);

		Utility.initDarkTabHost(this, mTabs);
		
		// Connect the TabHost with the ViewPager
		mTabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
				@Override
				public void onTabChanged(String id) {
					if (id.equals(comment)) {
						mPager.setCurrentItem(0);
					} else if (id.equals(repost)) {
						mPager.setCurrentItem(1);
					}
				}
		});*/
		
		mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positonOffset, int positionOffsetPixels) {
					
				}

				@Override
				public void onPageSelected(int position) {
					//mTabs.setCurrentTab(position);
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					
				}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.single, menu);
		mFav = menu.findItem(R.id.fav);
		mLike = menu.findItem(R.id.like);
		
		// Can only delete statuses post by me
		if (!mIsMine) {
			menu.findItem(R.id.delete).setVisible(false);
			setFavouriteIcon();
		} else {
			mFav.setVisible(false);
		}

		if (mLiked) {
			setLikeIcon();
		}
		
		ShareActionProvider share = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.share));
		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_TEXT, mMsg.text);
		i.setType("text/plain");
		share.setShareIntent(i);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		/*} else if (id == R.id.comment_on) {
			commentOn();
			return true;
		} else if (id == R.id.repost) {
			repost();
			return true;*/
		} else if (id == R.id.delete) {
			new AlertDialog.Builder(this)
							.setMessage(R.string.confirm_delete)
							.setCancelable(true)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									new DeleteTask().execute();
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
								}
							})
							.show();
			return true;
		} else if (id == R.id.fav) {
			if (!mFavTaskRunning) {
				new FavTask().execute();
			}
			return true;
		} else if (id == R.id.copy) {
			Utility.copyToClipboard(this, mMsg.text);

			return true;
		} else if (id == R.id.like) {
			if (!mLikeTaskRunning){
				new LikeTask().execute();
			}
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Dirty Fix: Strange focus on home as up button
		findViewById(R.id.single_focus).requestFocus();
	}

	@Override
	protected View getSwipeView() {
		return findViewById(R.id.single_root);
	}
	
	public void commentOn() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, CommentOnActivity.class);
		i.putExtra("msg", mMsg);
		startActivity(i);
	}
	
	public void repost() {
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, RepostActivity.class);
		i.putExtra("msg", mMsg);
		startActivity(i);
	}
	
	private void setFavouriteIcon() {
		mFav.setIcon(mFavourited ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
		mFav.setTitle(getString(mFavourited ? R.string.fav_del : R.string.fav_add));
	}

	private void setLikeIcon() {
		mLike.setIcon(mLiked ? R.drawable.ic_action_bad : R.drawable.ic_action_good);
		mLike.setTitle(getString(mLiked ? R.string.remove_attitude : R.string.attitudes));
	}
	
	private class DeleteTask extends AsyncTask<Void, Void, Void> {
		private ProgressDialog prog;
		
		@Override
		protected void onPreExecute() {
			prog = new ProgressDialog(SingleActivity.this);
			prog.setMessage(getResources().getString(R.string.plz_wait));
			prog.setCancelable(false);
			prog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			PostApi.deletePost(mMsg.id);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			prog.dismiss();
			finish();
		}
	}
	
	private class FavTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mFavTaskRunning = true;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if (mFavourited) {
				PostApi.unfav(mMsg.id);
			} else {
				PostApi.fav(mMsg.id);
			}
			
			mFavourited = !mFavourited;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setFavouriteIcon();
			mFavTaskRunning = false;
		}
	}

	private class LikeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLikeTaskRunning = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mLiked) {
				AttitudesApi.cancelLike(mMsg.id);
			} else {
				AttitudesApi.like(mMsg.id);
			}

			mLiked = !mLiked;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setLikeIcon();
			mLikeTaskRunning = false;
		}
	}

}
