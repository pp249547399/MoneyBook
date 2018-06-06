package tw.edu.ncut.login.group;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.database.MyDBloading;
import tw.edu.ncut.login.myapplication.R;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {
    //----------------------------bundle資料------------------------------------
    private Bundle bundle;
    private String AccName, AccMember;
    //------------------------------------------------------------------------------
    private RecyclerView rev_group;
    private Button btn_group_add, btn_buyList, btn_goDutch, btn_status, btn_buying;
    private TextView tv_accName, tv_status;
    ArrayList<String> gDataSet = new ArrayList<>(); //群體帳簿內容
    ArrayList<String> bDataSet = new ArrayList<>(); //購物清單
    ArrayList<String> yDataSet = new ArrayList<>(); //團購專區
    String dDataSet = new String();//我要均攤
    String dutchPrice = "";
    //----------------------偏好儲存---------------------------------------
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String myEmail, account;
    //------------------------網路傳輸--------------------------------------
    public static final int CONNECTION_TIMEOUT = 50000;
    public static final int READ_TIMEOUT = 50000;
    private Handler handler;
    private HandlerThread handlerThread;
    private String answer, inviter, email, php;
    boolean isShowing = false;
    boolean isConnect = true;
    //---------------------------SQLite--------------------------------------------
    private MyDBHelper helper;
    //---------------------------Go Dutch-----------------------------------------
    private ArrayList<Integer> arrMem = new ArrayList<>();
    private ArrayList<Integer> arrMon = new ArrayList<>();
    private int SPEECH_LIST = 1;
    private int SPEECH_ADD = 2;
    //----------------------------狀態 ----------------------------------------------
    private int STATUS_TYPE = 0;
    private static final int STATUS_RED = 1;//未銷帳
    private static final int STATUS_GREEN = 2;//已銷帳
    //----------------------------團購-----------------------------------------------
    private int BUYING_TYPE = 0;
    private static final int BUYING_RED = 1;
    private static final int BUYING_GREEN = 2;
    private String ID ;//開團人 OR 跟團人

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        rev_group = (RecyclerView) findViewById(R.id.rev_group);
        tv_accName = (TextView) findViewById(R.id.tv_accName);
        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_group_add = (Button) findViewById(R.id.btn_group_add);
        btn_buyList = (Button) findViewById(R.id.btn_buyList);
        btn_goDutch = (Button) findViewById(R.id.btn_goDutch);
        btn_status = (Button) findViewById(R.id.btn_status);
        btn_buying = (Button)findViewById(R.id.btn_buying);

        btn_status.setOnClickListener(this);
        btn_buying.setOnClickListener(this);
        btn_buyList.setOnClickListener(this);
        btn_goDutch.setOnClickListener(this);
        btn_group_add.setOnClickListener(this);


        getBundle();//取得AccName、AccMember
        getSQLite();//取得SQLite資料
        setAccName();
        BUYING_TYPE = BUYING_GREEN;//設定團購專區目前不為當前功能
        //
        handlerThread = new HandlerThread("conn");
        handlerThread.start();
        //receviceMessage.php==>收資料
        php = "http://140.128.88.166:8008/receviceMessage.php";//收資料
        handler = new Handler(handlerThread.getLooper());
        new Thread(serverListener).start();
        //
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //新增
            case R.id.btn_group_add:
                if (BUYING_TYPE == BUYING_GREEN) {
                    SpeechInput(SPEECH_ADD);
                }else if (BUYING_TYPE == BUYING_RED ){
                    String master = getMyEmail();
                    //購物專區
                    if (yDataSet.toString().equals("[]") ) {
                        ID = "MASTER";//開團人
                    }else if (master.equals(getMyEmail())){
                        ID = "ROOT";//開團人、但不能新增只能截止
                    } else{
                        ID = "MEMBER";//跟團人
                    }
                    AlertDialog.Builder buying = new AlertDialog.Builder(this);
                    final String cDate = getDate();
                    if (ID.equals("MASTER")){
                        buying.setTitle("團購項目");
                        final EditText editText = new EditText(this);
                        buying.setView(editText);
                        buying.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!editText.getText().toString().equals("")) {
                                    yDataSet.add("SentMessage" + "," + editText.getText().toString() + "," + getMyEmail() + "," + cDate);//SentMessage
                                    groupAdapter(yDataSet);
                                    //thread
                                }
                            }
                        });
                    }else if(ID.equals("ROOT")){
                        int count = yDataSet.size()-1;
                        buying.setTitle("團購人數");
                        buying.setMessage("一共要買"+count+"個");
                        buying.setPositiveButton("確定",null);
                        buying.setNegativeButton("結束團購", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                yDataSet.clear();
                            }
                        });
                    }else if (ID.equals("MEMBER")){
                        buying.setTitle("我要加一");
                        buying.setMessage("是否要團購**");
                        buying.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                yDataSet.add("ReceviceMessage" + "," + "+1" + "," + getMyEmail() + "," + cDate);//SentMessage
                                groupAdapter(yDataSet);
                                //thread
                            }
                        });
                    }
                    buying.show();
                }
                break;
            //我要均攤
            case R.id.btn_goDutch:
                if (BUYING_TYPE == BUYING_GREEN) {
                    if (STATUS_TYPE == STATUS_RED) {
                        Toast.makeText(GroupActivity.this, "上筆均攤還沒完成...", Toast.LENGTH_SHORT).show();
                    } else {
                        alertMultiCoice();
                    }
                }
                break;
            //購物清單
            case R.id.btn_buyList:
                if (BUYING_TYPE == BUYING_GREEN) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(GroupActivity.this);
                    adb.setTitle("購物清單");
                    String msg = "";
                    TextView textView = new TextView(GroupActivity.this);
                    for (int i = 0; i < bDataSet.size(); i++) {
                        msg += "　 " + (i + 1) + "." + bDataSet.get(i) + "\n";
                        textView.setText(msg);
                        textView.setTextSize(16);
                        textView.setTextColor(Color.BLACK);
                        adb.setView(textView);
                    }
                    //adb.setMessage(msg);
                    adb.setNegativeButton("取消", null);
                    adb.setPositiveButton("新增", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SpeechInput(SPEECH_LIST);
                        }
                    });
                    adb.setNeutralButton("刪除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bDataSet.clear();
                            new Thread(new sendMessage("clear")).start();
                            SQLiteDatabase db = helper.getWritableDatabase();
                            db.delete("groupSendRecevedData", "accName = ?", new String[]{"toBuyList"});
                        }
                    });
                    adb.setCancelable(false);
                    adb.show();
                }
                break;
            //狀態
            case R.id.btn_status:
                if (dDataSet != null) {
                    AlertDialog.Builder adb_status = new AlertDialog.Builder(GroupActivity.this);
                    adb_status.setTitle("均攤後 應收/應付");
                    adb_status.setMessage(dDataSet);
                    adb_status.setPositiveButton("確定", null);
                    adb_status.setNegativeButton("已結清", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //帳簿+++++  SQLite資料還會再>>要改
                            String db = getMyEmail()+".db";
                            MyDBloading myDBloading = new MyDBloading(GroupActivity.this, db);
                            String price = dutchPrice.replace("元", "");
                            //增加到個人帳簿內
                            myDBloading.saverubyLinToSQLite(currentDate()[0], currentDate()[1], currentDate()[2], myDBloading.getIncomeFlag(), 6, 5, "均攤", price );
                            STATUS_TYPE = STATUS_GREEN;
                            //新增狀態
                            update();
                        }
                    });
                    adb_status.show();
                }
                break;
            case R.id.btn_buying:
                AlertDialog.Builder buy_adb = new AlertDialog.Builder(this);
                buy_adb.setTitle("團購專區");
                buy_adb.setNegativeButton("取消",null);
                if (BUYING_TYPE == BUYING_RED){
                    BUYING_TYPE = BUYING_GREEN;
                    btn_buying.setBackground(getDrawable(R.drawable.group_background_fun));
                    btn_goDutch.setBackground(getDrawable(R.drawable.group_background_fun));
                    btn_buyList.setBackground(getDrawable(R.drawable.group_background_fun));
                    btn_group_add.setBackground(getDrawable(R.drawable.group_background_fun));
                    if (STATUS_TYPE == STATUS_RED){
                        btn_status.setBackgroundResource(R.drawable.group_background_fun2);
                        tv_status.setBackgroundResource(R.drawable.group_background_fun3);
                    }else {
                        btn_status.setBackgroundResource(R.drawable.group_background_fun);
                        tv_status.setBackgroundResource(R.drawable.group_background_sent2);
                    }
                    getSQLite();
                    groupAdapter(gDataSet);
                }else if(BUYING_TYPE == BUYING_GREEN){
                    btn_buying.setBackground(getDrawable(R.drawable.group_background_fun2));
                    btn_goDutch.setBackground(getDrawable(R.drawable.group_background_fun2));
                    btn_buyList.setBackground(getDrawable(R.drawable.group_background_fun2));
                    btn_group_add.setBackground(getDrawable(R.drawable.group_background_fun2));
                    btn_status.setBackground(getDrawable(R.drawable.group_background_fun2));
                    tv_status.setBackground(getDrawable(R.drawable.group_background_fun3));
                    Toast.makeText(this, "切換至團購專區", Toast.LENGTH_SHORT).show();
                    BUYING_TYPE = BUYING_RED;
                    //清空畫面
                    gDataSet.clear();
                    groupAdapter(gDataSet);
                }
                break;
        }
    }

