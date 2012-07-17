package com.jelastic.energy.zombie;

import android.content.SharedPreferences;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.text.Text;
import org.andengine.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;

public class Game extends Observable {

    protected static Game instance;

    private boolean started = false;
    private int score = 0;
    private int highscore = 0;

    private long startTime = 0;
    private List<Target> targets = new ArrayList<Target>(15);

    protected static final int COLS = 5;
    protected static final int ROWS = 3;

    private final static int ROUND_TIME = 60;

    private IUpdateHandler updateHandler = new IUpdateHandler() {

        private float elapsed = 0;
        @Override
        public void onUpdate(float pSecondsElapsed) {
            if (!isStarted()) {
                return;
            }

            GameActivity.self.timerText.setText(" " + getTimeLeft() + " sec");

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
                setHighscore(getScore());
                setChanged();
                notifyObservers();
            }
        }

        @Override
        public void reset() {
        }
    };

    protected Game() {
        instance = this;
    }

    public static Game getInstance() {
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
        reset();
        startTime = new Date().getTime();
        GameActivity.self.timerText.setVisible(true);
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
        Text scoreText = GameActivity.self.scoreText;
        scoreText.setText("Score: " + getScore());
        GameActivity.self.timerText.setPosition(scoreText.getWidth() + scoreText.getX(), scoreText.getY());
    }

    public int getHighscore() {
        return GameActivity.self.settings.getInt("highscore", 0);
    }

    public void setHighscore(int newHighscore) {
        if (newHighscore > 0 && newHighscore > getHighscore()) {
            this.highscore = newHighscore;
            SharedPreferences.Editor editor = GameActivity.self.settings.edit();
            editor.putInt("highscore", score);
            editor.commit();
            Text highscoreText = GameActivity.self.overlay.highscoreText;
            highscoreText.setText("Highscore: " + highscore);
            highscoreText.setPosition((GameActivity.self.getWidth() - highscoreText.getWidth())  / 2, highscoreText.getY());
            highscoreText.setVisible(true);
        }
    }

    public void resetScore() {
        SharedPreferences.Editor editor = GameActivity.self.settings.edit();
        editor.putInt("highscore", 0);
        editor.commit();
        Text highscoreText = GameActivity.self.overlay.highscoreText;
        highscoreText.setText("Highscore: 0");
        highscoreText.setPosition((GameActivity.self.getWidth() - highscoreText.getWidth())  / 2, highscoreText.getY());
        highscoreText.setVisible(false);
    }
}
