package com.jelastic.energy.zombie;

import android.view.Display;

public class Layout {

    protected static final int COLS = 5;
    protected static final int ROWS = 5;
    private Display display;

    public Layout(Display display) {
        this.display = display;
    }


    public int getTargetSize() {
        return -1;
    }

    public int getOffsetX() {
        return -1;
    }

    public int getOffsetY() {
        return -1;
    }

    public int getOffsetDeltaX() {
        return -1;
    }

    public int getOffsetDeltaY() {
        return -1;
    }
}
