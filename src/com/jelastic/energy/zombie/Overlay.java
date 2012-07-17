package com.jelastic.energy.zombie;

import android.content.Intent;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBounceOut;
import org.andengine.util.modifier.ease.EaseStrongOut;

public class Overlay extends Rectangle {

    private Sprite shareButton;
    private Sprite startButton;
    protected Text highscoreText;

    private float startScale;

    {
        setAlpha(.5f);
        setColor(0, 0, 0);
        setZIndex(10);

        int width = GameActivity.self.getWidth(), height = GameActivity.self.getHeight();

        startButton = new Sprite(500, 200, GameActivity.self.theme.getStartButton(), GameActivity.self.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (!pSceneTouchEvent.isActionDown()) {
                    return false;
                }
                GameActivity.self.theme.getButtonSound().play();
                hide();
                return true;
            }
        };
        startScale = width / (4 * startButton.getWidth()) ;
        startButton.setPosition((width - startButton.getWidth())/ 2 , (height - startButton.getHeight()) / 2);
        startButton.setScale(startScale);

        highscoreText = new Text(startButton.getX(), startButton.getY() + startButton.getHeight() * 2, GameActivity.self.mFont, "Highscore: " + Game.getInstance().getHighscore(), 15, GameActivity.self.getVertexBufferObjectManager());
        highscoreText.setPosition((GameActivity.self.getWidth() - highscoreText.getWidth())  / 2, highscoreText.getY());
        highscoreText.setVisible(Game.getInstance().getHighscore() > 0);

        attachChild(startButton);
        attachChild(highscoreText);

        shareButton = new Sprite(0, 0, GameActivity.self.theme.getShareButton(), GameActivity.self.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (!pSceneTouchEvent.isActionDown()) return false;
                GameActivity.self.theme.getButtonSound().play();
                share();
                return true;
            }
        };
        shareButton.setPosition(width - shareButton.getWidth(), height - shareButton.getHeight());
        shareButton.setVisible(false);
        float scale = (height / 4) * 100f / shareButton.getHeight();
        shareButton.setScaleCenter(shareButton.getWidth(), shareButton.getHeight());
        shareButton.setScale(scale / 100f);

        attachChild(shareButton);
    }

    public Overlay(float pX, float pY, float pWidth, float pHeight) {
        super(pX, pY, pWidth, pHeight, GameActivity.self.getVertexBufferObjectManager());
    }

    public void hide() {
        final Scene scene = getScene();
        registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                scene.clearTouchAreas();
                shareButton.setVisible(false);
                startButton.registerEntityModifier(new ScaleModifier(.5f, startButton.getScaleX(), 0, EaseStrongOut.getInstance()));
            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                Game.getInstance().reset();
                Game.getInstance().start();

                setVisible(false);

                for (Target target : Game.getInstance().getTargets()) {
                    scene.registerTouchArea(target);
                }
            }
        }, new AlphaModifier(.5f, .5f, 0)));

    }

    public void show() {
        final Scene scene = (Scene) getParent();
        setVisible(true);
        registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {

            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                scene.clearTouchAreas();
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
                }, new ScaleModifier(.3f, 0f, startScale, EaseStrongOut.getInstance())));

                shareButton.setVisible(true);
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

    public void onLoad() {
        startButton.setScale(0);
        startButton.registerEntityModifier(new ScaleModifier(1.5f, 0f, startScale, EaseBounceOut.getInstance()));

        getScene().registerTouchArea(startButton);
        getScene().registerTouchArea(shareButton);
    }

    private Scene getScene() {
        return (Scene) getParent();
    }
}
