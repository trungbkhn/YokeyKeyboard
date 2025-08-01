package com.tapbi.spark.yokey.data.local.dbversion1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class Version1SqliteOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "fontvcAndroid.sqlite";
    private static String DB_PATH = "";
    private final Context mContext;


    public Version1SqliteOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);// 1? Its database Version
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.mContext = context;
    }


    public void createDataBase() {
        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            try {
                this.getReadableDatabase();
                //this.close();
                //Copy the database from assests
                copyDataBase();
                //Timber.e("createDatabase database created");
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    //Check that the database exists here: /data/data/your package/databases/Da Name
    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(Build.VERSION_CODES.P == Build.VERSION.SDK_INT)db.disableWriteAheadLogging();
    }
}

