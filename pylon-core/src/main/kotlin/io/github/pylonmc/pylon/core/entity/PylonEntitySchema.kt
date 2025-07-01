package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.util.findConstructorMatching
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import java.lang.invoke.MethodHandle
import java.util.UUID


sealed class PylonEntitySchema(private val key: NamespacedKey) : Keyed {

    class Real(
        key: NamespacedKey,
        val entityClass: Class<*>,
        pylonEntityClass: Class<out RealPylonEntity<*>>
    ) : PylonEntitySchema(key) {
        override val loadConstructor: MethodHandle = pylonEntityClass.findConstructorMatching(entityClass)
        ?: throw NoSuchMethodException("Entity '$key' (${pylonEntityClass.simpleName}) is missing a load constructor (${entityClass.simpleName})")
    }

    class Packet(
        key: NamespacedKey,
        val entityType: EntityType,
        @JvmSynthetic internal val pylonEntityClass: Class<out PacketPylonEntity>
    ) : PylonEntitySchema(key) {
        override val loadConstructor: MethodHandle = pylonEntityClass.findConstructorMatching(
            UUID::class.java,
            WrapperEntity::class.java,
            Location::class.java
        ) ?: throw NoSuchMethodException(
            "Entity '$key' (${pylonEntityClass.simpleName}) is missing a load constructor (UUID, WrapperEntity, Location)"
        )
    }

    internal abstract val loadConstructor: MethodHandle

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonEntitySchema)?.key

    override fun hashCode(): Int = key.hashCode()
}