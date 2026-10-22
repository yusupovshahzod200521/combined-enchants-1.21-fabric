package uz.shahzod.combinedenchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(AnvilScreenHandler.class)
public class AnvilMixin {

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void onUpdateResult(CallbackInfo ci) {
        AnvilScreenHandler handler = (AnvilScreenHandler)(Object)this;

        ItemStack left = handler.getSlot(0).getStack();
        ItemStack right = handler.getSlot(1).getStack();

        if (left.isEmpty() || right.isEmpty()) return;
        if (!(left.getItem() instanceof net.minecraft.item.ArmorItem)) return;

        Map<Enchantment, Integer> rightEnchs = new HashMap<>();
        EnchantmentHelper.get(right).forEach((entry, level) ->
            rightEnchs.put(entry.value(), level)
        );

        if (rightEnchs.isEmpty()) return;

        boolean hasProtection =
            rightEnchs.containsKey(Enchantments.PROTECTION) ||
            rightEnchs.containsKey(Enchantments.FIRE_PROTECTION) ||
            rightEnchs.containsKey(Enchantments.BLAST_PROTECTION) ||
            rightEnchs.containsKey(Enchantments.PROJECTILE_PROTECTION);

        if (!hasProtection) return;

        ItemStack result = handler.getSlot(2).getStack();
        ItemStack newResult = result.isEmpty() ? left.copy() : result.copy();

        Map<Enchantment, Integer> currentEnchs = new HashMap<>();
        EnchantmentHelper.get(newResult).forEach((entry, level) ->
            currentEnchs.put(entry.value(), level)
        );

        for (Map.Entry<Enchantment, Integer> entry : rightEnchs.entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();
            int existing = currentEnchs.getOrDefault(ench, 0);
            int newLevel = existing == level
                ? Math.min(level + 1, ench.getMaxLevel())
                : Math.max(existing, level);
            currentEnchs.put(ench, newLevel);
        }

        EnchantmentHelper.set(newResult, currentEnchs.entrySet().stream()
            .collect(Collectors.toMap(
                e -> net.minecraft.registry.Registries.ENCHANTMENT
                    .getEntry(net.minecraft.registry.Registries.ENCHANTMENT.getId(e.getKey()))
                    .orElseThrow(),
                Map.Entry::getValue
            )));

        handler.getSlot(2).setStack(newResult);
    }
}
