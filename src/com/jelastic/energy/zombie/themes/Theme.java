package com.jelastic.energy.zombie.themes;

import com.jelastic.energy.zombie.util.GameSound;
import org.anddev.andengine.entity.scene.background.IBackground;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public interface Theme {

    void load();

    IBackground getBackground();
    TiledTextureRegion getTiles();
    TextureRegion getStartButton();
    TextureRegion getShareButton();

    GameSound getHitSound();
    GameSound getFailSound();
    GameSound getButtonSound();
    int getTextColor();
}
