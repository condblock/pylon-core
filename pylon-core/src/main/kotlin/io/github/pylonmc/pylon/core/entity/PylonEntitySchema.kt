package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.util.findConstructorMatching
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Keyed
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataContainer
import java.lang.invoke.MethodHandle


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

        val createConstructor: MethodHandle = pylonEntityClass.findConstructorMatching(
            WrapperEntity::class.java,
            NamespacedKey::class.java,
            Location::class.java
        ) ?: throw NoSuchMethodException(
            "Entity '$key' (${pylonEntityClass.simpleName}) is missing a create constructor (WrapperEntity, NamespacedKey, Location)"
        )

        override val loadConstructor: MethodHandle = pylonEntityClass.findConstructorMatching(
            WrapperEntity::class.java,
            PersistentDataContainer::class.java
        ) ?: throw NoSuchMethodException(
            "Entity '$key' (${pylonEntityClass.simpleName}) is missing a load constructor (WrapperEntity, PersistentDataContainer)"
        )
    }

    internal abstract val loadConstructor: MethodHandle

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = key == (other as? PylonEntitySchema)?.key

    override fun hashCode(): Int = key.hashCode()
}