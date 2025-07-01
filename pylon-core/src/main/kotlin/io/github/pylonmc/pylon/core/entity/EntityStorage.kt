package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityLoadEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.EntitiesLoadEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Consumer
import kotlin.random.Random


object EntityStorage : Listener {

    private val entities: MutableMap<UUID, PylonEntity> = ConcurrentHashMap()
    private val entitiesByKey: MutableMap<NamespacedKey, MutableSet<PylonEntity>> = ConcurrentHashMap()
    private val entityAutosaveTasks: MutableMap<UUID, Job> = ConcurrentHashMap()
    private val whenEntityLoadsTasks: MutableMap<UUID, MutableSet<Consumer<PylonEntity>>> = ConcurrentHashMap()
    private val packetEntities: MutableMap<ChunkPosition, MutableSet<PacketPylonEntity>> = ConcurrentHashMap()

    val loadedEntities: Collection<PylonEntity>
        get() = entities.values

    // Access to entities, entitiesById fields must be synchronized to prevent them
    // briefly going out of sync
    private val entityLock = ReentrantReadWriteLock()

    @JvmStatic
    fun get(uuid: UUID): PylonEntity? = lockEntityRead { entities[uuid] }

    @JvmStatic
    fun get(entity: Entity): PylonEntity? = get(entity.uniqueId)

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, uuid: UUID): T? {
        val entity = get(uuid) ?: return null
        if (!clazz.isInstance(entity)) {
            return null
        }
        return clazz.cast(entity)
    }

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, entity: Entity): T? = getAs(clazz, entity.uniqueId)

    inline fun <reified T> getAs(uuid: UUID): T? = getAs(T::class.java, uuid)

    inline fun <reified T> getAs(entity: Entity): T? = getAs(T::class.java, entity)

    @JvmStatic
    fun getByKey(key: NamespacedKey): Collection<PylonEntity> =
        if (key in PylonRegistry.ENTITIES) {
            lockEntityRead {
                entitiesByKey[key].orEmpty()
            }
        } else {
            emptySet()
        }

    /**
     * Schedules a task to run when the entity with id [uuid] is loaded, or runs the task immediately
     * if the entity is already loaded
     */
    @JvmStatic
    fun whenEntityLoads(uuid: UUID, consumer: Consumer<PylonEntity>) {
        val pylonEntity = get(uuid)
        if (pylonEntity != null) {
            consumer.accept(pylonEntity)
        } else {
            whenEntityLoadsTasks.getOrPut(uuid) { mutableSetOf() }.add {
                consumer.accept(it)
            }
        }

    }

    /**
     * Schedules a task to run when the entity with id [uuid] is loaded, or runs the task immediately
     * if the entity is already loaded
     */
    @JvmStatic
    fun <T : PylonEntity> whenEntityLoads(uuid: UUID, clazz: Class<T>, consumer: Consumer<T>) {
        val pylonEntity = getAs(clazz, uuid)
        if (pylonEntity != null) {
            consumer.accept(pylonEntity)
        } else {
            whenEntityLoadsTasks.getOrPut(uuid) { mutableSetOf() }.add {
                consumer.accept(
                    getAs(clazz, uuid)
                        ?: throw IllegalStateException("Entity $uuid was not of expected type ${clazz.simpleName}")
                )
            }
        }
    }

    /**
     * Schedules a task to run when the entity with id [uuid] is loaded, or runs the task immediately
     * if the entity is already loaded
     */
    @JvmStatic
    inline fun <reified T : PylonEntity> whenEntityLoads(uuid: UUID, noinline consumer: (T) -> Unit) =
        whenEntityLoads(uuid, T::class.java, consumer)

    @JvmStatic
    fun isPylonEntity(uuid: UUID): Boolean = get(uuid) != null

    @JvmStatic
    fun isPylonEntity(entity: Entity): Boolean = get(entity) != null

    @JvmStatic
    fun add(entity: PylonEntity): PylonEntity = lockEntityWrite {
        entities[entity.uuid] = entity
        entitiesByKey.getOrPut(entity.schema.key, ::mutableSetOf).add(entity)

        if (entity is PacketPylonEntity) {
            packetEntities.getOrPut(entity.location.chunk.position, ::mutableSetOf).add(entity)
        }

        // autosaving
        entityAutosaveTasks[entity.uuid] = PylonCore.launch {

            // Wait a random delay before starting, this is to help smooth out lag from saving
            delay(Random.nextLong(PylonConfig.entityDataAutosaveIntervalSeconds * 1000))

            while (true) {
                lockEntityWrite {
                    entity.save()
                }
                delay(PylonConfig.entityDataAutosaveIntervalSeconds * 1000)
            }
        }
        entity
    }

    @JvmSynthetic
    internal fun remove(entity: PylonEntity) = lockEntityWrite {
        if (entities.remove(entity.uuid) != null) {
            entitiesByKey[entity.schema.key]!!.remove(entity)
            if (entitiesByKey[entity.schema.key]!!.isEmpty()) {
                entitiesByKey.remove(entity.schema.key)
            }
            entityAutosaveTasks.remove(entity.uuid)?.cancel()
            if (entity is PacketPylonEntity) {
                packetEntities[entity.location.chunk.position]?.remove(entity)
            }
        }
    }

    @JvmSynthetic
    internal fun movePacketEntity(entity: PacketPylonEntity, oldLocation: Location, newLocation: Location) {
        val oldChunk = oldLocation.chunk.position
        val newChunk = newLocation.chunk.position
        if (oldChunk == newChunk) return
        lockEntityWrite {
            packetEntities[oldChunk]?.remove(entity)
            packetEntities.getOrPut(newChunk, ::mutableSetOf).add(entity)
        }
    }

    private fun loadEntity(entity: PylonEntity) {
        add(entity)

        val tasks = whenEntityLoadsTasks[entity.uuid]
        if (tasks != null) {
            for (task in tasks) {
                try {
                    task.accept(entity)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            whenEntityLoadsTasks.remove(entity.uuid)
        }

        PylonEntityLoadEvent(entity).callEvent()
    }

    @EventHandler
    private fun onEntityLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            val pylonEntity = RealPylonEntity.deserialize(entity) ?: continue
            loadEntity(pylonEntity)
        }
    }

    // This currently does not differentiate between unloaded and dead entities because the API
    // is broken (lol), hence the lack of an entity death listener
    @EventHandler
    private fun onEntityUnload(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = get(event.entity.uniqueId) ?: return

        if (!event.entity.isDead) {
            pylonEntity.save()
            PylonEntityUnloadEvent(pylonEntity).callEvent()
        } else {
            PylonEntityDeathEvent(pylonEntity, event).callEvent()
        }

        remove(pylonEntity)
    }

    @EventHandler
    private fun onChunkLoad(event: ChunkLoadEvent) {
        val entities = event.chunk.persistentDataContainer.get(
            PacketPylonEntity.packetEntitiesKey,
            PacketPylonEntity.packetEntitiesType
        ) ?: return

        for ((uuid, entity) in entities) {
            if (get(uuid) != null) continue // Already loaded
            loadEntity(entity)
        }
        packetEntities[event.chunk.position] = entities.values.toMutableSet()
    }

    @EventHandler
    private fun onChunkUnload(event: ChunkLoadEvent) {
        val entities = packetEntities.remove(event.chunk.position) ?: return
        for (entity in entities) {
            PylonEntityUnloadEvent(entity).callEvent()
            remove(entity)
        }
        event.chunk.persistentDataContainer.set(
            PacketPylonEntity.packetEntitiesKey,
            PacketPylonEntity.packetEntitiesType,
            entities.associateBy { it.uuid }
        )
    }

    @JvmSynthetic
    internal fun cleanup(addon: PylonAddon) = lockEntityWrite {
        for ((_, value) in entitiesByKey.filter { it.key.isFromAddon(addon) }) {
            for (entity in value) {
                entity.save()
            }
        }

        entities.values.removeIf { it.schema.key.isFromAddon(addon) }
        entitiesByKey.keys.removeIf { it.isFromAddon(addon) }
    }

    @JvmSynthetic
    internal fun cleanupEverything() {
        for (entity in entities.values) {
            entity.save()
        }
    }

    private inline fun <T> lockEntityRead(block: () -> T): T {
        entityLock.readLock().lock()
        try {
            return block()
        } finally {
            entityLock.readLock().unlock()
        }
    }

    private inline fun <T> lockEntityWrite(block: () -> T): T {
        entityLock.writeLock().lock()
        try {
            return block()
        } finally {
            entityLock.writeLock().unlock()
        }
    }
}