package com.jelastic.energy.zombie;

import android.content.Intent;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseStrongOut;

import java.util.Date;

public class Overlay extends Rectangle {

    private Sprite shareButton;
    private Sprite startButton;

    {
        setAlpha(.5f);
        setColor(0, 0, 0);
        setZIndex(10);

        Sprite startButton = new Sprite(500, 200, GameActivity.startTexture) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (!pSceneTouchEvent.isActionDown()) {
                    return false;
                }

                GameActivity.startSound.play();
                registerEntityModifier(new ScaleModifier(.5f, this.getScaleX(), 0, EaseStrongOut.getInstance()));
                return true;
            }
        };
        startScale = width / (4 * startButton.getWidth()) ;
        startButton.setPosition((width - startButton.getWidth())/ 2 , (height - startButton.getHeight())/ 2);
        startButton.setScale(startScale);

        attachChild(startButton);

        Sprite shareButton = new Sprite(0, 0, GameActivity.shareTexture) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (!pSceneTouchEvent.isActionDown()) return false;
                GameActivity.startSound.play();
                share();
                return true;
            }
        };
        shareButton.setPosition(width - shareButton.getWidth(), height - shareButton.getHeight());
        float scale = (height / 3) * 100f / shareButton.getHeight();
        shareButton.setScaleCenter(shareButton.getWidth(), shareButton.getHeight());
        shareButton.setScale(scale / 100f);

        attachChild(shareButton);
    }

    public Overlay(float pX, float pY, float pWidth, float pHeight) {
        super(pX, pY, pWidth, pHeight);
    }

    public Overlay(float pX, float pY, float pWidth, float pHeight, RectangleVertexBuffer pRectangleVertexBuffer) {
        super(pX, pY, pWidth, pHeight, pRectangleVertexBuffer);
    }


    public void hide() {
        final Scene scene = (Scene) getParent();
        scene.clearTouchAreas();
        registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                shareButton.setVisible(false);
            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                Game.getInstance().reset();
                Game.getInstance().start();

                ///timerText.setVisible(true);
                setVisible(false);

                for (Target target : Game.getInstance().getTargets()) {
                    scene.registerTouchArea(target);
                }

                /*
                        //started = true;
                        //score = 0;
                        //startTime = new Date().getTime();
                        //timerText.setVisible(true);

                        scene.clearTouchAreas();
                        for (Target target : targets) {
                            scene.registerTouchArea(target);
                        }
                 */
            }
        }, new AlphaModifier(.5f, .5f, 0)));

    }

    public void show() {
        final Scene scene = (Scene) getParent();
        setVisible(true);
        registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {

            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                startButton.setScale(0);
                startButton.setVisible(true);
                startButton.registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                    }

                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        scene.registerTouchArea(startButton);
                    }
                }, new ScaleModifier(.3f, 0f, 1f, EaseStrongOut.getInstance())));

                shareButton.setVisible(true);
                //timerText.setVisible(false);
                scene.clearTouchAreas();
                scene.registerTouchArea(shareButton);
            }
        }, new AlphaModifier(.5f, 0, .5f)));
    }


    private void share() {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hit the Zombie");
        String shareMessage = "Take a look at the \"Hit the Zombie\" game on android market\nhttps://market.android.com/details?id=com.jelastic.energy.zombie";
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        GameActivity.self.startActivity(Intent.createChooser(shareIntent, "Insert share chooser title here"));
    }
}
