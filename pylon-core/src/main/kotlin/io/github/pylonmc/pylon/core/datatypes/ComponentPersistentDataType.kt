package io.github.pylonmc.pylon.core.datatypes

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object ComponentPersistentDataType : PersistentDataType<String, Component> {
    override fun getPrimitiveType(): Class<String> = String::class.java
    override fun getComplexType(): Class<Component> = Component::class.java

    override fun toPrimitive(complex: Component, context: PersistentDataAdapterContext): String {
        return JSONComponentSerializer.json().serialize(complex)
    }

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): Component {
        return JSONComponentSerializer.json().deserialize(primitive)
    }
}