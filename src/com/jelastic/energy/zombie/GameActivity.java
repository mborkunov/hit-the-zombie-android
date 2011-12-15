package com.jelastic.energy.zombie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.SimplePreferences;
import org.anddev.andengine.util.modifier.IModifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameActivity extends LayoutGameActivity {

    // sounds
    protected static Sound failSound;
    protected static Sound hitSound;

    private static final int COLS = 5;
    private static final int ROWS = 3;
    private TextureRegion backgroundTexture;

    private List<Target> targets = new ArrayList<Target>(15);
    private int width;
    private int height;

    private SharedPreferences preferences;

    private Font mFont;
    private TiledTextureRegion targetTiles;

    private Rectangle overlay;
    private Scene scene;

    protected int score;
    protected int highscore;

    private ChangeableText scoreText;
    private ChangeableText highscoreText;
    private ChangeableText timerText;
    
    private final static int ROUND_TIME = 60;
    private long startTime = 0;

    public static GameActivity self;
    {
        self = this;
    }

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

        // textures
        BitmapTextureAtlas targetTextureAtlas = new BitmapTextureAtlas(2048, 256, TextureOptions.BILINEAR);
        BitmapTextureAtlas backgroundTextureAtlas = new BitmapTextureAtlas(2048, 2048, TextureOptions.BILINEAR);

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.targetTiles = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(targetTextureAtlas, this, "tiles.png", 0, 0, 7, 1);
        this.backgroundTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundTextureAtlas, this, "cemetery.png", 0, 0);

        this.mEngine.getTextureManager().loadTexture(targetTextureAtlas);
        this.mEngine.getTextureManager().loadTexture(backgroundTextureAtlas);

        // sounds
        SoundFactory.setAssetBasePath("sounds/");
        try {
            hitSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "hit.ogg");
            failSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "fail.ogg");
        } catch (final Throwable e) {
            Debug.e(e);
        }

        // multi touch
        try {
            this.mEngine.setTouchController(new MultiTouchController());
        } catch (MultiTouchException e) {
            Debug.e(e);
        }

        // fonts
        FontFactory.setAssetBasePath("font/");
        BitmapTextureAtlas mFontTexture = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR);
        mFont = FontFactory.createFromAsset(mFontTexture, this.getApplicationContext(), "andy.ttf", height / 10, true, Color.YELLOW);
        mEngine.getTextureManager().loadTexture(mFontTexture);
        mEngine.getFontManager().loadFont(mFont);
    }

    @Override
    public Scene onLoadScene() {
        int size = getTargetSize();
        int dx = width / 25, dy = height / 50;

        int topOffset = height - ((getTargetSize() + dy) * 3) - (int) (height * .05);
        int leftOffset = (width - (5 * (size + dx))) / 2;

        final Scene scene = new Scene();

        highscoreText = new ChangeableText(leftOffset, 10, mFont, "Score: " + getHighScore() + "    ");
        timerText = new ChangeableText(150, 50, mFont, String.valueOf(100 - getProgress()), HorizontalAlign.RIGHT, 4);
        scene.attachChild(highscoreText);
        scene.attachChild(timerText);
        for (int j = 0; j < ROWS; j++) {
            for (int i = 0; i < COLS; i++) {
                final Target targetSprite = new Target(leftOffset + (size + dx) * i, topOffset + (size + dy) * j, this.targetTiles.deepCopy());
                targetSprite.setWidth(getTargetSize());
                targetSprite.setHeight(getTargetSize());
                targetSprite.setScaleCenterX(getTargetSize() / 2);
                targetSprite.setZIndex(2);
                scene.attachChild(targetSprite);
                this.targets.add(targetSprite);
            }
        }
        float sx = width / (float) backgroundTexture.getWidth(), sy = height / (float) backgroundTexture.getHeight();
        Sprite bgSprite = new Sprite(0, 0, this.backgroundTexture);
        bgSprite.setScale(sx, sy);
        bgSprite.setWidth(width);
        bgSprite.setHeight(height);
        scene.setBackground(new SpriteBackground(bgSprite));
        scene.registerUpdateHandler(new IUpdateHandler() {
            private float elapsed = 0;
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if (!started) {
                    return;
                }

                timerText.setText(String.valueOf(100 - getProgress()));

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

                if (getTimeLeft() == 0) {
                    started = false;
                    overlay.setVisible(true);
                    overlay.registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {

                        @Override
                        public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                            animation = true;
                        }

                        @Override
                        public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                            animation = false;

                            overlay.getFirstChild().setVisible(true);
                            overlay.getLastChild().setVisible(true);
                            scene.clearTouchAreas();
                            scene.registerTouchArea((Shape) getOverlay().getFirstChild());
                            scene.registerTouchArea((Shape) getOverlay().getLastChild());
                        }
                    }, new AlphaModifier(.5f, 0, .5f)));
                }
            }

            @Override
            public void reset() {
            }
        });
        scene.attachChild(getOverlay());
        scene.registerTouchArea((Shape) getOverlay().getFirstChild());
        scene.registerTouchArea((Shape) getOverlay().getLastChild());
        this.scene = scene;

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
    
    protected void setScore(int score) {
        this.score = Math.max(0, score);
        setHighScore(this.score);
    }

    protected void setHighScore(int score) {
        //if (highscore != score) {
            highscore = score;
            preferences.edit().putInt("highscore", highscore);
            preferences.edit().commit();
            highscoreText.setText("Score: " + highscore);
        //}
    }


    public boolean hasHighScore() {
        return preferences.contains("highscore");
    }

    public int getHighScore() {
        return hasHighScore() ? preferences.getInt("highscore", 0) : 0;
    }

    protected int getScore() {
        return score;
    }
    
    protected int getProgress() {
        if (startTime == 0) return 0;
        return (int) Math.abs(100 - getTimeLeft() * 100f / ROUND_TIME);
    }
    
    protected boolean hasStarted() {
        return started;
    }

    protected int getTimeLeft() {
        if (!hasStarted()) return -1;
        return Math.abs(ROUND_TIME - (int) (new Date().getTime() - startTime) / 1000);
    }
    
    private float getProbability(boolean front) {
        float extra = 0.05f * getProgress() / 100;
        return (front ? 0.005f : .0007f) + extra;
    }

    @Override
    public void onLoadComplete() {
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.setVisibility(android.view.View.VISIBLE);
        adView.setEnabled(true);

        AdRequest request = new AdRequest();
        adView.loadAd(request);
    }

    private boolean started = false;
    private boolean animation = false;

    public void start() {
        this.scene.unregisterTouchArea(overlay);
        getOverlay().registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                animation = true;
                overlay.getFirstChild().setVisible(false);
                overlay.getLastChild().setVisible(false);
            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                started = true;
                score = 0;
                startTime = new Date().getTime();
                animation = false;
                overlay.setVisible(false);

                scene.clearTouchAreas();
                for (Target target : targets) {
                    scene.registerTouchArea(target);
                }
            }
        }, new AlphaModifier(.5f, .5f, 0)));
    }

    public void stop() {
        started = false;
        //getOverlay().registerEntityModifier(new AlphaModifier(1, 0, .5f));
    }

    private Rectangle getOverlay() {
        if (overlay == null) {
            overlay = new Rectangle(0, 0, width, height);
            overlay.setAlpha(.5f);
            overlay.setColor(0, 0, 0);
            overlay.setZIndex(10);

            ChangeableText shareButton = new ChangeableText(100, 200, mFont, "Share") {
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    share();
                    return true;
                }
            };
            overlay.attachChild(shareButton);

            ChangeableText startButton = new ChangeableText(500, 200, mFont, "Start") {
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    if (animation) return false;
                    start();
                    return true;
                }
            };
            overlay.attachChild(startButton);
        }
        return overlay;
    }


    private void share() {
        //create the intent
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        //set the type
        shareIntent.setType("text/plain");
        //add a subject
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hit the Zombie");
        //build the body of the message to be shared
        String shareMessage = "Take a look at the \"Hit the Zombie\" game on android market";
        //add the message
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,shareMessage);
        //start the chooser for sharing
        startActivity(Intent.createChooser(shareIntent, "Insert share chooser title here"));
    }

}