package com.jelastic.energy.zombie;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.MathUtils;

import java.util.Date;


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

            if (!isRotating()) return;

            if (angle >= 360) {
                angle %= 360;
            }

            if (this.angle % 180 == 0) {
                currentTurn++;
                if (currentTurn > turns) {
                    setRotating(false);
                }
            }
            int step = getAngleStep();

            if (currentTurn <= turns) {
                if ((this.angle < 90 && this.angle + ANGLE_STEP >= 90)) {
                    front = false;
                    setFlippedHorizontal(MathUtils.random(0, 1) == 1);
                    if (!front) {
                        setCurrentTileIndex(MathUtils.random(1, 3));
                    }
                } else if (this.angle < 270 && this.angle + ANGLE_STEP >= 270) {
                    front = true;
                    setFlippedHorizontal(MathUtils.random(0, 1) == 1);
                    setCurrentTileIndex(0);
                }
                this.angle += ANGLE_STEP;
                hit = false;
                setScale(Math.abs((float) Math.cos(this.angle * Math.PI / 180)), 1f);
            }
        }

        @Override
        public void reset() {
            setScale(1f, 1f);
        }
    };

    private int getAngleStep() {
        return 10 + (int) (GameActivity.self.getProgress() / 10f);
    }

    private boolean rotating = false;
    private int currentTurn = 0;
    private int turns = 0;
    protected boolean front = true;
    private static final int ANGLE_STEP = 15;
    protected long stopTime = new Date().getTime();
    private boolean hit = false;


    public Target(float pX, float pY, TiledTextureRegion pTiledTextureRegion) {
        super(pX, pY, pTiledTextureRegion);
        this.registerUpdateHandler(handler);
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
        if (!isRotating()) {
            if (!front) {
                setCurrentTileIndex(getCurrentTileIndex() + 3);
                rotate(1);
                GameActivity.hitSound.play();
                GameActivity.self.setScore(GameActivity.self.getScore() + 10);
            } else {
                if (!hit) {
                    hit = true;
                    GameActivity.failSound.play();
                    GameActivity.self.setScore(GameActivity.self.getScore() - 5);
                }
            }
        }
        return true;
    }
}
