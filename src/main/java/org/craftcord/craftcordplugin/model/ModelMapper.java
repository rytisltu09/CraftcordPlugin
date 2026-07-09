package org.craftcord.craftcordplugin.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class ModelMapper {
    public PlayerModel toPlayerModel(Player player) {
        Location location = player.getLocation();
        return new PlayerModel(
                player.getUniqueId().toString(),
                player.getName(),
                player.getHealth(),
                player.getWorld().getName(),
                new LocationModel(
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch()
                )
        );
    }
}

