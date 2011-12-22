package com.jelastic.energy.zombie;

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
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;

import java.util.Observable;
import java.util.Observer;

public class GameActivity extends LayoutGameActivity implements Observer {

    // sounds
    //protected static GameSound failSound;
    //protected static GameSound hitSound;
    //protected static GameSound startSound;

    protected static Font mFont;

    protected static TextureRegion bgTexture;
    protected static TextureRegion shareTexture;
    protected static TiledTextureRegion targetTiles;
    protected static TextureRegion startTexture;

    private int width;
    private int height;

    private Overlay overlay;

    protected int score;

    private ChangeableText scoreText;
    private ChangeableText timerText;
    

    public static GameActivity self;
    private SharedPreferences settings;
    private boolean sound = true;

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
        timerText = new ChangeableText(scoreText.getWidth() + leftOffset, topTextOffset, mFont, " " + Game.getInstance().getTimeLeft() + " sec", HorizontalAlign.RIGHT, 7);
        timerText.setVisible(false);
        scene.attachChild(scoreText);
        scene.attachChild(timerText);
        for (int j = 0; j < Game.ROWS; j++) {
            for (int i = 0; i < Game.COLS; i++) {
                final Target targetSprite = new Target(leftOffset + (size + dx) * i, topOffset + (size + dy) * j, targetTiles.deepCopy());
                targetSprite.setWidth(getTargetSize());
                targetSprite.setHeight(getTargetSize());
                targetSprite.setScaleCenterX(getTargetSize() / 2);
                targetSprite.setZIndex(2);
                scene.attachChild(targetSprite);
                Game.getInstance().addTarget(targetSprite);
            }
        }
        float scaleX = width / (float) bgTexture.getWidth(), scaleY = height / (float) bgTexture.getHeight();
        Sprite bgSprite = new Sprite(0, 0, bgTexture);
        bgSprite.setScaleCenter(0, 0);
        bgSprite.setScale(scaleX, scaleY);
        scene.setBackground(new SpriteBackground(bgSprite));
        scene.registerUpdateHandler(Game.getInstance().getUpdateHandler());
        overlay = new Overlay(0, 0, width, height);
        scene.attachChild(overlay);
        Game.getInstance().addObserver(this);
        return scene;
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
    
    @Override
    public void onLoadComplete() {
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.setVisibility(android.view.View.VISIBLE);
        adView.setEnabled(true);

        AdRequest request = new AdRequest();
        adView.loadAd(request);

        overlay.onLoad();
        sound = settings.getBoolean("sound", true);
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
        if (!((Game) observable).isStarted()) {
            overlay.show();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}