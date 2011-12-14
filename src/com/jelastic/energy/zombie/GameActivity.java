package com.jelastic.energy.zombie;

import android.content.SharedPreferences;
import android.view.Display;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
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
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.SimplePreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameActivity extends LayoutGameActivity implements IAccelerometerListener {
    protected static Sound failSound;
    protected static Sound hitSound;
    private static final int COLS = 5;
    private static final int ROWS = 3;
    private TextureRegion backgroundTexture;
    private TextureRegion targetTextures;

    private List<Target> targets = new ArrayList<Target>(15);
    private int width;
    private int height;
    private BitmapTextureAtlas backgroundTextureAtlas;
    private BitmapTextureAtlas targetTextureAtlas;
    private SharedPreferences preferences;
    private BitmapTextureAtlas mFontTexture;
    private Font mFont;
    private TiledTextureRegion tiles;

    @Override
    public Engine onLoadEngine() {
        this.preferences = SimplePreferences.getInstance(this.getApplicationContext());
        Display display = getWindowManager().getDefaultDisplay();
        this.width = display.getWidth();
        this.height = display.getHeight();

        final Camera camera = new Camera(0, 0, width, height);
        return new Engine(new EngineOptions(true, EngineOptions.ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(width, height), camera).setNeedsSound(true));
    }

    @Override
    protected int getLayoutID() {
        return R.layout.main;
    }

    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.xmllayoutexample_rendersurfaceview;
    }

    private int getTargetSize() {
        return (int) (this.height / 4.2f);
    }

    @Override
    public void onLoadResources() {
        this.mEngine.registerUpdateHandler(new FPSLogger());
        this.targetTextureAtlas = new BitmapTextureAtlas(2048, 256, TextureOptions.BILINEAR);
        this.backgroundTextureAtlas = new BitmapTextureAtlas(2048, 2048, TextureOptions.BILINEAR);

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.tiles = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.targetTextureAtlas, this, "tiles.png", 0, 0, 7, 1);
        this.backgroundTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundTextureAtlas, this, "cemetery.png", 0, 0);

        SoundFactory.setAssetBasePath("sounds/");
        try {
            hitSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "hit.ogg");
            failSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "fail.ogg");
        } catch (final Throwable e) {
            Debug.e(e);
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
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLS; i++) {
                final Target targetSprite = new Target(leftOffset + (size + dx) * i, topOffset + (size + dy) * j, this.tiles.deepCopy());
                targetSprite.setWidth(getTargetSize());
                targetSprite.setHeight(getTargetSize());
                targetSprite.setScaleCenterX(getTargetSize() / 2);
                scene.attachChild(targetSprite);
                this.targets.add(targetSprite);
                scene.registerTouchArea(targetSprite);
            }
        }
        scene.setTouchAreaBindingEnabled(true);
        float sx = width / (float) backgroundTexture.getWidth(), sy = height / (float) backgroundTexture.getHeight();
        Sprite bgSprite = new Sprite(0, 0, this.backgroundTexture);
        bgSprite.setScale(sx, sy);
        bgSprite.setWidth(width);
        bgSprite.setHeight(height);
        scene.setBackground(new SpriteBackground(bgSprite));
        scene.registerUpdateHandler(new IUpdateHandler() {
            @Override
            public void onUpdate(float pSecondsElapsed) {

                List<Target> sleeping = getSleepTargets();
                if (!sleeping.isEmpty()) {
                    for (Target target : sleeping) {
                        float extra = 0.5f;
                        float probability = (target.front ? 0.005f : .0005f) + extra;

                        if (!target.front && ((new Date().getTime()) - target.stopTime) < 1000) {
                            continue;
                        }

                        if (Math.random() < probability) {
                            target.rotate(MathUtils.random(0, 5));
                        }
                    }
                }
            }

            @Override
            public void reset() {
            }
        });
        return scene;
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

    @Override
    public void onLoadComplete() {
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.setVisibility(android.view.View.VISIBLE);
        adView.setEnabled(true);

        AdRequest request = new AdRequest();
        adView.loadAd(request);
    }

    @Override
    public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
    }
}