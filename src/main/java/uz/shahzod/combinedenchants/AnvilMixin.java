package uz.shahzod.combinedenchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!(left.getItem() instanceof net.minecraft.item.ArmorItem)) return;

        Map<RegistryEntry<Enchantment>, Integer> rightEnchs = EnchantmentHelper.get(right);
        if (rightEnchs.isEmpty()) return;

        boolean hasProtection = rightEnchs.keySet().stream().anyMatch(e -> {
            String id = e.getKey().map(k -> k.getValue().toString()).orElse("");
            return id.equals(PROTECTION) || id.equals(FIRE_PROTECTION)
                || id.equals(BLAST_PROTECTION) || id.equals(PROJECTILE_PROTECTION);
        });

        if (!hasProtection) return;

        ItemStack result = handler.getSlot(2).getStack();
        ItemStack newResult = result.isEmpty() ? left.copy() : result.copy();

        Map<RegistryEntry<Enchantment>, Integer> currentEnchs = new HashMap<>(EnchantmentHelper.get(newResult));

        for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : rightEnchs.entrySet()) {
            RegistryEntry<Enchantment> ench = entry.getKey();
            int level = entry.getValue();
            int existing = currentEnchs.getOrDefault(ench, 0);
            int newLevel = existing == level
                ? Math.min(level + 1, ench.value().getMaxLevel())
                : Math.max(existing, level);
            currentEnchs.put(ench, newLevel);
        }

        EnchantmentHelper.set(newResult, currentEnchs);
        handler.getSlot(2).setStack(newResult);
    }
}
