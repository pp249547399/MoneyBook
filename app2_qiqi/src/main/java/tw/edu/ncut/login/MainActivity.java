package tw.edu.ncut.login;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.myapplication.R;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Pager adapter;
    //-------------------------資料庫---------------------------------------
    private MyDBHelper helper;
    private Cursor friendData;
    //----------------------偏好儲存---------------------------------------
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String myEmail,account ;
    //------------------------網路傳輸--------------------------------------
    public static final int CONNECTION_TIMEOUT=50000;
    public static final int READ_TIMEOUT=50000;
    private String php = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hi! User" + getMyEmail());

        //tab id
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        //add the tabs
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.tab_new));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.tab_piechart));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.tab_setting));
        mTabLayout.addTab(mTabLayout.newTab().setIcon(R.drawable.tab_group));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        adapter = new Pager(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.d("aaaaaa","55555");
                //adapter.notifyDataSetChanged();
                mTabLayout.getTabAt(position).getIcon().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
                if(position==0){
                    for(int i=1;i<4;i++)
                        mTabLayout.getTabAt(i).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                }
                if (position>0)
                    mTabLayout.getTabAt(position-1).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                if (position<3)
                    mTabLayout.getTabAt(position+1).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setScrollPosition(position, 0, true);
                mTabLayout.setSelected(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //登入時與PHP連線
        String php = "http://140.128.88.166:8008/invite.php";
        new CreateNewActivity().execute(php, getMyEmail());
    }
    //開啟APP時檢查是否有邀請
    class CreateNewActivity extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception " ;
            }
            try {
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String query;
                if(strings[1]== "yes" || strings[1]=="0"|| strings[1]=="1") {
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("answer", strings[1])
                            .appendQueryParameter("inviter",strings[2])
                            .appendQueryParameter("email",strings[3]);
                    query = builder.build().getEncodedQuery();
                }else {
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("email", strings[1]);
                    query = builder.build().getEncodedQuery();
                }

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
                return  "exception";
            }

            try {
                int response_code = conn.getResponseCode();
                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK ) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // Pass data to onPostExecute method
                    Log.d("jdkjlkdjodjo",result.toString());
                    //parseJson(result.toString());
                    return(result.toString());
                }else{
                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) { //PHP回傳的值
            pdLoading.dismiss();
            parseJson(result);
        }
    }
    //myEmail
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }
    //alert==>收到邀請
    public void alert(final String inviter, final String email){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb
                .setTitle("您有一則訊息")
                .setMessage("來自"+inviter+"("+email+")"+"的交友邀請,請問您是否接受?")
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveInviterToSQL("friend",inviter,email);
                        String php = "http://140.128.88.166:8008/answer.php";
                        new CreateNewActivity().execute(php, "0",email,myEmail);
                    }
                })
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String php = "http://140.128.88.166:8008/answer.php";
                        new CreateNewActivity().execute(php, "1",email,myEmail);
                    }
                })
                .setCancelable(false)
                .show();
    }
    //
    //alert2==>送出的邀請被接受
    public void alert2(final String inviter, final String email){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb
                .setTitle("您有一則訊息")
                .setMessage("您送出的邀請"+inviter+"("+email+")"+"已接受")
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveInviterToSQL("friend",inviter,email);
                        String php = "http://140.128.88.166:8008/answer.php";
                        new CreateNewActivity().execute(php, "0",email,myEmail);
                    }
                })
                .setCancelable(false)
                .show();
    }
    //alert3==>groupinvite
    public void alert3(final String groupAccName, final String groupAccMember, final  String inviter){
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb
                .setTitle("您有一則訊息")
                .setMessage("您被邀請加入"+groupAccName+"群體記帳簿"+
                        "此帳簿成員有"+groupAccMember)
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveInviterToSQL("group",groupAccName,groupAccMember+"/"+inviter);//accNam
                        String php = "http://140.128.88.166:8008/answer.php";
                        new CreateNewActivity().execute(php,"yes",groupAccName,myEmail);//通知伺服器資料收到了
                    }
                })
                .setCancelable(false)
                .show();
    }
    //儲存邀請==>好友/群體
    private void saveInviterToSQL(String type,String inviter,String email) {
        ContentValues value = new ContentValues();
        account = getMyEmail()+".db";
        helper = new MyDBHelper(MainActivity.this, account, null, 1);
        if (type.equals("friend")) {
            value.put("userName", inviter);
            value.put("email", email);
            long id = helper.getWritableDatabase().insert("friendData", null, value);
        }else if (type.equals("group")){
            String [] accMember = email.split("/");
            value.put("groupAccName", inviter);
            value.put("groupAccMember", accMember[0]);
            value.put("groupInviter", accMember[1]);
            long id = helper.getWritableDatabase().insert("groupAccData", null, value);
        }

        Log.d("result",""+inviter);
        helper.close();
    }
    //
    private void parseJson(String result){
        String TAG = "JSON TEST";
        try {
            JSONArray array = new JSONArray(result);// array_:[{"invite":{"invite":"inviter","user":"zzz","email":"zz"}}]
            //JSONObject object = array.getJSONObject(0);//object_:{"invite":{"invite":"inviter","user":"zzz","email":"zz"}}
            Log.d(TAG, "obj:" + String.valueOf(array));
            String getInvite  = "";
            String getGroupInvite = "";
            String getInviter = "";
            for(int i=0; i<array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);//{"invite":[{"email":"ok@gmail.com","user":"ok","invite":"inviter"}]}
                Log.d(TAG, "obj:" + String.valueOf(obj));

                getInvite = obj.optString("invite");//[{"email":"ok@gmail.com","user":"ok","invite":"inviter"}]
                getGroupInvite = obj.optString("groupinvite");
                getInviter = obj.optString("inviter");
                Log.d(TAG, "objI:" + String.valueOf(getInvite));

                JSONArray arrayInvite = null;JSONObject objectInvite = null;
                String invite = "";
                if (!getInvite.equals("")) {
                    arrayInvite = new JSONArray(getInvite);

                }else if (!getInviter.equals("")){
                    arrayInvite = new JSONArray(getInviter);
                }else if (!getGroupInvite.equals("")){
                    arrayInvite = new JSONArray(getGroupInvite);
                }
                if (arrayInvite != null) {
                    for (int j = 0; j < arrayInvite.length(); j++) {
                        objectInvite = arrayInvite.getJSONObject(j);
                        invite = objectInvite.optString("invite");
                        Log.d(TAG, "objII:" + invite);
                    }
                }


                //好友邀請
                String email = "";
                String user = "";
                String groupAccName = "";
                String groupAccMember = "";

                if (invite.equals("inviter")) {//被邀請
                    email = objectInvite.getString("email");
                    user = objectInvite.getString("user");
                    alert(user, email);//inviter,email==>
                } else if (invite.equals("invite")) {//邀請人
                    email = objectInvite.getString("email");
                    user = objectInvite.getString("user");
                    alert2(user, email);
                } else if (invite.equals("groupInvite")) {//收到群體帳簿邀請
                    //email = obj.getString("email");//被邀請加入通知
                    //user = obj.getString("user");
                    groupAccName = objectInvite.optString("groupAccName");
                    groupAccMember = objectInvite.optString("groupAccMember");
                    email = objectInvite.optString("email");//邀請人
                    alert3(groupAccName, groupAccMember,email);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
