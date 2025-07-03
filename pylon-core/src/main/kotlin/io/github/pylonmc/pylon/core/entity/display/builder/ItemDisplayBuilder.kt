package io.github.pylonmc.pylon.core.entity.display.builder

import io.github.pylonmc.pylon.core.entity.PacketPylonEntity
import io.github.pylonmc.pylon.core.entity.display.PylonItemDisplay
import io.github.pylonmc.pylon.core.entity.display.builder.transform.TransformBuilder
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Display.Brightness
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.joml.Matrix4f

@Suppress("unused", "DuplicatedCode")
class ItemDisplayBuilder() {

    var itemStack: ItemStack? = null
    var transformation: Matrix4f? = null
    var brightness: Brightness? = null
    var glowColor: Color? = null
    var billboard: Billboard? = null
    var viewRange: Float? = null
    var interpolationDelay: Int? = null
    var interpolationDuration: Int? = null

    constructor(other: ItemDisplayBuilder) : this() {
        this.itemStack = other.itemStack
        this.transformation = other.transformation
        this.brightness = other.brightness
        this.glowColor = other.glowColor
        this.billboard = other.billboard
        this.viewRange = other.viewRange
        this.interpolationDelay = other.interpolationDelay
        this.interpolationDuration = other.interpolationDuration
    }

    // @formatter:off
    fun material(material: Material) = apply { this.itemStack = ItemStack(material) }
    fun itemStack(itemStack: ItemStack?) = apply { this.itemStack = itemStack }
    fun transformation(transformation: Matrix4f?) = apply { this.transformation = transformation }
    fun transformation(builder: TransformBuilder) = apply { this.transformation = builder.buildForItemDisplay() }
    fun transformLocal(transformation: Matrix4f) = apply { this.transformation = (this.transformation ?: Matrix4f()).mul(transformation) }
    fun transformLocal(builder: TransformBuilder) = transformLocal(builder.buildForItemDisplay())
    fun brightness(brightness: Brightness) = apply { this.brightness = brightness }
    fun brightness(brightness: Int) = brightness(Brightness(0, brightness))
    fun glow(glowColor: Color?) = apply { this.glowColor = glowColor }
    fun billboard(billboard: Billboard?) = apply { this.billboard = billboard }
    fun viewRange(viewRange: Float) = apply { this.viewRange = viewRange }
    fun interpolationDelay(interpolationDelay: Int) = apply { this.interpolationDelay = interpolationDelay }
    fun interpolationDuration(interpolationDuration: Int) = apply { this.interpolationDuration = interpolationDuration }
    // @formatter:on

    fun buildReal(location: Location): ItemDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0.0F
        finalLocation.pitch = 0.0F
        return finalLocation.getWorld().spawn(finalLocation, ItemDisplay::class.java) { display ->
            if (display !is ItemDisplay) {
                throw IllegalArgumentException("Must provide an ItemDisplay")
            }
            itemStack?.let { display.setItemStack(it) }
            transformation?.let { display.setTransformationMatrix(it) }
            glowColor?.let {
                display.isGlowing = true
                display.glowColorOverride = it
            }
            brightness?.let { display.brightness = it }
            billboard?.let { display.billboard = it }
            viewRange?.let { display.viewRange = it }
            interpolationDelay?.let { display.interpolationDelay = it }
            interpolationDuration?.let { display.interpolationDuration = it }
        }
    }

    fun buildPacketBased(key: NamespacedKey, location: Location): PylonItemDisplay {
        val finalLocation = location.clone()
        finalLocation.yaw = 0.0F
        finalLocation.pitch = 0.0F
        val entity = PacketPylonEntity.spawn(key, finalLocation) as PylonItemDisplay
        entity.item = itemStack ?: ItemStack(Material.AIR)
        transformation?.let { entity.transformation = it }
        glowColor?.let {
            entity.meta.isGlowing = true
            entity.glowColorOverride = it
        }
        brightness?.let { entity.brightness = it }
        billboard?.let { entity.billboard = it }
        viewRange?.let { entity.viewRange = it }
        interpolationDelay?.let { entity.interpolationDelay = it }
        interpolationDuration?.let { entity.interpolationDuration = it }
        return entity
    }

}