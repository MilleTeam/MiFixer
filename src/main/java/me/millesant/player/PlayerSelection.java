package me.millesant.player;

import cn.nukkit.level.Position;
import me.millesant.math.BoundingBox;

public record PlayerSelection(
    Position firstPosition,
    Position secondPosition
)
{

    public enum SelectionState
    {
        NOT_STARTED,
        FIRST_POSITION_SET,
        COMPLETED
    }

    public PlayerSelection()
    {
        this(null, null);
    }

    public SelectionState getSelectionState()
    {
        if (firstPosition == null)
        {
            return SelectionState.NOT_STARTED;
        }
        else if (secondPosition == null)
        {
            return SelectionState.FIRST_POSITION_SET;
        }
        else
        {
            return SelectionState.COMPLETED;
        }
    }

    public boolean isComplete()
    {
        return getSelectionState() == SelectionState.COMPLETED;
    }

    public PlayerSelection withFirstPosition(Position position)
    {
        return new PlayerSelection(position, null);
    }

    public PlayerSelection withSecondPosition(Position position)
    {
        return new PlayerSelection(firstPosition, position);
    }

    /**
     * Validates that positions are in the same level
     */
    public boolean isValid()
    {
        return isComplete() && firstPosition.getLevel().equals(secondPosition.getLevel());
    }

    /**
     * Returns the bounding box coordinates sorted (min, max)
     */
    public BoundingBox getBoundingBox()
    {
        if (!isComplete())
        {
            throw new IllegalStateException("Selection is not complete");
        }

        int minX = Math.min(firstPosition.getFloorX(), secondPosition.getFloorX());
        int minY = Math.min(firstPosition.getFloorY(), secondPosition.getFloorY());
        int minZ = Math.min(firstPosition.getFloorZ(), secondPosition.getFloorZ());
        int maxX = Math.max(firstPosition.getFloorX(), secondPosition.getFloorX());
        int maxY = Math.max(firstPosition.getFloorY(), secondPosition.getFloorY());
        int maxZ = Math.max(firstPosition.getFloorZ(), secondPosition.getFloorZ());

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ, firstPosition.getLevel());
    }

}
