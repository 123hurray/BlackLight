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

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentStatePagerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

import java.util.List;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import us.shandian.blacklight.R;
import us.shandian.blacklight.api.attitudes.AttitudesApi;
import us.shandian.blacklight.api.statuses.PostApi;
import us.shandian.blacklight.cache.login.LoginApiCache;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageModel;
import us.shandian.blacklight.model.MessageListModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.Utility;
import us.shandian.blacklight.ui.comments.CommentOnActivity;
import us.shandian.blacklight.ui.comments.StatusCommentFragment;
import us.shandian.blacklight.ui.common.AbsActivity;

public class SingleActivity extends AbsActivity
{
	private MessageModel mMsg;
	
	private Fragment mMsgFragment;
	private Fragment mCommentFragment;
	private Fragment mRepostFragment;
	
	@InjectView(R.id.single_pager) ViewPager mPager;
	@InjectView(R.id.single_root) SlidingUpPanelLayout mRoot;
	@InjectView(R.id.single_content) View mContent;
	
	@InjectView(R.id.single_tabs) TabHost mTabs;
	@InjectView(R.id.iv_collapse) ImageView mCollapse;
	
	private MenuItem mFav, mLike;
	
	private boolean mIsMine = false;
	private boolean mFavourited = false;
	private boolean mLiked = false;
	private boolean mFavTaskRunning = false;
	private boolean mLikeTaskRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.single);
		
		// Arguments
		mMsg = getIntent().getParcelableExtra("msg");
		mFavourited = mMsg.favorited;
		mLiked = mMsg.liked;
		if (mMsg.user != null && mMsg.user.id != null) {
			mIsMine = new LoginApiCache(this).getUid().equals(mMsg.user.id);
		}
		
		// Inject
		ButterKnife.inject(this);
		
		mMsgFragment = new HackyFragment();
		mCommentFragment = new StatusCommentFragment(mMsg.id);
		mRepostFragment = new RepostTimeLineFragment(mMsg.id);
		getFragmentManager().beginTransaction().replace(R.id.single_content, mMsgFragment).commit();
		
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
				Utility.setActionBarTranslation(SingleActivity.this, mRoot.getCurrentParalaxOffset());
			}

			@Override
			public void onPanelCollapsed(View panel) {
				mCollapse.setRotation(180);
				Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_180);
				mCollapse.startAnimation(animation);
			}

			@Override
			public void onPanelExpanded(View panel) {
				mCollapse.setRotation(0);
				Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_180);
				mCollapse.startAnimation(animation);
			}

			@Override
			public void onPanelAnchored(View panel) {
				
			}
			
		});
		
		mTabs.setup();
		
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
		});
		
		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positonOffset, int positionOffsetPixels) {
					
				}

				@Override
				public void onPageSelected(int position) {
					mTabs.setCurrentTab(position);
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					
				}
		});

		// Expand the pane on startup
		getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mRoot.expandPane();
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
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.comment_on) {
			commentOn();
			return true;
		} else if (id == R.id.repost) {
			repost();
			return true;
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
		mLike.setTitle(getString(mLiked ? R.string.remove_attitude : R.string.like));
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
	
	private class HackyApiCache extends HomeTimeLineApiCache {
		public HackyApiCache(Context context) {
			super(context);
			mMessages = new MessageListModel();
			((List<MessageModel>) mMessages.getList()).add(mMsg);
		}

		@Override
		public void loadFromCache() {
			
		}

		@Override
		public void load(boolean newWeibo) {
			
		}
		
		@Override
		public void cache() {
			
		}
	}
	
	private class HackyFragment extends TimeLineFragment {
		
		public HackyFragment() {
			mShowCommentStatus = false;
		}

		@Override
		protected void bindSwipeToRefresh(ViewGroup v) {
			
		}

		@Override
		protected HomeTimeLineApiCache bindApiCache() {
			return new HackyApiCache(getActivity());
		}
		
		@Override
		protected void initTitle() {
			
		}
		
		@Override
		protected void bindNewButton(View v) {

		}
	}
}
