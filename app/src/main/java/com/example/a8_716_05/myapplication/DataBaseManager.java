package com.example.a8_716_05.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Shin on 2016-11-20.
 */

public class DataBaseManager {
    private static String TAG = "DBManager";

    private SQLiteDatabase db;
    private DataBaseHelper dbHelper;
    private static String DB_NAME = "Applock.db";
    private static int DB_VERSION = 2;
    public static String DB_TABLE = "applockers";
    private final Context mContext;
    
    private static class DataBaseHelper extends SQLiteOpenHelper{

        public DataBaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        // DB와 Table을 생성함
        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "create table " + DB_TABLE
                    + "(_id text not null ,"
                    + "en INTEGER,"
                    + "ch INTEGER)";
            try{
                db.execSQL(sql);
            }catch (SQLException ex){
                Log.w(TAG, ex);
            }
        }

        // DataBase가 업데이트 되는 경우
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }
    }

    public DataBaseManager(Context context){
        this.mContext = context;
    }

    // DataBaseHelper를 생성
    public DataBaseManager open() throws SQLException{
        dbHelper = new DataBaseHelper(this.mContext);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    // Data insert
    public void insertRecord(ContentValues recordValues){
        db = dbHelper.getWritableDatabase();
        try{
            db.insert(DB_TABLE, null, recordValues);
        }catch (SQLException ex){
            Log.w(TAG, "insertRecord(ContentValues recordValues)" + ex);
        }

    }

    // DataBase에서 id가 패키지명인 레코드를 찾음.
    public Cursor executeOneQuery(String str){

        db = dbHelper.getReadableDatabase();

        String SQL = "select _id, en, ch FROM applockers WHERE _id=?";
        Cursor c1 = null;
        try{
            c1= db.rawQuery(SQL, new String[] {str});
        }catch (SQLException ex){
            Log.w(TAG, "executeOneQuery(String str)" + ex);
        }
        return c1;
    }


    // Record의 값을 바꿈
    public void updateRecord(String str, ContentValues recordValues){
        db = dbHelper.getWritableDatabase();
        String[] whereArgs ={str};
        try{
            db.update(DB_TABLE, recordValues,"_id=?", whereArgs);
        }catch (SQLException ex){
            Log.w(TAG, "UpdateRecord" + ex);
        }
    }

    // 모든 레코드의 ch 값을 0으로 초기화
    public void allUpdateRecord(){
        ContentValues recordValues = new ContentValues();
        recordValues.put("ch",0);
        db = dbHelper.getWritableDatabase();
        try{
            db.update(DB_TABLE, recordValues, null, null);
        }catch (SQLException ex){
            Log.w(TAG, "AllUpdateRecord" + ex);
        }
    }

}
