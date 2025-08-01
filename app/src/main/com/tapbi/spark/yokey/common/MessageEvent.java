package com.tapbi.spark.yokey.common;



import java.util.HashMap;

public class MessageEvent {
    private int type = 0;
    private int extraAction= 1;
    private boolean extraBoolean= false;
    private int extraInt= -1;
    private int extraInteger=0;
    private String extraString= "";
    private HashMap<String,String> extraMapString = new HashMap<>();

    public MessageEvent(int type) {
        this.type = type;
    }

    public MessageEvent(int type, int extraAction) {
        this.type = type;
        this.extraAction = extraAction;
    }
    public MessageEvent(int type, boolean extraBoolean) {
        this.type = type;
        this.extraBoolean = extraBoolean;
    }
    public MessageEvent(int type, String extraString) {
        this.type = type;
        this.extraString = extraString;
    }

    public MessageEvent(int type, int extraAction, int extraInt) {
        this.type = type;
        this.extraAction = extraAction;
        this.extraInt = extraInt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HashMap<String, String> getExtraMapString() {
        return extraMapString;
    }

    public void setExtraMapString(HashMap<String, String> extraMapString) {
        this.extraMapString = extraMapString;
    }

    public int getExtraInt() {
        return extraInt;
    }

    public void setExtraInt(int extraInt) {
        this.extraInt = extraInt;
    }

    public int getExtraAction() {
        return extraAction;
    }

    public void setExtraAction(int extraAction) {
        this.extraAction = extraAction;
    }

    public boolean isExtraBoolean() {
        return extraBoolean;
    }

    public void setExtraBoolean(boolean extraBoolean) {
        this.extraBoolean = extraBoolean;
    }

    public String getExtraString() {
        return extraString;
    }

    public void setExtraString(String extraString) {
        this.extraString = extraString;
    }
}
