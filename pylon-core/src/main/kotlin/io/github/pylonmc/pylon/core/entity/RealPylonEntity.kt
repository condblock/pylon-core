package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.waila.WailaConfig
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer

abstract class RealPylonEntity<E: Entity>(val entity: E) : PylonEntity {

    private val key = entity.persistentDataContainer.get(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY)
        ?: throw IllegalStateException("Entity did not have a Pylon key; did you mean to call PylonEntity(NamespacedKey, Entity) instead of PylonEntity(Entity)?")
    final override val uuid = entity.uniqueId

    final override var location: Location
        get() = entity.location
        set(value) { entity.teleport(value) }

    constructor(key: NamespacedKey, entity: E): this(initialisePylonEntity<E>(key, entity))

    open fun getWaila(player: Player): WailaConfig? = null

    /**
     * Write all the state saved in the Pylon entity class to the entity's persistent data
     * container.
     */
    open fun write(pdc: PersistentDataContainer) {}

    fun getSettings(): Config
            = Settings.get(key)

    final override fun remove() {
        entity.remove()
    }

    final override fun save() {
        write(entity.persistentDataContainer)
    }

    override fun getKey() = key

    companion object {

        private val pylonEntityKeyKey = pylonKey("pylon_entity_key")

        private fun <E: Entity> initialisePylonEntity(key: NamespacedKey, entity: E): E {
            entity.persistentDataContainer.set(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY, key)
            return entity
        }

        @JvmSynthetic
        internal fun deserialize(entity: Entity): PylonEntity? {
            // Stored outside of the try block so it is displayed in error messages once acquired
            var key: NamespacedKey? = null

            try {
                key = entity.persistentDataContainer.get(pylonEntityKeyKey, PylonSerializers.NAMESPACED_KEY)
                    ?: return null

                // We fail silently here because this may trigger if an addon is removed or fails to load.
                // In this case, we don't want to delete the data, and we also don't want to spam errors.
                val schema = PylonRegistry.ENTITIES[key] as? PylonEntitySchema.Real
                    ?: return null

                if (!schema.entityClass.isInstance(entity)) {
                    return null
                }

                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                return schema.loadConstructor.invoke(entity) as PylonEntity

            } catch (t: Throwable) {
                PylonCore.logger.severe("Error while loading entity $key with UUID ${entity.uniqueId}")
                t.printStackTrace()
                return null
            }
        }
    }
}