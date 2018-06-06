package tw.edu.ncut.login.event;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.myapplication.R;

public class EventActivity extends AppCompatActivity   {
    private ListView lv_event;
    private MyDBHelper helper;
    private Cursor sqliteCalendarEventCursor;
    private SharedPreferences sharedPreferences;
    private String myEmail,account;
    private int year,month,day,nowYear,nowMonth,nowDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        account = getMyEmail() + ".db";
        helper = new MyDBHelper(this, account, null, 1);
        //-------------拿BUNDLE中的Calendar選擇的日期
        Bundle bundle = getIntent().getExtras();
        nowYear =bundle.getInt("nowYear");//當天日期
        nowMonth=bundle.getInt("nowMonth");
        nowDay=bundle.getInt("nowDay");
        year =bundle.getInt("year");//選擇的事件紀錄日期
        month=bundle.getInt("month");
        day=bundle.getInt("day");
        //---------------------
        FloatingActionButton fab_new = (FloatingActionButton) findViewById(R.id.fab_new);
        fab_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //新增事件的條件: 不能低於當天日期
                if(nowYear<year||(nowYear==year&&nowMonth<month)||(nowYear==year&&nowMonth==month&&nowDay<=day)) {
                    FabOnClickEvent();
                }else{
                    //Toast.makeText(EventActivity.this, "now"+nowYear+"/"+nowMonth+"/"+nowDay+"select"+year+"/"+month+"/"+day, Toast.LENGTH_SHORT).show();
                    Toast.makeText(EventActivity.this, "這裡只提供記錄當天或未來的預計消費活動", Toast.LENGTH_SHORT).show();
                }



            }
        });
        //---------------------------------------------------------------------
        lv_event = (ListView) findViewById(R.id.lv_event);
        ListViewAdapter();
       lv_event.setOnItemClickListener(onClickListView);
    }

    private void FabOnClickEvent() {

        AlertDialog.Builder builder  = new AlertDialog.Builder(EventActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_new_event,null);
        final EditText ed_tital = view.findViewById(R.id.ed_tital);
        final TextView tv_date =view.findViewById(R.id.tv_date);
        final EditText ed_remider =view.findViewById(R.id.ed_remider);
        final EditText ed_context = view.findViewById(R.id.ed_context);
        final Button btn_cancel = view.findViewById(R.id.btn_eventdialog_cancel);
        final Button btn_ok =view.findViewById(R.id.btn_eventdialog_determine);
        //
        tv_date.setText(year+"/"+month+"/"+day);
        setDateStyle(ed_remider);
        //
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ed_tital.equals("")||ed_remider.equals("")||ed_context.equals("")){
                    Toast.makeText(EventActivity.this, "別忘記填寫消費活動名稱、提醒日期跟內容", Toast.LENGTH_SHORT).show();
                }else{
                    SetCalendarSpecialEvent(year,month,day,ed_remider.getText().toString(),ed_tital.getText().toString(), "colorPurple",ed_context.getText().toString());
                    ListViewAdapter();
                    dialog.dismiss();
                }

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent =new Intent();
        setResult(101,intent);
        super.onBackPressed();
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if(sqliteCalendarEventCursor.getCount()!=0) {

                    // Toast.makeText(EventActivity.this, "HI~"+lv_event.getItemIdAtPosition(position), Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(EventActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.dialog_listview_check_event, null);
                    final TextView tv_check_tital = mView.findViewById(R.id.tv_check_tital);
                    final TextView tv_check_date = mView.findViewById(R.id.tv_check_date);
                    final TextView tv_check_rimider = mView.findViewById(R.id.tv_check_rimider);
                    final TextView tv_check_context = mView.findViewById(R.id.tv_check_context);
                    final Button btn_check_delete = mView.findViewById(R.id.btn_check_listview_delete);
                    final Button btn_check_determine = mView.findViewById(R.id.btn_check_listview_determine);
                    //Toast.makeText(EventActivity.this, "test P" + position + "id" + id, Toast.LENGTH_SHORT).show();
                    sqliteCalendarEventCursor.moveToFirst();
                    sqliteCalendarEventCursor.move(position);//往下移position格  所以還是要置頂 在移
                    if (!sqliteCalendarEventCursor.isAfterLast()) {
                        tv_check_tital.setText(sqliteCalendarEventCursor.getString(1));
                        tv_check_date.setText(sqliteCalendarEventCursor.getString(4) + "/" + sqliteCalendarEventCursor.getString(5) + "/" + sqliteCalendarEventCursor.getString(6));
                        tv_check_rimider.setText(sqliteCalendarEventCursor.getString(7));
                        tv_check_context.setText(sqliteCalendarEventCursor.getString(3));
                    }
                    mBuilder.setView(mView);
                    final AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    btn_check_determine.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            dialog.dismiss();
                        }
                    });
                    btn_check_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sqliteCalendarEventCursor.moveToFirst();
                            sqliteCalendarEventCursor.move(position);//往下移position格  所以還是要置頂 在移
                            long id = helper.getWritableDatabase().delete("calendarEvent", "_ID=" + sqliteCalendarEventCursor.getInt(0), null);
