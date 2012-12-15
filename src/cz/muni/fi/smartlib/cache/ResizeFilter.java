package cz.muni.fi.smartlib.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ResizeFilter implements HttpImageManager.BitmapFilter {

	public static final int DEFAULT_BOOK_WIDTH = 64;
	
	private Context mContext;
	
	public ResizeFilter(Context context) {
		this.mContext = context;
	}
	
	@Override
	public Bitmap filter(Bitmap in) {
		Bitmap out = null;
		if (in != null) {
			int width = in.getWidth();
			int height = in.getHeight();
			float newWidthDIP = scaleToDIP(mContext, DEFAULT_BOOK_WIDTH);
			float scale = newWidthDIP / width;
			out = Bitmap.createScaledBitmap(in, Math.round(newWidthDIP), Math.round(height * scale), true);

		}
		return out;
	}
	
	private static float scaleToDIP(Context context, int pixels) {
		final float scale = context.getResources().getDisplayMetrics().density;
		//return pixels * scale;
		return (pixels * scale + 0.5f); // 0.5f for rounding
	}

	public Bitmap filter(Drawable drawable) {
		return filter(((BitmapDrawable)drawable).getBitmap());
	}

}
