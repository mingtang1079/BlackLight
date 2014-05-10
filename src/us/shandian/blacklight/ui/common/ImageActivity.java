package us.shandian.blacklight.ui.common;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.roisoleil.gifview.GifView;

import uk.co.senab.photoview.PhotoView;

import us.shandian.blacklight.R;
import us.shandian.blacklight.cache.statuses.HomeTimeLineApiCache;
import us.shandian.blacklight.model.MessageModel;

public class ImageActivity extends SwipeBackActivity
{
	private ViewPager mPager;
	private MessageModel mModel;
	private HomeTimeLineApiCache mApiCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mApiCache = new HomeTimeLineApiCache(this);
		
		mModel = getIntent().getParcelableExtra("model");
		int def = getIntent().getIntExtra("defaultId", 0);
		
		setContentView(R.layout.image_activity);
		
		mPager = (ViewPager) findViewById(R.id.image_pager);
		mPager.setAdapter(new ImageAdapter());
		mPager.setCurrentItem(def);
		
		if (Build.VERSION.SDK_INT >= 19) {
			int flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
			getWindow().setFlags(flags, flags);
		}
	}
	
	private class ImageAdapter extends PagerAdapter {
		private ArrayList<View> mViews = new ArrayList<View>();
		
		public ImageAdapter() {
			for (int i = 0; i < getCount(); i++) {
				mViews.add(null);
			}
		}
		
		@Override
		public int getCount() {
			return mModel.hasMultiplePictures() ? mModel.pic_urls.size() : 1;
		}

		@Override
		public boolean isViewFromObject(View v, Object obj) {
			return v == obj;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = mViews.get(position);
			if (v != null) {
				container.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				return v;
			} else {
				LinearLayout ll = new LinearLayout(ImageActivity.this);
				ll.setGravity(Gravity.CENTER);
				ll.addView(new ProgressBar(ImageActivity.this));
				container.addView(ll, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				mViews.set(position, ll);
				new DownloadTask().execute(new Object[]{ll, position});
				return ll;
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
	}
	
	private class DownloadTask extends AsyncTask<Object, Void, Object[]> {

		@Override
		protected Object[] doInBackground(Object[] params) {
			int id = params[1];
			Object img = mApiCache.getLargePic(mModel, id);
			return new Object[]{params[0], img};
		}

		@Override
		protected void onPostExecute(Object[] result) {
			super.onPostExecute(result);
			ViewGroup v = (ViewGroup) result[0];
			Object img = result[1];
			
			if (img != null) {
				v.removeAllViews();
				if (img instanceof Bitmap) {
					PhotoView p = new PhotoView(ImageActivity.this);
					p.setImageBitmap((Bitmap) img);
					v.addView(p, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				} else if (img instanceof Movie) {
					GifView g = new GifView(ImageActivity.this);
					g.setMovie((Movie) img);
					v.addView(g, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				}
			}
		}

	}
	
}
