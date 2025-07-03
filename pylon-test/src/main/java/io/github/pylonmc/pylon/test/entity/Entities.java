package io.github.pylonmc.pylon.test.entity;

import io.github.pylonmc.pylon.core.entity.PylonEntity;
import org.bukkit.entity.LivingEntity;


public final class Entities {

    private Entities() {}

    public static void register() {
        PylonEntity.registerReal(SimpleEntity.KEY, LivingEntity.class, SimpleEntity.class);
    }
}
