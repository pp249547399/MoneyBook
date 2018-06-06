package tw.edu.ncut.login.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tw.edu.ncut.login.myapplication.R;

/**
 * Created by aaa on 2018/3/2.
 */

public class SettingAdapter extends ArrayAdapter {

    public static final int Type_switch=0;
    public static  final  int Type_Text =1;
    private  int resourceId;
    private ArrayList<Settinglistview> objects  = null;
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    @Override
    public int getItemViewType(int position) {
        Settinglistview settinglistview = objects.get(position);
        if(settinglistview.getType()==Type_switch){
           return Type_switch;
        }else if (settinglistview.getType() == Type_Text){
            return Type_Text;
        }
        return Type_Text;
    }

    public SettingAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Settinglistview> objects) {
        super(context, resource, objects);
        this.objects = (ArrayList<Settinglistview>) objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Settinglistview settinglistview = objects.get(position);

        int listViewItemType = getItemViewType(position);

            if(listViewItemType == Type_switch) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.setting_listview_switch,null);
                Switch switchT = (Switch) convertView.findViewById(R.id.setting_switch);
                ImageView image = (ImageView) convertView.findViewById(R.id.switch_image);
                //
                switchT.setChecked(getBoolSW(position));
                switchT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        setSwitchRf(position,String.valueOf(b));
                    }
                });
                //

                switchT.setText(settinglistview.getText());
                //
                image.setImageResource(settinglistview.getImageId());
                return convertView;
            }
            else{
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.setting_listview_text,null);
                TextView text = (TextView) convertView.findViewById(R.id.setting_text);
                ImageView image = (ImageView) convertView.findViewById(R.id.text_image);
                text.setText(settinglistview.getText());
                image.setImageResource(settinglistview.getImageId());
                return convertView;
            }
    }
    //switch初始值
    SharedPreferences sharedPreferences;
    boolean spBooleanSwitch;

    private boolean getBoolSW(int swSelect) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (swSelect == 0) {
            spBooleanSwitch = Boolean.valueOf(sharedPreferences.getString("switchBooleanRa", ""));
        }
        else if(swSelect==1) {
            spBooleanSwitch = Boolean.valueOf(sharedPreferences.getString("switchBooleanSp", ""));
        }
        return spBooleanSwitch;
    }
    public void setSwitchRf(int swSelect,String b) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (swSelect == 0) {
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
}
