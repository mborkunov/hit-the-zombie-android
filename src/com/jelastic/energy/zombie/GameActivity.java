package com.jelastic.energy.zombie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.jelastic.energy.zombie.themes.CatTheme;
import com.jelastic.energy.zombie.themes.Theme;
import com.jelastic.energy.zombie.themes.ZombieTheme;
import com.jelastic.energy.zombie.util.Resources;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;

import java.util.Observable;
import java.util.Observer;

public class GameActivity extends LayoutGameActivity implements Observer {

    protected Font mFont;

    private int width;
    private int height;

    protected Overlay overlay;

    protected ChangeableText scoreText;
    protected ChangeableText timerText;


    public static GameActivity self;
    protected SharedPreferences settings;
    private Scene scene;
    public Theme theme;

    {
        self = this;
    }

    @Override
    public Engine onLoadEngine() {
        Display display = getWindowManager().getDefaultDisplay();
        
        width = display.getWidth();
        height = display.getHeight();
        settings = getSharedPreferences("zombie.dat", 0);
        new Game();

        final Camera camera = new Camera(0, 0, width, height);
        return new Engine(new EngineOptions(true, EngineOptions.ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), camera).setNeedsSound(true));
   }

    @Override
    protected int getLayoutID() {
        return R.layout.main;
    }


    @Override
    protected int getRenderSurfaceViewID() {
        return R.id.render;
    }

    private int getTargetSize() {
        return (int) (this.height / 4.2f);
    }

    @Override
    public void onLoadResources() {
        mEngine.registerUpdateHandler(new FPSLogger());
        Resources.init();
        theme = getThemeName().equals("zombie") ? new ZombieTheme() : new CatTheme();
        theme.load();

        // font
        mFont = Resources.loadFont(this, "andy.ttf", 512, 256, height / 10, theme.getTextColor());

        try {
            mEngine.setTouchController(new MultiTouchController());
        } catch (MultiTouchException e) {
            Debug.e(e);
        }
    }

    @Override
    public Scene onLoadScene() {
        scene = new Scene();
        createScene(scene);
        return scene;
    }

    private void createScene(Scene scene) {
        int size = getTargetSize();
        int dx = width / 25, dy = height / 50;

        int topOffset = height - ((getTargetSize() + dy) * 3) - (int) (height * .05);
        int leftOffset = (width - (5 * (size + dx))) / 2;
        int topTextOffset = height / 80;

        scoreText = new ChangeableText(leftOffset, topTextOffset, mFont, "Score: " + Game.getInstance().getScore(), 11);
        timerText = new ChangeableText(scoreText.getWidth() + leftOffset, topTextOffset, mFont, " " + Game.getInstance().getTimeLeft() + " sec", HorizontalAlign.RIGHT, 7);
        timerText.setVisible(false);
        scene.attachChild(scoreText);
        scene.attachChild(timerText);
        Game.getInstance().getTargets().clear();
        for (int j = 0; j < Game.ROWS; j++) {
            for (int i = 0; i < Game.COLS; i++) {
                final Target targetSprite = new Target(leftOffset + (size + dx) * i, topOffset + (size + dy) * j, theme.getTiles().deepCopy());
                targetSprite.setWidth(getTargetSize());
                targetSprite.setHeight(getTargetSize());
                targetSprite.setScaleCenterX(getTargetSize() / 2);
                targetSprite.setZIndex(2);
                scene.attachChild(targetSprite);
                Game.getInstance().addTarget(targetSprite);
            }
        }
        scene.setBackground(theme.getBackground());
        scene.registerUpdateHandler(Game.getInstance().getUpdateHandler());
        overlay = new Overlay(0, 0, width, height);
        scene.attachChild(overlay);
        Game.getInstance().addObserver(this);
    }

    @Override
    public void onLoadComplete() {
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.setVisibility(android.view.View.VISIBLE);
        adView.setEnabled(true);

        AdRequest request = new AdRequest();
        adView.loadAd(request);

        overlay.onLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        MenuItem qualityItem = menu.findItem(R.id.quality);
        qualityItem.setTitle(isQuality() ? R.string.quality_high : R.string.quality_low);

        MenuItem soundItem = menu.findItem(R.id.sound);
        soundItem.setTitle(isSound() ? R.string.sound_on : R.string.sound_off);

        MenuItem themeItem = menu.findItem(R.id.theme);
        themeItem.setTitle(getThemeName().equals("zombie") ? R.string.theme_zombie : R.string.theme_cat);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sound:
                setSound(!isSound());
                item.setTitle(isSound() ? R.string.sound_on : R.string.sound_off);
                return true;
            case R.id.theme:
                setThemeName(getThemeName().equals("zombie") ? "cat" : "zombie");
                item.setTitle(getThemeName().equals("zombie") ? R.string.theme_zombie : R.string.theme_cat);
                restart();
                return true;
            case R.id.quality:
                setQuality(!isQuality());
                item.setTitle(isQuality() ? R.string.quality_high : R.string.quality_low);
                restart();
                return true;
            case R.id.reset:
                Game.getInstance().resetScore();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restart() {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags()));
        System.exit(2);
    }

    public boolean isQuality() {
        return settings.getBoolean("quality", true);
    }

    public void setQuality(boolean quality) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("quality", quality);
        editor.commit();

        Resources.textureOptions = isQuality() ? TextureOptions.BILINEAR : TextureOptions.NEAREST;
        Resources.fontAntiAliasing = isQuality();
    }

    public boolean isSound() {
        return settings.getBoolean("sound", true);
    }

    public void setSound(boolean sound) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("sound", sound);
        editor.commit();
    }

    public void setThemeName(String theme) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("theme", theme);
        editor.commit();
    }

    public String getThemeName() {
        return settings.getString("theme", "zombie");
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