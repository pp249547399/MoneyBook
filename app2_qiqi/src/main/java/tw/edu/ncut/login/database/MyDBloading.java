package tw.edu.ncut.login.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

import tw.edu.ncut.login.myapplication.R;

/**
 * Created by user on 2018/3/31.
 */

public class MyDBloading {
    public MyDBloading(Activity a, String DatabaseName){

        helper = new MyDBHelper(a, DatabaseName, null, 1);

    }
    private MyDBHelper helper;
    private Cursor sqlData;
    //---------------------------
    private  String scName [][]= {
            {"早餐","早午餐","午餐","晚餐","宵夜","飲料","點心","水果","食材"},
            {"上衣","褲子","外套","洋裝","裙子","鞋子","配件","剪髮理容"},
            {"房租","管理費","水費","電費","瓦斯費","用品","網路","家具"},
            {"交通","油錢","停車費","過路費","洗車與保養"},
            {"學雜費","補習費","文具","書籍","雜誌"},
            {"遊戲","玩具","電影","旅遊","運動"},
            {"家人","另一伴","請客","送禮","外借","均攤"},
            {"手續","保險","基金","股票","其他"},
            {"醫檢","勞健保費"},{"寶貝寵物","雜記"},{"通信費","3C產品"},
            {"收入"}};
    //---------------------------------
    //---------收入
    private String incomeName[] ={"現金","銀行","信用卡"};
    private int incomeCategory[]={0,1,2};
    private int incomeIcon[]={R.drawable.ic_incom_money,R.drawable.ic_income_banks,R.drawable.ic_income_creditcard};
    private int incomeFlag;
    //------------------importMyDB--------------------------------
    private int typeIm[] = {R.drawable.ic_food,R.drawable.ic_sock, R.drawable.ic_home,
            R.drawable.ic_bike,R.drawable.ic_book,R.drawable.ic_play,
            R.drawable.ic_relationship,R.drawable.ic_money,R.drawable.ic_health,
            R.drawable.ic_thing,R.drawable.ic_3c, R.drawable.ic_income};
//*-------------------------------------------
    public String getincomeName(){
        return incomeName[incomeFlag];
    }
    public String getscName(int bigC, int smallC){

        return scName [bigC][smallC];
    }
    public int getscNameLength(int i){

       return scName [i].length;
    }
    public void setincomeFlag(int incomeFlag){
        this.incomeFlag =incomeFlag;
    }
    public int getIncomeFlag(){
        return incomeFlag;
    }
    public String getscNameBasic(int bigC, int smallC){

        return scName [bigC][smallC];
    }

    //(3)資料匯入SQLite RubyLinTable
    public void saverubyLinToSQLite(int year, int month, int day, int incomeID, int type, int type2, String itemName, String itemMoney){
        helper.getWritableDatabase();
        ContentValues value = new ContentValues();
        //日期
        value.put("dateYear",year);
        value.put("dateMonth",month);
        value.put("dateDay",day);
        //項目
        value.put("incomeID",incomeID);
        value.put("itemCategory",type);
        value.put("smallCategory",type2);
        value.put("itemType",typeIm[type]);
        value.put("itemName",itemName);
        value.put("itemMoney",itemMoney);
        value.put("categoryName",getscName(type,type2));
        helper.getWritableDatabase().insert("rubyLin",null,value);
        //Log.d("ADD ",id+","+itemName+","+itemMoney+","+type2+","+typeIm[type]+","+year+"/"+month+"/"+day);
        helper.close();
    }
    //匯入SQLite小類別的名稱(categoryTable初始化)init
    public void initCategoryTable(){
        helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //  TABLE:categoryTable"+"(_id INTEGER PRIMARY KEY  NOT NULL , "+"itemCategory INTEGER," +"smallCategory INTEGER," +"categoryName TEXT)"
        for(int i=0;i<scName.length;i++){
            for(int j =0;j<scName[i].length;j++){
                values.put("itemCategory",i);
                values.put("smallCategory ",j);
                values.put("categoryName",scName[i][j]);
                helper.getWritableDatabase().insert("categoryTable",null,values);
                helper.close();
            }
        }
    }
        //匯入SQLite 收入初始化init
    public void initIncomeCategoryTable(){
//    sqLiteDatabase.execSQL(
//            "CREATE TABLE incomeTable"+
//            "(_id INTEGER PRIMARY KEY  NOT NULL , "+
//            "incomeIcon INTEGER"+
//            "incomeName TEXT"+
//            "incomeCategory TEXT"+
//            "TotalMoney TEXT" +
//            "incomeTotalMoney" +
//            "expenditureTotalMoney)");

        helper.getWritableDatabase();
        ContentValues values =new ContentValues();
        for(int i=0;i<incomeName.length;i++){
            //  TABLE : incomeTable""incomeIcon INTEGER"+ "incomeName TEXT"+ "incomeCategory TEXT"+ "incomeMoney TEXT
            values.put("incomeIcon",incomeIcon[i]);
            values.put("incomeName", incomeName[i]);
            values.put("incomeCategory",incomeCategory[i]);
            helper.getWritableDatabase().insert("incomeCategoryTable",null,values);
        }
        helper.close();
    }


}

