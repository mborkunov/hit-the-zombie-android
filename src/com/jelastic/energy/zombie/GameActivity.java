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

    protected Overlay overlay;

    protected Text scoreText;
    protected Text timerText;


    public static GameActivity self;
    protected SharedPreferences settings;
    public Theme theme;
    private Options options;
    private Game game;

    {
        self = this;
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        Display display = getWindowManager().getDefaultDisplay();

        settings = getSharedPreferences("settings", 0);
        options = new Options(settings);
        game = Game.self;
        Layout layout = new Layout(display);
        game.setLayout(layout);

        final Camera camera = new Camera(0, 0, layout.getWidth(), layout.getHeight());
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

    @Override
    public void onCreateResources() {
        mEngine.registerUpdateHandler(new FPSLogger());
        Resources.init();
        theme = options.getThemeName().equals("zombie") ? new ZombieTheme() : new CatTheme();
        theme.load();

        // font
        mFont = Resources.loadFont(this, "andy.ttf", 512, 256, game.getLayout().getFontHeight(), theme.getTextColor());

        boolean multiTouch = MultiTouch.isSupported(getApplicationContext());
        if (multiTouch) {
            mEngine.setTouchController(new MultiTouchController());
        }
    }

    @Override
    public Scene onCreateScene() {
        final Scene scene = new Scene();
        Layout layout = game.getLayout();
        int size = layout.getTargetSize();
        int dx = layout.getDeltaX(), dy = layout.getDeltaY();

        int topOffset = layout.getOffsetY();
        int leftOffset = layout.getOffsetX();
        int topTextOffset = layout.getTextOffsetY();

        scoreText = new Text(leftOffset, topTextOffset, mFont, getString(R.string.score)  + ": " + game.getScore(), 11, getVertexBufferObjectManager());
        timerText = new Text(scoreText.getWidth() + leftOffset, topTextOffset, mFont, " " + game.getTimeLeft() + " " + getString(R.string.sec), 7, getVertexBufferObjectManager());
        timerText.setVisible(false);
        scene.attachChild(scoreText);
        scene.attachChild(timerText);
        game.getTargets().clear();
        for (int j = 0; j < Game.ROWS; j++) {
            for (int i = 0; i < Game.COLS; i++) {
                final Target targetSprite = new Target(leftOffset + (size + dx) * i, topOffset + (size + dy) * j, theme.getTiles().deepCopy());
                targetSprite.setWidth(layout.getTargetSize());
                targetSprite.setHeight(layout.getTargetSize());
                targetSprite.setScaleCenterX(layout.getTargetSize() / 2);
                targetSprite.setZIndex(2);
                scene.attachChild(targetSprite);
                game.addTarget(targetSprite);
            }
        }
        scene.setBackground(theme.getBackground());
        scene.registerUpdateHandler(game.getUpdateHandler());
        overlay = new Overlay(this, 0, 0, layout.getWidth(), layout.getHeight());
        scene.attachChild(overlay);
        game.addObserver(this);
        return scene;
    }

    @Override
    public synchronized void onGameCreated() {
        overlay.onLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        String offString = getString(R.string.off);
        String onString = getString(R.string.on);
        String qualityString = getString(R.string.quality);
        String soundString = getString(R.string.sound);
        String delimiter = " - ";

        MenuItem qualityItem = menu.findItem(R.id.quality);


        qualityItem.setTitle(qualityString + delimiter + getString(options.isQuality() ? R.string.high : R.string.low));

        MenuItem soundItem = menu.findItem(R.id.sound);

        if (options.isSound()) {
            soundItem.setTitle(soundString + delimiter + onString);
            soundItem.setIcon(R.drawable.ic_audio_vol);
        } else {
            soundItem.setTitle(soundString + delimiter + offString);
            soundItem.setIcon(R.drawable.ic_audio_vol_mute);
        }

        MenuItem vibrateItem = menu.findItem(R.id.vibrate);
        if (options.isVibrate()) {
            vibrateItem.setTitle(getString(R.string.vibrate) + delimiter + onString);
            vibrateItem.setIcon(R.drawable.ic_vibrate);
        } else {
            vibrateItem.setTitle(getString(R.string.vibrate) + delimiter + onString);
            vibrateItem.setIcon(R.drawable.ic_vibrate_off);
        }

        MenuItem themeItem = menu.findItem(R.id.theme);

        if (options.getThemeName().equals("zombie")) {
            themeItem.setTitle(getString(R.string.theme) + delimiter + getString(R.string.zombie));
            themeItem.setIcon(R.drawable.ic_theme_zombie);
        } else {
            themeItem.setTitle(getString(R.string.theme) + delimiter + getString(R.string.cat));
            themeItem.setIcon( R.drawable.ic_theme_cat);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String offString = getString(R.string.off);
        String onString = getString(R.string.on);
        String qualityString = getString(R.string.quality);
        String soundString = getString(R.string.sound);
        String delimiter = " - ";

        switch (item.getItemId()) {
            case R.id.sound:
                options.setSound(!options.isSound());
                item.setTitle(soundString + delimiter + (options.isSound() ? onString : offString));
                item.setIcon(options.isSound() ? R.drawable.ic_audio_vol : R.drawable.ic_audio_vol_mute);
                break;
            case R.id.theme:
                options.setThemeName(options.getThemeName().equals("zombie") ? "cat" : "zombie");

                if (options.getThemeName().equals("zombie")) {
                    item.setTitle(getString(R.string.theme) + delimiter + getString(R.string.zombie));
                    item.setIcon(R.drawable.ic_theme_zombie);
                } else {
                    item.setTitle(getString(R.string.theme) + delimiter + getString(R.string.cat));
                    item.setIcon( R.drawable.ic_theme_cat);
                }
                restart();
                break;
            case R.id.quality:
                options.setQuality(!options.isQuality());
                item.setTitle(qualityString + delimiter + getString(options.isQuality() ? R.string.high : R.string.low));
                restart();
                break;
            case R.id.vibrate:
                options.setVibrate(!options.isVibrate());
                String vibrateString = getString(R.string.vibrate);
                item.setTitle(vibrateString + delimiter + (options.isVibrate() ? onString : offString));
                item.setIcon(options.isVibrate() ? R.drawable.ic_vibrate : R.drawable.ic_vibrate_off);
                break;
            case R.id.reset:
                game.resetScore();
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

    public Options getOptions() {
        return options;
    }
}