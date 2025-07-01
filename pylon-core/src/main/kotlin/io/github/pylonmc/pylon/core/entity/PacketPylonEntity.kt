package io.github.pylonmc.pylon.core.entity

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import com.github.retrooper.packetevents.protocol.world.Location as ProtocolLocation

abstract class PacketPylonEntity(
    private val key: NamespacedKey,
    val entity: WrapperEntity,
    location: Location
) : PylonEntity {

    final override val uuid = entity.uuid

    final override var location = location
        set(value) {
            EntityStorage.movePacketEntity(this, field, value)
            field = value
            entity.teleport(value.toProtocolLocation())
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
        fun <E : PacketPylonEntity> spawn(clazz: Class<E>, location: Location): E {
            val schema = PylonRegistry.ENTITIES.filterIsInstance<PylonEntitySchema.Packet>()
                .find { it.pylonEntityClass == clazz }
                ?: throw IllegalArgumentException("No packet entity schema found for class ${clazz.name}")

            val protocolType = EntityTypes.getByName(schema.entityType.key.toString())
            val entity = WrapperEntity(UUID.randomUUID(), protocolType)
            entity.spawn(location.toProtocolLocation())

            @Suppress("UNCHECKED_CAST")
            val pylonEntity = schema.loadConstructor.invoke(schema.key, entity, location) as E
            EntityStorage.add(pylonEntity)
            return pylonEntity
        }
    }

    private object PDT : PersistentDataType<PersistentDataContainer, PacketPylonEntity> {

        private val keyKey = pylonKey("key")
        private val uuidKey = pylonKey("uuid")
        private val locationKey = pylonKey("location")
        private val entityTypeKey = pylonKey("entity_type")

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

            val wrapper = WrapperEntity(uuid, EntityTypes.getByName(entityType))
            val schema = PylonRegistry.ENTITIES[key] as PylonEntitySchema.Packet
            return schema.loadConstructor.invoke(key, wrapper, location) as PacketPylonEntity
        }
    }
}

private fun Location.toProtocolLocation(): ProtocolLocation {
    return ProtocolLocation(this.x, this.y, this.z, this.yaw, this.pitch)
}