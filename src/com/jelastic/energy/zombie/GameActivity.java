package com.jelastic.energy.zombie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
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
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseBounceOut;
import org.anddev.andengine.util.modifier.ease.EaseStrongOut;

import java.util.*;

public class GameActivity extends LayoutGameActivity implements Observer {

    // sounds
    protected static GameSound failSound;
    protected static GameSound hitSound;
    protected static GameSound startSound;

    protected static Font mFont;

    protected static TextureRegion bgTexture;
    protected static TextureRegion shareTexture;
    protected static TiledTextureRegion targetTiles;
    protected static TextureRegion startTexture;

    private static final int COLS = 5;
    private static final int ROWS = 3;

    private List<Target> targets = new ArrayList<Target>(15);
    private int width;
    private int height;

    private Overlay overlay;
    private Scene scene;

    protected int score;

    private ChangeableText scoreText;
    private ChangeableText timerText;
    
    private final static int ROUND_TIME = 60;
    private long startTime = 0;

    public static GameActivity self;
    private SharedPreferences settings;
    private boolean sound = true;
    private float startScale;

    {
        self = this;
    }

    @Override
    public Engine onLoadEngine() {
        Display display = getWindowManager().getDefaultDisplay();
        this.width = display.getWidth();
        this.height = display.getHeight();
        settings = getSharedPreferences("zombie.dat", 0);

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

        Resources.init();

        // textures
        bgTexture    = Resources.loadTexture(this, "cemetery.png", 512, 512);
        shareTexture = Resources.loadTexture(this, "share.png", 256, 256);
        startTexture = Resources.loadTexture(this, "start.png", 256, 64);
        targetTiles  = Resources.loadTexture(this, "tiles.png", 1024, 128, 7, 1);

        // sounds
        hitSound   = Resources.loadSound("hit.ogg", this);
        failSound  = Resources.loadSound("fail.ogg", this);
        startSound = Resources.loadSound("start.ogg", this);

        // font
        mFont = Resources.loadFont(this, "andy.ttf", 512, 256, height / 10, Color.YELLOW);

        try {
            this.mEngine.setTouchController(new MultiTouchController());
        } catch (MultiTouchException e) {
            Debug.e(e);
        }
    }

    @Override
    public Scene onLoadScene() {
        int size = getTargetSize();
        int dx = width / 25, dy = height / 50;

        int topOffset = height - ((getTargetSize() + dy) * 3) - (int) (height * .05);
        int leftOffset = (width - (5 * (size + dx))) / 2;
        int topTextOffset = height / 80;

        final Scene scene = new Scene();

        if (settings.contains("score")) {
            score = settings.getInt("score", 0);
        }

        scoreText = new ChangeableText(leftOffset, topTextOffset, mFont, "Score: " + getScore(), 11);
        timerText = new ChangeableText(scoreText.getWidth() + leftOffset, topTextOffset, mFont, " " + getTimeLeft() + " sec", HorizontalAlign.RIGHT, 7);
        timerText.setVisible(false);
        scene.attachChild(scoreText);
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
                Game.getInstance().addTarget(targetSprite);
            }
        }
        float scaleX = width / (float) bgTexture.getWidth(), scaleY = height / (float) bgTexture.getHeight();
        Sprite bgSprite = new Sprite(0, 0, this.bgTexture);
        bgSprite.setScaleCenter(0, 0);
        bgSprite.setScale(scaleX, scaleY);
        scene.setBackground(new SpriteBackground(bgSprite));
        scene.registerUpdateHandler(Game.getInstance().getUpdateHandler());
        scene.attachChild(getOverlay());
        scene.registerTouchArea((Shape) getOverlay().getFirstChild());
        scene.registerTouchArea((Shape) getOverlay().getLastChild());
        this.scene = scene;

        Game.getInstance().addObserver(this);

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
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("score", score);
        editor.commit();
        this.score = Math.max(0, score);
        scoreText.setText("Score: " + getScore());
        timerText.setPosition(scoreText.getWidth() + scoreText.getX(), scoreText.getY());
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
        if (!hasStarted()) return 60;
        return Math.abs(ROUND_TIME - (int) (System.currentTimeMillis() - startTime) / 1000);
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

        IEntity btn = getOverlay().getFirstChild();
        float _scaleX = btn.getScaleX();
        btn.setScale(0);
        btn.registerEntityModifier(new ScaleModifier(1.5f, 0f, _scaleX, EaseBounceOut.getInstance()));

        sound = settings.getBoolean("sound", true);
    }

    private boolean started = false;
    private boolean animation = false;

    public void start() {
        this.scene.unregisterTouchArea(overlay);
        getOverlay().registerEntityModifier(new SequenceEntityModifier(new IEntityModifier.IEntityModifierListener() {
            @Override
            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                animation = true;
                overlay.getLastChild().setVisible(false);
            }

            @Override
            public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                started = true;
                score = 0;
                startTime = new Date().getTime();
                animation = false;
                timerText.setVisible(true);
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
    }

    private Rectangle getOverlay() {
        if (overlay == null) {
            overlay = new Overlay(0, 0, width, height);

            Sprite startButton = new Sprite(500, 200, startTexture) {
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    if (animation) return false;
                    startSound.play();
                    registerEntityModifier(new ScaleModifier(.5f, getScaleX(), 0, EaseStrongOut.getInstance()));
                    start();
                    return true;
                }
            };
            startScale = width / (4 * startButton.getWidth()) ;
            startButton.setPosition((width - startButton.getWidth())/ 2 , (height - startButton.getHeight())/ 2);
            startButton.setScale(startScale);

            overlay.attachChild(startButton);

            Sprite shareButton = new Sprite(0, 0, this.shareTexture) {
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    if (!pSceneTouchEvent.isActionDown()) return false;
                    startSound.play();
                    share();
                    return true;
                }
            };
            shareButton.setPosition(width - shareButton.getWidth(), height - shareButton.getHeight());
            float scale = (height / 3) * 100f / shareButton.getHeight();
            shareButton.setScaleCenter(shareButton.getWidth(), shareButton.getHeight());
            shareButton.setScale(scale / 100f);

            overlay.attachChild(shareButton);
        }
        return overlay;
    }


    private void share() {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Hit the Zombie");
        String shareMessage = "Take a look at the \"Hit the Zombie\" game on android market\nhttps://market.android.com/details?id=com.jelastic.energy.zombie";
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Insert share chooser title here"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sound:
                setSound(!isSound());
                item.setTitle(isSound() ? R.string.sound_off : R.string.sound_on);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("sound", isSound());
                editor.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    @Override
    public void update(Observable observable, Object o) {
        Game game = (Game) observable;

        if (game.isStarted()) {
            overlay.show();
        } else {
            overlay.hide();
        }
    }
}