package com.jelastic.energy.zombie.themes;

import android.graphics.Color;
import com.jelastic.energy.zombie.Game;
import com.jelastic.energy.zombie.GameActivity;
import com.jelastic.energy.zombie.Layout;
import com.jelastic.energy.zombie.util.Resources;
import org.andengine.entity.scene.background.IBackground;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;

public class ZombieTheme extends AbstractTheme {

    private IBackground background;

    {
        id = "zombie";
    }

    @Override
    public IBackground getBackground() {
        if (background == null) {
            TextureRegion backgroundTexture = Resources.loadTexture(GameActivity.self, "zombie/background.png", 512, 512);

            Sprite sprite = new Sprite(0, 0, backgroundTexture, GameActivity.self.getVertexBufferObjectManager());
            Layout layout = Game.self.getLayout();
            float scaleX = layout.getWidth() / (float) backgroundTexture.getWidth(),
                  scaleY = layout.getHeight() / (float) backgroundTexture.getHeight();
            sprite.setScaleCenter(0, 0);
            sprite.setScale(scaleX, scaleY);
            background = new SpriteBackground(sprite);
        }

        return background;
    }

    @Override
    public int getTextColor() {
        return Color.YELLOW;
    }
}
