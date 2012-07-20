package com.jelastic.energy.zombie;

import android.view.Display;

public class Layout {

    protected static final int COLS = 5;
    protected static final int ROWS = 5;
    private Display display;
    private int width;
    private int height;

    public Layout(Display display) {
        this.width = display.getWidth();
        this.height = display.getHeight();
    }

    public int getTargetSize() {
        return (int) (getHeight() / 4.2f);
    }

    public int getOffsetX() {
        return (width - (5 * (getTargetSize() + getDeltaX()))) / 2;
    }

    public int getOffsetY() {
        return height - ((getTargetSize() + getDeltaY()) * 3) - (int) (height * .05);
    }

    public int getTextOffsetY() {
        return height / 80;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFontHeight() {
        return height / 10;
    }

    public int getDeltaX() {
        return width / 25;
    }

    public int getDeltaY() {
        return height / 50;
    }
}
