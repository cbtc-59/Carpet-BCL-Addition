package com.bcl.carpet.bcladdition.mixin;

import com.bcl.carpet.bcladdition.settings.BCLAdditionSettings;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows trident channeling to spawn lightning without weather/sky conditions.
 * Controlled by {@code channelingIgnoreConditions} rule.
 */
@Mixin(TridentEntity.class)
public abstract class ChannelingIgnoreConditionsMixin extends PersistentProjectileEntity {

    protected ChannelingIgnoreConditionsMixin(EntityType<?> type, World world) {
        super((EntityType<? extends PersistentProjectileEntity>) type, world);
        throw new AssertionError("Mixin constructor should not be called");
    }

    @Inject(method = "onBlockHitEnchantmentEffects", at = @At("TAIL"))
    private void onBlockHit(ServerWorld world, BlockHitResult hit, ItemStack stack, CallbackInfo ci) {
        BlockPos pos = hit.getBlockPos();
        if (world.getBlockState(pos).isOf(Blocks.LIGHTNING_ROD)) {
            trySpawnLightning(world, pos.up(), stack);
        }
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void onEntityHit(net.minecraft.util.hit.EntityHitResult entityHitResult, CallbackInfo ci) {
        if (this.getWorld().isClient) return;
        ServerWorld world = (ServerWorld) this.getWorld();
        trySpawnLightning(world, entityHitResult.getEntity().getBlockPos(), this.getWeaponStack());
    }

    private void trySpawnLightning(ServerWorld world, BlockPos pos, ItemStack weapon) {
        String mode = BCLAdditionSettings.channelingIgnoreConditions;
        if ("false".equals(mode)) return;

        // Check if the weapon has channeling enchantment
        int channelingLevel = EnchantmentHelper.getLevel(
                world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.CHANNELING).get(),
                weapon);
        if (channelingLevel <= 0) return;

        // If it's already raining and thundering, vanilla handles it
        if (world.isRaining() && world.isThundering()) return;

        // Check sky access based on mode
        if ("ignore_weather_and_sky".equals(mode) || world.isSkyVisible(pos)) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.spawn(world, pos, SpawnReason.TRIGGERED);
            if (lightning != null && this.getOwner() instanceof ServerPlayerEntity player) {
                lightning.setChanneler(player);
            }
        }
    }
}
