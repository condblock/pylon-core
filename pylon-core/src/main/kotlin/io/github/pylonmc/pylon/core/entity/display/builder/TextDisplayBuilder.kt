package io.github.pylonmc.pylon.core.entity.display.builder

import io.github.pylonmc.pylon.core.entity.PacketPylonEntity
import io.github.pylonmc.pylon.core.entity.display.PylonTextDisplay
import io.github.pylonmc.pylon.core.entity.display.builder.transform.TransformBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.TextDisplay.TextAlignment
import org.joml.Matrix4f

@Suppress("unused", "DuplicatedCode")
class TextDisplayBuilder() {

    private var text: Component? = null
    private var transformation: Matrix4f? = null
    private var brightness: Brightness? = null
    private var glowColor: Color? = null
    private var viewRange: Float? = null
    private var billboard: Billboard? = null
    private var alignment: TextAlignment? = null
    private var backgroundColor: Color? = null
    private var interpolationDelay: Int? = null
    private var interpolationDuration: Int? = null

    constructor(other: TextDisplayBuilder) : this() {
        this.text = other.text
        this.transformation = other.transformation
        this.brightness = other.brightness
        this.glowColor = other.glowColor
        this.viewRange = other.viewRange
        this.billboard = other.billboard
        this.alignment = other.alignment
        this.backgroundColor = other.backgroundColor
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    // @formatter:off
    fun text(text: String) = apply { this.text = Component.text(text) }
    fun text(text: Component?) = apply { this.text = text }
    fun transformation(transformation: Matrix4f?) = apply { this.transformation = transformation }
    fun transformation(builder: TransformBuilder) = apply { this.transformation = builder.buildForTextDisplay() }
    fun transformLocal(transformation: Matrix4f) = apply { this.transformation = (this.transformation ?: Matrix4f()).mul(transformation) }
    fun transformLocal(builder: TransformBuilder) = transformLocal(builder.buildForTextDisplay())
    fun transformToUnitSquare() = apply { this.transformation = squareTransformation }
    fun brightness(brightness: Brightness) = apply { this.brightness = brightness }
    fun brightness(brightness: Int) = brightness(Brightness(0, brightness))
    fun glow(glowColor: Color?) = apply { this.glowColor = glowColor }
    fun viewRange(viewRange: Float) = apply { this.viewRange = viewRange }
    fun billboard(billboard: Billboard?) = apply { this.billboard = billboard }
    fun alignment(alignment: TextAlignment?) = apply { this.alignment = alignment }
    fun backgroundColor(backgroundColor: Color?) = apply { this.backgroundColor = backgroundColor }
    fun interpolationDelay(interpolationDelay: Int) = apply { this.interpolationDelay = interpolationDelay }
    fun interpolationDuration(interpolationDuration: Int) = apply { this.interpolationDuration = interpolationDuration }
    // @formatter:on

    fun buildReal(location: Location): TextDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0f
        finalLocation.pitch = 0f

        return finalLocation.world.spawn(finalLocation, TextDisplay::class.java) { display ->
            text?.let { display.text(it) }
            transformation?.let { display.setTransformationMatrix(it) }
            brightness?.let { display.brightness = it }
            glowColor?.let {
                display.isGlowing = true
                display.glowColorOverride = it
            }
            viewRange?.let { display.viewRange = it }
            billboard?.let { display.billboard = it }
            alignment?.let { display.alignment = it }
            backgroundColor?.let { display.backgroundColor = it }
            interpolationDelay?.let { display.interpolationDelay = it }
            interpolationDuration?.let { display.interpolationDuration = it }
        }
    }

    fun buildPacketBased(key: NamespacedKey, location: Location): PylonTextDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0f
        finalLocation.pitch = 0f

        val display = PacketPylonEntity.spawn(key, finalLocation) as PylonTextDisplay
        text?.let { display.text = it }
        transformation?.let { display.transformation = it }
        brightness?.let { display.brightness = it }
        glowColor?.let {
            display.meta.isGlowing = true
            display.glowColorOverride = it
        }
        viewRange?.let { display.viewRange = it }
        billboard?.let { display.billboard = it }
        alignment?.let { display.alignment = it }
        backgroundColor?.let { display.backgroundColor = it }
        interpolationDelay?.let { display.interpolationDelay = it }
        interpolationDuration?.let { display.interpolationDuration = it }

        return display
    }

    companion object {
        @JvmStatic
        // https://github.com/TheCymaera/minecraft-hologram/blob/d67eb43308df61bdfe7283c6821312cca5f9dea9/src/main/java/com/heledron/hologram/utilities/rendering/textDisplays.kt#L15
        val squareTransformation: Matrix4f
            get() = Matrix4f()
                .translate(-0.1f + .5f, -0.5f + .5f, 0f)
                .scale(8.0f, 4.0f, 1f)
    }
}