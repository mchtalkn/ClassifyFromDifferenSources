package com.example.classifyfromdifferensources;

public class FrameNumberListener {

    private int frameNumber = 0;
    private onValueChangeListener valueChangeListener;

    int getFrameNumber() {
        return frameNumber;
    }

    void setFrameNumber(int value) {
        frameNumber = value;
        if (valueChangeListener != null) valueChangeListener.onChange();
    }

    public onValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    void setValueChangeListener(onValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    public interface onValueChangeListener {
        void onChange();
    }

}
