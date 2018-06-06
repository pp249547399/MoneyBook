package tw.edu.ncut.login;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.myapplication.R;

/**
 * Created by aaa on 2018/3/30.
 */

public class incomeActivity extends AppCompatActivity {



    private TextView total_income,total_expenses,total,total_income2,total_expenses2,total2;
    private ListView lv_income;
    private Button btn_add_income, btn_determine;
    private MyDBHelper helper;
    private Cursor incomeData,addData;
    private int incomeTotalMoney,expenditureTotalMoney;
    private ArrayList<String> incomeNameArrayList;
    private  int incomeflag=0; //輸入來源的指標 初始化  incomeflag 是決定AddedFragment.class 的支出來源
    //--------------------------------------------------------------------------
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String myEmail,account;
    private String SPincomeName[] ={"現金","銀行","信用卡"};
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);
        total_income = (TextView) findViewById(R.id.total_income);
        total_expenses = (TextView) findViewById(R.id.total_expenses);
        total = (TextView) findViewById(R.id.total);
        total_income2 = (TextView) findViewById(R.id.total_income2);
        total_expenses2 = (TextView) findViewById(R.id.total_expenses2);
        total2 = (TextView) findViewById(R.id.total2);
        lv_income = (ListView) findViewById(R.id.lv_income);
        btn_add_income = (Button) findViewById(R.id.btn_add_income);
        btn_determine = (Button) findViewById(R.id.btn_determine);
        incomeNameArrayList = new ArrayList<String>();
        //-----------------------------------------------------------------------


        loadingIncome();
        setlayout();
        refreshIncomeListView();
        ListViewOnClick();
        btnListener();

    }
    // btn的監聽事件
    private void btnListener() {
        //結束此畫面 並把 incomeFlag 收入來源指標 回傳到 101 給AddedFragment 使用
        btn_determine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle budle = new Bundle();
                budle.putInt("incomeFlag", incomeflag);
                intent.putExtras(budle);
                setResult(101, intent);
                finish();
            }
            });
        //客製化 AlertDialog  : 做income類別 新增
        btn_add_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(incomeActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_new_income, null);
                final Spinner sp_income = (Spinner)mView.findViewById(R.id.sp_income);
                final EditText ed_name = (EditText) mView.findViewById(R.id.ed_name);
                Button btn_new =(Button) mView.findViewById(R.id.btn_new);
                //
                ArrayAdapter<String> adp = new ArrayAdapter<String>(incomeActivity.this,
                        android.R.layout.simple_spinner_item,
                        SPincomeName);
                adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//下拉式選單 選 {現金，銀行，信用卡}012
                sp_income.setAdapter(adp);
                //
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                btn_new.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(incomeActivity.this, ""+sp_income.getSelectedItem().toString()+":pp"+sp_income.getSelectedItem()+"f"+, Toast.LENGTH_SHORT).show();
                        if(!ed_name.getText().toString().equals("")){
                            if(sureName(ed_name.getText().toString())){

                                //
                                int incomeIcon[]={R.drawable.ic_incom_money,R.drawable.ic_income_banks,R.drawable.ic_income_creditcard};
                                account = getMyEmail() + ".db";
                                helper = new MyDBHelper(incomeActivity.this, account, null, 1);
                                helper.getWritableDatabase();
                                ContentValues values =new ContentValues();
                                values.put("incomeIcon",incomeIcon[(int) sp_income.getSelectedItemId()]);
                                values.put("incomeName",ed_name.getText().toString());
                                helper.getWritableDatabase().insert("incomeCategoryTable",null,values);
                                helper.close();
                                //
                                Log.d("XXXXXXXXXXXXX",sp_income.getSelectedItem().toString());
                                loadingIncome();
                                refreshIncomeListView();
                                //

                                dialog.dismiss();
                            }else {
                                Toast.makeText(incomeActivity.this, "名稱不能重複", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(incomeActivity.this, "請輸入名稱", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

//testIncome();
            }
        });


    }
    public boolean sureName(String s){//有沒有重複
        if(s.equals("")){return  false;}
        int n = incomeNameArrayList.size();
        for(int i=0;i<n;i++){
            if(incomeNameArrayList.contains(s)){
                return false;
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent =new Intent();
        Bundle budle = new Bundle();
        budle.putInt("incomeFlag",incomeflag);
        intent.putExtras(budle);
        setResult(101,intent);
        super.onBackPressed();

    }


    public  void setlayout(){

        total.setText(incomeNameArrayList.get(incomeflag)+"的目前總資產:");
        total_income.setText(incomeNameArrayList.get(incomeflag)+"的收入總金額:");
        total_expenses.setText(incomeNameArrayList.get(incomeflag)+"的支出總金額:");
       if(getrubyLinAddData(incomeflag)) {
           total2.setText("" +(incomeTotalMoney-expenditureTotalMoney)+ "元");
           total_income2.setText("" + incomeTotalMoney+ "元");
           total_expenses2.setText("" + expenditureTotalMoney+ "元");
       }else{
           total2.setText("0元");
           total_expenses2.setText("0元");
           total_income2.setText("0元");
       }
    }
    public void refreshIncomeListView(){

        SimpleCursorAdapter adapter_sql = new SimpleCursorAdapter(this, R.layout.income_listview, incomeData,
                new String[]{"incomeIcon","incomeName"}, new int[]{R.id.lv_income_image,R.id.lv_income_text}, 0);
        lv_income.setAdapter(adapter_sql);
    }
    private void ListViewOnClick() {
        lv_income.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int poistion, long id) {
                incomeflag=poistion;
                loadingIncome();
                setlayout();
            }
        });
    }
    public void testIncome() {
        account = getMyEmail() + ".db";
        helper = new MyDBHelper(this, account, null, 1);
        incomeData = helper.getReadableDatabase().query("incomeCategoryTable",
                new String[]{"_id", "incomeIcon", "incomeName"},
                null, null,
                null, null, null);
        int n = incomeData.getCount();
        incomeData.moveToFirst();
        while (!incomeData.isAfterLast()) {
            Log.d("3a417067", "name" + incomeData.getString(2) + "_id" + incomeData.getInt(0));
            incomeData.moveToNext();
        }
    }
    public void loadingIncome(){
        //     "CREATE TABLE incomeTable"+
//             "(_id INTEGER PRIMARY KEY  NOT NULL , "+
//             "incomeIcon INTEGER,"+
//             "incomeName TEXT,"+
//             "incomeCategory INTEGER,"+
//             "totalMoney INTEGER," +
//             "incomeTotalMoney INTEGER," +
//             "expenditureTotalMoney INTEGER)");
        account = getMyEmail() + ".db";
        helper = new MyDBHelper(this, account, null, 1);
        incomeData = helper.getReadableDatabase().query("incomeCategoryTable",
                new String[]{"_id","incomeIcon","incomeName"},
                null, null,
                null,null,null);
        int n= incomeData.getCount();
        incomeData.moveToFirst();

        while (!incomeData.isAfterLast()) {
            if(!(incomeNameArrayList.contains(incomeData.getString(2)))){
                incomeNameArrayList.add(incomeData.getString(2));
            }
//            incomeName[incomeData.getInt(3)] =incomeData.getString(2);
         incomeData.moveToNext();
        }


    }
    public boolean getrubyLinAddData(int vIncomeFlag){
        account = getMyEmail() + ".db";
        helper = new MyDBHelper(this, account, null, 1);
        addData = helper.getReadableDatabase().query("rubyLin",
                new String[]{"_id","itemCategory","itemMoney"},
                "incomeID = ?",new String[]{String.valueOf(vIncomeFlag)},
                null,null,null);
        if(addData.getCount()==0){
            return false;
        }

        addData.moveToFirst();
        incomeTotalMoney=0;
        expenditureTotalMoney=0;
        while (!addData.isAfterLast()){
            if(addData.getInt(1)==11){//收入類別
               incomeTotalMoney=incomeTotalMoney+Integer.parseInt(addData.getString(2));
            }else {//支出類別
                expenditureTotalMoney=expenditureTotalMoney+Integer.parseInt(addData.getString(2));
            }
            addData.moveToNext();
        }
        return true;
    }


    //(7)myEmail
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }

}
