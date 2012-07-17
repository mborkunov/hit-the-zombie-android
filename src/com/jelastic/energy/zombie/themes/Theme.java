package com.jelastic.energy.zombie.themes;

import com.jelastic.energy.zombie.util.GameSound;
import org.andengine.entity.scene.background.IBackground;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;

public interface Theme {

    void load();

    Font getFont();
    IBackground getBackground();
    TiledTextureRegion getTiles();
    TextureRegion getStartButton();
    TextureRegion getShareButton();

    GameSound getHitSound();
    GameSound getFailSound();
    GameSound getButtonSound();
    int getTextColor();
}
