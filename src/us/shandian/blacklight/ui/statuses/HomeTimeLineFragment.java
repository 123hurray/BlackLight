package us.shandian.blacklight.ui.statuses;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.support.adapter.WeiboAdapter;
import static us.shandian.blacklight.cache.Constants.HOME_TIMELINE_PAGE_SIZE;

public class HomeTimeLineFragment extends Fragment implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener
{
	private ListView mList;
	private View mFooter;
	private WeiboAdapter mAdapter;
	private HomeTimeLineApiCache mCache;
	
	// Pull To Refresh
	private SwipeRefreshLayout mSwipeRefresh;
	
	private boolean mRefreshing = false;
	private boolean mNoMore = false;
	
	protected boolean mBindOrig = true;
	
	private int mLastCount = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		initTitle();
		
		View v = inflater.inflate(R.layout.home_timeline, null);
		mList = (ListView) v.findViewById(R.id.home_timeline);
		mCache = bindApiCache();
		mCache.loadFromCache();
		mAdapter = new WeiboAdapter(getActivity(), mCache.mMessages, mBindOrig);
		mList.setAdapter(mAdapter);
		mList.setOnScrollListener(this);
		bindFooterView(inflater);
		
		// Swipe To Refresh
		bindSwipeToRefresh((ViewGroup) v);
		
		if (mCache.mMessages.getSize() == 0) {
			new Refresher().execute(new Boolean[]{true});
		}
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		
		mCache.cache();
	}

	@Override
	public void onResume() {
		super.onResume();
		initTitle();
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Refresh when scroll nearly to bottom
		if (!mNoMore && !mRefreshing && totalItemCount >= HOME_TIMELINE_PAGE_SIZE &&  firstVisibleItem >= totalItemCount - 2 * visibleItemCount) {
			new Refresher().execute(new Boolean[]{false});
		}
	}
	
	@Override
	public void onRefresh() {
		if (!mRefreshing) {
			new Refresher().execute(new Boolean[]{true});
		} else {
			mSwipeRefresh.setRefreshing(false);
		}
	}
	
	protected HomeTimeLineApiCache bindApiCache() {
		return new HomeTimeLineApiCache(getActivity());
	}
	
	protected void initTitle() {
		getActivity().getActionBar().setTitle(R.string.timeline);
	}
	
	protected void bindFooterView(LayoutInflater inflater) {
		mList.addFooterView((mFooter = inflater.inflate(R.layout.timeline_footer, null)));
	}
	
	protected void bindSwipeToRefresh(ViewGroup v) {
		mSwipeRefresh = new SwipeRefreshLayout(getActivity());
		
		// Move child to SwipeRefreshLayout, and add SwipeRefreshLayout to root view
		v.removeViewInLayout(mList);
		v.addView(mSwipeRefresh, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mSwipeRefresh.addView(mList, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		mSwipeRefresh.setOnRefreshListener(this);
		mSwipeRefresh.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
									 android.R.color.holo_orange_dark, android.R.color.holo_red_dark);
	}
	
	private class Refresher extends AsyncTask<Boolean, Void, Void>
	{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLastCount = mCache.mMessages.getSize();
			mRefreshing = true;
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(true);
			}
			
			if (mNoMore) {
				mNoMore = false;
				mFooter.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		protected Void doInBackground(Boolean[] params) {
			mCache.load(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mAdapter.notifyDataSetChanged();
			mRefreshing = false;
			if (mSwipeRefresh != null) {
				mSwipeRefresh.setRefreshing(false);
			}
			
			// Cannot load more
			if (mCache.mMessages.getSize() == mLastCount) {
				mFooter.setVisibility(View.GONE);
				
				// Set this flag to true, and this task can't be started again
				mNoMore = true;
			}
		}

		
	}
}
