package tw.edu.ncut.login.event;

import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import tw.edu.ncut.login.myapplication.R;

/**
 * Created by aaa on 2018/5/20.
 */

public class ViewCursorAdapter extends BaseAdapter {
    //private String[] Data;  //二維存放的資料
    private Cursor cursor; //資料庫存放
    private LayoutInflater inflater;    //加載layout
    //初始化
    public ViewCursorAdapter(Cursor sqliteCalendarEventCursor, LayoutInflater inflater) {
        this.cursor =sqliteCalendarEventCursor;
        this.inflater =inflater;
    }


    //優化Listview 避免重新加載
    //這邊宣告你會動到的Item元件
    static class ViewHolder{//會更改的Item元鍵
        TextView tv_date,tv_color,tv_context;

    }


    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int i) {//return=id
        return i;
    }//getItemAtPosition

    @Override
    public long getItemId(int i) {//return=position

        return i ;}//getItemIdAtPosition(position)  讓他回傳 SQLite _id

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        ViewHolder holder;
        if(convertView==null){
            holder= new ViewHolder();
            convertView = inflater.inflate(R.layout.event_listview,null);
            holder.tv_date=convertView.findViewById(R.id.tv_date);
            holder.tv_color=convertView.findViewById(R.id.tv_color);
            holder.tv_context = convertView.findViewById(R.id.tv_context);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        //設定Layout值
        if(cursor.getCount()!=0) {
            cursor.moveToPosition(i);
            holder.tv_date.setText("活動時間"+cursor.getString(4) + "/" + cursor.getString(5) + "/" + cursor.getString(6)+"\n提醒時間:"+cursor.getString(7));
            switch (cursor.getString(2)) {
                case "colorRed":
                    holder.tv_color.setBackgroundColor(Color.rgb(239, 83, 80));
                    break;
                case "colorGreen":
                    holder.tv_color.setBackgroundColor(Color.rgb(102, 187, 106));
                    break;
                case "colorPurple":
                    holder.tv_color.setBackgroundColor(Color.rgb(186, 104, 200));
                    break;
                case "colorOrange":
                    holder.tv_color.setBackgroundColor(Color.rgb(255, 112, 67));
                    break;
            }
            holder.tv_context.setText(cursor.getString(1));
        }

        return convertView;
    }

}
