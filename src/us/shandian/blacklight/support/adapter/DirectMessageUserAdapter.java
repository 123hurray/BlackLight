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

package us.shandian.blacklight.support.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.user.UserApiCache;
import us.shandian.blacklight.model.DirectMessageUserModel;
import us.shandian.blacklight.model.DirectMessageUserListModel;
import us.shandian.blacklight.model.UserModel;
import us.shandian.blacklight.support.AsyncTask;
import us.shandian.blacklight.support.HackyMovementMethod;
import us.shandian.blacklight.support.SpannableStringUtils;
import us.shandian.blacklight.support.StatusTimeUtils;

public class DirectMessageUserAdapter extends BaseAdapter
{
	private DirectMessageUserListModel mList;
	private DirectMessageUserListModel mClone;
	private LayoutInflater mInflater;
	private UserApiCache mUserApi;
	private Context mContext;
	
	public DirectMessageUserAdapter(Context context, DirectMessageUserListModel list) {
		mList = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mUserApi = new UserApiCache(context);
		mContext = context;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mClone.getSize();
	}

	@Override
	public Object getItem(int position) {
		return mClone.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= getCount()) {
			return convertView;
		} else {
			DirectMessageUserModel user = mClone.get(position);
			View v;
			ViewHolder h;
			
			v = convertView != null ? convertView : mInflater.inflate(R.layout.direct_message_user, null);
			h = v.getTag() != null ? (ViewHolder) v.getTag() : new ViewHolder(v, user);
			h.user = user;
			
			TextView name = h.name;
			TextView text = h.text;
			
			name.setText(user.user.getName());
			text.setText(user.direct_message.text);
			h.avatar.setImageBitmap(null);
			
			new AvatarDownloader().execute(v, user);
			
			TextView date = h.date;
			
			date.setText(StatusTimeUtils.instance(mContext).buildTimeString(user.direct_message.created_at));
			
			return v;
		}
	}

	@Override
	public void notifyDataSetChanged() {
		mClone = mList.clone();
		super.notifyDataSetChanged();
	}
	
	private class AvatarDownloader extends AsyncTask<Object, Void, Object[]> {
		@Override
		protected Object[] doInBackground(Object... params) {
			if (params[0] != null) {
				DirectMessageUserModel u = (DirectMessageUserModel) params[1];
				
				Bitmap img = mUserApi.getSmallAvatar(u.user);
				
				return new Object[] {params[0], img, params[1]};
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			
			if (result != null) {
				View v = (View) result[0];
				Bitmap img = (Bitmap) result[1];
				DirectMessageUserModel usr = (DirectMessageUserModel) result[2];
				ViewHolder h = (ViewHolder) v.getTag();
				if (h.user == usr) {
					h.avatar.setImageBitmap(img);
				}
			}
		}
	}
	
	class ViewHolder {
		public DirectMessageUserModel user;
		@InjectView(R.id.direct_message_avatar) public ImageView avatar;
		@InjectView(R.id.direct_message_name) public TextView name;
		@InjectView(R.id.direct_message_text) public TextView text;
		@InjectView(R.id.direct_message_date) public TextView date;
		private View v;
		
		public ViewHolder(View v, DirectMessageUserModel user) {
			this.v = v;
			this.user = user;
			
			ButterKnife.inject(this, v);
			v.setTag(this);
		}
		
	}
}
