package com.jelastic.energy.zombie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import com.jelastic.energy.zombie.themes.CatTheme;
import com.jelastic.energy.zombie.themes.Theme;
import com.jelastic.energy.zombie.themes.ZombieTheme;
import com.jelastic.energy.zombie.util.Resources;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.opengl.font.Font;
import org.andengine.ui.activity.SimpleLayoutGameActivity;

import java.util.Observable;
import java.util.Observer;

public class GameActivity extends SimpleLayoutGameActivity implements Observer {

    protected Font mFont;

    private int width;
    private int height;

    protected Overlay overlay;

    protected Text scoreText;
    protected Text timerText;


    public static GameActivity self;
    protected SharedPreferences settings;
    public Theme theme;
    private Options options;
    private Layout layout;

    {
        self = this;
    }


    @Override
    public EngineOptions onCreateEngineOptions() {
        Display display = getWindowManager().getDefaultDisplay();

        layout = new Layout(display);

        width = display.getWidth();
        height = display.getHeight();
        settings = getSharedPreferences("settings", 0);
        options = new Options(settings);
        new Game();

        final Camera camera = new Camera(0, 0, width, height);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new FillResolutionPolicy(), camera);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
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
    public void onCreateResources() {
        mEngine.registerUpdateHandler(new FPSLogger());
        Resources.init();
        theme = options.getThemeName().equals("zombie") ? new ZombieTheme() : new CatTheme();
        theme.load();

        // font
        mFont = Resources.loadFont(this, "andy.ttf", 512, 256, height / 10, theme.getTextColor());

        boolean multiTouch = MultiTouch.isSupported(getApplicationContext());
        if (multiTouch) {
            mEngine.setTouchController(new MultiTouchController());
        }
    }

    @Override
    public Scene onCreateScene() {
        final Scene scene = new Scene();
        int size = getTargetSize();
        int dx = width / 25, dy = height / 50;

        int topOffset = height - ((getTargetSize() + dy) * 3) - (int) (height * .05);
        int leftOffset = (width - (5 * (size + dx))) / 2;
        int topTextOffset = height / 80;

        scoreText = new Text(leftOffset, topTextOffset, mFont, "Score: " + Game.getInstance().getScore(), 11, getVertexBufferObjectManager());
        timerText = new Text(scoreText.getWidth() + leftOffset, topTextOffset, mFont, " " + Game.getInstance().getTimeLeft() + " sec", 7, getVertexBufferObjectManager());
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
        return scene;
    }

    @Override
    public synchronized void onGameCreated() {
        overlay.onLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        MenuItem qualityItem = menu.findItem(R.id.quality);
        qualityItem.setTitle(options.isQuality() ? R.string.quality_high : R.string.quality_low);

        MenuItem soundItem = menu.findItem(R.id.sound);
        if (options.isSound()) {
            soundItem.setTitle(R.string.sound_on);
            soundItem.setIcon(R.drawable.ic_audio_vol);
        } else {
            soundItem.setTitle(R.string.sound_off);
            soundItem.setIcon(R.drawable.ic_audio_vol_mute);
        }

        MenuItem vibrateItem = menu.findItem(R.id.vibrate);
        if (options.isVibrate()) {
            vibrateItem.setTitle(R.string.vibrate_on);
            vibrateItem.setIcon(R.drawable.ic_vibrate);
        } else {
            vibrateItem.setTitle(R.string.vibrate_off);
            vibrateItem.setIcon(R.drawable.ic_vibrate_off);
        }

        MenuItem themeItem = menu.findItem(R.id.theme);

        if (options.getThemeName().equals("zombie")) {
            themeItem.setTitle(R.string.theme_zombie);
            themeItem.setIcon(R.drawable.ic_theme_zombie);
        } else {
            themeItem.setTitle( R.string.theme_cat);
            themeItem.setIcon( R.drawable.ic_theme_cat);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sound:
                options.setSound(!options.isSound());
                item.setTitle(options.isSound() ? R.string.sound_on : R.string.sound_off);
                item.setIcon(options.isSound() ? R.drawable.ic_audio_vol : R.drawable.ic_audio_vol_mute);
                break;
            case R.id.theme:
                options.setThemeName(options.getThemeName().equals("zombie") ? "cat" : "zombie");
                item.setTitle(options.getThemeName().equals("zombie") ? R.string.theme_zombie : R.string.theme_cat);
                item.setIcon(options.getThemeName().equals("zombie") ? R.drawable.ic_theme_zombie : R.drawable.ic_theme_cat);
                restart();
                break;
            case R.id.quality:
                options.setQuality(!options.isQuality());
                item.setTitle(options.isQuality() ? R.string.quality_high : R.string.quality_low);
                restart();
                break;
            case R.id.vibrate:
                options.setVibrate(!options.isVibrate());
                item.setTitle(options.isVibrate() ? R.string.vibrate_on : R.string.vibrate_off);
                item.setIcon(options.isVibrate() ? R.drawable.ic_vibrate : R.drawable.ic_vibrate_off);
                break;
            case R.id.reset:
                Game.getInstance().resetScore();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void restart() {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags()));
        System.exit(2);
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

    public Options getOptions() {
        return options;
    }
}