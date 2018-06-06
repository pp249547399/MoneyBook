package tw.edu.ncut.login.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import tw.edu.ncut.login.MainActivity;
import tw.edu.ncut.login.myapplication.R;

public class Login extends Activity {
    //
    Button btn_login,btn_create,btn_forget ;
    private EditText ed_user, ed_pw;
    public static final int CONNECTION_TIMEOUT=50000;
    public static final int READ_TIMEOUT=50000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        btn_create=findViewById(R.id.btn_create);
        btn_forget=findViewById(R.id.btn_forget);
        ed_user = findViewById(R.id.ed_user);
        ed_pw = findViewById(R.id.ed_pw);

        readData();
        if (!ed_user.getText().toString().equals("") && !ed_pw.getText().toString().equals("")) {
            Log.d("read2",ed_user.getText().toString()+"///"+ed_pw.getText().toString());
            Intent intent = new Intent(Login.this,MainActivity.class);
            startActivity(intent);
            Login.this.finish();
        }


        //創新帳號Listener
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,SignupActivity.class);
                startActivity(intent);

            }
        });
        //忘記密碼
        btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Login.this,ForgetPassword.class);
                startActivity(intent);
            }
        });
        //登入按鈕Listener
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get text from email and passord field
                final String email = ed_user.getText().toString();
                final String password = ed_pw.getText().toString();


                // Initialize  AsyncLogin() class with email and password
                new AsyncLogin().execute(email,password);
            }
        });
    }
    class AsyncLogin extends AsyncTask<String, String, String>
    {
        ProgressDialog pdLoading = new ProgressDialog(Login.this);
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
        protected String doInBackground(String... params) {
            try {
                // PHP檔
                url = new URL("http://140.128.88.166:8008/login.inc.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception" ;
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("email", params[0])
                        .appendQueryParameter("password", params[1]);
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

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "exception";
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
        //連接資料庫 判斷帳密是否正確
        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();
            if(result.equalsIgnoreCase(" true"))
            {
                Intent intent = new Intent(Login.this,MainActivity.class);
                startActivity(intent);
                Login.this.finish();
                //記住帳密
                saveData();

            }else if (result.equalsIgnoreCase(" false")){
                Toast.makeText(Login.this, "信箱或密碼錯誤", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception")) {
                Toast.makeText(Login.this, "未連接網路，請開啟WI-FI", Toast.LENGTH_LONG).show();

            }
            else if(result.equalsIgnoreCase("unsuccessful")){
                Toast.makeText(Login.this, "OOPs! Something went wrong. Connection Problem. unsuccessful", Toast.LENGTH_LONG).show();

            }
        }

    }
    //
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public void saveData(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString("email",ed_user.getText().toString())
                .putString("password",ed_pw.getText().toString())
                .commit();
    }
    public void readData(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ed_user.setText(sharedPreferences.getString("email", ""));
        ed_pw.setText(sharedPreferences.getString("password", ""));
    }
}
