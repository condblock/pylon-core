package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

sealed interface PylonEntity<E : Entity> : Keyed {

    val uuid: UUID

    val schema: PylonEntitySchema
        get() = PylonRegistry.ENTITIES.getOrThrow(key)

    var location: Location

    fun remove()

    @ApiStatus.Internal
    fun save()

    companion object {

        @JvmStatic
        fun register(key: NamespacedKey, entityClass: Class<*>, pylonEntityClass: Class<out PylonEntity<*>>) {
            PylonRegistry.ENTITIES.register(PylonEntitySchema(key, entityClass, pylonEntityClass))
        }
    }
}