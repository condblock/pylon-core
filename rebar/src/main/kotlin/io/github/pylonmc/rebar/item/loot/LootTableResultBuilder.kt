package io.github.pylonmc.rebar.item.loot

import io.github.pylonmc.rebar.nms.NmsAccessor
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.NamespacedKey.minecraft
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.loot.LootContext
import org.bukkit.loot.LootTable
import org.bukkit.loot.Lootable
import org.bukkit.util.Vector
import org.joml.Vector3d

/**
 * A custom API for getting items from [LootTable]s, the existing api ([LootContext]) is very limited
 * and only supports a few of the parameters that can be used in loot tables, this however supports them all.
 */
class LootTableResultBuilder {
    var thisEntity: Entity? = null
        private set
    var interactingEntity: Entity? = null
        private set
    var targetEntity: Entity? = null
        private set
    var lastDamagePlayer: Player? = null
        private set
    var damageSource: DamageSource? = null
        private set
    var attackingEntity: Entity? = null
        private set
    var directAttackingEntity: Entity? = null
        private set
    var origin: Vector3d? = null
        private set
    var blockData: BlockData? = null // In NMS -> BlockState
        private set
    var blockState: BlockState? = null // In NMS -> BlockEntity
        private set
    var tool: ItemStack? = null
        private set
    var explosionRadius: Float? = null
        private set

    /**
     * The entity the loot is being generated from.
     *
     * For ex: The zombie dying.
     */
    fun setThisEntity(entity: Entity?) = apply { this.thisEntity = entity }

    /**
     * The entity interacting with what the loot is being generated from.
     *
     * For ex: The player who is using a brush on an armadillo.
     */
    fun setInteractingEntity(entity: Entity?) = apply { this.interactingEntity = entity }

    /**
     * The target of the [interactingEntity]
     *
     * For ex: The armadillo being brushed by a player.
     */
    fun setTargetEntity(entity: Entity?) = apply { this.targetEntity = entity }

    /**
     * The last player to damage the entity the loot is being generated from.
     */
    fun setLastDamagePlayer(player: Player?) = apply { this.lastDamagePlayer = player }

    /**
     * The last damage source of the entity the loot is being generated from.
     *
     * For ex: Fire from a block of fire
     */
    fun setDamageSource(source: DamageSource?) = apply { this.damageSource = source }

    /**
     * The entity that caused the damage source of the entity the loot is being generated from.
     *
     * For ex: The player who shot the arrow which hit the entity
     */
    fun setAttackingEntity(entity: Entity?) = apply { this.attackingEntity = entity }

    /**
     * The direct entity that caused the damage source of the entity the loot is being generated from.
     *
     * For ex: The arrow shot by a player which hit the entity.
     */
    fun setDirectAttackingEntity(entity: Entity?) = apply { this.directAttackingEntity = entity }

    /**
     * The origin of this loot table generation.
     *
     * For ex: The location of the block/entity generating this loot
     */
    fun setOrigin(origin: Vector3d?) = apply { this.origin = origin }

    /**
     * The origin of this loot table generation.
     *
     * For ex: The location of the block/entity generating this loot
     */
    fun setOrigin(origin: Vector?) = setOrigin(origin?.toVector3d())

    /**
     * The origin of this loot table generation.
     *
     * For ex: The location of the block/entity generating this loot
     */
    fun setOrigin(origin: Location?) = setOrigin(origin?.toVector())

    /**
     * The block data of the block generating this loot table
     *
     * For ex: The chest block data of a villager chest
     */
    fun setBlockData(blockData: BlockData?) = apply { this.blockData = blockData }

    /**
     * The block state of the block generating this loot table
     *
     * For ex: The chest block state of a villager chest
     */
    fun setBlockState(blockState: BlockState?) = apply { this.blockState = blockState }

    /**
     * The tool involved in the generation of this loot table
     *
     * For ex: The shears used on a sheep, The pickaxe used on diamond ore
     */
    fun setTool(tool: ItemStack?) = apply { this.tool = tool }

