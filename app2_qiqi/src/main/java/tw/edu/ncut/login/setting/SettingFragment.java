package tw.edu.ncut.login.setting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.login.Login;
import tw.edu.ncut.login.myapplication.R;

public class SettingFragment extends Fragment {


    private EditText ed_user,ed_pw;
    //-----------------------lv------------
    private int [] image ={R.drawable.setting_price, R.drawable.setting_speaking, R.drawable.sync, R.drawable.setting_logout};
    private  int [] type = {0,0,1,1};//0為switch 1為noswitch
    private  String[] name = {"價格合理性","全語音系統","資料同步","登出"};
    private final int lvRationalAmount=0 ,lvspeach=1,lvSyn = 2,lvSignout =3;//列表位置對應上面
    private Switch switchButtoun;
    private  String switchBooleanSp,switchBooleanRA;
    //------------------------------------
    private ListView listView;
    private List<Settinglistview> list =new ArrayList<>();
    //----------------------------------網路連線----------------------------------------------
    private int READ_TIMEOUT=50000;
    private int CONNECTION_TIMEOUT=50000;
    //---------------------------------資料庫--------------------------------------------------
    private String myEmail,account;
    private MyDBHelper helper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_setting, container, false);
        View login = inflater.inflate(R.layout.activity_login,container, false);
        View sw = inflater.inflate(R.layout.setting_listview_switch, container ,false);
        ed_user = login.findViewById(R.id.ed_user);
        ed_pw = login.findViewById(R.id.ed_pw);
        listView = (ListView)view.findViewById(R.id.lv_setting);
        switchButtoun = (Switch)view.findViewById(R.id.setting_switch);
        //-----設定switch起始狀態init()
        switchBooleanSp = "True";
        switchBooleanRA ="True";
        //---------------------------------------------------
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        initSetting();
        SettingAdapter adapter = new SettingAdapter(getActivity(), R.layout.setting_listview_switch,list);
        listView.setAdapter(adapter);
        listListener();
    }
    //
    private  void initSetting(){
        for(int i =0;i<type.length;i++){
            Settinglistview add= new Settinglistview(image[i],name[i],type[i]);
            list.add(add);
        }
    }
    //按鈕事件
    public void listListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position){
                    case lvSignout :
                        DeleteCount();
                        Intent intent=new Intent(getActivity(),Login.class);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case lvSyn :
                        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                        adb.setTitle("資料同步");
                        adb.setMessage("上傳 : 更新資料庫與本裝置資料同步\n"
                                +"下載 : 更新本裝置與資料庫資料同步\n");
                        adb.setPositiveButton("上傳", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new synSQLiteToMySQLDB("http://140.128.88.166:8008/syn.php").execute();
                                Toast.makeText(getActivity(), "上傳資料中...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        adb.setNegativeButton("下載", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new synSQLiteToMySQLDB("http://140.128.88.166:8008/download.php").execute();
                                Toast.makeText(getActivity(), "下載資料中...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        adb.setNeutralButton("取消", null);
                        adb.setCancelable(false);
                        adb.show();
                        Toast.makeText(getActivity(), "資料同步中...", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
    //偏好設定
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public void DeleteCount(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.edit()
                .putString("email","")
                .putString("password","")
                .commit();
    }
    //設定起始Switch偏好設定
    public void setSwitchRf(int swSelect,String b){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(swSelect==0) {
            sharedPreferences.edit()
                    .putString("switchBooleanRa", b)
                    .commit();
        }
        else if(swSelect==1){
            sharedPreferences.edit()
                    .putString("switchBooleanSp", b)
                    .commit();
        }
    }
    //
    //同步資料測試
    class synSQLiteToMySQLDB extends AsyncTask<Object, Object, String> {
        ProgressDialog pdLoading = new ProgressDialog(getActivity());
        String php ;
        public synSQLiteToMySQLDB(String php){
            this.php = php;
        }
        HttpURLConnection conn ;
        URL url = null;
        //String php = "http://140.128.88.166:8008/syn.php";
        @Override
        protected String doInBackground(Object... voids) {
            try {
                url = new URL(php);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                //

                ArrayList<HashMap<String,String>> SQLiteList= new ArrayList<>();
                account = getMyEmail()+".db";
                helper = new MyDBHelper(getActivity(),account,null,1);
                if (php.equals("http://140.128.88.166:8008/syn.php")) {
                    Cursor SQLiteData = helper.getReadableDatabase().query("rubyLin",
                            new String[]{"_id", "dateYear", "dateMonth", "dateDay",
                                    "itemCategory", "smallCategory", "categoryName",
                                    "itemType", "itemName", "itemMoney", "incomeID"}, null, null, null, null, null);
                    SQLiteData.moveToFirst();
                    //int count = SQLiteData.getCount();
                    while (!SQLiteData.isAfterLast()) {
                        HashMap<String, String> SQLiteMap = new HashMap<>();
                        SQLiteMap.put("email", getMyEmail());
                        SQLiteMap.put("dateYear", String.valueOf(SQLiteData.getInt(1)));
                        SQLiteMap.put("dateMonth", String.valueOf(SQLiteData.getInt(2)));
                        SQLiteMap.put("dateDay", String.valueOf(SQLiteData.getInt(3)));
                        SQLiteMap.put("itemCategory", String.valueOf(SQLiteData.getInt(4)));
                        SQLiteMap.put("smallCategory", String.valueOf(SQLiteData.getInt(5)));
                        SQLiteMap.put("categoryName", String.valueOf(SQLiteData.getString(6)));
                        SQLiteMap.put("itemType", String.valueOf(SQLiteData.getInt(7)));
                        SQLiteMap.put("itemName", String.valueOf(SQLiteData.getString(8)));
                        SQLiteMap.put("itemMoney", String.valueOf(SQLiteData.getInt(9)));
                        //SQLiteMap.put("incomeID", String.valueOf(SQLiteData.getInt(10)));

                        SQLiteList.add(SQLiteMap);
                        SQLiteData.moveToNext();
                    }

                }else if (php.equals("http://140.128.88.166:8008/download.php")){
                    //
                }
                //hm ==> hashmap
                HashMap<String, ArrayList<HashMap<String, String>>> hm = new HashMap<>();
                hm.put("SQLiteData", SQLiteList);
                Gson gson = new GsonBuilder().create();
                //gson.toJson(hm);
                //
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                String json = gson.toJson(hm);
                osw.write(json);
                Log.d("json",json);

                //osw.write(gson.toJson(SQLiteList));
                osw.close();
                osw.flush();
                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                int response_code = conn.getResponseCode();
                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // Pass data to onPostExecute method
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

            //return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pdLoading.dismiss();
            parseJson(s);
        }
    }
    //取得Email
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }
    //
    private void parseJson(String result){
        
    }

}