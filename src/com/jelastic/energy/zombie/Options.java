package com.jelastic.energy.zombie;

import android.content.SharedPreferences;
import com.jelastic.energy.zombie.util.Resources;
import org.andengine.opengl.texture.TextureOptions;

public class Options {

    protected SharedPreferences settings;

    public Options(SharedPreferences settings) {
        this.settings = settings;
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

    public boolean isVibrate() {
        return settings.getBoolean("vibrate", true);
    }

    public void setVibrate(boolean vibrate) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("vibrate", vibrate);
        editor.commit();
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
}
