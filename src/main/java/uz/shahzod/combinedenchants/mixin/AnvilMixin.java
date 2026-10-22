package uz.shahzod.combinedenchants.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public class AnvilMixin {

    private static final String PROTECTION = "minecraft:protection";
    private static final String FIRE_PROTECTION = "minecraft:fire_protection";
    private static final String BLAST_PROTECTION = "minecraft:blast_protection";
    private static final String PROJECTILE_PROTECTION = "minecraft:projectile_protection";

    @Inject(method = "onTakeOutput", at = @At("RETURN"))
    private void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack left = ((AnvilScreenHandler)(Object)this).getSlot(0).getStack();
        ItemStack right = ((AnvilScreenHandler)(Object)this).getSlot(1).getStack();

        if (left.isEmpty() || right.isEmpty()) return;
        if (!(left.getItem() instanceof ArmorItem)) return;

        ItemEnchantmentsComponent rightEnchs = right.getOrDefault(
            DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        if (rightEnchs.isEmpty()) return;

        boolean hasProtection = rightEnchs.getEnchantments().stream().anyMatch(e -> {
            String id = e.getKey().map(k -> k.getValue().toString()).orElse("");
            return id.equals(PROTECTION) || id.equals(FIRE_PROTECTION)
                || id.equals(BLAST_PROTECTION) || id.equals(PROJECTILE_PROTECTION);
        });

        if (!hasProtection) return;

        ItemEnchantmentsComponent currentEnchs = stack.getOrDefault(
            DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(currentEnchs);

        for (RegistryEntry<Enchantment> ench : rightEnchs.getEnchantments()) {
            int rightLevel = rightEnchs.getLevel(ench);
            int currentLevel = currentEnchs.getLevel(ench);
            int newLevel = currentLevel == rightLevel
                ? Math.min(rightLevel + 1, ench.value().getMaxLevel())
                : Math.max(currentLevel, rightLevel);
            builder.set(ench, newLevel);
        }

        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }
}
