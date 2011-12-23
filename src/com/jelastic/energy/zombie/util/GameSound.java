package com.jelastic.energy.zombie.util;

import com.jelastic.energy.zombie.GameActivity;
import org.anddev.andengine.audio.sound.Sound;

public class GameSound {
    private Sound sound;

    public GameSound(Sound sound) {
        this.sound = sound;
    }

    public void play() {
        if (GameActivity.self.isSound()) {
            this.sound.play();
        }
    }
}
