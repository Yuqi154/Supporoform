package slimeknights.tconstruct.gadgets.block;

import com.google.common.collect.ImmutableMap;
import com.iafenvoy.uranus.object.DamageUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class PunjiBlock extends Block {

    public static final DirectionProperty FACING = Properties.FACING;

    private static final BooleanProperty NORTH = Properties.NORTH;
    private static final BooleanProperty EAST = Properties.EAST;
    private static final BooleanProperty NORTHEAST = BooleanProperty.of("northeast");
    private static final BooleanProperty NORTHWEST = BooleanProperty.of("northwest");
    private static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public PunjiBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.DOWN)
                .with(NORTH, false)
                .with(EAST, false)
                .with(NORTHEAST, false)
                .with(NORTHWEST, false)
                .with(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public PathNodeType getBlockPathType(BlockState state, BlockView level, BlockPos pos, @Nullable MobEntity mob) {
        return PathNodeType.DAMAGE_OTHER;
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState facingState, WorldAccess world, BlockPos pos, BlockPos facingPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        // break if now invalid
        Direction direction = state.get(FACING);
        if (facing == direction && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        // apply north and east if relevant
        Direction north = getLocalNorth(direction);
        Direction east = getLocalEast(direction);
        if (facing == north) {
            state = state.with(NORTH, isConnected(world, direction, facingPos));
        } else if (facing == east) {
            state = state.with(EAST, isConnected(world, direction, facingPos));
        }

        // always update northeast and northwest, never gets direct updates
        BlockPos northPos = pos.offset(north);
        return state.with(NORTHEAST, isConnected(world, direction, northPos.offset(east)))
                .with(NORTHWEST, isConnected(world, direction, northPos.offset(east.getOpposite())));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, EAST, NORTHEAST, NORTHWEST, WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction direction = context.getSide().getOpposite();
        WorldView world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        BlockState state = this.getDefaultState().with(FACING, direction);
        // if the space is invalid, try again on other sides
        if (!state.canPlaceAt(world, pos)) {
            boolean isValid = false;
            for (Direction side : Direction.values()) {
                if (side != direction) {
                    state = state.with(FACING, side);
                    if (state.canPlaceAt(world, pos)) {
                        isValid = true;
                        direction = side;
                        break;
                    }
                }
            }
            if (!isValid) {
                return null;
            }
        }

        // apply connections
        Direction north = getLocalNorth(direction);
        Direction east = getLocalEast(direction);
        BlockPos northPos = pos.offset(north);
        return state.with(NORTH, isConnected(world, direction, northPos))
                .with(EAST, isConnected(world, direction, pos.offset(east)))
                .with(NORTHEAST, isConnected(world, direction, northPos.offset(east)))
                .with(NORTHWEST, isConnected(world, direction, northPos.offset(east.getOpposite())))
                .with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
    }

    /**
     * Checks if this should connect to the block at the position
     *
     * @param world  World instance
     * @param facing Punji stick facing
     * @param target Position to check
     * @return True if connected
     */
    private boolean isConnected(WorldView world, Direction facing, BlockPos target) {
        BlockState state = world.getBlockState(target);
        return state.getBlock() == this && state.get(FACING) == facing;
    }

    /**
     * Gets the facing relative north
     *
     * @param facing Punji stick facing
     * @return North for the given facing
     */
    private static Direction getLocalNorth(Direction facing) {
        return switch (facing) {
            case DOWN -> Direction.NORTH;
            case UP -> Direction.SOUTH;
            default -> Direction.UP;
        };
    }

    /**
     * Gets the facing relative east
     *
     * @param facing Punji stick facing
     * @return East for the given facing
     */
    private static Direction getLocalEast(Direction facing) {
        if (facing.getAxis() == Axis.Y) {
            return Direction.EAST;
        }
        return facing.rotateYClockwise();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos target = pos.offset(direction);
        return world.getBlockState(target).isSideSolidFullSquare(world, target, direction.getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity) {
            Direction side = state.get(FACING);
            Axis axis = side.getAxis();
            // only take damage if in the same half as the punji sticks
            if (side.getDirection() == AxisDirection.POSITIVE) {
                if (entityIn.getBoundingBox().getMax(axis) <= pos.getComponentAlongAxis(axis) + 0.5f) {
                    return;
                }
            } else {
                if (entityIn.getBoundingBox().getMin(axis) >= pos.getComponentAlongAxis(axis) + 0.5f) {
                    return;
                }
            }

            float damage = 1f;
            if (entityIn.fallDistance > 0) {
                damage += entityIn.fallDistance + 1;
            }
            entityIn.damage(DamageUtil.build(entityIn, DamageTypes.CACTUS), damage);
        }
    }

    /* Bounds */
    private static final ImmutableMap<Direction, VoxelShape> BOUNDS;

    static {
        ImmutableMap.Builder<Direction, VoxelShape> builder = ImmutableMap.builder();
        builder.put(Direction.DOWN, VoxelShapes.cuboid(0.1875, 0, 0.1875, 0.8125, 0.375, 0.8125));
        builder.put(Direction.UP, VoxelShapes.cuboid(0.1875, 0.625, 0.1875, 0.8125, 1, 0.8125));
        builder.put(Direction.NORTH, VoxelShapes.cuboid(0.1875, 0.1875, 0, 0.8125, 0.8125, 0.375));
        builder.put(Direction.SOUTH, VoxelShapes.cuboid(0.1875, 0.1875, 0.625, 0.8125, 0.8125, 1));
        builder.put(Direction.EAST, VoxelShapes.cuboid(0.625, 0.1875, 0.1875, 1, 0.8125, 0.8125));
        builder.put(Direction.WEST, VoxelShapes.cuboid(0, 0.1875, 0.1875, 0.375, 0.8125, 0.8125));

        BOUNDS = builder.build();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return BOUNDS.get(state.get(FACING));
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }
}