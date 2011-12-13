package com.jelastic.energy;

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;


public class Target extends Sprite {

    public Target(float pX, float pY, TextureRegion pTextureRegion) {
        super(pX, pY, pTextureRegion);
    }

    public Target(float pX, float pY, float pWidth, float pHeight, TextureRegion pTextureRegion) {
        super(pX, pY, pWidth, pHeight, pTextureRegion);
    }

    public Target(float pX, float pY, TextureRegion pTextureRegion, RectangleVertexBuffer pRectangleVertexBuffer) {
        super(pX, pY, pTextureRegion, pRectangleVertexBuffer);
    }

    public Target(float pX, float pY, float pWidth, float pHeight, TextureRegion pTextureRegion, RectangleVertexBuffer pRectangleVertexBuffer) {
        super(pX, pY, pWidth, pHeight, pTextureRegion, pRectangleVertexBuffer);
    }
}
