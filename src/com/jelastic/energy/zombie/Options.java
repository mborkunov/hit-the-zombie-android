package com.jelastic.energy.zombie;

import android.content.SharedPreferences;
import com.jelastic.energy.zombie.util.Resources;
import org.andengine.opengl.texture.TextureOptions;

public class Options {

    public static final String THEME_STRING = "theme";
    public static final String SOUND_STRING = "sound";
    public static final String VIBRATE_STRING = "vibrate";
    public static final String QUALITY_STRING = "quality";
    public static final String DEFAULT_THEME = "zombie";

    protected SharedPreferences settings;

    public Options(SharedPreferences settings) {
        this.settings = settings;
    }

    public boolean isQuality() {
        return settings.getBoolean(QUALITY_STRING, true);
    }

    public void setQuality(boolean quality) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(QUALITY_STRING, quality);
        editor.commit();

        Resources.textureOptions = isQuality() ? TextureOptions.BILINEAR : TextureOptions.NEAREST;
        Resources.fontAntiAliasing = isQuality();
    }

    public boolean isSound() {
        return settings.getBoolean(SOUND_STRING, true);
    }

    public boolean isVibrate() {
        return settings.getBoolean(VIBRATE_STRING, true);
    }

    public void setVibrate(boolean vibrate) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(VIBRATE_STRING, vibrate);
        editor.commit();
    }

    public void setSound(boolean sound) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SOUND_STRING, sound);
        editor.commit();
    }

    public void setThemeName(String theme) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(THEME_STRING, theme);
        editor.commit();
    }

    public String getThemeName() {
        return settings.getString(THEME_STRING, DEFAULT_THEME);
    }
}
