package com.jelastic.energy.zombie.util;

import com.jelastic.energy.zombie.GameActivity;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import java.io.IOException;

public class Resources {

    public static TextureOptions textureOptions;
    public static boolean fontAntiAliasing;

    public static void init() {
        textureOptions = GameActivity.self.isQuality() ? TextureOptions.BILINEAR : TextureOptions.NEAREST;
        fontAntiAliasing = GameActivity.self.isQuality();

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        SoundFactory.setAssetBasePath("sounds/");
        FontFactory.setAssetBasePath( "font/");
    }

    public static TiledTextureRegion loadTexture(BaseGameActivity ctx, String path, int atlasWidth, int atlasHeight, int cols, int rows) {
        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(atlasWidth, atlasHeight, textureOptions);
        TiledTextureRegion region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, ctx, path, 0, 0, cols, rows);
        ctx.getEngine().getTextureManager().loadTexture(textureAtlas);

        return region;
    }

    public static TextureRegion loadTexture(BaseGameActivity ctx, String path, int atlasWidth, int atlasHeight) {
        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(atlasWidth, atlasHeight, textureOptions);
        TextureRegion region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, ctx, path, 0, 0);
        ctx.getEngine().getTextureManager().loadTexture(textureAtlas);
        return region;
    }


    public static GameSound loadSound(String path, BaseGameActivity ctx) {
        try {
            return new GameSound(SoundFactory.createSoundFromAsset(ctx.getEngine().getSoundManager(), ctx, path));
        } catch (IOException e) {
            Debug.e(e);
            return null;
        }
    }

    public static Font loadFont(GameActivity game, String path, int atlasWidth, int atlasHeight, int height, int color) {
        BitmapTextureAtlas mFontTexture = new BitmapTextureAtlas(atlasWidth, atlasHeight, textureOptions);
        Font font = FontFactory.createFromAsset(mFontTexture, game.getApplicationContext(), path, height, fontAntiAliasing, color);
        game.getTextureManager().loadTexture(mFontTexture);
        game.getEngine().getFontManager().loadFont(font);
        return font;
    }
}
