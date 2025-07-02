package io.github.pylonmc.pylon.core.entity.display

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.meta.display.BlockDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.data.BlockData
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.MustBeInvokedByOverriders

/**
 * In addition to the things [PylonDisplayEntity] persists, this entity also persists the block data
 */
open class PylonBlockDisplay : PylonDisplayEntity {

    constructor(entity: WrapperEntity, key: NamespacedKey, location: Location) : super(entity, key, location)

    constructor(entity: WrapperEntity, pdc: PersistentDataContainer) : super(entity, pdc) {
        meta.blockId = pdc.get(blockKey, PylonSerializers.INTEGER)!!
    }

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        super.write(pdc)
        pdc.set(blockKey, PylonSerializers.INTEGER, meta.blockId)
    }

    final override val meta: BlockDisplayMeta = entity.getEntityMeta(BlockDisplayMeta::class.java)

    var block: BlockData
        get() = SpigotConversionUtil.toBukkitBlockData(WrappedBlockState.getByGlobalId(meta.blockId))
        set(value) {
            meta.blockId = SpigotConversionUtil.fromBukkitBlockData(value).globalId
        }

    companion object {
        private val blockKey = pylonKey("block")
    }
}