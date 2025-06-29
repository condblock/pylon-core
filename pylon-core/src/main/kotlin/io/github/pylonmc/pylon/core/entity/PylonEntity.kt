package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import java.util.UUID

sealed interface PylonEntity<E : Entity> : Keyed {

    val uuid: UUID

    val schema: PylonEntitySchema
        get() = PylonRegistry.ENTITIES.getOrThrow(key)

    fun save()

    companion object {

        @JvmStatic
        fun register(key: NamespacedKey, entityClass: Class<*>, pylonEntityClass: Class<PylonEntity<*>>) {
            PylonRegistry.ENTITIES.register(PylonEntitySchema(key, entityClass, pylonEntityClass))
        }

        @JvmSynthetic
        internal fun serialize(pylonEntity: PylonEntity<*>) {
            when (pylonEntity) {
                is RealPylonEntity<*> -> RealPylonEntity.serialize(pylonEntity)
            }
        }
    }
}