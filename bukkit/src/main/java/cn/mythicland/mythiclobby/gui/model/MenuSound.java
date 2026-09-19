package cn.mythicland.mythiclobby.gui.model;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Objects;

public record MenuSound(Sound sound, float volume, float pitch) {
    public MenuSound(Sound sound, float volume, float pitch) {
        this.sound = Objects.requireNonNull(sound, "sound");
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
