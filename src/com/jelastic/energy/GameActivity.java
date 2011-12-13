package com.jelastic.energy;

import android.content.SharedPreferences;
import android.view.Display;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.SimplePreferences;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends BaseGameActivity {
    private Sound failSound;
    private Sound hitSound;
    private static final int CAMERA_WIDTH = 1280;
    private static final int CAMERA_HEIGHT = 800;
    private BitmapTextureAtlas bgBitmapTextureAtlas;

    private TextureRegion backgroundTexture;
    private TextureRegion targetTexture;

    private List<Sprite> targets = new ArrayList<Sprite>(15);
    private int width;
    private int height;
    private BitmapTextureAtlas backgroundTextureAtlas;
    private BitmapTextureAtlas targetTextureAtlas;
    private SharedPreferences preferences;
    private BitmapTextureAtlas mFontTexture;
    private Font mFont;

    @Override
    public Engine onLoadEngine() {
        this.preferences = SimplePreferences.getInstance(this.getApplicationContext());
        Display display = getWindowManager().getDefaultDisplay();
        this.width = display.getWidth();
        this.height = display.getHeight();

        //Toast.makeText(this, "Screen.resolution: " + this.width + "x" + this.height, Toast.LENGTH_LONG).show();
        final Camera camera = new Camera(0, 0, width, height);
        return new Engine(new EngineOptions(true, EngineOptions.ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(width, height), camera).setNeedsSound(true));
    }


    private int getTargetSize() {
        return (int) (this.height / 4.2f);
    }

    @Override
    public void onLoadResources() {
        this.mEngine.registerUpdateHandler(new FPSLogger());
        this.targetTextureAtlas = new BitmapTextureAtlas(1024, 1024, TextureOptions.NEAREST);
        this.backgroundTextureAtlas = new BitmapTextureAtlas(2048, 2048, TextureOptions.DEFAULT);
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.targetTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.targetTextureAtlas, this, "target.png", 0, 0);
        this.backgroundTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundTextureAtlas, this, "cemetery.png", 0, 0);

        SoundFactory.setAssetBasePath("sounds/");
        try {
            this.hitSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "hit.ogg");
            this.failSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "fail.ogg");
        } catch (final Throwable e) {
            Debug.e(e);
            e.printStackTrace();
        }
        this.mEngine.getTextureManager().loadTexture(this.targetTextureAtlas);
        this.mEngine.getTextureManager().loadTexture(this.backgroundTextureAtlas);
        try {
            this.mEngine.setTouchController(new MultiTouchController());
        } catch (MultiTouchException e) {
            Debug.e(e);
        }

        // loading font
        /*this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
        this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.BLACK);

        this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
        this.getFontManager().loadFont(this.mFont);*/
    }

    @Override
    public Scene onLoadScene() {
        if (!this.preferences.contains("highscore")) {
            this.preferences.edit().putInt("highscore", 1150);
        }

        int size = getTargetSize();
        int dx = width / 25, dy = height / 50;

        int topOffset = height - ((getTargetSize() + dy) * 3) - (int) (height * .05);
        int leftOffset = (width - (5 * (size + dx))) / 2;

        final Scene scene = new Scene();
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 5; i++) {
                final Sprite targetSprite = new Sprite(leftOffset + (size + dx) * i, topOffset + (size + dy) * j, this.targetTexture) {
                    @Override
                    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                        if (pSceneTouchEvent.isActionDown()) {
                            hitSound.play();
                        }
                        return true;
                    }
                };
                targetSprite.setWidth(getTargetSize());
                targetSprite.setHeight(getTargetSize());
                targetSprite.setScaleCenterX(getTargetSize() / 2);

                targetSprite.registerUpdateHandler(new IUpdateHandler() {
                    private int angle = 0;
                    private float elapsed;

                    @Override
                    public void onUpdate(float pSecondsElapsed) {
                        elapsed += pSecondsElapsed;

                        //if (elapsed >= .03f) {
                            elapsed = 0;
                            this.angle += 5 + MathUtils.random(0, 5);
                            targetSprite.setScale(Math.abs((float) Math.cos(this.angle * Math.PI / 180)), 1f);
                        //}
                    }

                    @Override
                    public void reset() {

                    }
                });
                scene.attachChild(targetSprite);
                this.targets.add(targetSprite);
                scene.registerTouchArea(targetSprite);
            }
        }
        scene.setTouchAreaBindingEnabled(true);
        scene.setBackground(new SpriteBackground(new Sprite(0, 0, this.backgroundTexture)));
        return scene;
    }

    @Override
    public void onLoadComplete() {
    }
}