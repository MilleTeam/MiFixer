package me.millesant.block;

import cn.nukkit.item.Item;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.math.BlockFace;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntPredicate;

public class BlockFixerService
{

    private static final Map<Integer, BlockFixOperation> BLOCK_FIX_OPERATIONS = Map.ofEntries(
        Map.entry(3, (cm, x, y, z, id) ->
        {
            if (cm.getBlockDataAt(x, y, z) == 2)
            {
                cm.setBlockIdAt(x, y, z, Item.PODZOL);
                return true;
            }
            return false;
        }),
        Map.entry(125, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.DOUBLE_WOODEN_SLAB);
            return true;
        }),
        Map.entry(126, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.WOOD_SLAB);
            return true;
        }),
        Map.entry(95, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.STAINED_GLASS);
            return true;
        }),
        Map.entry(157, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.ACTIVATOR_RAIL);
            return true;
        }),
        Map.entry(158, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.DROPPER);
            return true;
        }),
        Map.entry(160, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.GLASS_PANE);
            cm.setBlockDataAt(x, y, z, 0);
            return true;
        }),
        Map.entry(166, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.INVISIBLE_BEDROCK);
            return true;
        }),
        Map.entry(177, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.AIR);
            return true;
        }),
        Map.entry(Item.STONE_BUTTON, (cm, x, y, z, id) -> fixButton(cm, x, y, z)),
        Map.entry(Item.WOODEN_BUTTON, (cm, x, y, z, id) -> fixButton(cm, x, y, z)),
        Map.entry(Item.TRAPDOOR, (cm, x, y, z, id) -> fixTrapdoor(cm, x, y, z)),
        Map.entry(Item.IRON_TRAPDOOR, (cm, x, y, z, id) -> fixTrapdoor(cm, x, y, z))
    );

    private static final IntPredicate IS_SHULKER_BOX = id -> id >= 219 && id <= 234;

    private static final Map<Integer, BlockFixOperation> SPECIAL_BLOCKS_OPERATIONS = new HashMap<>();

    static
    {
        SPECIAL_BLOCKS_OPERATIONS.put(198, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.END_ROD);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(199, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.CHORUS_PLANT);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(202, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.PURPUR_BLOCK);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(204, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.PURPUR_BLOCK);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(205, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.PURPUR_BLOCK);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(207, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.BEETROOT_BLOCK);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(208, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.GRASS_PATH);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(210, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, 188);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(211, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, 189);
            return true;
        });
        SPECIAL_BLOCKS_OPERATIONS.put(218, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.OBSERVER);
            return true;
        });
    }

    private static final Map<Integer, BlockFixOperation> FENCE_FIX_OPERATIONS = new HashMap<>();

    static
    {
        FENCE_FIX_OPERATIONS.put(188, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.FENCE);
            cm.setBlockDataAt(x, y, z, 1);
            return true;
        });
        FENCE_FIX_OPERATIONS.put(189, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.FENCE);
            cm.setBlockDataAt(x, y, z, 2);
            return true;
        });
        FENCE_FIX_OPERATIONS.put(190, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.FENCE);
            cm.setBlockDataAt(x, y, z, 3);
            return true;
        });
        FENCE_FIX_OPERATIONS.put(191, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.FENCE);
            cm.setBlockDataAt(x, y, z, 4);
            return true;
        });
        FENCE_FIX_OPERATIONS.put(192, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.FENCE);
            cm.setBlockDataAt(x, y, z, 5);
            return true;
        });
    }

    private static final Map<Integer, BlockFixOperation> GLAZED_TERRACOTTA_OPERATIONS = Map.ofEntries(
        Map.entry(235, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.WHITE_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(236, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.ORANGE_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(237, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.MAGENTA_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(238, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.LIGHT_BLUE_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(239, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.YELLOW_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(240, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.LIME_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(241, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.PINK_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(242, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.GRAY_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(243, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.SILVER_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(244, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.CYAN_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(245, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.PURPLE_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(246, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.BLUE_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(247, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.BROWN_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(248, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.GREEN_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(249, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.RED_GLAZED_TERRACOTTA);
            return true;
        }),
        Map.entry(250, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.BLACK_GLAZED_TERRACOTTA);
            return true;
        })
    );

    private static final Map<Integer, BlockFixOperation> OTHER_BLOCKS_OPERATIONS = Map.ofEntries(
        Map.entry(251, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.CONCRETE);
            return true;
        }),
        Map.entry(252, (cm, x, y, z, id) ->
        {
            cm.setBlockIdAt(x, y, z, Item.CONCRETE_POWDER);
            return true;
        })
    );

    /**
     * Fix a block at the given position
     *
     * @return true if the block was changed
     */
    public boolean fixBlock(
        ChunkManager cm,
        int x,
        int y,
        int z,
        int id
    )
    {
        if (IS_SHULKER_BOX.test(id))
        {
            cm.setBlockIdAt(x, y, z, Item.SHULKER_BOX);
            cm.setBlockDataAt(x, y, z, id - 219);
            return true;
        }

        BlockFixOperation operation;

        if (BLOCK_FIX_OPERATIONS.containsKey(id))
        {
            operation = BLOCK_FIX_OPERATIONS.get(id);
        }
        else if (FENCE_FIX_OPERATIONS.containsKey(id))
        {
            operation = FENCE_FIX_OPERATIONS.get(id);
        }
        else if (SPECIAL_BLOCKS_OPERATIONS.containsKey(id))
        {
            operation = SPECIAL_BLOCKS_OPERATIONS.get(id);
        }
        else if (GLAZED_TERRACOTTA_OPERATIONS.containsKey(id))
        {
            operation = GLAZED_TERRACOTTA_OPERATIONS.get(id);
        }
        else if (OTHER_BLOCKS_OPERATIONS.containsKey(id))
        {
            operation = OTHER_BLOCKS_OPERATIONS.get(id);
        }
        else
        {
            operation = null;
        }

        return operation != null && operation.fix(cm, x, y, z, id);
    }

    private static boolean fixButton(
        ChunkManager cm,
        int x,
        int y,
        int z
    )
    {
        int data = cm.getBlockDataAt(x, y, z);
        int face = data & 0b111;

        int meta = switch (face)
        {
            case 0 -> BlockFace.DOWN.getIndex();
            case 1 -> BlockFace.EAST.getIndex();
            case 2 -> BlockFace.WEST.getIndex();
            case 3 -> BlockFace.SOUTH.getIndex();
            case 4 -> BlockFace.NORTH.getIndex();
            case 5 -> BlockFace.UP.getIndex();
            default -> 0;
        };

        if ((data & 0x08) == 0x08)
        {
            meta |= 0x08;
        }

        cm.setBlockDataAt(x, y, z, meta);
        return true;
    }

    private static boolean fixTrapdoor(
        ChunkManager cm,
        int x,
        int y,
        int z
    )
    {
        int currentDamage = cm.getBlockDataAt(x, y, z);
        int key           = currentDamage >> 2;

        int damage = switch (key)
        {
            case 0 -> 3 - currentDamage;
            case 1,
                 2 -> 15 - currentDamage;
            case 3 -> 27 - currentDamage;
            default -> currentDamage;
        };

        cm.setBlockDataAt(x, y, z, damage);
        return true;
    }

    /**
     * Functional interface for block fix operations
     */
    @FunctionalInterface
    private interface BlockFixOperation
    {

        boolean fix(
            ChunkManager cm,
            int x,
            int y,
            int z,
            int id
        );

    }

}