    /**
     * The explosion radius of the explosion that caused the generation of this loot table.
     *
     * For ex: The explosion radius of the tnt which broke the block generating this loot table
     */
    fun setExplosionRadius(radius: Float?) = apply { this.explosionRadius = radius }

    /**
     * Generates a random set of items based on this [LootTableResultBuilder]'s parameters, the [contextSet] provided, and the [LootTable] and [seed][Lootable.seed] of the [lootable] provided.
     */
    fun getRandomItems(world: World, contextSet: NamespacedKey, lootable: Lootable): Collection<ItemStack>? {
        val lootTable = lootable.lootTable ?: return null
        return getRandomItems(world, contextSet, lootTable, lootable.seed)
    }

    /**
     * Generates a random set of items based on this [LootTableResultBuilder]'s parameters, the [contextSet] provided, and the [LootTable] and [optionalLootTableSeed] provided.
     */
    @JvmOverloads
    fun getRandomItems(world: World, contextSet: NamespacedKey, lootTable: LootTable, optionalLootTableSeed: Long? = null): Collection<ItemStack> {
        return NmsAccessor.instance.getRandomItems(world, contextSet, lootTable, optionalLootTableSeed, this)
    }

    companion object {
        /**
         * Loot generated needing no context.
         *
         * For ex: Trial Spawner Rewards
         */
        @JvmField val EMPTY = minecraft("empty")

        /**
         * Loot generated by/for chests.
         *
         * For ex: Dungeon chests, Villager chests, etc.
         */
        @JvmField val CHEST = minecraft("chest")

        /**
         * Loot generated by a command.
         */
        @JvmField val COMMAND = minecraft("command")

        /**
         * Loot generated by fishing.
         */
        @JvmField val FISHING = minecraft("fishing")

        /**
         * Loot generated by/for an entity.
         *
         * For ex: The death of a pig.
         */
        @JvmField val ENTITY = minecraft("entity")

        /**
         * Loot generated for by/for archaeology.
         *
         * For ex: The generation of items in suspicious sand/gravel.
         */
        @JvmField val ARCHAEOLOGY = minecraft("archaeology")

        /**
         * Loot generated for by/for a gift.
         *
         * For ex: Villagers giving items to players with the Hero of the Village effect,
         * Cat's giving their owner items, chicken's laying eggs.
         */
        @JvmField val GIFT = minecraft("gift")

        /**
         * Loot generated for a piglin barter.
         */
        @JvmField val PIGLIN_BARTER = minecraft("barter")

        /**
         * Loot generated by vaults.
         */
        @JvmField val VAULT = minecraft("vault")

        /**
         * Loot generated for an advancement reward.
         */
        @JvmField val ADVANCEMENT_REWARD = minecraft("advancement_reward")

        /**
         * Generic loot generation.
         */
        @JvmField val GENERIC = minecraft("generic")

        /**
         * Loot generated by/for a block.
         *
         * For ex: A block dropping loot when broken.
         */
        @JvmField val BLOCK = minecraft("block")

        /**
         * Loot generated by shearing an entity/block.
         */
        @JvmField val SHEARING = minecraft("shearing")

        /**
         * Loot generated by interacting with an entity.
         *
         * For ex: Using a brush on an armadillo
         */
        @JvmField val ENTITY_INTERACT = minecraft("entity_interact")

        /**
         * Loot generated by interacting with a block.
         *
         * For ex: Harvesting glow berries
         */
        @JvmField val BLOCK_INTERACT = minecraft("block_interact")

        @JvmStatic
        fun of(event: EntityDeathEvent): LootTableResultBuilder {
            return LootTableResultBuilder()
                .setThisEntity(event.entity)
                .setOrigin(event.entity.location)
                .setDamageSource(event.damageSource)
                .setAttackingEntity(event.damageSource.causingEntity)
                .setDirectAttackingEntity(event.damageSource.directEntity)
                .setLastDamagePlayer(event.damageSource.causingEntity as? Player)
        }
    }
}