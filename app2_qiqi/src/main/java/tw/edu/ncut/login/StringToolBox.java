package tw.edu.ncut.login;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by aaa on 2018/3/30.
 */

public class StringToolBox {
    //讓文字左右分離加底線(未完成)
    public static String setLRUnderLineTextView(String L, String R, TextView tv){
        //需要設定textview的寬度不能用match_parent
        TextPaint paint=tv.getPaint();
        tv.measure(0, 0);
        //可以真加 paddingLeft or Right 5dp 以免被吃字
        int width = tv.getMeasuredWidth()-tv.getPaddingLeft()-tv.getPaddingRight();//總寬度減掉左右邊界
        float widthR = paint.measureText(R);//取得String寬
        float widthL = paint.measureText(L);
        float spaceWidth = paint.measureText(" ");
        int spaceCount =(int) ((width-widthR-widthL)/spaceWidth);//算出中間要補幾個SPACE
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(L);
        for(int i =0;i<spaceCount;i++){
            stringBuilder.append(" ");
        }
        stringBuilder.append(R);
//        Log.d("ddfdfdfd","width:"+width+"  widthR:"+widthR+"  widthL:"+widthL+"\n  spacewidth:"+spaceWidth+"  spaceCount"+spaceCount
//                +"\n"+stringBuilder.toString());

        SpannableString content = new SpannableString(stringBuilder.toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//加底線
        Log.d("fdfddf", ""+content);
        return String.valueOf(content);

    }
    public static String setLRTextView(String L, String R, TextView tv){//左右文字分開
        TextPaint paint=tv.getPaint();
        tv.measure(0, 0);
        //可以真加 paddingLeft or Right 5dp 以免被吃字
        int width = tv.getMeasuredWidth()-tv.getPaddingLeft()-tv.getPaddingRight();//總寬度減掉左右邊界

        float widthR = paint.measureText(R);//取得String寬
        float widthL = paint.measureText(L);
        float spaceWidth = paint.measureText(" ");
        int spaceCount =(int) ((width-widthR-widthL)/spaceWidth);//算出中間要補幾個SPACE
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(L);
        for(int i =0;i<spaceCount;i++){
            stringBuilder.append(" ");
        }
        stringBuilder.append(R);
//        Log.d("ddfdfdfd","width:"+width+"  widthR:"+widthR+"  widthL:"+widthL+"\n  spacewidth:"+spaceWidth+"  spaceCount"+spaceCount
//                +"\n"+stringBuilder.toString());
        return stringBuilder.toString();

    }
}