//                            Log.d("xxxxxxxx", "" + id);
                            ListViewAdapter();
                            dialog.dismiss();
                        }
                    });
                }else {
                    Toast.makeText(EventActivity.this, "按右下角來新增消費活動", Toast.LENGTH_SHORT).show();
                }

            }



    };



    private void ListViewAdapter() {

        sqliteCalendarEventCursor = helper.getReadableDatabase().query("calendarEvent",          //table name
                 new String[]{"_id","title","color","eventContext","dateYear","dateMonth","dateDay","remiderDate"}, //column name 0 1 2    remiderDate: 年月日xxxx/xx/xx
                "dateYear = ? AND dateMonth = ? AND dateDay = ?",       //condition
                new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(day)},//condition value
                null, null, null);
        if(sqliteCalendarEventCursor.getCount()>0) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewCursorAdapter adapter = new ViewCursorAdapter(sqliteCalendarEventCursor, inflater);//ViewCursorAdapter 是我自己定的calss
            lv_event.setAdapter(adapter);
        }
        else{//如果還沒有活動
            String [] item =new String []{"尚未有消費活動"};
            ArrayAdapter adapter = new ArrayAdapter(this, R.layout.event_listview, R.id.tv_context,item);
            lv_event.setAdapter(adapter);
        }
    }



    //(7)myEmail
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }
    //Calendar Event  記特別節慶事項與內容
    public void SetCalendarSpecialEvent(int yearE, int monthE, int dayE, String remiderDate, String title, String color, String textContext){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(EventActivity.this, account, null, 1);
        ContentValues value = new ContentValues();
        //日期
        value.put("dateYear",yearE);
        value.put("dateMonth",monthE);
        value.put("dateDay",dayE);
        //提醒日期  格式: yyyy/mm/dd
        value.put("remiderDate",remiderDate);
        //內容
        value.put("title",title);
        value.put("color",color);
        value.put("eventContext",textContext);
        helper.getWritableDatabase().insert("calendarEvent",null,value);
        helper.close();

    }

//    setOnClickListener()和setOnFocusChangeListener()，如果不设置setOnFocusChangeListener()需要点击两次EditText控件，第一次获得焦点
// ，第二次点击才会触发setOnClickListener()。所以为了点击一次就能弹出日期选择框，需要设置setOnFocusChangeListener()
    public void setDateStyle(final EditText editText)  {
        editText.setInputType(InputType.TYPE_NULL);//不顯示系統輸鍵盤
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    //展示日期选择对话框
                    DatePickerDialog dpd = new DatePickerDialog(EventActivity.this, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // TODO Auto-generated method stub
                            editText.setText(year+"/"+(monthOfYear+1)+"/"+dayOfMonth);

                        }


                    }, year,month-1,day);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(year, month-1, day, 0, 0, 0);//設定他選擇事件日期
                    dpd.getDatePicker().setMaxDate(cal.getTimeInMillis());//不能超過事件日期
                    cal.set(nowYear, nowMonth-1, nowDay, 0, 0, 0);//設定現在日期
                    dpd.getDatePicker().setMinDate(cal.getTimeInMillis());//不能低於當天日期
                    dpd.show();
                }
            }

        });
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //展示日期选择对话框
                DatePickerDialog dpd =new DatePickerDialog(EventActivity.this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub
                        editText.setText(year+"/"+(monthOfYear+1)+"/"+dayOfMonth);

                    }
                }, year,month-1,day);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                cal.set(year, month-1, day, 0, 0, 0);//設定他選擇事件日期
                dpd.getDatePicker().setMaxDate(cal.getTimeInMillis());//不能超過事件日期
                cal.set(nowYear, nowMonth-1, nowDay, 0, 0, 0);//設定現在日期
                dpd.getDatePicker().setMinDate(cal.getTimeInMillis());//不能低於當天日期
                dpd.show();
            }
        });


    }


}
