package tw.edu.ncut.login.chart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tw.edu.ncut.login.database.MyDBHelper;
import tw.edu.ncut.login.gridview.ChartItem;
import tw.edu.ncut.login.gridview.PieChartItem;
import tw.edu.ncut.login.myapplication.R;

/**
 * Created by suraj on 23/6/17.
 */

public class ChartFragment extends Fragment {
    //
    int x = 11;
    private Spinner spinner;
    //
    private GridView gridView;
    //----------------------偏好儲存---------------------------------------
    private SharedPreferences sharedPreferences;
    private String myEmail,account;
    //---------------------------日期----------------------------------------
    private Button btn_year;
    private Calendar calender;
    private int year,month,day;
    //-------------------------分析圖---------------------------------------
    private float[] yDataRight = {0,0,0,0,0,0,0,0,0,0,0};//{}
    private String[] xDataRight = {"食","衣","住","行","育","樂","人","財","健","雜","3C"};
    private View pieChartViewRight, barChartViewRight, lineChartViewRight;
    private PieChart  pieChartRight;
    private BarChart  barChartRight;
    private LineChart lineChartRight;
    private Cursor chartData ;
    private FrameLayout  fl_right;
    private ListView  lv_right;
    private int sort = 0;
    final int[] VORDIPLOM_COLORS = {
            Color.rgb(192, 255, 140), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140),
            Color.rgb(140, 234, 255), Color.rgb(255, 140, 157), Color.rgb(194, 223, 255),
            Color.rgb(214, 255, 238),Color.rgb(255, 255, 204), Color.rgb(255, 218, 199),
            Color.rgb(180, 217, 217),Color.rgb(224, 224, 224),
    };
    //-------------------------資料庫---------------------------------------
    private MyDBHelper helper;
    //private SimpleCursorAdapter adapter_sqlRight;
    private Cursor sqlData,detailData;
    private String sql = "";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chart, container, false);
        fl_right = view.findViewById(R.id.fl_right);
        //pie chart
        pieChartViewRight = inflater.inflate(R.layout.chart_pie,null,false);
        pieChartRight = pieChartViewRight.findViewById(R.id.pieChart);
        //barchart
        barChartViewRight = inflater.inflate(R.layout.chart_bar,null,false);
        barChartRight = barChartViewRight.findViewById(R.id.barChart);
        //line chart
        lineChartViewRight = inflater.inflate(R.layout.chart_line,null,false);
        lineChartRight = lineChartViewRight.findViewById(R.id.lineChart);
        //
        btn_year = view.findViewById(R.id.btn_year);
        spinner = view.findViewById(R.id.spinner);
        //
        gridView = view.findViewById(R.id.gv);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);//資料庫名字,標準模式,版本
        //pie
        setPieChart(pieChartRight,yDataRight,xDataRight);
        //bar
        setBarChart(barChartRight,yDataRight,xDataRight);
        //line
        setLineChart(lineChartRight,yDataRight,xDataRight);

        datPicker();
        //seeItem();
        spinner();
        //
        String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
        setGrid(gridSQL);
    }

    //(1)圓餅圖設計
    public void setPieChart(final PieChart pieChart, final float []yData, final String []xData){
        pieChart.getDescription().setText("");
        pieChart.getDescription().setTextSize(12);
        pieChart.setHoleRadius(25f);//半徑
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setCenterText(" ");
        //按下圓餅圖
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                sql = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
                addDataSetPie(pieChart, yData, xData);
                x = (int)h.getX();
                int n[]={0,0,0,0,0,0,0,0,0,0,0};
                int k=0;
                for(int i=0;i<11;i++){
                    for(int j=k;j<11;j++){
                        if(yData[j]!=0){
                            n[i]=j;
                            k=j+1;
                            break;
                        }
                    }
                }
                x=n[x];
                String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ? AND smallCategory = ?";
                setGrid(gridSQL);
            }

            @Override
            public void onNothingSelected() {
                btn_year.setText(year+"年"+month+"月");
                setPieChart(pieChart, yData, xData);
                x=11;
                String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
                setGrid(gridSQL);
            }
        });
        //add data
        addDataSetPie(pieChart, yData, xData);
        //legend
        pieChart.getLegend().setEnabled(false);
        Legend legend = pieChart.getLegend();
        legend.setXEntrySpace(7);
        legend.setYEntrySpace(5);
    }
    //(2)圓餅圖資料設定
    public void addDataSetPie(PieChart pieChart, float []yData, String []xData) {
        Log.d("set","addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        //資料設置
        if (yData.length == 11) {
            refreshChartDataRight(yData);
        }
        for(int i=0; i<yData.length; i++){
            if(yData[i]!=0) {
                Log.d("xxx",i+","+yData[i]);
                yEntrys.add(new PieEntry(yData[i], xData[i % xData.length]));
            }
        }
        //
        PieDataSet pieDataSet = new PieDataSet(yEntrys,"");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(14);
        //圓餅顏色
        pieDataSet.setColors(VORDIPLOM_COLORS);
        pieDataSet.setDrawValues(false);
        //刷新
        PieData pieData = new PieData( pieDataSet);//xEntrys
        pieData.setValueFormatter(new MyValueFormatter());
        pieChart.setUsePercentValues(true);
        pieChart.setData(pieData);
        pieChart.invalidate();
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(14);
        pieChart.setHoleColor(Color.alpha(0));
    }
    //pieChart x +%
    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value) + " %"; // e.g. append a dollar-sign
        }
    }
    //(4)Right刷新分析圖資料
    public void refreshChartDataRight(float []yData){
        account = getMyEmail()+".db";
        helper = new MyDBHelper(getActivity(), account, null, 1);//資料庫名字,標準模式,版本
        for(int i=0;i<11;i++) {
            chartData = helper.getReadableDatabase().query("rubyLin", new String[]{"_id", "itemMoney"}, "itemCategory = ? AND dateYear = ? AND dateMonth = ?", new String[]{String.valueOf(i), String.valueOf(year), String.valueOf(month)}, null, null, null);
            int id, pie = 0;
            yData[i] = 0;
            if (chartData.getCount() > 0) {
                while (chartData.moveToNext()) {
                    id = chartData.getInt(0);
                    pie += chartData.getInt(1);
                    Log.d("id", id + "," + pie);
                    switch (i) {
                        case 0:yData[0] = pie;break;
                        case 1:yData[1] = pie;break;
                        case 2:yData[2] = pie;break;
                        case 3:yData[3] = pie;break;
                        case 4:yData[4] = pie;break;
                        case 5:yData[5] = pie;break;
                        case 6:yData[6] = pie;break;
                        case 7:yData[7] = pie;break;
                        case 8:yData[8] = pie;break;
                        case 9:yData[9] = pie;break;
                        case 10:yData[10] = pie;break;
                    }
                }
            }
        }
        helper.close();
    }
    //(5)月份選擇
    public void datPicker(){
        calender = Calendar.getInstance();
        year = calender.get(Calendar.YEAR);
        month = calender.get(Calendar.MONTH)+1;
        day = calender.get(Calendar.DAY_OF_MONTH);
        if(year==0 && month==0 && day==0) {
            btn_year.setText("請選擇年月份");
        }else {
            btn_year.setText(year + "年" + month + "月");
        }
        btn_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == btn_year) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                            AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            year = i;
                            month = i1 + 1;
                            day = i2;
                            btn_year.setText(year + "年" + month + "月");
                            //刷新ListView and PieChart
                            sql = "dateYear = ? AND dateMonth = ?  OR itemCategory = ?";
                            x=11;//set itemCategory
                            //griddd
                            String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
                            setGrid(gridSQL);
                            //
                            setPieChart(pieChartRight, yDataRight, xDataRight);
                            setBarChart(barChartRight,yDataRight,xDataRight);
                            setLineChart(lineChartRight,yDataRight,xDataRight);
                        }
                    }, year, month - 1, day);
                    //day隱藏
                    ( datePickerDialog.getDatePicker()).findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
                    datePickerDialog.show();

                }
            }
        });
    }
    //(6)查看明細
    public void seeItem(){
        //piechart
        lv_right.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final long itemId = lv_right.getItemIdAtPosition(i);
            Log.d("ID",""+itemId);
            detailData = helper.getReadableDatabase()
                    .query("rubyLin", new String[]{"_id", "dateYear", "dateMonth", "dateDay", "itemName", "itemMoney","itemCategory","smallCategory"}
                            , "_id = ?", new String[]{String.valueOf(itemId)}, null, null, null);

            android.support.v7.app.AlertDialog.Builder adb = new android.support.v7.app.AlertDialog.Builder(getActivity());//abd用來警示視窗

            int deYear = 0,deMonth = 0 , deDay = 0 , deMoney = 0,deitemCategory =0 ,deSmallCategory=0;
            String deName = "",nameCategory ="";

            if(detailData.getCount()>0) {
                while (detailData.moveToNext()) {
                    deYear = detailData.getInt(1);
                    deMonth = detailData.getInt(2);
                    deDay = detailData.getInt(3);
                    deName = detailData.getString(4);
                    deMoney = detailData.getInt(5);
                    deitemCategory = detailData.getInt(6);
                    deSmallCategory = detailData.getInt(7);
                }
                Cursor chartData  =  helper.getReadableDatabase()
                        .query("categoryTable", new String[]{"_id", "categoryName"},
                                "itemCategory = ? AND smallCategory = ? ",
                                new String[]{String.valueOf(deitemCategory), String.valueOf(deSmallCategory)}, null, null, null);
                while (chartData.moveToNext()) {
                    int id = chartData.getInt(0);
                    nameCategory = chartData.getString(1);
                }
                adb.setTitle("明細");
                adb.setMessage("時間: " + deYear + "年" + deMonth + "月" + deDay + "日\n" +
                        "項目: "+nameCategory+ "\n" +
                        "內容: " + deName + "\n" +
                        "金額: " + deMoney);
                adb.setNegativeButton("刪除", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        long id = helper.getWritableDatabase().delete("rubyLin","_ID="+itemId,null);
                        Log.d("del",""+id);
                        //griddd
                        String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
                        setGrid(gridSQL);
                        //
                        setPieChart(pieChartRight, yDataRight, xDataRight);
                        setBarChart(barChartRight,yDataRight,xDataRight);
                        setLineChart(lineChartRight,yDataRight,xDataRight);
                    }
                });
                adb.setPositiveButton("確定",null);
                adb.show();
            }
        }
    });

}
    //(7)spinner
    public void spinner (){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String charType = "";
                switch (i){
                    case 0:
                        fl_right.removeAllViews();
                        fl_right.addView(pieChartViewRight);
                        charType="pieChart";
                        setPieChart(pieChartRight, yDataRight, xDataRight);
                        if(year==0 && month==0){
                            year = calender.get(Calendar.YEAR);
                            month = calender.get(Calendar.MONTH)+1;
                        }
                        break;
                    case 1:
                        fl_right.removeAllViews();
                        fl_right.addView(barChartViewRight);
                        charType="barChart";
                        setBarChart(barChartRight,yDataRight,xDataRight);
                        if(year==0 && month==0){
                            year = calender.get(Calendar.YEAR);
                            month = calender.get(Calendar.MONTH)+1;
                        }
                        break;
                    case 2:
                        fl_right.removeAllViews();
                        fl_right.addView(lineChartViewRight);
                        charType="lineChart";
                        setLineChart(lineChartRight,yDataRight,xDataRight);
                        if(year==0 && month==0){
                            year = calender.get(Calendar.YEAR);
                            month = calender.get(Calendar.MONTH)+1;
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    //(8)長條圖設定
    public void setBarChart(final BarChart barChart, final float []yData, final String []xData){
        final float [] barData = {0,0,0,0,0,0,0,0,0,0,0};
        //x軸值名稱.間格設定
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(10);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xData));
        //y軸值設定
        barChart.getAxisRight().setEnabled(false);
        if (yData.length == 11) {
            refreshChartDataRight(yData);
        }
        for(int i=0;i<yData.length;i++){
            if(yData[i]!=0){
                barData[i]=yData[i];
            }
        }
        ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i=0;i<11;i++){
            entries.add(new BarEntry(i,barData[i]));
        }
        //
        barChart.getDescription().setText(" ");
        BarDataSet set = new BarDataSet(entries,"Bar");
        BarData data = new BarData(set);
        data.setBarWidth(0.7F);
        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.invalidate();

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                setBarChart(barChart,yData,xData);
                final String xData = barChart.getXAxis().getValueFormatter().getFormattedValue(e.getX(), barChart.getXAxis());
                onValueSelect(h,yData);

            }

            @Override
            public void onNothingSelected() {
                sql = "dateYear = ? AND dateMonth = ?  OR itemCategory = ?";
                btn_year.setText(year+"年"+month+"月");
                setBarChart(barChart,yData,xData);
                //griddd
                x=11;
                String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
                setGrid(gridSQL);
            }
        });
    }
    //(9)折線圖設定
    private void setLineChart(final LineChart lineChart, final float []yData, final String []xData) {
        final float [] lineData = {0,0,0,0,0,0,0,0,0,0,0};
        //x軸值名稱.間格設定
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(10);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xData));
        //y軸值設定
        lineChart.getAxisRight().setEnabled(false);
        if (yData.length == 11) {
            refreshChartDataRight(yData);
        }
        for(int i=0;i<yData.length;i++){
            if(yData[i]!=0){
                lineData[i]=yData[i];
            }
        }
        ArrayList<Entry> entries = new ArrayList<>();
        for(int i=0;i<11;i++){
            entries.add(new Entry(i,lineData[i]));
        }
        //
        lineChart.getDescription().setText(" ");
        LineDataSet set = new LineDataSet(entries,"Line");
        LineData data = new LineData(set);
        lineChart.setData(data);
        lineChart.invalidate();

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                setLineChart(lineChart,yData,xData);
                final String xData = lineChart.getXAxis().getValueFormatter().getFormattedValue(e.getX(), lineChart.getXAxis());
                onValueSelect(h,yData);
            }

            @Override
            public void onNothingSelected() {
                sql = "dateYear = ? AND dateMonth = ?  OR itemCategory = ?";
                btn_year.setText(year+"年"+month+"月");
                setLineChart(lineChart,yData,xData);
                //griddd
                x=11;
                String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
                setGrid(gridSQL);
            }
        });
    }
    //onValueSelect
    private void onValueSelect(Highlight h,float []yData){
        sql = "dateYear = ? AND dateMonth = ?  AND itemCategory = ?";
        x = (int)h.getX();
        Toast.makeText(getActivity(), "X:"+x+" -"+ yData[x], Toast.LENGTH_SHORT).show();
//        int n[]={0,0,0,0,0,0,0,0,0,0,0};
//        int k=0;
//        for(int i=0;i<11;i++){
//            for(int j=k;j<11;j++){
//                if(yData[j]!=0){
//                    n[i]=j;
//                    k=j+1;
//                    break;
//                }
//            }
//        }
//        x=n[x];
        Toast.makeText(getActivity(), "n[x]"+x, Toast.LENGTH_SHORT).show();
        String gridSQL = "dateYear = ? AND dateMonth = ?  AND itemCategory = ? AND smallCategory = ?";
        setGrid(gridSQL);

//        switch (xData){
//            case "食":x=0;refreshListViewRight();break;
//            case "衣":x=1;refreshListViewRight();break;
//            case "住":x=2;refreshListViewRight();break;
//            case "行":x=3;refreshListViewRight();break;
//            case "育":x=4;refreshListViewRight();break;
//            case "樂":x=5;refreshListViewRight();break;
//            case "人":x=6;refreshListViewRight();break;
//            case "財":x=7;refreshListViewRight();break;
//            case "健":x=8;refreshListViewRight();break;
//            case "雜":x=9;refreshListViewRight();break;
//            case "3C":x=10;refreshListViewRight();break;
//        }
    }
    //myEmail
    private String getMyEmail(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        myEmail = sharedPreferences.getString("email", "");
        return myEmail;
    }





    //set gridview
    private void setGrid(String sql){
        ArrayList<ChartItem>list = new ArrayList<>();
        float []centerText = new float[yDataRight.length];//類別百分比==>正中間
        final int [] pieMoney = new int [yDataRight.length];//類別金額==>底部
        final String [] pieType = new String[xDataRight.length];//類別名稱==>中下
        float total = 0;
        for(int i=0 ;i<yDataRight.length;i++) {
            int count = 0;
            String[] sqlValue = null;
            if (sql == "dateYear = ? AND dateMonth = ?  AND itemCategory = ?") {
                sqlValue = new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(i)};
            }else if (sql == "dateYear = ? AND dateMonth = ?  AND itemCategory = ? AND smallCategory = ?"){
                sqlValue = new String[]{String.valueOf(year), String.valueOf(month), String.valueOf(x), String.valueOf(i)};
            }
            sqlData = helper.getReadableDatabase().query("rubyLin",
                    new String[]{"_id", "itemType", "itemName", "itemMoney", "categoryName", "smallCategory"},
                    sql, sqlValue, null, null, null);

            sqlData.moveToFirst();
            while (!sqlData.isAfterLast()) {
                pieMoney[i] += Integer.parseInt(sqlData.getString(3));
                pieType[i] = xDataRight[i];
                //大類別
                if (sql.equals("dateYear = ? AND dateMonth = ?  AND itemCategory = ?")) {
                    pieType[i] = xDataRight[i];
                // 小類別
                }else if (sql.equals("dateYear = ? AND dateMonth = ?  AND itemCategory = ? AND smallCategory = ?")){
                    pieType[i] = sqlData.getString(4);
                }
                count++;
                sqlData.moveToNext();
            }
            total += pieMoney[i];
        }
        //百分比
        for (int i=0;i<yDataRight.length;i++){
            centerText[i] = (float)Math.round (pieMoney[i]/total*1000)/10;
        }
        final int smallcate[] =new int [yDataRight.length];
        int sc = 0;
        for(int i=0;i< yDataRight.length;i++) {
            if  (pieMoney[i]!=0) {
                smallcate[sc] = i;
                //chartData(不顯示), context, centerText, money, type//名稱, (金額, 總額), 第幾項
                list.add(new PieChartItem(generateDataPie(pieType[i], new float[]{pieMoney[i], total}, i),
                        getActivity(), centerText[i] + "%", "$" + pieMoney[i], pieType[i]));
                sc++;
            }
        }
        ChartDataAdapter cda = new ChartDataAdapter(getActivity(), list);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //x==>大類別 smallcate==>小類別
                Toast.makeText(getActivity(), "x"+x+" i"+i+" sm"+ smallcate[i], Toast.LENGTH_SHORT).show();
                ad(x,smallcate[i]).show();
            }
        });
        gridView.setAdapter(cda);
    }

    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        public ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            return getItem(position).getItemType();
        }

        @Override
        public int getViewTypeCount() {
            return 3; // we have 3 different item-types
        }
    }

    private PieData generateDataPie(String type,float[] data, int n) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        float [] a = data;
        if (data[0]==data[1]){
            entries.add(new PieEntry(a[1], type));
        }else {
            for (int i = 0; i < 2; i++) {
                entries.add(new PieEntry(a[i], type));//類別&全部 ex:食10元  TOTAL:100元 ==>10/100==>10%
            }
        }

        PieDataSet d = new PieDataSet(entries, "");

        // space between slices
        d.setSliceSpace(2f);
        //顏色
        for (int i=0; i<=n ; i++){
            d.setColors(VORDIPLOM_COLORS[i],Color.WHITE);
        }
        d.setDrawValues(false);//圖表裡文字

        PieData cd = new PieData(d);
        return cd;
    }


    private AlertDialog.Builder ad(int bigcate, int smallcate){
        int bigc = bigcate ;//xdata
        int smallc = smallcate;//smallCategory==>sqlite
        String sql = ""; String []sql2 ; String cate = "";String cate2 = "";
        if (bigc==11){
            sql = "itemCategory = ? ";
            sql2 = new String[]{String.valueOf(smallc)};
        }else {
            sql = "itemCategory = ? AND smallCategory = ?";
            sql2 = new String[]{String.valueOf(bigc), String.valueOf(smallc)};
        }
        sqlData = helper.getReadableDatabase().query("rubyLin", new String[]{
                "_id", "dateYear","dateMonth","dateDay","itemType",
                "itemName", "itemMoney", "categoryName", "smallCategory"}, sql,sql2, null, null, null);

        final SimpleCursorAdapter sca = new SimpleCursorAdapter(
                getActivity(), R.layout.adapter_chart_list,sqlData,
                new String[]{"itemType","itemName","itemMoney","dateYear","dateMonth","dateDay"},
                new int[]{R.id.iv_Type, R.id.tv_item, R.id.tv_money,R.id.tv_year,R.id.tv_month,R.id.tv_day},0);



        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder
                .setTitle("項目:")
                .setNegativeButton("OK",null)
                .setAdapter(sca, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder;
    }
}