//先連線==>recevicedMessage.php==>通知使用者上線==>伺服器看有沒有要給使用者的資料
//如果有==>okMessage .php==>傳送已收到訊息給伺服器
//記訊息==>sendMessage.php
//<==========================收資料&確認收到資料================================>

    class AnswerInvite implements Runnable {
        //確認收到訊息anserMessage.php
        String sender;
        String message;
        String number;
        String php = "http://140.128.88.166:8008/okMessage.php";

        //確認收到訊息==>okMessage
        //
        public AnswerInvite(String sender, String message, String number) {
            this.sender = sender;
            this.message = message;
            this.number = number;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            URL url;
            try {
                url = new URL(php);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                //傳資料給伺服器==>answer編號, emailMesage.php
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("accName", AccName)
                        .appendQueryParameter("sender", sender)
                        .appendQueryParameter("message", message)
                        .appendQueryParameter("number", number)
                        .appendQueryParameter("email", getMyEmail());

                String query = builder.build().getEncodedQuery();
                conn.getOutputStream().write(query.getBytes());
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
                conn.getInputStream().read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable serverListener = new Runnable() {
        URL url;

        @Override
        public void run() {
            //receviceMessage.php
            try {
                url = new URL(php);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            //無窮迴圈==while(true) == for(; ;)
            while (isConnect) {
                try {
                    while (isShowing) ;
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setReadTimeout(READ_TIMEOUT);
                    conn.setConnectTimeout(CONNECTION_TIMEOUT);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setChunkedStreamingMode(1024);
                    //通知伺服器，此帳號上線狀態
                    String query = "";
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("accName", AccName)
                            .appendQueryParameter("email", getMyEmail());//sender==>abc@gmail.com
                    query = builder.build().getEncodedQuery();
                    //把email寫給伺服器
                    conn.getOutputStream().write(query.getBytes());

                    setStatus();

                    int response_code = conn.getResponseCode();
                    if (response_code == HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        //收伺服器給的資料
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        String text;
                        int c;
                        byte[] buffer = new byte[256];
                        //一次讀取[256]長度
                        while ((c = is.read(buffer)) >= 0) {
                            bao.write(buffer, 0, c);
                        }
                        is.close();
                        text = new String(bao.toByteArray());
                        bao.close();
                        //
                        if (!text.equals(" \n")) {
                            isShowing = true;
                            Log.d("parseJson:", text);
                            parseJson(text);
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handlerThread != null) {
            handlerThread.quit();
        }
    }

    //解析Json
    private void parseJson(String result) {
        try {
            //sender, message, time
            //message==>itemName & itemMoney
            JSONArray array = new JSONArray(result);
            Log.d("TAGJSON", String.valueOf(array));
            String parseType = "";
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JSONArray array_type = null;
                if (obj.optJSONArray("recevicemessage")!= null){
                    array_type = obj.getJSONArray("recevicemessage");
                    parseType = "recevicemessage";
                }else if (obj.optJSONArray("shoplist") != null){
                    array_type = obj.getJSONArray("shoplist");
                    parseType = "shoplist";
                }else if (obj.optJSONArray("checkaverage")!=null){
                    array_type = obj.getJSONArray("checkaverage");
                    parseType = "checkaverage";
                }

                for (int j=0; j<array_type.length(); j++){
                    JSONObject obj_type = array_type.getJSONObject(j);
//                    //判斷哪個功能的值
//                    String type = obj_type.getString("type");
                    //群體帳簿
                    String sender = obj_type.optString("sender");
                    String message = obj_type.optString("message");
                    String number = obj_type.optString("number");
                    String date = obj_type.optString("date");
                    //我要均攤
                    String dutchMoney = obj_type.optString("price");
                    String dutchStutas = obj_type.optString("status");
                    //購物清單
                    String item = obj_type.optString("item");
                    if (parseType.equals("recevicemessage")) { //群體帳簿
                        new saveToSQLite(sender, getItemName(message), getItemMoney(message), date, "ReceviceMessage");
                        gDataSet.add("ReceviceMessage" + "," + message + "," + sender + "," + date);//ReceviceMessage
                        groupAdapter(gDataSet);
                        //通知伺服器已收到
                        new Thread(new AnswerInvite(sender, message, number)).start();//sender, message, number
                    } else if (parseType.equals("checkaverage")) {//我要均攤
                        new saveToSQLite(dutchMoney, dutchStutas);
                        dDataSet = getMyEmail() + "\n" + "支付狀態:" + dutchStutas + "\n" + dutchMoney;
                        STATUS_TYPE = STATUS_RED;
                    } else if (parseType.equals("shoplist")){
                        bDataSet.add(item);
                        new saveToSQLite(item);
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            isShowing = false;
        }
    }

    //寫資料給伺服器
    class sendMessage implements Runnable {
        //群體帳簿 > 記帳人(sender)、內容(message)
        //我要均攤 > 均攤成員(member)、均攤金額(pay)>每個人不一樣
        String accName, message, date; // 群體帳簿
        String php;
        String dutchMember, dutchMoney, dutchStatus;//均攤
        String toBuyList;//購物清單
        //群體帳簿
        public sendMessage(String accName, String message, String date) {
            this.accName = accName;
            this.message = message;
            this.date = date;
            this.php = "http://140.128.88.166:8008/sendMessage.php";
        }
        //我要均攤
        public sendMessage(String accName, String dutchMember, String dutchMoney, String dutchStatus) {
            this.accName = accName;
            this.dutchMember = dutchMember;
            this.dutchMoney = dutchMoney;
            this.dutchStatus = dutchStatus;
            this.php = "http://140.128.88.166:8008/average.php";
        }
        //購物清單
        public sendMessage(String toBuyList) {
            this.toBuyList = toBuyList;
            this.php = "http://140.128.88.166:8008/shoplist.php";
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            URL url;

            try {
                url = new URL(php);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                //傳資料給伺服器
                Uri.Builder builder = new Uri.Builder();
                if (php.equals("http://140.128.88.166:8008/sendMessage.php")) {
                    //傳資料給senderMessage.php > 群體帳簿資料
                    builder.appendQueryParameter("accName", accName);
                    builder.appendQueryParameter("sender", getMyEmail());
                    builder.appendQueryParameter("message", message);
                    builder.appendQueryParameter("date", date);
                } else if (php.equals("http://140.128.88.166:8008/average.php")) {
                    //我要均攤資料
                    builder.appendQueryParameter("master", getMyEmail());
                    builder.appendQueryParameter("member", dutchMember);//member(ok,)
                    builder.appendQueryParameter("price", dutchMoney);//monry(100,)
                    builder.appendQueryParameter("status", dutchStatus);//status已銷帳,
                    builder.appendQueryParameter("accName", accName);
                } else if (php.equals("http://140.128.88.166:8008/shoplist.php")) {
                    //購物清單資料
                    builder.appendQueryParameter("item", toBuyList);
                    builder.appendQueryParameter("master", getMyEmail());
                    builder.appendQueryParameter("accName", AccName);
                }

                String query = builder.build().getEncodedQuery();
                conn.getOutputStream().write(query.getBytes());
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
                conn.getInputStream().read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//<==========================其他程式================================>

    //語音輸入
    public void SpeechInput(int SPEECH_TYPE) {
        Intent input = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        input.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        input.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN.toString());
        input.putExtra(RecognizerIntent.EXTRA_PROMPT, "");
        //
        try {
            startActivityForResult(input, SPEECH_TYPE);//requestCode
        } catch (ActivityNotFoundException in) {
            Toast.makeText(this, "try again", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (requestCode == SPEECH_ADD) {
                lvDataSelect(initData(result), SPEECH_ADD);//語音選項
            } else if (requestCode == SPEECH_LIST) {
                lvDataSelect(initData(result), SPEECH_LIST);
            }
        }
    }

    //樣式設定
    private void groupAdapter(final ArrayList<String> dataSet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GroupMessageAdapter groupMessageAdapter = new GroupMessageAdapter(dataSet);
                LinearLayoutManager layoutManager = new LinearLayoutManager(GroupActivity.this); //設定此 layoutManager 為 linearlayout (類似ListView)
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //設定此 layoutManager 為垂直堆疊
                //rev_group.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //設定分割線
                layoutManager.setStackFromEnd(true);
                rev_group.setLayoutManager(layoutManager); //設定 LayoutManager
                rev_group.setAdapter(groupMessageAdapter); //設定 Adapter
            }
        });

    }

    //bundle==>groupAccName, groupAccMember
    private void getBundle() {
        bundle = getIntent().getExtras();
        AccName = bundle.getString("groupAccName");
        AccMember = "";
        String bundleInviter = bundle.getString("groupInviter");
        String bundleMember = bundle.getString("groupAccMember");//沒邀請人
//        Toast.makeText(this, "bundleInviter:"+bundleInviter+"/bundleMember:"+bundleMember, Toast.LENGTH_SHORT).show();
        String[] member = bundleMember.split(",");
        for (int i = 0; i < member.length; i++) {
            if (member[i].equals(getMyEmail())) {//(被邀請人)自己在成員裡
                AccMember += bundleInviter + ",";//把邀請人放進去
            } else {
                AccMember += member[i] + ",";
            }
        }
    }

    //myEmail==>自己Email
    private String getMyEmail() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }

    //語音選擇
    public ArrayList initData(ArrayList<String> result) {
        ArrayList<String> lvData = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            lvData.add(result.get(i));
        }
        lvData.add("手動輸入");
        return lvData;
    }

    //語音選項==>AlertDialog
    public void lvDataSelect(final ArrayList<String> lvData, final int SPEECH_TYPE) {
        new AlertDialog.Builder(this)
                .setItems(lvData.toArray(new String[lvData.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String item = lvData.get(i);
                        if (SPEECH_TYPE == SPEECH_ADD) {
                            speechDataAdd(i, item, lvData);
                        } else if (SPEECH_TYPE == SPEECH_LIST) {
                            speechDataList(i, item, lvData);
                            //bDataSet.add(item);
                        }
                    }
                })
                .show();
    }

    //------------------------------------語音選項 & 手動選項-----------------------------------------------//
    private void speechDataAdd(int i, String item, ArrayList<String> lvData) {
        final String cDate = getDate();
        if (inCheck(item) == true) {
            new Thread(new sendMessage(AccName, item, cDate)).start();
            gDataSet.add("SentMessage" + "," + item + "," + getMyEmail() + "," + cDate);//SentMessage
            groupAdapter(gDataSet);
            new saveToSQLite(getMyEmail(), getItemName(item), getItemMoney(item), cDate, "SentMessage");
        } else if (lvData.get(i).equals("手動輸入")) {
            final EditText editText = new EditText(GroupActivity.this);
            AlertDialog.Builder adb = new AlertDialog.Builder(GroupActivity.this);
            adb.setTitle("輸入格式: 項目+空格+金額+元\n" + "範例: 早餐  10元");
            adb.setView(editText);
            adb.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String item = editText.getText().toString();
                    if (item != "" && inCheck(item) == true) {
                        new Thread(new sendMessage(AccName, item, cDate)).start();
                        gDataSet.add("SentMessage" + "," + item + "," + getMyEmail() + "," + cDate);//SentMessage
                        groupAdapter(gDataSet);
                        new saveToSQLite(getMyEmail(), getItemName(item), getItemMoney(item), cDate, "SentMessage");
                    } else {
                        Toast.makeText(GroupActivity.this, "輸入格式錯誤!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            adb.setNegativeButton("取消", null);
            adb.setCancelable(false);
            adb.show();
        }
    }

    private void speechDataList(int i, final String item, ArrayList<String> lvData) {
        if (lvData.get(i).equals("手動輸入")) {
            final EditText editText = new EditText(GroupActivity.this);
            AlertDialog.Builder adb = new AlertDialog.Builder(GroupActivity.this);
            adb.setTitle("輸入清單項目:");
            adb.setView(editText);
            adb.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Thread(new sendMessage(item)).start();
                    String item = editText.getText().toString();
                    bDataSet.add(item);
                    new saveToSQLite(item);
                }
            });
            adb.setNegativeButton("取消", null);
            adb.setCancelable(false);
            adb.show();
        } else {
            new Thread(new sendMessage(item)).start();
            bDataSet.add(item);
            new saveToSQLite(item);
        }
    }
    //---------------------------------------------------------------------------------------------------------------//

    //(1)分割字串
    public String getItemName(String item) {
        String resultString = "", resultInt = "";
        int lenItem = item.length();
        boolean resultBoolean[] = new boolean[lenItem];
        boolean inCheck = false;
        //判斷是否為有效輸入
        if (lenItem == 0) resultString = "";
        else if (item.charAt(lenItem - 1) == '元' || item.charAt(lenItem - 1) == '塊') {
            resultBoolean[lenItem - 1] = true;
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
        }
        return resultString;
    }

    //(1)分割數字&元
    public String getItemMoney(String item) {
        String resultString = "", resultInt = "";
        int lenItem = item.length();
        boolean resultBoolean[] = new boolean[lenItem];
        //判斷是否為有效輸入
        if (lenItem == 0) resultInt = "";
        else if (item.charAt(lenItem - 1) == '元' || item.charAt(lenItem - 1) == '塊') {
            resultBoolean[lenItem - 1] = true;
            //true = 金額
            for (int i = lenItem - 2; i >= 0; i--) {
                if ((item.charAt(i) == '0' || item.charAt(i) == '1' || item.charAt(i) == '2' ||
                        item.charAt(i) == '3' || item.charAt(i) == '4' || item.charAt(i) == '5' ||
                        item.charAt(i) == '6' || item.charAt(i) == '7' || item.charAt(i) == '8' ||
                        item.charAt(i) == '9') && resultBoolean[i + 1] == true) {
                    resultBoolean[i] = true;
                }
            }
            for (int i = 0; i < lenItem; i++) {
                if (resultBoolean[i] == true && item.charAt(i) != '元' && item.charAt(i) != '塊')
                    resultInt += item.charAt(i);
            }
        }
        return resultInt;
    }

    //防呆
    public boolean inCheck(String item) {
        int lenItem = item.length();
        boolean resultBoolean[] = new boolean[lenItem];
        boolean inCheck = false;
        if (getItemMoney(item) == "") inCheck = false;
        if (getItemName(item) == "") inCheck = false;
        else if (item.charAt(lenItem - 1) == '元' || item.charAt(lenItem - 1) == '塊') {
            resultBoolean[lenItem - 1] = true;
            inCheck = true;
        }
        return inCheck;
    }

    private class saveToSQLite {
        private MyDBHelper helper;
        private String [] saveValue = new String [10];
        //儲存帳簿內容
        public saveToSQLite(String sender, String itemName, String itemMoney, String date, String messageType){
            saveValue[0] = "groupAcc";
            saveValue[1] = sender;
            saveValue[2] = itemName;
            saveValue[3] = itemMoney;
            saveValue[4] = date;
            saveValue[5] = messageType;
            insert();
        }
        //儲存我要均攤.. 均攤金額 / 狀態
        public saveToSQLite(String dutchPrice, String dutchStatus ){
            saveValue[0] = "goDutch";
            saveValue[1] = dutchPrice;
            saveValue[2] = dutchStatus;
            insert();
        }
        //儲存購物清單.. 清單內容
        private saveToSQLite(String toBuyList){
            saveValue[0] = "toBuyList";
            saveValue[1] = toBuyList;
            insert();
        }

        private void insert (){
            account = getMyEmail() + ".db";
            helper = new MyDBHelper(GroupActivity.this, account, null, 1);
            helper.getWritableDatabase();
            ContentValues value = new ContentValues();

            if (saveValue[0].equals("groupAcc")){
                value.put("groupType", "groupAcc");
                value.put("accName", AccName);
                value.put("sender", saveValue[1]);
                value.put("itemName", saveValue[2]);
                value.put("itemMoney", saveValue[3] + "元");
                value.put("date", saveValue[4]);
                value.put("messageType", saveValue[5]);
            }else if (saveValue[0].equals("goDutch")){
                value.put("groupType", "goDutch");
                value.put("accName", AccName);
                value.put("dutchPrice", saveValue[1]);//dutchPrice
                value.put("dutchStatus", saveValue[2]);//dutchStatus
            }else if (saveValue[0].equals("toBuyList")) {
                value.put("groupType", "toBuyList");
                value.put("accName", AccName);
                value.put("toBuyList", saveValue[1]);//tobuylist
            }
            helper.getWritableDatabase().insert("groupSendRecevedData", null, value);
            helper.close();
        }

    }

    //載入SQLite資料
    private void getSQLite() {
        account = getMyEmail() + ".db";
        helper = new MyDBHelper(this, account, null, 1);
        for (int i = 0; i <= 2; i++) {
            String groupType = "";
            if (i==0)groupType = "groupAcc";
            else if (i==1) groupType = "toBuyList";
            else if (i==2) groupType = "goDutch";
            Cursor cursor = helper.getReadableDatabase().query("groupSendRecevedData",
                    new String[]{"_id", "sender", "itemName", "itemMoney", "messageType", "date"
                                    ,"dutchPrice", "dutchStatus", "toBuyList"},
                    //(1)sender、(2)itemName、(3)itemMoney、(4)messageType、(5)date、(6)dutchPrice、(7)dutchStatus、(8)toBuyList
                    "accName=? AND groupType = ?",
                    new String[]{AccName, groupType},
                    null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (i == 0 ) {//群體帳簿
                    String sender = cursor.getString(1);
                    String itemName = cursor.getString(2);
                    String itemMoney = cursor.getString(3);
                    String messageType = cursor.getString(4);
                    String date = cursor.getString(5);
                    String data = messageType + "," + itemName + itemMoney + "," + sender + "," + date;//GroupMessageAdapter==>sender[]{0},{1},{2}  , messageType, message, sender
                    gDataSet.add(data);
                } else if (i == 1) {//購物清單
                    String toBuyList = cursor.getString(8);
                    bDataSet.add(toBuyList);
                } else if (i == 2) {//我要均攤
                    String messageType = cursor.getString(4);
                    dutchPrice = cursor.getString(6);
                    String dutchStatus = cursor.getString(7);
                    if (dutchStatus.equals("yes") && messageType==null) {//status
                        dutchStatus = "已銷帳";
                        dDataSet = getMyEmail() + "\n" + "支付狀態:" + dutchStatus + "\n" + "應收到:" + dutchPrice;
                        STATUS_TYPE = STATUS_RED;
                    } else if(dutchStatus.equals("no") && messageType==null){
                        dutchStatus = "未銷帳";
                        dDataSet = getMyEmail() + "\n" + "支付狀態:" + dutchStatus + "\n" + "請支付:" + dutchPrice;
                        STATUS_TYPE = STATUS_RED;
                    }
                }
                cursor.moveToNext();
            }
            groupAdapter(gDataSet);
        }
        helper.close();
    }

    //設置帳簿名字set accName
    private void setAccName() {
        //getBundle();
        tv_accName.setText(AccName);
    }

    //多選項
    public void alertMultiCoice() {
        //選擇均攤成員==>Alert==>multiChoice
        //選擇均攤項目==>Alert==>muiltiCoice
        //顯示均攤金額==>Alert==>setMeaage
        final String[] dutchMember = AccMember.split(",");
        boolean[] booleanMember = new boolean[dutchMember.length];
        multiAlertDialog(dutchMember, booleanMember, "均攤成員")
                .setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (arrMem.size() > 0) {
                            int count = 0;
                            Cursor dutchAllData = helper.getReadableDatabase().query("groupSendRecevedData",
                                    null, "accName = ? AND groupType = ?", new String[]{AccName,"groupAcc"}, null, null, null);
                            final String[] dutchItem = new String[dutchAllData.getCount()];
                            final int[] dutchMoney = new int[arrMem.size() + 1];
                            final double[] dutchTotal = new double[1];
                            boolean[] booleanItem = new boolean[dutchItem.length];
                            for (int n = 0; n <= arrMem.size(); n++) {
                                String member = "";
                                if (n == arrMem.size()) {
                                    member = getMyEmail();
                                } else {
                                    member = dutchMember[arrMem.get(n)];
                                }

                                account = getMyEmail() + ".db";
                                Cursor dutchData = helper.getReadableDatabase().query(
                                        "groupSendRecevedData",
                                        new String[]{"_id", "sender", "itemName", "itemMoney"},
                                        "accName = ? AND sender = ? AND groupType = ?" ,
                                        new String[]{AccName, member, "groupAcc"}, null, null, null);

                                dutchData.moveToFirst();
                                while (!dutchData.isAfterLast()) {
                                    dutchItem[count] = dutchData.getString(1) + "\n"
                                            + dutchData.getString(2) + "\n"
                                            + dutchData.getString(3) + "\n";
                                    dutchData.moveToNext();
                                    count++;
                                }
                            }

                            multiAlertDialog(dutchItem, booleanItem, "均攤項目")
                                    .setPositiveButton("下一步", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (arrMon.size() > 0) {
                                                String dutchMsg = "";
                                                for (int n = 0; n <= arrMem.size(); n++) {
                                                    if (n == arrMem.size()) {
                                                        for (int m = 0; m < arrMon.size(); m++) {
                                                            String[] split = dutchItem[arrMon.get(m)].split("\n");
                                                            if (getMyEmail().equals(split[0])) {
                                                                dutchMoney[n] += Integer.parseInt(split[2].replace("元", ""));
                                                            }
                                                        }
                                                    } else {
                                                        for (int m = 0; m < arrMon.size(); m++) {
                                                            String[] split = dutchItem[arrMon.get(m)].split("\n");
                                                            if (dutchMember[arrMem.get(n)].equals(split[0])) {
                                                                dutchMoney[n] += Integer.parseInt(split[2].replace("元", ""));
                                                            }
                                                        }
                                                    }
                                                    dutchTotal[0] += dutchMoney[n];
                                                }
                                                String sendMon = "", sendMem = "", sendSta = "";
                                                String mon = "", sta = "";
                                                for (int n = 0; n <= arrMem.size(); n++) {
                                                    String member = "";
                                                    int totalMember = arrMem.size() + 1;
                                                    if (n == arrMem.size()) {
                                                        member = getMyEmail();
                                                    } else {
                                                        member = dutchMember[arrMem.get(n)];
                                                        if (n < arrMem.size() - 1)
                                                            sendMem += member + ",";
                                                        else sendMem += member;
                                                    }

                                                    if (((dutchTotal[0] / totalMember) - dutchMoney[n]) > 0) {
                                                        dutchMsg += member + "\n"
                                                                + "支付狀態: 未銷帳\n"
                                                                + "請支付:" + Math.round((dutchTotal[0] / totalMember) - dutchMoney[n]) + "元\n\n";
                                                        if (n < arrMem.size() - 1) {
                                                            sendSta += "no,";
                                                            sendMon += Math.round((dutchTotal[0] / totalMember) - dutchMoney[n]) + "元,";
                                                        } else if (!member.equals(getMyEmail()) && n < arrMem.size()) {
                                                            sendSta += "no";
                                                            sendMon += Math.round((dutchTotal[0] / totalMember) - dutchMoney[n]) + "元";
                                                        } else {
                                                            sta = "no";
                                                            mon = Math.round((dutchTotal[0] / totalMember) - dutchMoney[n]) + "元";
                                                        }

                                                    } else {
                                                        dutchMsg += member + "\n"
                                                                + "支付狀態: 已銷帳\n"
                                                                + "應收到:" + Math.round(dutchMoney[n] - (dutchTotal[0] / totalMember)) + "元\n\n";
                                                        if (n < arrMem.size() - 1) {
                                                            sendSta += "yes,";
                                                            sendMon += Math.round(dutchMoney[n] - (dutchTotal[0] / totalMember)) + "元,";
                                                        } else if (!member.equals(getMyEmail()) && n < arrMem.size()) {
                                                            sendSta += "yes";
                                                            sendMon += Math.round(dutchMoney[n] - (dutchTotal[0] / totalMember)) + "元";
                                                        } else {
                                                            sta = "yes";
                                                            mon = Math.round(dutchMoney[n] - (dutchTotal[0] / totalMember)) + "元";
                                                        }
                                                    }
                                                }
                                                AlertDialog.Builder adb = new AlertDialog.Builder(GroupActivity.this);
                                                adb.setTitle("均攤後每人支付: " + Math.round(dutchTotal[0] / (arrMem.size() + 1)) + "元");
                                                adb.setMessage(dutchMsg);
                                                adb.setCancelable(false);
                                                //別人
                                                final String finalSendMem = sendMem;
                                                final String finalSendMon = sendMon;
                                                final String finalSendSta = sendSta;
                                                //自己
                                                final String finalSta = sta;
                                                final String finalMon = mon;

                                                adb.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        //accName, dutchMember, dutchMsg, status
                                                        new Thread(new sendMessage(AccName, finalSendMem, finalSendMon, finalSendSta)).start();
                                                        new saveToSQLite(finalMon, finalSta);
                                                        dDataSet = "";
                                                        if (finalSta.equals("yes")) {
                                                            dDataSet = getMyEmail() + "\n" + "支付狀態:" + "已銷帳" + "\n" + finalMon;
                                                        } else {
                                                            dDataSet = getMyEmail() + "\n" + "支付狀態:" + "未銷帳" + "\n" + finalMon;
                                                        }
                                                        dutchPrice = finalMon;
                                                        STATUS_TYPE = STATUS_RED;
                                                    }
                                                });
                                                adb.setNegativeButton("取消", null);
                                                adb.show();
                                            }
                                        }
                                    })
                                    .show();
                        }
                    }
                })
                .show();
        arrMem.clear();
        arrMon.clear();
        helper.close();
    }

    //按下返回鍵時==>連線中斷
    @Override
    public void onBackPressed() {
        isConnect = false;
        super.onBackPressed();
    }

    //警示框>多選項
    public AlertDialog.Builder multiAlertDialog(String option[], final boolean checkItem[], final String title) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb
                .setTitle(title)
                .setMultiChoiceItems(option, checkItem, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                        if (isChecked && title.equals("均攤成員")) {
                            arrMem.add(i);
                        } else if (isChecked && title.equals("均攤項目")) {
                            arrMon.add(i);
                        } else if (!isChecked && title.equals("均攤成員")) {
                            arrMem.remove((Integer.valueOf(i)));
                        } else if (!isChecked && title.equals("均攤項目")) {
                            arrMon.remove((Integer.valueOf(i)));
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton("取消", null);
        return adb;
    }

    //取得目前日期
    private String getDate() {
        String currentData = "", currentDate24 = "";
        //判斷上午下午
        SimpleDateFormat format24 = new SimpleDateFormat("HH");
        currentDate24 = format24.format(new java.util.Date());
        if (Integer.parseInt(currentDate24) > 12) currentDate24 = "下午";
        else currentDate24 = "上午";
        //取得時間
        SimpleDateFormat format = new SimpleDateFormat("hh:mm MM/dd");
        currentData = format.format(new java.util.Date());
        return currentDate24 + currentData;
    }
    //
    private int[] currentDate(){
        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int []currentDate = {year, month+1, day};
        Log.d("aaaaaaaaaaa",year+""+month+""+day);
        return  currentDate;
    }
    //設置狀態
    private void setStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (STATUS_TYPE == STATUS_RED) {
                    btn_status.setBackgroundResource(R.drawable.group_background_fun2);
                    tv_status.setBackgroundResource(R.drawable.group_background_fun3);
                    tv_status.setText("未結清");
                } else if (STATUS_TYPE == STATUS_GREEN) {
                    btn_status.setBackgroundResource(R.drawable.group_background_fun);
                    tv_status.setBackgroundResource(R.drawable.group_background_sent2);
                    tv_status.setText("已結清");
                }
            }
        });


    }
    //更新資料庫
    private void update(){
        try {
            ContentValues values = new ContentValues();
            values.put("messageType", "已均攤");
            account = getMyEmail() + ".db";
            helper = new MyDBHelper(this, account, null, 1);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.update("groupSendRecevedData", values, "dutchStatus  = 'yes' OR dutchStatus = 'no'", null);
        }catch (Exception e){
            Log.d("XXXXXXXXX",e.toString());
        }

    }
}

