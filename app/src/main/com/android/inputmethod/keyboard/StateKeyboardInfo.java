package com.android.inputmethod.keyboard;

public class StateKeyboardInfo {
    public int currentAutoCapsState;
    public int currentRecapitalizeState;

    public StateKeyboardInfo(int currentAutoCapsState, int currentRecapitalizeState) {
        this.currentAutoCapsState = currentAutoCapsState;
        this.currentRecapitalizeState = currentRecapitalizeState;
    }
}
