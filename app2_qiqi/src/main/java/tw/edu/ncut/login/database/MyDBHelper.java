package tw.edu.ncut.login.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Created by us on 2018/1/15.
 */

public class MyDBHelper extends SQLiteOpenHelper {
    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("ADD ","123");
        sqLiteDatabase.execSQL(
                "CREATE  TABLE main.rubyLin " +
                "(_id INTEGER PRIMARY KEY  NOT NULL , " +
                "dateYear INTEGER , " +
                "dateMonth INTEGER ," +
                "dateDay INTEGER ," +
                "itemCategory INTEGER," +
                "smallCategory INTEGER ," +
                "categoryName TEXT,"+//選擇小類別的名稱
                "itemType INTEGER ," +//小圖示
                "incomeID INTEGER,"+//從哪個income支出收入
                "itemName TEXT," +
                "itemMoney TEXT)");
        sqLiteDatabase.execSQL(
                "CREATE TABLE incomeCategoryTable"+
                "(_id INTEGER PRIMARY KEY  NOT NULL , "+
                "incomeIcon INTEGER,"+
                "incomeName TEXT,"+
                "incomeCategory INTEGER)");
//        sqLiteDatabase.execSQL(
//                "CREATE TABLE income"+
//                "totalMoney INTEGER," +
//                "incomeTotalMoney INTEGER," +
//                "expenditureTotalMoney INTEGER)");
        sqLiteDatabase.execSQL(
                "CREATE TABLE categoryTable"+
                "(_id INTEGER PRIMARY KEY  NOT NULL , "+
                "itemCategory INTEGER," +
                "smallCategory INTEGER," +
                "categoryName TEXT)");
        sqLiteDatabase.execSQL(
                "CREATE TABLE friendData"+
                        "(_id INTEGER PRIMARY KEY  NOT NULL , "+
                        "email TEXT," +
                        "userName TEXT)");
        sqLiteDatabase.execSQL(
                "CREATE TABLE calendarEvent"+
                        "(_id INTEGER PRIMARY KEY NOT NULL,"+
                        "dateYear INTEGER , " +
                        "dateMonth INTEGER ," +
                        "dateDay INTEGER ," +
                        "remiderDate TEXT,"+
                        "title TEXT,"+
                        "color TEXT,"+
                        "eventContext TEXT)");

        sqLiteDatabase.execSQL(
                "CREATE TABLE groupAccData"+
                        "(_id INTEGER PRIMARY KEY  NOT NULL , "+
                        "groupAccName TEXT," +
                        "groupAccMember TEXT,"+
                        "groupInviter TEXT,"+
                        "groupStatue INTEGER)");
        sqLiteDatabase.execSQL(
                "CREATE TABLE groupSendRecevedData"+
                        "(_id INTEGER PRIMARY KEY  NOT NULL , "+
                        "accName TEXT,"+
                        "sender TEXT," +
                        "itemName TEXT,"+
                        "itemMoney TEXT,"+
                        "messageType TEXT,"+
                        "date TEXT,"+
                        "dutchPrice TEXT,"+
                        "dutchStatus TEXT,"+
                        "toBuyList TEXT,"+
                        "groupType TEXT)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
