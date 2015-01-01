package com.johnsimon.payback.util;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class ThumbnailLoader implements ImageLoadingListener {
	private HashMap<String, Bitmap> cache;
	private HashMap<String, ArrayList<SimpleImageLoadingListener>> callbackMap;

	private static ThumbnailLoader instance;

	private ThumbnailLoader() {
		cache = new HashMap<String, Bitmap>();
		callbackMap = new HashMap<String, ArrayList<SimpleImageLoadingListener>>();
	}

	public static ThumbnailLoader getInstance() {
		if(instance == null) {
			instance = new ThumbnailLoader();
		}

		return instance;
	}


	public void load(String uri, final SimpleImageLoadingListener callback) {
		//TODO MEGA DIRTY
		uri = uri.replaceAll("/photo$", ""); //Det funkade

		if(cache.containsKey(uri)) {
			Bitmap cached = cache.get(uri);

			if(cached != null) {
				callback.onLoadingComplete(uri, null, cached);
				return;
			}
		}

		ArrayList<SimpleImageLoadingListener> callbacks;
		if(callbackMap.containsKey(uri)) {
			callbacks = callbackMap.get(uri);
		} else {
			callbacks = new ArrayList<>();
			callbackMap.put(uri, callbacks);
			cache.put(uri, null);

			ImageLoader.getInstance().loadImage(uri, this);
		}
		callbacks.add(callback);
	}

	@Override
	public void onLoadingStarted(String imageUri, View view) {
	}

	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		cache.put(imageUri, loadedImage);

		ArrayList<SimpleImageLoadingListener> callbacks = callbackMap.get(imageUri);

		for(SimpleImageLoadingListener callback : callbacks) {
			callback.onLoadingComplete(imageUri, view, loadedImage);
		}

		callbackMap.remove(callbacks);
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
	}
}
