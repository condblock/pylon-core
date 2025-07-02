package io.github.pylonmc.pylon.core.entity.display

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.PacketPylonEntity
import io.github.pylonmc.pylon.core.entity.display.transform.TransformUtil.toTransformation
import io.github.pylonmc.pylon.core.nms.NmsAccessor
import io.github.pylonmc.pylon.core.util.pylonKey
import me.tofaa.entitylib.meta.types.DisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.MustBeInvokedByOverriders
import org.joml.Matrix4f
import com.github.retrooper.packetevents.util.Quaternion4f as ProtocolQuaternion4f
import com.github.retrooper.packetevents.util.Vector3f as ProtocolVector3f
import org.joml.Quaternionf as JomlQuaternionf
import org.joml.Vector3f as JomlVector3f

/**
 * In addition to the three things that [PacketPylonEntity] persists,
 * this entity also persists the display transformation
 */
open class PylonDisplayEntity : PacketPylonEntity {

    constructor(entity: WrapperEntity, key: NamespacedKey, location: Location) : super(entity, key, location)

    constructor(entity: WrapperEntity, pdc: PersistentDataContainer) : super(entity, pdc) {
        transformation = pdc.get(transformationKey, PylonSerializers.MATRIX_4F)!!
    }

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        pdc.set(transformationKey, PylonSerializers.MATRIX_4F, transformation)
    }

    val meta: DisplayMeta = entity.getEntityMeta(DisplayMeta::class.java)

    var transformation: Matrix4f
        get() = Matrix4f()
            .translate(meta.translation.toJoml())
            .rotate(meta.leftRotation.toJoml())
            .scale(meta.scale.toJoml())
            .rotate(meta.rightRotation.toJoml())
        set(value) {
            val transform = value.toTransformation()
            meta.translation = transform.translation.toProtocol()
            meta.leftRotation = transform.leftRotation.toProtocol()
            meta.scale = transform.scale.toProtocol()
            meta.rightRotation = transform.rightRotation.toProtocol()
        }

    var interpolationDelay by meta::interpolationDelay
    var interpolationDuration by meta::transformationInterpolationDuration
    var teleportDuration by meta::positionRotationInterpolationDuration

    var billboard: Display.Billboard
        get() = when (meta.billboardConstraints) {
            DisplayMeta.BillboardConstraints.FIXED -> Display.Billboard.FIXED
            DisplayMeta.BillboardConstraints.VERTICAL -> Display.Billboard.VERTICAL
            DisplayMeta.BillboardConstraints.HORIZONTAL -> Display.Billboard.HORIZONTAL
            DisplayMeta.BillboardConstraints.CENTER -> Display.Billboard.CENTER
        }
        set(value) {
            meta.billboardConstraints = when (value) {
                Display.Billboard.FIXED -> DisplayMeta.BillboardConstraints.FIXED
                Display.Billboard.VERTICAL -> DisplayMeta.BillboardConstraints.VERTICAL
                Display.Billboard.HORIZONTAL -> DisplayMeta.BillboardConstraints.HORIZONTAL
                Display.Billboard.CENTER -> DisplayMeta.BillboardConstraints.CENTER
            }
        }

    var brightness: Display.Brightness
        get() = NmsAccessor.instance.unpackBrightness(meta.brightnessOverride)
        set(value) {
            meta.brightnessOverride = NmsAccessor.instance.packBrightness(value)
        }

    var viewRange by meta::viewRange
    var shadowRadius by meta::shadowRadius
    var shadowStrength by meta::shadowStrength

    var displayWidth by meta::width
    var displayHeight by meta::height

    var glowColorOverride: Color
        get() = Color.fromARGB(meta.glowColorOverride)
        set(value) {
            meta.glowColorOverride = value.asARGB()
        }

    companion object {
        private val transformationKey = pylonKey("transformation")
    }
}

private fun ProtocolVector3f.toJoml() = JomlVector3f(x, y, z)
private fun ProtocolQuaternion4f.toJoml() = JomlQuaternionf(x, y, z, w)
private fun JomlVector3f.toProtocol() = ProtocolVector3f(x, y, z)
private fun JomlQuaternionf.toProtocol() = ProtocolQuaternion4f(x, y, z, w)