package io.github.pylonmc.pylon.core.entity

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.EntityLib
import me.tofaa.entitylib.spigot.SpigotEntityLibPlatform
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import com.github.retrooper.packetevents.protocol.world.Location as ProtocolLocation

/**
 * Unlike "real" entities, packet entities only persist three things: UUID, location, and type.
 */
open class PacketPylonEntity private constructor(
    val entity: WrapperEntity,
    private val key: NamespacedKey,
    location: Location,
    @Suppress("unused") marker: Nothing? // used to make this constructor different from the secondary
) : PylonEntity {

    constructor(entity: WrapperEntity, key: NamespacedKey, location: Location) : this(
        entity,
        key,
        location,
        null
    )

    constructor(entity: WrapperEntity, pdc: PersistentDataContainer) : this(
        entity,
        pdc.get(PDT.keyKey, PylonSerializers.NAMESPACED_KEY)!!,
        pdc.get(PDT.locationKey, PylonSerializers.LOCATION)!!,
        null
    )

    final override val uuid = entity.uuid

    final override var location = location
        set(value) {
            EntityStorage.movePacketEntity(this, field, value)
            field = value
            entity.teleport(value.toProtocolLocation())
        }

    open fun write(pdc: PersistentDataContainer) {}

    fun addViewers(vararg viewers: Player) {
        for (viewer in viewers) {
            entity.addViewer(viewer.uniqueId)
        }
    }

    fun removeViewers(vararg viewers: Player) {
        for (viewer in viewers) {
            entity.removeViewer(viewer.uniqueId)
        }
    }

    final override fun remove() {
        EntityStorage.remove(this)
        entity.remove()
    }

    final override fun save() {
        val chunkPdc = location.chunk.persistentDataContainer
        val entities = chunkPdc.get(packetEntitiesKey, packetEntitiesType)?.toMutableMap() ?: mutableMapOf()
        entities[uuid] = this
        chunkPdc.set(packetEntitiesKey, packetEntitiesType, entities)
    }

    override fun getKey() = key

    companion object {

        @JvmSynthetic
        internal val packetEntitiesKey = pylonKey("packet_entities")

        @JvmSynthetic
        internal val packetEntitiesType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.UUID, PDT)

        @JvmStatic
        fun spawn(key: NamespacedKey, location: Location): PacketPylonEntity {
            val schema = PylonRegistry.ENTITIES[key] as? PylonEntitySchema.Packet
                ?: throw IllegalArgumentException("No such entity schema registered for key: $key")

            val protocolType = SpigotConversionUtil.fromBukkitEntityType(schema.entityType)
            val entity = WrapperEntity(UUID.randomUUID(), protocolType)
            entity.spawn(location.toProtocolLocation())

            val pylonEntity = schema.createConstructor.invoke(entity, schema.key, location) as PacketPylonEntity
            EntityStorage.add(pylonEntity)
            return pylonEntity
        }
    }

    private object PDT : PersistentDataType<PersistentDataContainer, PacketPylonEntity> {

        val keyKey = pylonKey("key")
        val uuidKey = pylonKey("uuid")
        val locationKey = pylonKey("location")
        val entityTypeKey = pylonKey("entity_type")

        override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
        override fun getComplexType(): Class<PacketPylonEntity> = PacketPylonEntity::class.java

        override fun toPrimitive(
            complex: PacketPylonEntity,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            val container = context.newPersistentDataContainer()
            container.set(keyKey, PylonSerializers.NAMESPACED_KEY, complex.getKey())
            container.set(uuidKey, PylonSerializers.UUID, complex.uuid)
            container.set(locationKey, PylonSerializers.LOCATION, complex.location)
            container.set(entityTypeKey, PylonSerializers.STRING, complex.entity.entityType.name.toString())
            complex.write(container)
            return container
        }

        override fun fromPrimitive(
            primitive: PersistentDataContainer,
            context: PersistentDataAdapterContext
        ): PacketPylonEntity {
            val key = primitive.get(keyKey, PylonSerializers.NAMESPACED_KEY)!!
            val uuid = primitive.get(uuidKey, PylonSerializers.UUID)!!
            val location = primitive.get(locationKey, PylonSerializers.LOCATION)!!
            val entityType = primitive.get(entityTypeKey, PylonSerializers.STRING)!!

            val wrapper = EntityLib.getApi<SpigotEntityLibPlatform>().getEntity(uuid)
                ?: WrapperEntity(uuid, EntityTypes.getByName(entityType)).also { it.spawn(location.toProtocolLocation()) }
            val schema = PylonRegistry.ENTITIES[key] as PylonEntitySchema.Packet
            return schema.loadConstructor.invoke(wrapper, primitive) as PacketPylonEntity
        }
    }
}

private fun Location.toProtocolLocation(): ProtocolLocation {
    return ProtocolLocation(this.x, this.y, this.z, this.yaw, this.pitch)
}