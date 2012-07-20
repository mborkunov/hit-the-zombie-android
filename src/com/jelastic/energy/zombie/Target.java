package com.jelastic.energy.zombie;

import android.content.Context;
import android.os.Vibrator;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.util.math.MathUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Target extends TiledSprite {

    private IUpdateHandler handler = new IUpdateHandler() {
        private int angle = 0;
        private float elapsed = 0;

        @Override
        public void onUpdate(float pSecondsElapsed) {
            elapsed += pSecondsElapsed;
            if (elapsed < .02) {
                return;
            } else {
                elapsed = 0;
            }

            if (!Game.self.isStarted() && !front && !isRotating()) {
                rotate(1);
            }

            if (!isRotating())return;

            if (angle >= 360) {
                angle %= 360;
            }

            int step = getAngleStep();
            if (this.angle % 180 < step) {
                currentTurn++;
                if (currentTurn > turns) {
                    setRotating(false);
                }
            }

            if (currentTurn <= turns) {
                if ((this.angle < 90 && this.angle + step >= 90)) {
                    front = false;
                    setFlippedHorizontal(MathUtils.random(0, 1) == 1);
                    if (!front) {
                        setCurrentTileIndex(MathUtils.random(1, 3));
                    }
                } else if (this.angle < 270 && this.angle + step >= 270) {
                    front = true;
                    setFlippedHorizontal(MathUtils.random(0, 1) == 1);
                    setCurrentTileIndex(0);
                }
                this.angle += step;

                if (!isRotating()) {
                    angle -= angle % 180;
                }
                setScale(Math.abs((float) Math.cos(this.angle * Math.PI / 180)), 1f);
            }
        }

        @Override
        public void reset() {
            setScale(1f, 1f);
        }
    };
    private List<TargetListener> listeners = new ArrayList<TargetListener>();

    private int getAngleStep() {
        return 15 + (int) (Game.self.getProgress() / 15f);
    }

    private boolean rotating = false;
    private int currentTurn = 0;
    private int turns = 0;
    protected boolean front = true;
    protected long stopTime = new Date().getTime();

    public Target(float pX, float pY, TiledTextureRegion pTiledTextureRegion) {
        super(pX, pY, pTiledTextureRegion, GameActivity.self.getVertexBufferObjectManager());
        this.registerUpdateHandler(handler);

        addTargetListener(new TargetListener() {
            @Override
            public void onHit() {
                if (getCurrentTileIndex() + 3 < getTileCount()) {
                    setCurrentTileIndex(getCurrentTileIndex() + 3);
                    rotate(1);

                    GameActivity.self.theme.getHitSound().play();
                    if (GameActivity.self.getOptions().isVibrate()) {
                        Vibrator v = (Vibrator) GameActivity.self.getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(100);
                    }
                    Game.self.setScore(Game.self.getScore() + 10);
                }
            }

            @Override
            public void onMiss() {
                GameActivity.self.theme.getFailSound().play();
                Game.self.setScore(Game.self.getScore() - 5);
            }
        });
    }

    public boolean isRotating() {
        return rotating;
    }

    public void setRotating(boolean rotating) {
        this.rotating = rotating;
        if (!isRotating()) {
            stopTime = new Date().getTime();
        }
    }

    public void rotate(int turns) {
        setRotating(true);
        this.turns = turns;
        this.currentTurn = 0;
    }

    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
        if (!Game.self.isStarted()) return true;
        if (!pSceneTouchEvent.isActionDown()) return true;
        for (TargetListener listener : listeners) {
            if (!front) {
                listener.onHit();
            } else {
                listener.onMiss();
            }
        }
        return true;
    }

    public void addTargetListener(TargetListener listener) {
        listeners.add(listener);
    }

    public static interface TargetListener {
        void onHit();
        void onMiss();
    }
}
