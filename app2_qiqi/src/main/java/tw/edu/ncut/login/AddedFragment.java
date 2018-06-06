package tw.edu.ncut.login;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.CalendarDayEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.database.MyDBloading;
import tw.edu.ncut.login.event.EventActivity;
import tw.edu.ncut.login.myapplication.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aaa on 2018/1/17.
 */

public class AddedFragment extends Fragment {
    //
    private ListView lv ;
    private ImageButton btn_add,btn_use_inform;
    private Button btn_income,btn_event;
    private ImageButton btn_last_month,btn_next_month;
    //----------------------偏好儲存---------------------------------------
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String myEmail,account;
    //-------------------------資料庫---------------------------------------
    private MyDBHelper helper;
    private SimpleCursorAdapter adapter_sql;
    private Cursor sqlData,detailData,speechData;


    //---------------------------日期----------------------------------------

    private Calendar calendar;
    private int year,month,day,nowYear,nowMonth,nowDay;//Calendar點選到的日期 //前面有NOW 的是當天日期
    private TextView calendartextView;
    private CompactCalendarView compactCalendarView;//calendarView
    private SimpleDateFormat dateFormatForMonth =
            new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());
    private Map<String, Integer> monthChangeIntMap;
    //-------------------------多選項---------------------------------------
    private List<String> lvData;
    private String name = "";

    private List<String>lvCategory,lvFood,lvClothes,lvLife,lvTraffic,lvEducation,lvFun,lvRelationship,lvMoney,lvHealth,lvThing,lv3C,lvincome;
    private List<String> lvsmallCategory ;//決定該顯示哪一個小類別
    private  int bigCategory,smallCategory;//顯示的類別對應數
    private  boolean spBooleanSwitch ; //全語音選擇
    private  boolean raBooleanSwitch;  //價格合理性
    //-------------------------------------------------------------------------
    private MyDBloading myDBloading;
    private final int incomeActivtiyRequestCode = 101;
    private final int eventActivtiyRequestCode = 102;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_added, container, false);
        //
        lv = view.findViewById (R.id.lv);
        lv = view.findViewById (R.id.lv);
        btn_use_inform=view.findViewById(R.id.btn_use_inform);
        btn_add = view.findViewById(R.id.btn_add);
        btn_income=view.findViewById(R.id.btn_income);
        btn_event=view.findViewById(R.id.btn_event);
        btn_last_month=view.findViewById(R.id.btn_last_month);
        btn_next_month=view.findViewById(R.id.btn_next_month);

        calendartextView=view.findViewById(R.id.calendartextView);
        compactCalendarView = view.findViewById(R.id.calendarView);
        compactCalendarView.drawSmallIndicatorForEvents(true); //設置使用三字母縮寫
        compactCalendarView.setUseThreeLetterAbbreviation(true); //繪製小型活動指標


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        monthChangeIntMap = new HashMap<String, Integer>();
        monthChangeIntMap.put("Jan", 1);
        monthChangeIntMap.put("Feb", 2);
        monthChangeIntMap.put("Mar",3);
        monthChangeIntMap.put("Apr",4);
        monthChangeIntMap.put("May",5);
        monthChangeIntMap.put("Jun",6);
        monthChangeIntMap.put("Jul",7);
        monthChangeIntMap.put("Aug",8);
        monthChangeIntMap.put("Sep",9);
        monthChangeIntMap.put("Oct",10);
        monthChangeIntMap.put("Nov",11);
        monthChangeIntMap.put("Dev",12);
        datebaseLoading();
        myDBloading.setincomeFlag(0);
        CalendarPicker();
        refreshListView();
        selectIncome();
        btnTextInit(myDBloading.getIncomeFlag());
        delItem();
        getSPBoolean(); //取得語音選擇布靈值

        //按下語音輸入按鈕
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechInput();
            }
        });
        super.onActivityCreated(savedInstanceState);

        //按下use_inform使用資訊
        btn_use_inform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_useing_inform, null);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });
        //按下"查看事件的按鈕"
        btn_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();//需要把日期搬去做處理
                intent.setClass(getActivity(),EventActivity.class);
                //傳Calendar點選的日期
                Bundle bundle =new Bundle();
                bundle.putInt("nowYear",nowYear);
                bundle.putInt("nowMonth",nowMonth);
                bundle.putInt("nowDay",nowDay);
                bundle.putInt("year", year);
                bundle.putInt("month",month);
                bundle.putInt("day",day);
                intent.putExtras(bundle);
                startActivityForResult(intent,eventActivtiyRequestCode);// 讓EventActivity.calss 可以把　incomeflag到回來, 是決定eventActivtiyRequestCode


            }
        });

        ///calendarview 日期的上下月按鈕監聽
        calendarviewBTNListener();


    }

    //btn_event UI
    private void btn_eventUI() {
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        Cursor checkTableExists = helper.getReadableDatabase().rawQuery("SELECT count(name) FROM sqlite_master WHERE type='table' and name='calendarEvent'", null);//檢查總表單內有沒有創建calendarEvent
        checkTableExists.moveToFirst();
        if(checkTableExists.getInt(0) != 0) {//有的話就不為0
            Cursor eventData = helper.getReadableDatabase().query("calendarEvent",
                    new String[]{"_id","title"},
                    "dateYear =? AND dateMonth = ? AND dateDay=?",
                    new String[]{String.valueOf(year), String.valueOf(month),String.valueOf(day)},
                    null, null, null);

            eventData.moveToFirst();
            if(eventData.getCount()!= 0) {
                StringBuilder sb=new StringBuilder("");
                while (!eventData.isAfterLast()) {
                    sb.append(eventData.getString(1)+"  ");
                    eventData.moveToNext();
                }
                btn_event.setText("今天的活動："+sb);
            }else{
                btn_event.setText("按此新增事件");
            }
            eventData.close();
        }
        checkTableExists.close();
        helper.close();
    }


    private void datebaseLoading() {
        account = getMyEmail()+".db";
        myDBloading=new MyDBloading(getActivity(),account);
        helper = new MyDBHelper(getActivity(), account, null, 1);//資料庫名字,標準模式,版本
        speechData = helper.getReadableDatabase().query("categoryTable",
                new String[]{"_id","itemCategory","smallCategory","categoryName"}, null, null, null, null, null);
        //Toast.makeText(getActivity(), "ya!!!!!!!!!!!"+speechData.getCount(), Toast.LENGTH_SHORT).show();
        if(speechData.getCount()==0){//如果有問題把手機內程式快取清除
           // Toast.makeText(getActivity(), "ya!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
            myDBloading.initCategoryTable();//匯入SQLite 類別細項\
            myDBloading.initIncomeCategoryTable();//匯入SQLite 收入細項&&設定收入預設指定
        }
        speechData.close();
        helper.close();
    }

    public void btnTextInit(int n){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        Cursor cursor =helper.getReadableDatabase().query("incomeCategoryTable",
                new String[]{"_id","incomeName"},
                "_id=?", new String[]{""+(n+1)},
                null, null, null);
        cursor.moveToFirst();
        Log.d("sdjsdjsdadasdai","cursor:"+cursor.getCount());
        if(!cursor.isAfterLast()) {
            btn_income.setText("支出來源: "+cursor.getString(1)+"\n"+getrubyLinTotal(myDBloading.getIncomeFlag())+"元");
            cursor.moveToNext();
        }
        cursor.close();
        helper.close();
    }

    public  void selectIncome(){
        btn_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),incomeActivity.class);
                startActivityForResult(intent,incomeActivtiyRequestCode);// 讓EventActivity.calss 可以把　incomeflag到回來, 是決定AddedFragment.class 的支出來源

            }
        });
    }



    //-------------------------------------------
    //語音輸入
    public void SpeechInput(){
        Intent input = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        input.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        input.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN.toString());
        input.putExtra(RecognizerIntent.EXTRA_PROMPT,"");
        //
        try{
            startActivityForResult(input,0);
        }catch(ActivityNotFoundException in){
            Toast.makeText(AddedFragment.this.getActivity(),"try again",Toast.LENGTH_LONG).show();
        }
    }
    //全語音功能選擇
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //

        //取得IncomeActivity.class  傳回來的　incomeFlag 支出來源的指標
        if(requestCode ==incomeActivtiyRequestCode){

            myDBloading.setincomeFlag(data.getExtras().getInt("incomeFlag"));
            btnTextInit(myDBloading.getIncomeFlag());
            refreshListView();
            btnTextInit(myDBloading.getIncomeFlag());
        }
        if(requestCode == eventActivtiyRequestCode){//從EventActivity跳回來
            CalendarSpecialEventSet();//設定當月有特殊事件的日期做標點 calendarEvent table
        }
        if (resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            initData(result);

            if(getSPBoolean()) {  //全語音選擇
                lvSpeechSelect();
            }
            else {
                lvDataSelect();
            }

        }
    }
    //取得偏好設定的語音選擇布靈值
    private Boolean getSPBoolean(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spBooleanSwitch = Boolean.valueOf(sharedPreferences.getString("switchBooleanSp", ""));
        Log.d("getboolean",spBooleanSwitch+"   /added");
        return spBooleanSwitch;
    }

    //(0)新增項目 type1=bigCategory type2=smallCategory
    public void addItem(int type,int type2,String add){

        myDBloading.saverubyLinToSQLite( year,month,day,myDBloading.getIncomeFlag(),type,type2,getItemName(add),getItemMoney(add));
        //刷新ListView
        refreshListView();
        btnTextInit(myDBloading.getIncomeFlag());
    }
    //(0)刪除項目及查看細項
    public void delItem(){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final long itemId = lv.getItemIdAtPosition(i);
                Log.d("ID",""+itemId);
                //
                detailData = helper.getReadableDatabase()
                        .query("rubyLin", new String[]{"_id", "dateYear", "dateMonth", "dateDay", "itemName", "itemMoney","itemCategory","smallCategory"}
                                , "_id = ?", new String[]{String.valueOf(itemId)}, null, null, null);

                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());//abd用來警示視窗

                int deYear = 0,deMonth = 0 , deDay = 0 , deMoney = 0,deitemCategory =0 ,deSmallCategory=0;
                String deName = "",nameCategory ="";

                if(detailData.getCount()>0) {
                    while (detailData.moveToNext()) {
                        deYear = detailData.getInt(1);
                        deMonth = detailData.getInt(2);
                        deDay = detailData.getInt(3);
                        deName = detailData.getString(4);
                        deMoney = detailData.getInt(5);
                        deitemCategory = detailData.getInt(6);
                        deSmallCategory = detailData.getInt(7);
                    }
                        Cursor chartData  =  helper.getReadableDatabase()
                                .query("categoryTable", new String[]{"_id", "categoryName"},
                                        "itemCategory = ? AND smallCategory = ? ",
                                        new String[]{String.valueOf(deitemCategory), String.valueOf(deSmallCategory)}, null, null, null);
                    while (chartData.moveToNext()) {
                        int id = chartData.getInt(0);
                        nameCategory = chartData.getString(1);
                    }
                    adb.setTitle("明細");
                    adb.setMessage("時間: " + deYear + "年" + deMonth + "月" + deDay + "日\n" +
                            "來源: "+myDBloading.getincomeName()+"\n"+
                            "項目: "+nameCategory+ "\n" +
                            "內容: " + deName + "\n" +
                            "金額: " + deMoney);
                    adb.setNegativeButton("刪除", new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            long id = helper.getWritableDatabase().delete("rubyLin","_ID="+itemId,null);
                            Log.d("del",""+id);
                            refreshListView();
                            btnTextInit(myDBloading.getIncomeFlag());
                        }
                    });
                    adb.setPositiveButton("確定",null);
                    adb.show();
                }

            }
        });
    }
    //(1)輸入防呆
    public boolean inCheck(String item){
        int lenItem = item.length();
        boolean resultBoolean[] = new boolean[lenItem];
        boolean inCheck = false;
        if  (item.charAt(lenItem-1)=='元' || item.charAt(lenItem-1)=='塊'){
            resultBoolean[lenItem-1] = true;
            inCheck = true;
        }
        if (getItemName(item)=="") inCheck=false;
        if (getItemMoney(item)=="")inCheck=false;
        return inCheck;
    }
    //(1)分割字串
    public String getItemName(String item){
        String resultString = "" , resultInt = "";
        int lenItem = item.length();
        boolean resultBoolean[] = new boolean[lenItem];
        boolean inCheck = false;
        //判斷是否為有效輸入
        if  (item.charAt(lenItem-1)=='元' || item.charAt(lenItem-1)=='塊'){
            resultBoolean[lenItem-1] = true;
        }
        //true = 金額
        for (int i = lenItem - 2; i >= 0; i--) {
            if ((item.charAt(i) == '0' || item.charAt(i) == '1' || item.charAt(i) == '2' ||
                    item.charAt(i) == '3' || item.charAt(i) == '4' || item.charAt(i) == '5' ||
                    item.charAt(i) == '6' || item.charAt(i) == '7' || item.charAt(i) == '8' ||
                    item.charAt(i) == '9' || resultBoolean[i] == true) && resultBoolean[i + 1] == true) {
                resultBoolean[i] = true;
            }
        }
        for (int i = 0; i < lenItem; i++) {
            if (resultBoolean[i] == false)
                resultString += item.charAt(i);
        }

        return resultString;
    }

    //(1)分割數字&元
    public String getItemMoney(String item){
        String resultString = "" , resultInt = "";
        int lenItem = item.length();
        boolean resultBoolean[] = new boolean[lenItem];
        //判斷是否為有效輸入
        if  (item.charAt(lenItem-1)=='元' || item.charAt(lenItem-1)=='塊'){
            resultBoolean[lenItem-1] = true; //
        }
        //true = 金額
        for(int i=lenItem-2 ; i>=0 ; i--){
            if ((item.charAt(i) == '0' || item.charAt(i) == '1' || item.charAt(i) == '2' ||
                    item.charAt(i) == '3' || item.charAt(i) == '4' || item.charAt(i) == '5' ||
                    item.charAt(i) == '6' || item.charAt(i) == '7' || item.charAt(i) == '8' ||
                    item.charAt(i) == '9') && resultBoolean[i+1]==true){
                resultBoolean[i] = true ;
            }
        }
        for(int i=0 ; i<lenItem ; i++){
            if(resultBoolean[i]==true && item.charAt(i) != '元' && item.charAt(i) != '塊' )
                resultInt += item.charAt(i);
        }
        return resultInt;
    }

    //(3)刷新ListView
    public void refreshListView(){
        sqlData = helper.getReadableDatabase().query("rubyLin",          //table name
                new String[]{"_id","itemType","itemName","itemMoney"}, //column name
                "dateYear = ? AND dateMonth = ? AND dateDay = ? AND incomeID = ?",       //condition
                new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(day),String.valueOf(myDBloading.getIncomeFlag())},//condition value
                null, null, null);
        adapter_sql = new SimpleCursorAdapter(getActivity(),R.layout.adapter_type,sqlData,
                new String[]{"itemType","itemName","itemMoney"},new int[]{R.id.iv_Type,R.id.tv_item,R.id.tv_money},0);
        lv.setAdapter(adapter_sql);
    }

    //(4)載入選項跟ArrayList項目內容
    private void initData(ArrayList<String> result) {
        //語音輸入選項
        lvData = new ArrayList<>();
        int dataLen = result.size();
        for(int i=0;i<dataLen; i++){
            lvData.add(result.get(i));
        }

        //-----------------大類別-------------------------------
        lvCategory = new ArrayList<>();
        lvCategory.add(0,"食");
        lvCategory.add(1,"衣");
        lvCategory.add(2,"住");
        lvCategory.add(3,"行");
        lvCategory.add(4,"育");
        lvCategory.add(5,"樂");
        lvCategory.add(6,"人");
        lvCategory.add(7,"財");
        lvCategory.add(8,"健");
        lvCategory.add(9,"雜");
        lvCategory.add(10,"3C");
        lvCategory.add(11,"收入");
        //-----------------大類別-------------------------------

        //------------------小類別-------------------------------
        //------------------食-------------------------------
        lvFood = new ArrayList<>();
        int len=myDBloading.getscNameLength(0);
        for(int n=0; n<len;n++) {
            lvFood.add(n,myDBloading.getscNameBasic(0,n));
        }

        //------------------衣-------------------------------
        len=myDBloading.getscNameLength(1);
        lvClothes = new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvClothes.add(n,myDBloading.getscNameBasic(1,n));
        }


        //------------------住-------------------------------
        len=myDBloading.getscNameLength(2);
        lvLife = new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvLife.add(n,myDBloading.getscNameBasic(2,n));
        }


        //------------------行-------------------------------
        len=myDBloading.getscNameLength(3);
        lvTraffic = new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvTraffic.add(n,myDBloading.getscNameBasic(3,n));
        }



        //------------------育-------------------------------
        len=myDBloading.getscNameLength(4);
        lvEducation =new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvEducation.add(n,myDBloading.getscNameBasic(4,n));
        }


        //------------------樂-------------------------------
        len=myDBloading.getscNameLength(5);
        lvFun = new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvFun.add(n,myDBloading.getscNameBasic(5,n));
        }
        //----------------------人--------------------------------
        len=myDBloading.getscNameLength(6);
        lvRelationship= new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvRelationship.add(n,myDBloading.getscNameBasic(6,n));
        }
        //----------------------財--------------------------------
        len=myDBloading.getscNameLength(7);
        lvMoney= new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvMoney.add(n,myDBloading.getscNameBasic(7,n));
        }
        //----------------------健--------------------------------------
        len=myDBloading.getscNameLength(8);
        lvHealth= new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvHealth.add(n,myDBloading.getscNameBasic(8,n));
        }
        //----------------------雜--------------------------------------
        len=myDBloading.getscNameLength(9);
        lvThing= new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvThing.add(n,myDBloading.getscNameBasic(9,n));
        }
        //----------------------3c--------------------------------------
        len=myDBloading.getscNameLength(10);
        lv3C= new ArrayList<>();
        for(int n=0; n<len;n++) {
            lv3C.add(n,myDBloading.getscNameBasic(10,n));
        }
        //--------------------------收入---------------------------------
        len=myDBloading.getscNameLength(11);
        lvincome= new ArrayList<>();
        for(int n=0; n<len;n++) {
            lvincome.add(n,myDBloading.getscNameBasic(11,n));
        }
    }
    //(4)非全語音輸入的類別選擇AlertDialog
    public void lvDataSelect() {
        new AlertDialog.Builder(AddedFragment.this.getActivity())
                .setItems(lvData.toArray(new String[lvData.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        name = lvData.get(which);
                        //
                        if (inCheck(name) == true){
                            new AlertDialog.Builder(AddedFragment.this.getActivity())
                                    .setItems(lvCategory.toArray(new String[lvCategory.size()]), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String name1=lvCategory.get(which);
                                            bigCategory=which;  //大類別 0食 衣 住 行 育 樂 人 財 健 雜 3C 收入
                                            selectsmallv(bigCategory);

                                            new AlertDialog.Builder(AddedFragment.this.getActivity())
                                                    .setItems(lvsmallCategory.toArray(new String[lvsmallCategory.size()]), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            smallCategory =which;
                                                            Toast.makeText(getActivity(), "bigCategory"+bigCategory, Toast.LENGTH_SHORT).show();
                                                            if (getRaBoolean()) {//選擇開啟價格合理性
                                                                rationalAmount(bigCategory, smallCategory, getItemName(name), getItemMoney(name));
                                                            } else {
                                                                addItem(bigCategory, smallCategory, name);
                                                            }

                                                        }
                                                    })
                                                    .show();
                                            //
                                            Toast.makeText(AddedFragment.this.getActivity(), ""+bigCategory+name1, Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .show();
                        }else{
                            Toast.makeText(AddedFragment.this.getActivity(), "請重新輸入", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .show();
    }

    //非全語音的小項目內容
    public  void selectsmallv(int bigCategory) {
                                        switch (bigCategory) {
                                            case 0://食
                                                lvsmallCategory = lvFood;
                                                break;
                                            case 1://衣
                                                lvsmallCategory = lvClothes;
                                                break;
                                            case 2://住
                                                lvsmallCategory = lvLife;
                                                break;
                                            case 3://行
                                                lvsmallCategory = lvTraffic;
                                                break;
                                            case 4://育
                                                lvsmallCategory = lvEducation;
                                                break;
                                            case 5://樂
                                                lvsmallCategory = lvFun;
                                                break;
                                            case 6://人
                                                lvsmallCategory = lvRelationship;
                                                break;
                                            case 7://財
                                                lvsmallCategory = lvMoney;
                                                break;
                                            case 8://健
                                                lvsmallCategory = lvHealth;
                                                break;
                                            case 9://雜
                                                lvsmallCategory = lvThing;
                                                break;
                                            case 10://3c
                                                lvsmallCategory = lv3C;
                                                break;
                                            case 11://收入
                                                lvsmallCategory=lvincome;
                                                break;

                                        }
                                    } //決定該顯示哪一個大類別 lvsmallCategory
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    String scn = "";
    int ten = 0,tnn = 0, sn = 0;
    //(4)全語音選擇  內有價格合理選擇
    public void lvSpeechSelect(){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        speechData = helper.getReadableDatabase().query("categoryTable",
                new String[]{"_id","itemCategory","smallCategory","categoryName"}, null, null, null, null, null);
        sn = 0;
        final int [] itc = new int [speechData.getCount()];
        final int [] smc = new int [speechData.getCount()];
        final String [] sc = new String[speechData.getCount()];
        //
        if (speechData.getCount()  > 0) {
            while (speechData.moveToNext()) {
                itc[sn] = speechData.getInt(1);  //大分類代號
                smc[sn] = speechData.getInt(2); //小分類代號
                sc[sn] = speechData.getString(3);//小分類字串
                sn++;
            }
        }
        new AlertDialog.Builder(AddedFragment.this.getActivity())
                .setItems(lvData.toArray(new String[lvData.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                name = lvData.get(i);
                if (inCheck(name) == true){
                    scn = getItemName(name);
                    for(int a = 0;a < sc.length;a++){
                        ten = 0;
                        if(scn.length()<sc[a].length())tnn = 0;
                        else tnn = sc[a].length();
                        for(int b=0;b<tnn;b++){
                            char t1 =scn.charAt(b), t2 = sc[a].charAt(b);
                            if(t1 == t2)ten++;
                            if(ten == sc[a].length()) {
//                                if(itc[a]==10){
//                                    //收入
//
//                                }
                                if(getRaBoolean()) {//選擇開啟價格合理性
                                    rationalAmount(itc[a], smc[a], getItemName(spiltName(sc[a], name)), getItemMoney(name));
                                }else{
                                    addItem(itc[a],smc[a],spiltName(sc[a],name));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }).show();helper.close();
    }
    //取得價格合理性布林值
    public boolean getRaBoolean(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        raBooleanSwitch = Boolean.valueOf(sharedPreferences.getString("switchBooleanRa", ""));
        Log.d("getboolean",raBooleanSwitch+"   /added");
        return raBooleanSwitch;
    }

    //分割小類別
    public String spiltName(String sc,String name){
        int scl = sc.length() , nal = name.length() , noml = getItemName(name).length();
        String scn = "";
        String nameb [] = new String [nal];
        Log.d("sc:",sc+""+scl+"/"+nal);
        for(int i=0; i<nal;i++){
            nameb[i] = String.valueOf(name.charAt(i));
            Log.d("nameb:",nameb[i]);
        }
        if(scl==noml){
            return name;
        }else {
            for (int i = scl; i < nal; i++) {
                scn += nameb[i];
                Log.d("scn:", scn);
            }
            return scn;
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    //(5)Calendar日期選擇---------------------------------------------------------------------
    public void CalendarPicker(){
        calendar = Calendar.getInstance();
        nowYear = calendar.get(Calendar.YEAR);
        nowMonth = calendar.get(Calendar.MONTH)+1;
        nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        calendartextView.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
        CalendarEventSet();//設定當月有記帳的日期做標點 ruby table
        CalendarSpecialEventSet();//設定當月有特殊事件的日期做標點 calendarEvent table
        refreshListView();
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                String d[]=dateClicked.toString().split(" "); // 0:星期幾 1: 月份  2: 日期.....
                day= Integer.valueOf(d[2]);
                //刷新ListView
                refreshListView();
                CalendarEventSet();
                btn_eventUI();//btn_event.setText
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {//滑動式上下月 可是因為在TAB MOD 下不好滑動
                // Changes toolbar title on monthChange
                calendartextView.setText(dateFormatForMonth.format(firstDayOfNewMonth));
                Log.d("XXXX",firstDayOfNewMonth.toString());
                String d[]=firstDayOfNewMonth.toString().split(" ");// 0:星期幾 1: 月份  2: 日期.....
                int m = 1;
                m = monthChangeIntMap.get(""+d[1]);
                month=m;
                day= Integer.valueOf(d[2]);
                year=Integer.valueOf(d[5]);
                CalendarEventSet();//設定當月有記帳的日期做標點 ruby table
                CalendarSpecialEventSet();//設定當月有特殊事件的日期做標點 calendarEvent table
                btn_eventUI();//btn_event.setText
                refreshListView();
            }

        });
    }



    //(5)按鈕式上下月
    private void calendarviewBTNListener() {
        btn_last_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH,-1);
                compactCalendarView.setCurrentDate(calendar.getTime());
                calendartextView.setText(dateFormatForMonth.format(calendar.getTime()));
                Log.d("3A417067","");

                String d[]=calendar.getTime().toString().split(" ");// 0:星期幾 1: 月份  2: 日期.....
                int m = 1;
                m = monthChangeIntMap.get(""+d[1]);
                month=m;
                day= Integer.valueOf(d[2]);
                year=Integer.valueOf(d[5]);
                CalendarEventSet();//設定當月有記帳的日期做標點 ruby table
                CalendarSpecialEventSet();//設定當月有特殊事件的日期做標點 calendarEvent table
                btn_eventUI();//btn_event.setText
                refreshListView();
            }
        });
        btn_next_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH,+1);
                compactCalendarView.setCurrentDate(calendar.getTime());
                calendartextView.setText(dateFormatForMonth.format(calendar.getTime()));
                String d[]=calendar.getTime().toString().split(" ");// 0:星期幾 1: 月份  2: 日期.....
                int m = 1;
                m = monthChangeIntMap.get(""+d[1]);
                month=m;
                day= Integer.valueOf(d[2]);
                year=Integer.valueOf(d[5]);
                CalendarEventSet();//設定當月有記帳的日期做標點 ruby table
                CalendarSpecialEventSet();//設定當月有特殊事件的日期做標點 calendarEvent table
                btn_eventUI();//btn_event.setText
                refreshListView();
            }
        });
    }
    //
    //(5) Calendar 有預計消費活動的做標記
    public void CalendarSpecialEventSet(){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        Cursor checkTableExists = helper.getReadableDatabase().rawQuery("SELECT count(name) FROM sqlite_master WHERE type='table' and name='calendarEvent'", null);//檢查總表單內有沒有創建calendarEvent
        checkTableExists.moveToFirst();
        if(checkTableExists.getInt(0) != 0) {//有的話就不為0
            Cursor eventData = helper.getReadableDatabase().query("calendarEvent",
                    new String[]{"_id", "dateDay"},
                    "dateYear =? AND dateMonth = ? ",
                    new String[]{String.valueOf(year), String.valueOf(month)},
                    null, null, null);

            eventData.moveToFirst();
            while (!eventData.isAfterLast()) {
                CalendarSpecialDate(Integer.valueOf(eventData.getString(1)));
                eventData.moveToNext();
            }
            eventData.close();
        }
        checkTableExists.close();
        helper.close();
    }
    //標點的副程式
    private void CalendarSpecialDate(Integer d) {//d = 要標點的日期
        Calendar c1 =Calendar.getInstance();
        c1.set(Calendar.YEAR, year);
        //Month to 月份縮寫
        switch (month){
            case 1:
                c1.set(Calendar.MONTH, Calendar.JANUARY); break;
            case 2:
                c1.set(Calendar.MONTH, Calendar.FEBRUARY); break;
            case 3:
                c1.set(Calendar.MONTH, Calendar.MARCH); break;
            case 4:
                c1.set(Calendar.MONTH, Calendar.APRIL); break;
            case 5:
                c1.set(Calendar.MONTH, Calendar.MAY); break;
            case 6:
                c1.set(Calendar.MONTH, Calendar.JUNE); break;
            case 7:
                c1.set(Calendar.MONTH, Calendar.JULY); break;
            case 8:
                c1.set(Calendar.MONTH, Calendar.AUGUST); break;
            case 9:
                c1.set(Calendar.MONTH, Calendar.SEPTEMBER); break;
            case 10:
                c1.set(Calendar.MONTH, Calendar.OCTOBER); break;
            case 11:
                c1.set(Calendar.MONTH, Calendar.NOVEMBER); break;
            case 12:
                c1.set(Calendar.MONTH, Calendar.DECEMBER); break;
        }
        //
        c1.set(Calendar.DATE, d);
        c1.set(Calendar.HOUR_OF_DAY, 0);//設定小時
        c1.set(Calendar.MINUTE, 0);//分
        c1.set(Calendar.SECOND, 0);//秒
        c1.set(Calendar.MILLISECOND, 0);//毫秒
        //
        compactCalendarView.addEvent(new CalendarDayEvent(c1.getTimeInMillis(), Color.rgb(244, 105, 116)),true);//getTimeInMillis：1522195200000 把日期換成時間
        compactCalendarView.invalidate();
    }

    //(5)Calendar 有記帳資料的做標記
    public void CalendarEventSet(){
        int CESday=1;
        sqlData = helper.getReadableDatabase().query("rubyLin",
                new String[]{"_id","dateDay"},
                "dateYear = ? AND dateMonth = ?",
                new String[]{String.valueOf(year), String.valueOf(month)},
                null,null,null);
        sqlData.moveToFirst();
        while (!sqlData.isAfterLast()) {

            CESday = Integer.valueOf(sqlData.getString(1));
            CalendarDate(CESday);
            sqlData.moveToNext();
        }
        sqlData.close();
        helper.close();
    }
    //(5)標點的副程式
    public void CalendarDate(int d){//d = 要標點的日期
        Calendar c1 =Calendar.getInstance();
        c1.set(Calendar.YEAR, year);
        //Month to 月份縮寫
        switch (month){
            case 1:
                c1.set(Calendar.MONTH, Calendar.JANUARY); break;
            case 2:
                c1.set(Calendar.MONTH, Calendar.FEBRUARY); break;
            case 3:
                c1.set(Calendar.MONTH, Calendar.MARCH); break;
            case 4:
                c1.set(Calendar.MONTH, Calendar.APRIL); break;
            case 5:
                c1.set(Calendar.MONTH, Calendar.MAY); break;
            case 6:
                c1.set(Calendar.MONTH, Calendar.JUNE); break;
            case 7:
                c1.set(Calendar.MONTH, Calendar.JULY); break;
            case 8:
                c1.set(Calendar.MONTH, Calendar.AUGUST); break;
            case 9:
                c1.set(Calendar.MONTH, Calendar.SEPTEMBER); break;
            case 10:
                c1.set(Calendar.MONTH, Calendar.OCTOBER); break;
            case 11:
                c1.set(Calendar.MONTH, Calendar.NOVEMBER); break;
            case 12:
                c1.set(Calendar.MONTH, Calendar.DECEMBER); break;
        }
        //
        c1.set(Calendar.DATE, d);
        c1.set(Calendar.HOUR_OF_DAY, 0);//設定小時
        c1.set(Calendar.MINUTE, 0);//分
        c1.set(Calendar.SECOND, 0);//秒
        c1.set(Calendar.MILLISECOND, 0);//毫秒

        compactCalendarView.addEvent(new CalendarDayEvent(c1.getTimeInMillis(), Color.rgb(160, 252, 90)), false);//getTimeInMillis：1522195200000 把日期換成時間
        compactCalendarView.invalidate();
    }
    //------------------------------------------------------------------------------------------------------------------------------------
    //(6)合理性判斷
    public void rationalAmount(final int type, final int type2, final String item, final String amount){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        sqlData = helper.getReadableDatabase().query("rubyLin",          //table name
                new String[]{"_id","itemType","itemName","itemMoney"}, //column name
                "itemCategory=? AND smallCategory=? AND itemName=?",       //condition
                new String[]{String.valueOf(type),String.valueOf(type2),item},//condition value
                null, null, null);
        //
        int am[] = new int [sqlData.getCount()];
        if(sqlData.getCount()>0){
            for(int i =0;i<sqlData.getCount();i++){
                sqlData.moveToNext();
                am[i] = Integer.parseInt(sqlData.getString(3));
                Log.d("am",i+":"+am[i]);
            }
            //小 - 大 排序
            for(int i=0;i<sqlData.getCount();i++){
                for(int j=i;j<sqlData.getCount();j++){
                    if(am[j]<am[i]){
                        int k = am[i];
                        am[i] = am[j];
                        am[j] = k;
                    }
                }
            }

            int rationalMin = (int) (am[0] - (am[0]*0.1)), rationalMax =(int) (am[sqlData.getCount()-1]+(am[sqlData.getCount()-1]*0.1));
            if (Integer.parseInt(amount) > rationalMax || Integer.parseInt(amount)<rationalMin){
                //跳出警示框
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle("確定金額是否正確");
                alertDialog.setMessage(
                        "您平常輸入\"" + item +"\"的金額為 : $" + rationalMin + " ~ $" + rationalMax +
                        "\n您目前輸入\"" + item + "\"的金額為 : $" + amount +
                        "\n\n您確定輸入金額是否正確?");
                alertDialog.setNegativeButton("取消", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.setPositiveButton("確定", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myDBloading.saverubyLinToSQLite(year,month,day,myDBloading.getIncomeFlag(),type,type2,item,amount);
                        //刷新ListView
                        refreshListView();
                        btnTextInit(myDBloading.getIncomeFlag());
                    }
                });
                alertDialog.show();
            }else{
                myDBloading.saverubyLinToSQLite(year,month,day,myDBloading.getIncomeFlag(),type,type2,item,amount);
                //刷新ListView
                refreshListView();
                btnTextInit(myDBloading.getIncomeFlag());
            }

        }else{
            myDBloading.saverubyLinToSQLite(year,month,day,myDBloading.getIncomeFlag(),type,type2,item,amount);
            //刷新ListView
            refreshListView();
            btnTextInit(myDBloading.getIncomeFlag());
        }
        helper.close();
        //
    }
    //(7)myEmail
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }
    //(8)回傳總金額
    public int getrubyLinTotal(int vIncomeFlag){
        account = getMyEmail() + ".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        Cursor addData = helper.getReadableDatabase().query("rubyLin",
                new String[]{"_id","itemCategory","itemMoney"},
                "incomeID = ?",new String[]{String.valueOf(vIncomeFlag)},
                null,null,null);
        if(addData.getCount()==0){
            return 0;
        }
        addData.moveToFirst();
        int incomeTotalMoney=0;
        int expenditureTotalMoney=0;
        while (!addData.isAfterLast()){
            if(addData.getInt(1)==11){//收入類別
                incomeTotalMoney=incomeTotalMoney+Integer.parseInt(addData.getString(2));
            }else {//支出類別
                expenditureTotalMoney=expenditureTotalMoney+Integer.parseInt(addData.getString(2));
            }
            addData.moveToNext();
        }
        return (incomeTotalMoney-expenditureTotalMoney);
    }
}
