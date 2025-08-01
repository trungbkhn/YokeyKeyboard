package company.librate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;


public class FontUntil {
    private static long timeToast = 0;

    public static Typeface getTypeface(String name, Context context) {
        return Typeface.createFromAsset(context.getAssets(), name);
    }

    public static void customToast(Context context, String name) {
        if (System.currentTimeMillis() - timeToast > 2000) {
            timeToast = System.currentTimeMillis();
            LayoutInflater inflater =(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            @SuppressLint("InflateParams")
            View toastRoot = inflater.inflate(R.layout.custom_toast, null);
            TextView tv = toastRoot.findViewById(R.id.txt_toast);
            tv.setWidth(getScreenHeight() / 4);
            tv.setText(name);
            tv.setHeight((int) tv.getTextSize() * 4);
            Toast toast1 = new Toast(context);
            toast1.setGravity(Gravity.CENTER_HORIZONTAL, 0, getScreenHeight() / 4);
            toast1.setView(toastRoot);
            toast1.show();
            toast1.setDuration(Toast.LENGTH_SHORT);
        }
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
