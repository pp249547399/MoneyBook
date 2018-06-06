package tw.edu.ncut.login.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.regex.Pattern;

import tw.edu.ncut.login.myapplication.R;

public class SignupActivity extends AppCompatActivity {

    private Button btn_sent_mysql;
    private EditText et_username,et_email,et_password,et_passwordagain;
    public static final int CONNECTION_TIMEOUT=50000;
    public static final int READ_TIMEOUT=50000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btn_sent_mysql= (Button) findViewById(R.id.btn_sent_mysql);
        et_username =(EditText)findViewById(R.id.et_username);
        et_email=(EditText)findViewById(R.id.et_email);
        et_password=(EditText)findViewById(R.id.et_password);
        et_passwordagain=(EditText)findViewById(R.id.et_passwordagain);
        //------------------------------------------------------------
        btn_sent_mysql.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = et_username.getText().toString();
                final String email = et_email.getText().toString();
                final String password = et_password.getText().toString();
                final String passwordagain = et_passwordagain.getText().toString();
                if(username.equals("")||email.equals("")||password.equals("")||passwordagain.equals("")){
                    Toast.makeText(SignupActivity.this, "尚未輸入完成", Toast.LENGTH_SHORT).show();
                }
                else if (isValidEmail(email)){
                    Toast.makeText(SignupActivity.this, "輸入的Email 格式不正確", Toast.LENGTH_SHORT).show();

                }
                else if(!password.equals(passwordagain)){
                    Toast.makeText(SignupActivity.this, "再次輸入的密碼與前密碼不同", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SignupActivity.this, "上傳資料....", Toast.LENGTH_SHORT).show();
                    new CreateNewActivity().execute(email,username,password);
                }

            }
        });
    }
    class CreateNewActivity extends AsyncTask<String, String, String>{
        ProgressDialog pdLoading = new ProgressDialog(SignupActivity.this);
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
                url = new URL("http://140.128.88.166:8008/in.php");
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

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("email", strings[0])
                        .appendQueryParameter("username", strings[1])
                        .appendQueryParameter("password",strings[2]);
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
            Toast.makeText(SignupActivity.this, ""+result, Toast.LENGTH_SHORT).show();
            if(result.equalsIgnoreCase(" true"))
            {

                Toast.makeText(SignupActivity.this, "建立帳號成功  請去E-mail收取認證信", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this,Login.class);
                startActivity(intent);
                SignupActivity.this.finish();

            }else if (result.equalsIgnoreCase(" false")){


                Toast.makeText(SignupActivity.this, "你的E-mail有被註冊過", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") ) {

                Toast.makeText(SignupActivity.this, "連線異常 exception", Toast.LENGTH_LONG).show();

            }
            else if ( result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(SignupActivity.this, "連線異常 unsuccessful", Toast.LENGTH_LONG).show();

            }
        }
    }



    //Email 格式檢查程式------------------------------------------------------------------
    public static final Pattern EMAIL_PATTERN = Pattern
            .compile("^\\w+\\.*\\w+@(\\w+\\.){1,5}[a-zA-Z]{2,3}$");

    public static boolean isValidEmail(String email) {
        boolean result1 =true;//正確表示格式不對
        if (EMAIL_PATTERN.matcher(email).matches()) {
            result1 = false;
        }
        return result1;
    }
    //----------------------------------------------

}
