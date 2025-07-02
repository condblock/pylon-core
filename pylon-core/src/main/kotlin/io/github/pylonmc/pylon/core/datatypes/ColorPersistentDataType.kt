package io.github.pylonmc.pylon.core.datatypes

import org.bukkit.Color
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object ColorPersistentDataType : PersistentDataType<Int, Color> {
    override fun getPrimitiveType(): Class<Int> = Int::class.java
    override fun getComplexType(): Class<Color> = Color::class.java

    override fun toPrimitive(complex: Color, context: PersistentDataAdapterContext): Int {
        return complex.asARGB()
    }

    override fun fromPrimitive(primitive: Int, context: PersistentDataAdapterContext): Color {
        return Color.fromARGB(primitive)
    }
}