package com.tapbi.spark.yokey.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class CopyThemeFromAssetToInternalStorageAsync extends AsyncTask<Void, Void, Boolean> {
    private ProgressDialog progressDialog;
    private WeakReference<Context> context;
    private GetThemeInterface getThemeInterface;

    public CopyThemeFromAssetToInternalStorageAsync(Context mContext, GetThemeInterface mGetThemeInterface) {
        context = new WeakReference<>(mContext);
        getThemeInterface = mGetThemeInterface;


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

         return false;

    }

    @Override
    protected void onPostExecute(Boolean existsTheme) {
        super.onPostExecute(existsTheme);
        getThemeInterface.getData(existsTheme);
    }

    public interface GetThemeInterface {
        void getData(boolean isExistsTheme);
    }

}
