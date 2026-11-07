package net.excavator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExcavatorMod.MODID)
public class ModEvents {
    private static final Map<UUID, Direction> PLAYER_HIT_SIDES = new HashMap<>();
    private static final ThreadLocal<Boolean> IS_MINING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getFace() != null) {
            PLAYER_HIT_SIDES.put(event.getEntity().getUUID(), event.getFace());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide() || !(event.getPlayer() instanceof ServerPlayer player)) return;
        if (IS_MINING.get()) return;

        ItemStack heldItem = player.getMainHandItem();
        boolean isSuper = heldItem.getItem() == ModItems.SUPER_PICKAXE.get();
        boolean isPickme = heldItem.getItem() == ModItems.PICKME_PICKAXE.get();

        if (isSuper || isPickme) {
            Level level = player.level();
            BlockPos originPos = event.getPos();
            Direction side = PLAYER_HIT_SIDES.getOrDefault(player.getUUID(), Direction.UP);
            
            // Если оригинальная кирка — радиус 2 (5х5), если Pickme — радиус 1 (3х3)
            int radius = isSuper ? 2 : 1; 

            try {
                IS_MINING.set(true);
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            int offX = 0; int offY = 0; int offZ = 0;

                            if (side == Direction.UP || side == Direction.DOWN) {
                                offX = x; offY = 0; offZ = z;
                            } else if (side == Direction.EAST || side == Direction.WEST) {
                                offX = 0; offY = y; offZ = z;
                            } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                                offX = x; offY = y; offZ = 0;
                            }

                            BlockPos targetPos = originPos.offset(offX, offY, offZ);
                            if (targetPos.equals(originPos)) continue;

                            BlockState targetState = level.getBlockState(targetPos);
                            if (targetState.getDestroySpeed(level, targetPos) >= 0.0F) {
                                if (level instanceof ServerLevel serverLevel) {
                                    LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(targetPos))
                                            .withParameter(LootContextParams.TOOL, heldItem)
                                            .withOptionalParameter(LootContextParams.THIS_ENTITY, player);

                                    List<ItemStack> drops = targetState.getDrops(lootBuilder);
                                    for (ItemStack drop : drops) {
                                        Block.popResource(level, targetPos, drop);
                                    }
                                    level.levelEvent(2001, targetPos, Block.getId(targetState));
                                    level.removeBlock(targetPos, false);
                                }
                            }
                        }
                    }
                }
            } finally {
                IS_MINING.set(false);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack heldItem = player.getMainHandItem();
        boolean isSuper = heldItem.getItem() == ModItems.SUPER_HOE.get();
        boolean isPickme = heldItem.getItem() == ModItems.PICKME_HOE.get();

        if (isSuper || isPickme) {
            Level level = player.level();
            BlockPos originPos = event.getPos();
            BlockState originState = level.getBlockState(originPos);

            if (originState.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || 
                originState.is(net.minecraft.world.level.block.Blocks.DIRT) || 
                originState.is(net.minecraft.world.level.block.Blocks.DIRT_PATH) || 
                originState.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)) {

                if (isSuper) {
                    // Стандартная логика 3х3 вокруг центра для Супер-мотыги
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos targetPos = originPos.offset(x, 0, z);
                            tilledAction(level, targetPos, player);
                        }
                    }
                } else if (isPickme) {
                    // Умная логика 2х2 (всегда направо и вперед/вверх относительно взгляда игрока)
                    Direction playerFacing = player.getDirection();
                    
                    int forwardX = playerFacing.getStepX();
                    int forwardZ = playerFacing.getStepZ();
                    
                    // В Minecraft правое направление перпендикулярно направлению вперед
                    Direction rightDirection = playerFacing.getClockWise();
                    int rightX = rightDirection.getStepX();
                    int rightZ = rightDirection.getStepZ();

                    // Перебираем 4 блока: 
                    // 1. Сам блок (0,0)
                    // 2. Блок впереди (вверх)
                    // 3. Блок справа
                    // 4. Блок впереди и справа
                    BlockPos[] targets = new BlockPos[] {
                        originPos,
                        originPos.offset(forwardX, 0, forwardZ),
                        originPos.offset(rightX, 0, rightZ),
                        originPos.offset(forwardX + rightX, 0, forwardZ + rightZ)
                    };

                    for (BlockPos targetPos : targets) {
                        tilledAction(level, targetPos, player);
                    }
                }

                // Общие эффекты для обеих мотыг
                level.playSound(null, originPos, net.minecraft.sounds.SoundEvents.HOE_TILL, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
            }
        }
    }

    // Вспомогательный метод, чтобы не дублировать код вспахивания одного блока
    private static void tilledAction(Level level, BlockPos targetPos, ServerPlayer player) {
        BlockState targetState = level.getBlockState(targetPos);
        BlockPos abovePos = targetPos.above();

        if (level.isEmptyBlock(abovePos)) {
            if (targetState.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || 
                targetState.is(net.minecraft.world.level.block.Blocks.DIRT) ||
                targetState.is(net.minecraft.world.level.block.Blocks.DIRT_PATH) || 
                targetState.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)) {

                level.setBlock(targetPos, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 11);
                level.levelEvent(2001, targetPos, Block.getId(targetState));
            }
        }
    }
}