package tw.edu.ncut.login;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import tw.edu.ncut.login.chart.ChartFragment;
import tw.edu.ncut.login.group.GroupFragment;
import tw.edu.ncut.login.setting.SettingFragment;

/**
 * Created by suraj on 23/6/17.
 */

public class Pager extends FragmentStatePagerAdapter {

    int tabCount;

    public Pager(FragmentManager fm, int tabCount){
        super(fm);
        this.tabCount=tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0 :
                AddedFragment addedFragment =new AddedFragment();
                return addedFragment;
            case 1 :
                ChartFragment chartFragment =new ChartFragment();
                return chartFragment;
            case 2 :
                SettingFragment settingFragment=new SettingFragment();
                return settingFragment;
            case 3 :
                GroupFragment groupFragment=new GroupFragment();
                return groupFragment;
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

}
