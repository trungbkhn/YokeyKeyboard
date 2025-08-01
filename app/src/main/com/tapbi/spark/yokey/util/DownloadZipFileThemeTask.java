package com.tapbi.spark.yokey.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;

import androidx.core.util.Pair;

import com.tapbi.spark.yokey.data.model.theme.ThemeModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.ResponseBody;

@SuppressLint("StaticFieldLeak")
public class DownloadZipFileThemeTask extends AsyncTask<ResponseBody, Pair<Integer, Long>, Boolean> {
    private Context mContext;
    private String idTheme;
    private DetectDownloadedTheme detectDownloadedTheme;
    //  private ProgressDialog mProgressBar;

    public DownloadZipFileThemeTask(Context context, String fileName, DetectDownloadedTheme isDownloadedTheme) {
        mContext = context;
        idTheme = fileName;
        detectDownloadedTheme = isDownloadedTheme;
        //    mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //   mProgressBar.show();
    }

    @Override
    protected Boolean doInBackground(ResponseBody... responseBodies) {
        return saveThemeToInternalStorage(mContext, responseBodies[0], idTheme + ".zip");

    }

//    @Override
//    protected void onProgressUpdate(Pair<Integer, Long>... progress) {
//        super.onProgressUpdate(progress);
//        Toast.makeText(mContext, "Downloading", Toast.LENGTH_SHORT).show();
//        if (progress[0].first == 100){
//        }
//        if (progress[0].second > 0) {
//       //     int currentProgress = (int) ((double) progress[0].first / (double) progress[0].second * 100);
//           // progressBar.setProgress(currentProgress);
//         //   txtProgressPercent.setText("Progress " + currentProgress + "%");
//        }
//        if (progress[0].first == -1) {
//           // Toast.makeText(mContext, "Download failed", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);
        detectDownloadedTheme.isDownloadedTheme(s);
    }


    public interface DetectDownloadedTheme {
        void isDownloadedTheme(Boolean isDownloaded);
    }


    private boolean saveThemeToInternalStorage(Context context, ResponseBody body, String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        File destinationFile = contextWrapper.getDir(context.getFilesDir().getName(), Context.MODE_PRIVATE);
        File file = new File(destinationFile, fileName);

        try {
            InputStream inputStream = body.byteStream();
            OutputStream outputStream = new FileOutputStream(file);
            byte[] data = new byte[4096];
            int countSize;
            while ((countSize = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, countSize);

            }
            outputStream.flush();
            return unzip(context, destinationFile.getAbsolutePath() + "/" + idTheme + ".zip", destinationFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean unzip(Context context, String zipFile, String location) {
        try {
            File f = new File(location);

            if (!f.isDirectory()) {
                f.mkdirs();
            }
            try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + File.separator + ze.getName();
                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {

                        try (FileOutputStream fout = new FileOutputStream(path, false)) {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);

                            }
                            zin.closeEntry();
                        }
                    }
                }

            }
            ThemeModel themeModel = CommonUtil.parserJsonFromFileTheme(context, idTheme);
            ContextWrapper contextWrapper = new ContextWrapper(context);
            File file = contextWrapper.getDir(context.getFilesDir().getName(), Context.MODE_PRIVATE);


            if (themeModel != null) {
                File fileTheme = new File(file.toString(), themeModel.getId());
                if (fileTheme.exists()) {
                    File deleteFileZipAfterUnzip = new File(zipFile);
                    boolean deleted = deleteFileZipAfterUnzip.delete();
                    return true;
                }
            }

        } catch (Exception e) {
            File file = new File(zipFile);
            boolean deleted = file.delete();
            e.printStackTrace();
            return false;

        }


        return false;
    }


}
