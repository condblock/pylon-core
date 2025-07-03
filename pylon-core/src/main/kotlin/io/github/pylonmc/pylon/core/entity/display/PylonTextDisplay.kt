package io.github.pylonmc.pylon.core.entity.display

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import me.tofaa.entitylib.meta.display.TextDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.MustBeInvokedByOverriders

/**
 * In addition to the things [PylonDisplayEntity] persists, this entity also persists the text and background color
 */
open class PylonTextDisplay : PylonDisplayEntity {

    final override val meta: TextDisplayMeta = entity.getEntityMeta(TextDisplayMeta::class.java)

    constructor(entity: WrapperEntity, key: NamespacedKey, location: Location) : super(entity, key, location)

    constructor(entity: WrapperEntity, pdc: PersistentDataContainer) : super(entity, pdc) {
        text = pdc.get(textKey, PylonSerializers.COMPONENT)!!
        meta.backgroundColor = pdc.get(backgroundColorKey, PylonSerializers.INTEGER)!!
    }

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        super.write(pdc)
        pdc.set(textKey, PylonSerializers.COMPONENT, text)
        pdc.set(backgroundColorKey, PylonSerializers.INTEGER, meta.backgroundColor)
    }

    var text: Component by meta::text
    var lineWidth: Int by meta::lineWidth

    var backgroundColor: Color
        get() = Color.fromARGB(meta.backgroundColor)
        set(value) {
            meta.backgroundColor = value.asARGB()
        }

    var textOpacity: Byte by meta::textOpacity
    var isShadowed: Boolean
        get() = meta.isShadow
        set(value) {
            meta.isShadow = value
        }
    var isSeeThrough: Boolean
        get() = meta.isSeeThrough
        set(value) {
            meta.isSeeThrough = value
        }
    var useDefaultBackground: Boolean
        get() = meta.isUseDefaultBackground
        set(value) {
            meta.isUseDefaultBackground = value
        }

    var alignment: TextDisplay.TextAlignment
        get() = when {
            meta.isAlignLeft -> TextDisplay.TextAlignment.LEFT
            meta.isAlignRight -> TextDisplay.TextAlignment.RIGHT
            else -> TextDisplay.TextAlignment.CENTER
        }
        set(value) = when (value) {
            TextDisplay.TextAlignment.LEFT -> {
                meta.isAlignLeft = true
                meta.isAlignRight = false
            }

            TextDisplay.TextAlignment.RIGHT -> {
                meta.isAlignLeft = false
                meta.isAlignRight = true
            }

            TextDisplay.TextAlignment.CENTER -> {
                meta.isAlignLeft = false
                meta.isAlignRight = false
            }
        }

    companion object {
        private val textKey = pylonKey("text")
        private val backgroundColorKey = pylonKey("background_color")
    }
}