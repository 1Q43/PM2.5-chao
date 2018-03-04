package app.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

import app.Entity.Forecast;
import app.Entity.HeartRate;
import app.Entity.State;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * /**
 * Created by sweet on 15-10-4.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }

    static {
        cupboard().register(State.class);
        cupboard().register(Forecast.class);
        cupboard().register(HeartRate.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard().withDatabase(db).upgradeTables();
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

/*    private static boolean mainTmpDirSet = false;
    // 为了修复经常访问数据库出现无法访问文件的问题
    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (!mainTmpDirSet) {
            boolean rs = new File("/data/data/com.example.pm/databases/main").mkdir();
            Log.d("BandInfo-", rs + "");
            super.getReadableDatabase().execSQL("PRAGMA temp_store_directory = '/data/data/com.example.pm/databases/main'");
            mainTmpDirSet = true;
            return super.getWritableDatabase();
        }
        return super.getWritableDatabase();
    }*/
}
