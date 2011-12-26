package com.jelastic.energy.zombie.themes;

import com.jelastic.energy.zombie.GameActivity;
import com.jelastic.energy.zombie.util.GameSound;
import com.jelastic.energy.zombie.util.Resources;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public abstract class AbstractTheme implements Theme {

    protected String id;
    private GameSound hitSound;
    private GameSound failSound;
    private GameSound buttonSound;
    private TextureRegion shareButton;
    private TextureRegion startButton;
    private TiledTextureRegion tiles;

    @Override
    public void load() {
        getBackground();
        getTiles();
        getStartButton();
        getShareButton();
        getHitSound();
        getButtonSound();
        getFailSound();
    }


    @Override
    public TiledTextureRegion getTiles() {
        if (tiles == null) {
            tiles = Resources.loadTexture(GameActivity.self, id + "/tiles.png", 1024, 128, 7, 1);
        }
        return tiles;
    }

    @Override
    public TextureRegion getStartButton() {
        if (startButton ==  null) {
            startButton = Resources.loadTexture(GameActivity.self, id + "/start.png", 256, 64);
        }
        return startButton;
    }

    @Override
    public TextureRegion getShareButton() {
        if (shareButton ==  null) {
            shareButton = Resources.loadTexture(GameActivity.self, id + "/share.png", 128, 128);
        }
        return shareButton;
    }

    @Override
    public GameSound getHitSound() {
        if (hitSound == null) {
            hitSound = Resources.loadSound(id + "/hit.ogg", GameActivity.self);
        }
        return hitSound;
    }

    @Override
    public GameSound getFailSound() {
        if (failSound == null) {
            failSound = Resources.loadSound(id + "/fail.ogg", GameActivity.self);
        }
        return failSound;
    }

    @Override
    public GameSound getButtonSound() {
        if (buttonSound == null) {
            buttonSound = Resources.loadSound(id + "/button.ogg", GameActivity.self);
        }
        return buttonSound;
    }
}
