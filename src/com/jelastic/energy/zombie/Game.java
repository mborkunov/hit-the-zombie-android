package com.jelastic.energy.zombie;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.util.MathUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

public class Game extends Observable {

    private static Game instance;

    private boolean started = false;
    private int score = 0;
    private int highscore = 0;

    private long startTime = 0;
    private List<Target> targets = new ArrayList<Target>(15);

    protected static final int COLS = 5;
    protected static final int ROWS = 3;

    private final static int ROUND_TIME = 10;

    private IUpdateHandler updateHandler = new IUpdateHandler() {

        private float elapsed = 0;
        @Override
        public void onUpdate(float pSecondsElapsed) {
            if (!isStarted()) {
                return;
            }

            //timerText.setText(" " + getTimeLeft() + " sec");

            elapsed += pSecondsElapsed;
            if (elapsed < .02) {
                return;
            } else {
                elapsed = 0;
            }
            List<Target> sleeping = getSleepTargets();
            if (!sleeping.isEmpty()) {
                for (Target target : sleeping) {
                    float probability = getProbability(target.front);

                    if (!target.front && ((new Date().getTime()) - target.stopTime) < 1000) {
                        continue;
                    }

                    if (Math.random() < probability) {
                        target.rotate(MathUtils.random(0, 5));
                    }
                }
            }

            if (getTimeLeft() <= 0) {
                setStarted(false);
                setChanged();
                notifyObservers();
            }
        }

        @Override
        public void reset() {
        }
    };

    private Game() {
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void start() {
        if (started) return;
        setStarted(true);
        startTime = new Date().getTime();
        notifyObservers();
    }

    public void reset() {
        score = 0;
        startTime = -1;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void addTarget(Target target) {
        targets.add(target);
    }

    public IUpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    private List<Target> getSleepTargets() {
        List<Target> res = new ArrayList<Target>();
        for (Target target : targets) {
            if (!target.isRotating()) {
                res.add(target);
            }
        }
        return res;
    }

    protected int getProgress() {
        if (startTime == 0) return 0;
        return (int) Math.abs(100 - getTimeLeft() * 100f / ROUND_TIME);
    }

    protected int getTimeLeft() {
        if (!isStarted()) return ROUND_TIME;
        return ROUND_TIME - (int) (System.currentTimeMillis() - startTime) / 1000;
    }

    private float getProbability(boolean front) {
        float progressExtra = 0.05f * getProgress() / 100;
        return (front ? 0.005f : .0007f) + progressExtra;
    }


}
