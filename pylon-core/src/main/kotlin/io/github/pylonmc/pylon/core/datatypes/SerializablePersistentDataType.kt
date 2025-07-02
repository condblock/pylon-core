package io.github.pylonmc.pylon.core.datatypes

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class SerializablePersistentDataType<T : Serializable>(private val clazz: Class<T>) : PersistentDataType<ByteArray, T> {
    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java
    override fun getComplexType(): Class<T> = clazz

    override fun toPrimitive(complex: T, context: PersistentDataAdapterContext): ByteArray {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeObject(complex)
        }
        return baos.toByteArray()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): T {
        return ObjectInputStream(primitive.inputStream()).use { ois ->
            clazz.cast(ois.readObject())
        }
    }

    companion object {
        @JvmStatic
        fun <T : Serializable> serializableTypeFrom(type: Class<T>): PersistentDataType<ByteArray, T> {
            return SerializablePersistentDataType(type)
        }

        @JvmSynthetic
        inline fun <reified T : Serializable> serializableTypeFrom(): PersistentDataType<ByteArray, T> {
            return SerializablePersistentDataType(T::class.java)
        }
    }
}