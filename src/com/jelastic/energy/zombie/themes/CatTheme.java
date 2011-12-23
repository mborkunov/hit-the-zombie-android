package com.jelastic.energy.zombie.themes;

import android.graphics.Color;
import com.jelastic.energy.zombie.GameActivity;
import org.anddev.andengine.entity.scene.background.IBackground;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;

public class CatTheme extends AbstractTheme {

    private IBackground background;

    {
        id = "cat";
    }

    @Override
    public IBackground getBackground() {
        if (background == null) {
            AssetBitmapTextureAtlasSource atlas = new AssetBitmapTextureAtlasSource(GameActivity.self, "gfx/cat/background.png");
            background = new RepeatingSpriteBackground(GameActivity.self.getWidth(), GameActivity.self.getHeight(), GameActivity.self.getTextureManager(), atlas);
        }
        return background;
    }

    @Override
    public int getTextColor() {
        return Color.WHITE;
    }
}