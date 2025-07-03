package io.github.pylonmc.pylon.core.entity.display.builder

import io.github.pylonmc.pylon.core.entity.display.builder.transform.TransformBuilder
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display.Brightness
import org.joml.Matrix4f


@Suppress("unused")
class BlockDisplayBuilder() {

    private var material: Material? = null
    private var blockData: BlockData? = null
    private var transformation: Matrix4f? = null
    private var glowColor: Color? = null
    private var brightness: Brightness? = null
    private var viewRange: Float? = null
    private var interpolationDelay: Int? = null
    private var interpolationDuration: Int? = null

    constructor(other: BlockDisplayBuilder) : this() {
        this.material = other.material
        this.blockData = other.blockData
        this.transformation = other.transformation
        this.glowColor = other.glowColor
        this.brightness = other.brightness
        this.viewRange = other.viewRange
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    fun material(material: Material?): BlockDisplayBuilder {
        this.material = material
        return this
    }

    // @formatter:off
    fun blockData(blockData: BlockData?) = apply { this.blockData = blockData }
    fun transformation(transformation: Matrix4f?) = apply { this.transformation = transformation }
    fun transformation(builder: TransformBuilder) = apply { this.transformation = builder.buildForBlockDisplay() }
    fun transformLocal(transformation: Matrix4f) = apply { this.transformation = (this.transformation ?: Matrix4f()).mul(transformation) }
    fun transformLocal(builder: TransformBuilder) = transformLocal(builder.buildForBlockDisplay())
    fun brightness(brightness: Brightness) = apply { this.brightness = brightness }
    fun brightness(brightness: Int) = brightness(Brightness(0, brightness))
    fun glow(glowColor: Color?) = apply { this.glowColor = glowColor }
    fun viewRange(viewRange: Float) = apply { this.viewRange = viewRange }
    fun interpolationDelay(interpolationDelay: Int) = apply { this.interpolationDelay = interpolationDelay }
    fun interpolationDuration(interpolationDuration: Int) = apply { this.interpolationDuration = interpolationDuration }
    // @formatter:on

    fun build(location: Location): BlockDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0.0f
        finalLocation.pitch = 0.0f
        return location.world.spawn(finalLocation, BlockDisplay::class.java, this::update)
    }

    fun update(display: BlockDisplay) {
        if (material != null) {
            display.block = material!!.createBlockData()
        }
        if (blockData != null) {
            display.block = blockData!!
        }
        if (transformation != null) {
            display.setTransformationMatrix(transformation!!)
        }
        if (glowColor != null) {
            display.isGlowing = true
            display.glowColorOverride = glowColor
        }
        if (brightness != null) {
            display.brightness = brightness
        }
        if (viewRange != null) {
            display.viewRange = viewRange!!
        }
        if (interpolationDelay != null) {
            display.interpolationDelay = interpolationDelay!!
        }
        if (interpolationDuration != null) {
            display.interpolationDuration = interpolationDuration!!
        }
    }
}