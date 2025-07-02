package io.github.pylonmc.pylon.core.entity.display

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.meta.display.ItemDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.MustBeInvokedByOverriders

/**
 * In addition to the things that [PylonDisplayEntity] persists, this entity
 * also persists the item and display transform
 */
open class PylonItemDisplay : PylonDisplayEntity {

    constructor(entity: WrapperEntity, key: NamespacedKey, location: Location) : super(entity, key, location)

    constructor(entity: WrapperEntity, pdc: PersistentDataContainer) : super(entity, pdc) {
        item = pdc.get(itemKey, PylonSerializers.ITEM_STACK)!!
        displayTransform = pdc.get(displayTransformKey, PylonSerializers.ENUM.enumTypeFrom())!!
    }

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        super.write(pdc)
        pdc.set(itemKey, PylonSerializers.ITEM_STACK, item)
        pdc.set(displayTransformKey, PylonSerializers.ENUM.enumTypeFrom(), displayTransform)
    }

    final override val meta: ItemDisplayMeta = entity.getEntityMeta(ItemDisplayMeta::class.java)

    var item: ItemStack
        get() = SpigotConversionUtil.toBukkitItemStack(meta.item)
        set(value) {
            meta.item = SpigotConversionUtil.fromBukkitItemStack(value)
        }

    var displayTransform: ItemDisplay.ItemDisplayTransform
        get() = when (meta.displayType) {
            ItemDisplayMeta.DisplayType.NONE -> ItemDisplay.ItemDisplayTransform.NONE
            ItemDisplayMeta.DisplayType.THIRD_PERSON_LEFT_HAND -> ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND
            ItemDisplayMeta.DisplayType.THIRD_PERSON_RIGHT_HAND -> ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND
            ItemDisplayMeta.DisplayType.FIRST_PERSON_LEFT_HAND -> ItemDisplay.ItemDisplayTransform.FIRSTPERSON_LEFTHAND
            ItemDisplayMeta.DisplayType.FIRST_PERSON_RIGHT_HAND -> ItemDisplay.ItemDisplayTransform.FIRSTPERSON_RIGHTHAND
            ItemDisplayMeta.DisplayType.HEAD -> ItemDisplay.ItemDisplayTransform.HEAD
            ItemDisplayMeta.DisplayType.GUI -> ItemDisplay.ItemDisplayTransform.GUI
            ItemDisplayMeta.DisplayType.GROUND -> ItemDisplay.ItemDisplayTransform.GROUND
            ItemDisplayMeta.DisplayType.FIXED -> ItemDisplay.ItemDisplayTransform.FIXED
        }
        set(value) {
            meta.displayType = when (value) {
                ItemDisplay.ItemDisplayTransform.NONE -> ItemDisplayMeta.DisplayType.NONE
                ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND -> ItemDisplayMeta.DisplayType.THIRD_PERSON_LEFT_HAND
                ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND -> ItemDisplayMeta.DisplayType.THIRD_PERSON_RIGHT_HAND
                ItemDisplay.ItemDisplayTransform.FIRSTPERSON_LEFTHAND -> ItemDisplayMeta.DisplayType.FIRST_PERSON_LEFT_HAND
                ItemDisplay.ItemDisplayTransform.FIRSTPERSON_RIGHTHAND -> ItemDisplayMeta.DisplayType.FIRST_PERSON_RIGHT_HAND
                ItemDisplay.ItemDisplayTransform.HEAD -> ItemDisplayMeta.DisplayType.HEAD
                ItemDisplay.ItemDisplayTransform.GUI -> ItemDisplayMeta.DisplayType.GUI
                ItemDisplay.ItemDisplayTransform.GROUND -> ItemDisplayMeta.DisplayType.GROUND
                ItemDisplay.ItemDisplayTransform.FIXED -> ItemDisplayMeta.DisplayType.FIXED
            }
        }

    companion object {
        private val itemKey = pylonKey("item")
        private val displayTransformKey = pylonKey("display_transform")
    }
}