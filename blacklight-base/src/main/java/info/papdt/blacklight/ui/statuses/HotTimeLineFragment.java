package info.papdt.blacklight.ui.statuses;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import info.papdt.blacklight.cache.statuses.HomeTimeLineApiCache;
import info.papdt.blacklight.cache.statuses.HotTimeLineApiCache;
import info.papdt.blacklight.support.Settings;

public class HotTimeLineFragment extends TimeLineFragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		// Restore position
		int pos = mSettings.getInt(Settings.LAST_POSITION, 0);
		if (pos > 0 && pos < mAdapter.getCount()) {
			mList.scrollToPosition(pos);
		}
		mSettings.putInt(Settings.LAST_POSITION, 0);
		
		mAdapter.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					mSettings.putInt(Settings.LAST_POSITION, mManager.findFirstVisibleItemPosition());
				}
			}
		});
		return v;
	}
	@Override
	protected HomeTimeLineApiCache bindApiCache() {
		return new HotTimeLineApiCache(getActivity());
	}

	@Override
	protected void load(boolean param) {
		mCache.load(param);
		mCache.cache();
	}
}
