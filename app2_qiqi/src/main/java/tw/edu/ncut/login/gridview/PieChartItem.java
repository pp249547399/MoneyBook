package tw.edu.ncut.login.gridview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.PercentFormatter;

import tw.edu.ncut.login.myapplication.R;


/**
 * Created by us on 2018/4/1.
 */

public class PieChartItem extends ChartItem {

    private Typeface mTf;
    private SpannableString mCenterText;
    private String centerText,money,type;

    public PieChartItem(ChartData<?> cd,Context c, String centerText, String money, String type) {
        super(cd);
        this.centerText = centerText;
        this.money = money;
        this.type = type;
        //mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
        //mCenterText = generateCenterText();
    }

    @Override
    public int getItemType() {
        return TYPE_PIECHART;
    }

    @Override
    public View getView(int position, View convertView, Context c) {
        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.adapter_chart, null);
            holder.chart = (PieChart) convertView.findViewById(R.id.grid_pie);
            holder.money = convertView.findViewById(R.id.grid_money);
            holder.type = convertView.findViewById(R.id.grid_type);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //類別&金額
        holder.money.setText(money);
        holder.type.setText(type);
        holder.money.setTextColor(Color.BLACK);
        holder.type.setTextColor(Color.BLACK);


        // apply styling==>piechart設定
        holder.chart.getDescription().setEnabled(false);
        holder.chart.setHoleRadius(52f);
        holder.chart.setTransparentCircleRadius(57f);
        holder.chart.setCenterText(centerText);
        holder.chart.setCenterTextTypeface(mTf);
        holder.chart.setCenterTextSize(12f);
        holder.chart.setUsePercentValues(true);
        holder.chart.setRotationEnabled(false); //手動旋轉
        holder.chart.setTouchEnabled(false);// 按下圖表
        holder.chart.setDrawSliceText(false);//不要顯示X在表裡
        holder.chart.setHoleColor(Color.alpha(0));
        //holder.chart.setExtraOffsets(5, 10, 50, 10);

        mChartData.setValueFormatter(new PercentFormatter());
        //mChartData.setValueTypeface(mTf);
        mChartData.setValueTextSize(11f);
        mChartData.setValueTextColor(Color.WHITE);
        // set data
        holder.chart.setData((PieData) mChartData);

        holder.chart.getLegend().setEnabled(false);
        //legend==>關閉=============下面沒用
        Legend l = holder.chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateY(900);

        return convertView;
    }
//    private SpannableString generateCenterText() {
//        SpannableString s = new SpannableString("MPAndroidChart\ncreated by\nPhilipp Jahoda");
//        s.setSpan(new RelativeSizeSpan(1.6f), 0, 14, 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.VORDIPLOM_COLORS[0]), 0, 14, 0);
//        s.setSpan(new RelativeSizeSpan(.9f), 14, 25, 0);
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, 25, 0);
//        s.setSpan(new RelativeSizeSpan(1.4f), 25, s.length(), 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), 25, s.length(), 0);
//        return s;
//    }

    private static class ViewHolder {
        PieChart chart;
        TextView money,type;
    }
}
