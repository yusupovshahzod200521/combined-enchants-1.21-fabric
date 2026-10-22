package uz.shahzod.combinedenchants.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilMixin {

    private static final String PROTECTION = "minecraft:protection";
    private static final String FIRE_PROTECTION = "minecraft:fire_protection";
    private static final String BLAST_PROTECTION = "minecraft:blast_protection";
    private static final String PROJECTILE_PROTECTION = "minecraft:projectile_protection";

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void onUpdateResult(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler)(Object)this;

        ItemStack left = handler.getSlot(0).getStack();
        ItemStack right = handler.getSlot(1).getStack();

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

        ItemStack result = handler.getSlot(2).getStack();
        ItemStack newResult = result.isEmpty() ? left.copy() : result.copy();

        ItemEnchantmentsComponent currentEnchs = newResult.getOrDefault(
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

        newResult.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        handler.getSlot(2).setStack(newResult);
    }
}
