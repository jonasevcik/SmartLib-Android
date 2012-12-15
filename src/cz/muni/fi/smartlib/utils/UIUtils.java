package cz.muni.fi.smartlib.utils;
import cz.muni.fi.smartlib.R;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

public class UIUtils {
    private UIUtils() {
    }

    public static void showImageToast(Context context, int id, Drawable drawable) {
        final View view = LayoutInflater.from(context).inflate(R.layout.toast_book_notification, null);
        ((TextView) view.findViewById(R.id.message)).setText(id);
        ((ImageView) view.findViewById(R.id.cover)).setImageDrawable(drawable);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);

        toast.show();
    }

    public static void makeToast(Context context, int message) {
    	String convertedMessage = context.getString(message);
    	makeToast(context, convertedMessage);
    }
    
    public static void makeToast(Context context, String message) {
    	final View view = LayoutInflater.from(context).inflate(R.layout.toast_custom, null);
        ((TextView) view.findViewById(R.id.message_toast)).setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);

        toast.show();
    }
    

    public static void makeLongToast(Context context, int message) {
    	String convertedMessage = context.getString(message);
    	makeLongToast(context, convertedMessage);
    }
    
    public static void makeLongToast(Context context, String message) {
    	final View view = LayoutInflater.from(context).inflate(R.layout.toast_custom, null);
        ((TextView) view.findViewById(R.id.message_toast)).setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);

        toast.show();
    }
    
    public static void makeWarningToast(Context context, int message) {
    	String convertedMessage = context.getString(message);
    	makeWarningToast(context, convertedMessage);
    }
    
    public static void makeWarningToast(Context context, String message) {
    	final View view = LayoutInflater.from(context).inflate(R.layout.toast_warning, null);
        ((TextView) view.findViewById(R.id.message_warning_toast)).setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);

        toast.show();
    }
    
    /*
     *  "2012-05-12T16:17:40-04:00"   -->   12/5/2012
     *  
     * */
    public static String parseDateFromString(String s) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(s.subSequence(8, 10)).append("/").append(s.subSequence(5, 7)).append("/").append(s.subSequence(0, 4));
    	return builder.toString();
    }
    
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }
    
    
}
