package tw.edu.ncut.login.calendar;

import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user on 2018/3/26.
 */

public class CalendarPicker {///尚未完成
    private Calendar calender;
    private int year,month,day;
    private TextView calendartextView;
    private CompactCalendarView compactCalendarView;//calendarView
    private SimpleDateFormat dateFormatForMonth =
            new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());

    //Cpmpect

    //Calendar日期選擇
    public void CalendarPicker(){
        calender = Calendar.getInstance();
        year = calender.get(Calendar.YEAR);
        month = calender.get(Calendar.MONTH)+1;
        day = calender.get(Calendar.DAY_OF_MONTH);
        calendartextView.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                String d[]=dateClicked.toString().split(" "); // 0:星期幾 1: 月份  2: 日期.....
                day= Integer.valueOf(d[2]);
                //刷新ListView
               // AddedFragment.refreshListView();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                // Changes toolbar title on monthChange
                calendartextView.setText(dateFormatForMonth.format(firstDayOfNewMonth));
                String d[]=firstDayOfNewMonth.toString().split(" ");
                int m = 1;
                switch (d[1]){
                    case"Jan": m=1;break;
                    case"Feb": m=2;break;
                    case"Mar": m=3;break;
                    case"Apr": m=4;break;
                    case"May": m=5;break;
                    case"Jun": m=6;break;
                    case"Jul": m=7;break;
                    case"Aug": m=8;break;
                    case"Sep": m=9;break;
                    case"Oct": m=10;break;
                    case"Nov": m=11;break;
                    case"Dec": m=12;break;
                }
                month=m;
                day= Integer.valueOf(d[2]);
                year=Integer.valueOf(d[5]);

                //AddedFragment.refreshListView();
            }

        });
    }


    //(5)Calendar 有資料的做標記

}

