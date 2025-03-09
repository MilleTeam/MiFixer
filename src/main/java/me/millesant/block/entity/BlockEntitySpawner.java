package me.millesant.block.entity;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockChest;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class BlockEntitySpawner
{

    private static final Map<Integer, String> BLOCK_ENTITY_MAP = new HashMap<>();

    static
    {
        BLOCK_ENTITY_MAP.put(Block.CHEST, BlockEntity.CHEST);
        BLOCK_ENTITY_MAP.put(Block.FURNACE, BlockEntity.FURNACE);
        BLOCK_ENTITY_MAP.put(Block.BREWING_BLOCK, BlockEntity.BREWING_STAND);
        BLOCK_ENTITY_MAP.put(Block.FLOWER_POT_BLOCK, BlockEntity.FLOWER_POT);
        BLOCK_ENTITY_MAP.put(Block.BED_BLOCK, BlockEntity.BED);
        BLOCK_ENTITY_MAP.put(Block.ENDER_CHEST, BlockEntity.ENDER_CHEST);
        BLOCK_ENTITY_MAP.put(Block.SIGN_POST, BlockEntity.SIGN);
        BLOCK_ENTITY_MAP.put(Block.WALL_SIGN, BlockEntity.SIGN);
        BLOCK_ENTITY_MAP.put(Block.ENCHANT_TABLE, BlockEntity.ENCHANT_TABLE);
        BLOCK_ENTITY_MAP.put(Block.ITEM_FRAME_BLOCK, BlockEntity.ITEM_FRAME);
        BLOCK_ENTITY_MAP.put(Block.BEACON, BlockEntity.BEACON);
        BLOCK_ENTITY_MAP.put(Block.HOPPER_BLOCK, BlockEntity.HOPPER);
    }

    // Special handler for chests to handle pairing
    private static final Map<Integer, BiConsumer<FullChunk, Vector3>> SPECIAL_HANDLERS = Map.of(Block.CHEST,
        BlockEntitySpawner::handleChest);

    /**
     * Checks if a block entity should be spawned at the given position
     *
     * @return true if a block entity was spawned
     */
    public static boolean checkBlockEntity(
        int blockId,
        FullChunk chunk,
        Vector3 pos
    )
    {
        try
        {
            // First check if there's a special handler for this block type
            BiConsumer<FullChunk, Vector3> specialHandler = SPECIAL_HANDLERS.get(blockId);
            if (specialHandler != null)
            {
                specialHandler.accept(chunk, pos);
                return true;
            }

            // Check if this block needs a block entity
            String blockEntityId = BLOCK_ENTITY_MAP.get(blockId);
            if (blockEntityId != null)
            {
                Vector3 worldPos = pos.add(chunk.getX() * 16, 0, chunk.getZ() * 16);

                // Only create if one doesn't exist already
                if (chunk.getTile(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ()) == null)
                {
                    BlockEntity.createBlockEntity(blockEntityId, chunk, BlockEntity.getDefaultCompound(worldPos,
                        blockEntityId));
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            Logger.getLogger("BlockEntitySpawner").warning("Failed to spawn block entity: " + e.getMessage());
        }

        return false;
    }

    /**
     * Special handler for chest blocks to handle pairing
     */
    private static void handleChest(
        FullChunk chunk,
        Vector3 pos
    )
    {
        Vector3 worldPos = pos.add(chunk.getX() * 16, 0, chunk.getZ() * 16);

        // If there's already a block entity here, don't create another one
        if (chunk.getTile(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ()) != null)
        {
            return;
        }

        CompoundTag      nbt   = BlockEntity.getDefaultCompound(worldPos, BlockEntity.CHEST);
        BlockEntityChest chest = new BlockEntityChest(chunk, nbt);

        Block block = getBlock(chunk, worldPos);

        if (block instanceof BlockChest)
        {
            BlockFace chestFace = getChestFace(block.getDamage());

            if (chestFace == null)
            {
                return;
            }

            // Check the blocks perpendicular to the chest's facing direction
            for (BlockFace face : new BlockFace[] {
                chestFace.rotateYCCW(),
                chestFace.getOpposite().rotateYCCW()
            })
            {
                Block otherBlock = getBlock(chunk, worldPos.add(face.getXOffset(), face.getYOffset(),
                    face.getZOffset()));

                if (otherBlock instanceof BlockChest && otherBlock.getDamage() == block.getDamage())
                {
                    BlockEntity blockEntity = getBlockEntity(chunk, otherBlock);

                    if (blockEntity instanceof BlockEntityChest otherChest && !otherChest.isPaired())
                    {
                        otherChest.pairWith(chest);
                        chest.pairWith(otherChest);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Gets a block from a chunk or a neighboring chunk if the position is outside the chunk
     */
    private static Block getBlock(
        FullChunk chunk,
        Vector3 pos
    )
    {
        BlockVector3 floor = new BlockVector3(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());

        int chunkX = floor.x >> 4;
        int chunkZ = floor.z >> 4;

        Block block;
        if (chunkX == chunk.getX() && chunkZ == chunk.getZ())
        {
            BlockVector3 chunkPos = new BlockVector3(floor.x, floor.y, floor.z);
            chunkPos.x = chunkPos.x & 0xF; // Equivalent to % 16
            chunkPos.z = chunkPos.z & 0xF; // Equivalent to % 16

            block = Block.get(chunk.getBlockId(chunkPos.x, chunkPos.y, chunkPos.z), chunk.getBlockData(chunkPos.x,
                chunkPos.y, chunkPos.z));
        }
        else
        {
            block = chunk.getProvider().getLevel().getBlock(pos);
        }

        block.position(Position.fromObject(pos, chunk.getProvider().getLevel()));
        return block;
    }

    /**
     * Gets a block entity from a chunk or a neighboring chunk
     */
    private static BlockEntity getBlockEntity(
        FullChunk chunk,
        Block block
    )
    {
        Vector3      pos   = new Vector3(block.x, block.y, block.z);
        BlockVector3 floor = new BlockVector3(pos.getFloorX(), pos.getFloorY(), pos.getFloorZ());

        int chunkX = floor.x >> 4;
        int chunkZ = floor.z >> 4;

        if (chunkX == chunk.getX() && chunkZ == chunk.getZ())
        {
            BlockVector3 chunkPos = floor.clone();
            chunkPos.x = chunkPos.x & 0xF; // Equivalent to % 16
            chunkPos.z = chunkPos.z & 0xF; // Equivalent to % 16

            return chunk.getTile(chunkPos.x, chunkPos.y, chunkPos.z);
        }
        else
        {
            return chunk.getProvider().getLevel().getBlockEntity(pos);
        }
    }

    /**
     * Gets the BlockFace a chest is facing based on its metadata
     */
    private static BlockFace getChestFace(int meta)
    {
        return switch (meta)
        {
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.SOUTH;
            case 4 -> BlockFace.WEST;
            case 5 -> BlockFace.EAST;
            default -> null;
        };
    }

}