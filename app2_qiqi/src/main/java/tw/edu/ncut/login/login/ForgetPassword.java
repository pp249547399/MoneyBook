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
import android.widget.TextView;
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

public class ForgetPassword extends AppCompatActivity {
    private Button btn_forget ;
    private TextView ed_email;
    public static final int CONNECTION_TIMEOUT=50000;
    public static final int READ_TIMEOUT=50000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        btn_forget = (Button) findViewById(R.id.btn_forget);
        ed_email = (EditText) findViewById(R.id.ed_email);

        btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = ed_email.getText().toString();
                if(isValidEmail(email)){
                    Toast.makeText(ForgetPassword.this, "你的E-mail格式輸入錯誤", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ForgetPassword.this, "上傳資料....", Toast.LENGTH_SHORT).show();
                    new ForgetPassword.CreateNewActivity().execute(email);
                }
            }
        });

    }
    class CreateNewActivity extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(ForgetPassword.this);
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
                url = new URL("http://140.128.88.166:8008/email.php");
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
                        .appendQueryParameter("email", strings[0]);

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
            Toast.makeText(ForgetPassword.this, ""+result, Toast.LENGTH_SHORT).show();
            if(result.equalsIgnoreCase(" true"))
            {

                Toast.makeText(ForgetPassword.this, "請去E-mail收取認證信", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgetPassword.this,Login.class);
                startActivity(intent);
                ForgetPassword.this.finish();

            }else if (result.equalsIgnoreCase(" false")){


                Toast.makeText(ForgetPassword.this, "你的E-mail沒有被註冊過", Toast.LENGTH_LONG).show();

            } else if (result.equalsIgnoreCase("exception") ) {

                Toast.makeText(ForgetPassword.this, "連線異常 exception", Toast.LENGTH_LONG).show();

            }
            else if ( result.equalsIgnoreCase("unsuccessful")) {

                Toast.makeText(ForgetPassword.this, "連線異常 unsuccessful", Toast.LENGTH_LONG).show();

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
