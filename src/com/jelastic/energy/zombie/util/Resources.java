package com.jelastic.energy.zombie.util;

import android.content.Context;
import com.jelastic.energy.zombie.GameActivity;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import java.io.IOException;

public class Resources {

    public static TextureOptions textureOptions;
    public static boolean fontAntiAliasing;

    public static void init() {
        textureOptions = GameActivity.self.getOptions().isQuality() ? TextureOptions.BILINEAR : TextureOptions.NEAREST;
        fontAntiAliasing = GameActivity.self.getOptions().isQuality();

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        SoundFactory.setAssetBasePath("sounds/");
        FontFactory.setAssetBasePath( "font/");
    }

    public static TiledTextureRegion loadTexture(BaseGameActivity ctx, String path, int atlasWidth, int atlasHeight, int cols, int rows) {
        TextureManager textureManager = GameActivity.self.getTextureManager();
        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(textureManager, atlasWidth, atlasHeight, textureOptions);
        TiledTextureRegion region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, ctx, path, 0, 0, cols, rows);
        ctx.getEngine().getTextureManager().loadTexture(textureAtlas);

        return region;
    }

    public static TextureRegion loadTexture(BaseGameActivity ctx, String path, int atlasWidth, int atlasHeight) {
        TextureManager textureManager = GameActivity.self.getTextureManager();
        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(textureManager, atlasWidth, atlasHeight, textureOptions);
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
        BitmapTextureAtlas mFontTexture = new BitmapTextureAtlas(GameActivity.self.getTextureManager(), atlasWidth, atlasHeight, textureOptions);
        FontManager fontManager = GameActivity.self.getFontManager();
        Context applicationContext = game.getApplicationContext();
        Font font = FontFactory.createFromAsset(fontManager, mFontTexture, applicationContext.getAssets(), path, height, fontAntiAliasing, color);
        game.getTextureManager().loadTexture(mFontTexture);
        game.getEngine().getFontManager().loadFont(font);
        return font;
    }
}
