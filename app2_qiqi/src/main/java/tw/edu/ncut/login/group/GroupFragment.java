package tw.edu.ncut.login.group;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.myapplication.R;

public class GroupFragment extends Fragment {

    //
    private Button btn_fri,btn_acc,btn_search,btn_invite;
    private EditText et_search;
    private ListView lv_group;
    private String clickBtn ="btn_fri";
    //------------------------資料庫----------------------------------------
    private MyDBHelper helper;
    private SimpleCursorAdapter  friAdapter,accAdapter;
    private Cursor friData, accData;
    private String account;
    //private int groupType[] ={R.drawable.}
    //----------------------偏好儲存----------------------------------------
    private SharedPreferences sharedPreferences;
    private String myEmail;
    //-----------------------網路連線---------------------------------------
    public static final int CONNECTION_TIMEOUT=50000;
    public static final int READ_TIMEOUT=50000;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_group , container, false);
        lv_group = view.findViewById(R.id.lv_group);
        //下面
        btn_fri = view.findViewById(R.id.btn_fri);
        btn_acc = view.findViewById(R.id.btn_acc);
        //上面
        btn_invite = view.findViewById(R.id.btn_invite);
        //btn_search = view.findViewById(R.id.btn_search);
        et_search = view.findViewById(R.id.et_search);
        //
        btn_fri.setBackground(getResources().getDrawable(R.drawable.group_btn_click));
        btn_acc.setBackground(getResources().getDrawable(R.drawable.group_btn));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshListView();
        //
        setBtn_acc();
        setBtn_fri();
        setBtn_invite();
    }
    //(1)好友
    private void setBtn_fri(){
        btn_fri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickBtn = "btn_fri";
                Drawable drawable =getResources().getDrawable(R.drawable.ic_group_new_fri);
                btn_invite.setCompoundDrawablesWithIntrinsicBounds(null,drawable,null,null);
                et_search.setHint("輸入E-mail進行邀請");
                refreshListView();
                btn_fri.setBackground(getResources().getDrawable(R.drawable.group_btn_click));
                btn_acc.setBackground(getResources().getDrawable(R.drawable.group_btn));
            }
        });
    }
    //(1)帳簿
    private void setBtn_acc(){
        btn_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickBtn = "btn_acc";
                setGroupClick();
                Drawable drawable =getResources().getDrawable(R.drawable.ic_group_new_acc);
                btn_invite.setCompoundDrawablesWithIntrinsicBounds(null,drawable,null,null);
                et_search.setHint("新增群體帳簿請往右走");
                refreshListView();
                btn_acc.setBackground(getResources().getDrawable(R.drawable.group_btn_click));
                btn_fri.setBackground(getResources().getDrawable(R.drawable.group_btn));
            }
        });
    }

    //(1)邀請
    private void setBtn_invite(){
        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickBtn.equals("btn_fri")){
                    final String searchEmail = et_search.getText().toString();
                    myEmail = getMyEmail();
                    //寄出邀請(收,邀請者)
                    String php = "http://140.128.88.166:8008/inviter.php";
                    new CreateNewActivity().execute(php,searchEmail,myEmail);
                }
                //新增群體帳簿
                if (clickBtn.equals("btn_acc")){
                    account = getMyEmail()+".db";
                    helper = new MyDBHelper(getActivity(), account, null, 1);
                    accData = helper.getReadableDatabase().query("friendData", new String[]{"_id", "email", "userName"},
                            null,null,null,null,null,null);
                    final ArrayList<Integer> mUserItem = new ArrayList<Integer>();
                    final String listItem [] = new String[accData.getCount()];
                    boolean checkItem[] = new boolean[listItem.length];
                    int i=0;
                    accData.moveToFirst();
                    while (!accData.isAfterLast()){
                        listItem[i] = accData.getString(accData.getColumnIndex("userName"));
                        accData.moveToNext();
                        Log.d("listItem",listItem[i]+" , "+i);
                        i++;
                    }
                    //列出所有好友
                    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                    adb.setTitle("選擇好友");
                    adb.setMultiChoiceItems(listItem, checkItem, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                            if(isChecked){
                                mUserItem.add(position);
                            }else{
                                mUserItem.remove((Integer.valueOf(position)));
                            }
                        }
                    });
                    adb.setCancelable(false);
                    adb.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String groupAccName =getSerialNumber(myEmail);
                            String groupAccMember = "";
                            ContentValues values = new ContentValues();
                            for (int n=0; n<mUserItem.size(); n++){
                                if (groupAccMember.equals(""))
                                    groupAccMember = listItem[mUserItem.get(n)];
                                else
                                    groupAccMember = groupAccMember+","+listItem[mUserItem.get(n)];
                            }

                            String groupMember[] = groupAccMember.split(",");
                            for (int n=0; n<groupMember.length; n++) {
                                accData = helper.getReadableDatabase().query("friendData",
                                        new String[]{"_id", "email", "userName"},
                                        "userName = ?", new String[]{groupMember[n]}, null, null, null, null);
                                accData.moveToFirst();
                                while(!accData.isAfterLast()) {
                                    groupMember[n] = accData.getString(1);
                                    accData.moveToNext();
                                }
                            }
                            groupAccMember = "";
                            for (int n=0; n<mUserItem.size(); n++){
                                if (groupAccMember.equals(""))
                                    groupAccMember = groupMember[n];
                                else
                                    groupAccMember = groupAccMember+","+groupMember[n];
                            }

                            if(!groupAccMember.equals("")) {
                                //寄出群體帳簿邀請
                                String php = "http://140.128.88.166:8008/groupInviter.php";
                                new CreateNewActivity().execute(php,getMyEmail(),groupAccName,groupAccMember);

                                //
                                values.put("groupAccName", groupAccName);
                                values.put("groupAccMember", groupAccMember);
                                values.put("groupInviter", getMyEmail());
                                long id = helper.getWritableDatabase().insert("groupAccData", null, values);
                                refreshListView();
                            }

                        }
                    });
                    adb.show();

                }
            }
        });
    }
    //refresh listview
    private void refreshListView(){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);
        if (clickBtn.equals("btn_fri")){
            friData = helper.getReadableDatabase().query("friendData",
                    new String[]{"_id","email","userName"}, null, null, null, null, null);
            friAdapter = new SimpleCursorAdapter(getActivity(),R.layout.adapter_group_fri,friData,
                    new String[]{"email","userName"},new int[]{R.id.tv_status,R.id.tv_account},0);
            lv_group.setAdapter(friAdapter);
        }else if (clickBtn.equals("btn_acc")) {
            accData = helper.getReadableDatabase().query("groupAccData",
                    new String[]{"_id","groupAccName","groupAccMember"}, null, null, null, null, null);
            accAdapter = new SimpleCursorAdapter(getActivity(),R.layout.adapter_group_acc,accData,
                    new String[]{"groupAccMember","groupAccName"},new int[]{R.id.tv_status,R.id.tv_account},0);
            lv_group.setAdapter(accAdapter);
        }
        helper.close();
    }
    //set group listView
    private void setGroupClick(){
        lv_group.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (clickBtn.equals("btn_acc")){
                    final long itemId = lv_group.getItemIdAtPosition(i);
                    //
                    account = getMyEmail()+".db";
                    helper = new MyDBHelper(getActivity(), account, null, 1);
                    accData = helper.getReadableDatabase().query("groupAccData",new String[]{"_id","groupAccName","groupAccMember","groupInviter"},
                            "_id = ?",new String[]{String.valueOf(itemId)},null,null,null);
                    accData.moveToFirst();
                    String AccName = accData.getString(1);
                    String AccMember = accData.getString(2);
                    String AccIntviter = accData.getString(3);
                    helper.close();
                    //
                    Bundle bundle = new Bundle();
                    bundle.putString("groupAccName",AccName);
                    bundle.putString("groupAccMember",AccMember);
                    bundle.putString("groupInviter",AccIntviter);
                    //
                    Intent intent = new Intent(getActivity(), GroupActivity.class);

                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
    }
    //流水號
    private String getSerialNumber(String email){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String currentTime = format.format(date);
        String serialNumber = email+currentTime;
        serialNumber = serialNumber.replaceAll("[@.]","_");
        Log.d("serialNumber:",serialNumber);
        return serialNumber;
    }
    //寄出邀請--PHP連線
    class CreateNewActivity extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(getActivity());
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
            //
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

                Uri.Builder builder = new Uri.Builder();
                if(strings[0].equals("http://140.128.88.166:8008/inviter.php")) {//好友邀請
                    builder
                            .appendQueryParameter("email", strings[1])//searchEmail==>被邀請的email
                            .appendQueryParameter("inviter", strings[2]);//myEmail==>寄出邀請
                }else  if(strings[0].equals("http://140.128.88.166:8008/groupInviter.php")){//群體記帳邀請
                    builder
                            .appendQueryParameter("inviter", strings[1])//myEmail==>寄出邀請
                            .appendQueryParameter("groupAccName", strings[2])//群體帳簿名稱
                            .appendQueryParameter("groupMember",strings[3]);//群體帳體成員
                }

                String query = builder.build().getEncodedQuery();

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
        }

        @Override
        protected void onPostExecute(String result) { //回傳的值
            //this method will be running on UI thread
            pdLoading.dismiss();
            Toast.makeText(getActivity(), "是否寄出邀請:"+result, Toast.LENGTH_SHORT).show();
            Log.d("invite",result);
        }
    }
    //myEmail
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }

}
