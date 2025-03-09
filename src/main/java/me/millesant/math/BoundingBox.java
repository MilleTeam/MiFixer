package me.millesant.math;

import cn.nukkit.level.Level;

public record BoundingBox(
    int minX,
    int minY,
    int minZ,
    int maxX,
    int maxY,
    int maxZ,
    Level level
)
{

    /**
     * Returns the volume of the bounding box
     */
    public int getVolume()
    {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    /**
     * Checks if the bounding box is too large to process
     */
    public boolean isTooLarge(int maxVolume)
    {
        return getVolume() > maxVolume;
    }

}