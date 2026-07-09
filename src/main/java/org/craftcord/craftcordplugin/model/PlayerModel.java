package org.craftcord.craftcordplugin.model;

public record PlayerModel(
        String uuid,
        String username,
        double health,
        String world,
        LocationModel location
) {
}

