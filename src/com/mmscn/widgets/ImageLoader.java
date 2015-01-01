package com.mmscn.widgets;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
	private final static String LOG_TAG = "imageloader";
	private static Drawable mDrawable;
	private final Map<ImageView, OnImageLoadedListener> mListenerMap = new WeakHashMap<ImageView, OnImageLoadedListener>();
	private final Map<OnImageLoadedListener, BitmapDownloaderTask> mTaskMap = new WeakHashMap<ImageLoader.OnImageLoadedListener, ImageLoader.BitmapDownloaderTask>();

	private static final int MAX_SIZE = 600;

	private static final int MAX_ATTEMPTS = 5;

	public ImageLoader(Context context) {
		mDrawable = context.getResources().getDrawable(R.drawable.ic_menu_gallery);
	}

	public void load(String url, ImageView imageView) {
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);
		OnImageLoadedListener l = mListenerMap.get(imageView);

		if (bitmap == null || bitmap.isRecycled()) {
			if (url == null) {
				imageView.setImageDrawable(null);
				return;
			}
			if (null == l) {
				l = new ImageViewListener(imageView);
				mListenerMap.put(imageView, l);
			}
			if (cancelPotentialDownload(url, l)) {

				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(forceLoad(url, l));
				imageView.setImageDrawable(downloadedDrawable);
				imageView.setMinimumHeight(156);
			}
		} else {
			cancelPotentialDownload(url, l);
			imageView.setImageBitmap(bitmap);
		}
	}

	public void load(String url, OnImageLoadedListener l) {
		resetPurgeTimer();
		Bitmap bm = getBitmapFromCache(url);
		if (null == bm || bm.isRecycled()) {
			if (cancelPotentialDownload(url, l))
				forceLoad(url, l);
		} else {
			cancelPotentialDownload(url, l);
			l.onImageLoaded(bm);
		}
	}

	/*
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear. private void
	 * forceDownload(String url, ImageView view) { forceDownload(url, view, null);
	 * }
	 */

	/**
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear.
	 */
	private BitmapDownloaderTask forceLoad(String url, OnImageLoadedListener l) {
		BitmapDownloaderTask task = new BitmapDownloaderTask(l);
		mTaskMap.put(l, task);
		task.execute(url);
		return task;
	}

	private boolean cancelPotentialDownload(String url, OnImageLoadedListener l) {
		BitmapDownloaderTask bitmapDownloaderTask = mTaskMap.get(l);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	static Bitmap downloadBitmap(String url) throws OutOfMemoryError {
		// try {
		// Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
		// cr, Uri.fromFile(new File(url)));
		// return bitmap;
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return null;
		Log.i(LOG_TAG, "url = " + url);
		final int IO_BUFFER_SIZE = 4 * 1024;

		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client = AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					// return BitmapFactory.decodeStream(inputStream);
					// Bug on slow connections, fixed in future release.
					return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Incorrect URL: " + url);
		} catch (Exception e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
		} finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}

		return null;
	}

	/*
	 * An InputStream that skips the exact number of bytes provided, unless it
	 * reaches EOF.
	 */
	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	static class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String url;

		private OnImageLoadedListener listener;

		public BitmapDownloaderTask(OnImageLoadedListener l) {
			this.listener = l;
		}

		/**
		 * Actual download method.
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			Bitmap bm = null;
			int count = 0;
			try {
				do {
					bm = downloadBitmap(url);
					if (++count > MAX_ATTEMPTS)
						break;
				} while (null == bm);
				if (null != bm) {
					int width = bm.getWidth();
					int height = bm.getHeight();
					float ratio = (float) width / MAX_SIZE;
					ratio = Math.max(ratio, (float) height / MAX_SIZE);
					if (ratio > 1) {
						width /= ratio;
						height /= ratio;
						Bitmap thumbnail = Bitmap.createScaledBitmap(bm, width, height, false);
						bm.recycle();
						bm = thumbnail;
					}
				} else
					Log.e("download pacs image", "null image " + url);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			return bm;
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			addBitmapToCache(url, bitmap);

			listener.onImageLoaded(bitmap);

		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download
	 * is in progress.
	 * 
	 * <p>
	 * Contains a reference to the actual download task, so that a download task
	 * can be stopped if a new binding is required, and makes sure that only the
	 * last started download process can bind its result, independently of the
	 * download finish order.
	 * </p>
	 */
	static class DownloadedDrawable extends Drawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}

		@Override
		public void draw(Canvas canvas) {
			// Rect bounds = getBounds();
			// canvas.save();
			// canvas.clipRect(bounds);
			// canvas.translate(-bounds.left, -bounds.top);
			// Rect bounds = mDrawable.getBounds();
			mDrawable.setBounds(getBounds());
			mDrawable.draw(canvas);
			// mDrawable.setBounds(bounds);
			// canvas.restore();
		}

		@Override
		public int getOpacity() {
			return mDrawable.getOpacity();
		}

		@Override
		public void setAlpha(int alpha) {
			mDrawable.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mDrawable.setColorFilter(cf);
		}
	}

	public static void clearSoftCache() {
		Iterator<Entry<String, SoftReference<Bitmap>>> i = sSoftBitmapCache.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, SoftReference<Bitmap>> entry = i.next();
			Bitmap bm = entry.getValue().get();
			if (null != bm)
				bm.recycle();
		}
		sSoftBitmapCache.clear();
	}

	// public void setHardCacheCapacity(int capacity) {
	// HARD_CACHE_CAPACITY = capacity;
	// clearHardCache();
	// }

	public static void clearHardCache() {
		Iterator<Entry<String, Bitmap>> i = sHardBitmapCache.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, Bitmap> entry = i.next();
			entry.getValue().recycle();
		}
		sHardBitmapCache.clear();
	}

	/*
	 * Cache-related fields and methods.
	 * 
	 * We use a hard and a soft cache. A soft reference cache is too aggressively
	 * cleared by the Garbage Collector.
	 */

	private final static int HARD_CACHE_CAPACITY = 10;
	private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

	// Hard cache, with a fixed maximum capacity and a life duration
	private final static HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(
			HARD_CACHE_CAPACITY / 2, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
			if (size() > HARD_CACHE_CAPACITY) {
				// Entries push-out of hard reference cache are transferred to
				// soft reference cache
				sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
		}
	};

	// Soft cache for bitmaps kicked out of hard cache
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
			HARD_CACHE_CAPACITY / 2);

	private final Handler purgeHandler = new Handler();

	private final Runnable purger = new Runnable() {
		public void run() {
			clearCache();
		}
	};

	/**
	 * Adds this bitmap to the cache.
	 * 
	 * @param bitmap
	 *          The newly downloaded bitmap.
	 */
	private static void addBitmapToCache(String url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (sHardBitmapCache) {
				sHardBitmapCache.put(url, bitmap);
			}
		}
	}

	/**
	 * @param url
	 *          The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	protected final Bitmap getBitmapFromCache(String url) {
		// First try the hard reference cache
		synchronized (sHardBitmapCache) {
			final Bitmap bitmap = sHardBitmapCache.get(url);
			if (bitmap != null) {
				// Bitmap found in hard cache
				// Move element to first position, so that it is removed last
				sHardBitmapCache.remove(url);
				sHardBitmapCache.put(url, bitmap);
				return bitmap;
			}
		}

		// Then try the soft reference cache
		SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
		if (bitmapReference != null) {
			final Bitmap bitmap = bitmapReference.get();
			if (bitmap != null) {
				// Bitmap found in soft cache
				return bitmap;
			} else {
				// Soft reference has been Garbage Collected
				sSoftBitmapCache.remove(url);
			}
		}

		return null;
	}

	/**
	 * Clears the image cache used internally to improve performance. Note that
	 * for memory efficiency reasons, the cache will automatically be cleared
	 * after a certain inactivity delay.
	 */
	public static void clearCache() {
		sHardBitmapCache.clear();
		sSoftBitmapCache.clear();
	}

	/**
	 * Allow a new delay before the automatic cache clear is done.
	 */
	protected final void resetPurgeTimer() {
		purgeHandler.removeCallbacks(purger);
		purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
	}

	public interface OnImageLoadedListener {
		public void onImageLoaded(Bitmap bm);
	}

	private class ImageViewListener implements OnImageLoadedListener {
		private Reference<ImageView> imageViewReference;

		private ImageViewListener(ImageView iv) {
			this.imageViewReference = new WeakReference<ImageView>(iv);
		}

		@Override
		public void onImageLoaded(Bitmap bm) {

			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				ImageViewListener bitmapDownloaderTask = (ImageViewListener) mListenerMap.get(imageView);
				// Change bitmap only if this process is still associated with
				// it
				// Or if we don't use any bitmap to task association
				// (NO_DOWNLOADED_DRAWABLE mode)
				if ((this == bitmapDownloaderTask)) {
					if (bm != null)
						imageView.setImageBitmap(bm);
				}
			}
		}

	}
}
