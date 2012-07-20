package com.jelastic.energy.zombie.themes;

import android.graphics.Color;
import com.jelastic.energy.zombie.Game;
import com.jelastic.energy.zombie.GameActivity;
import com.jelastic.energy.zombie.Layout;
import org.andengine.entity.scene.background.IBackground;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;

public class CatTheme extends AbstractTheme {

    private IBackground background;

    {
        id = "cat";
    }

    @Override
    public IBackground getBackground() {
        if (background == null) {
            AssetBitmapTextureAtlasSource atlas = AssetBitmapTextureAtlasSource.create(GameActivity.self.getAssets(), "gfx/cat/background.png");
            Layout layout = Game.self.getLayout();
            int cameraWidth = layout.getWidth();
            int cameraHeight = layout.getHeight();
            background = new RepeatingSpriteBackground(cameraWidth, cameraHeight, GameActivity.self.getTextureManager(), atlas, GameActivity.self.getVertexBufferObjectManager());
        }
        return background;
    }

    @Override
    public int getTextColor() {
        return Color.WHITE;
    }
}