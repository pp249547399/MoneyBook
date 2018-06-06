package tw.edu.ncut.login.setting;

/**
 * Created by aaa on 2018/3/2.
 */

public class Settinglistview {
    private String text;
    private int type;
    private int image;



    public Settinglistview(int image,String text, int type) {

        this.text = text;
        this.type = type;
        this.image =image;
    }
    public int getImageId(){
        return  image;
    }
    public String getText() {
        return text;
    }


    public int getType(){
        return type;
    }


}
