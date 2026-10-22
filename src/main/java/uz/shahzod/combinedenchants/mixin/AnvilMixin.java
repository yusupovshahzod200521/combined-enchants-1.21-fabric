package uz.shahzod.combinedenchants.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ArmorItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class AnvilMixin {

    private static final String PROTECTION = "minecraft:protection";
    private static final String FIRE_PROTECTION = "minecraft:fire_protection";
    private static final String BLAST_PROTECTION = "minecraft:blast_protection";
    private static final String PROJECTILE_PROTECTION = "minecraft:projectile_protection";

    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private static void allowProtectionCombine(
            RegistryEntry<Enchantment> first,
            RegistryEntry<Enchantment> second,
            CallbackInfoReturnable<Boolean> cir) {

        String id1 = first.getKey().map(k -> k.getValue().toString()).orElse("");
        String id2 = second.getKey().map(k -> k.getValue().toString()).orElse("");

        boolean firstIsProtection = isProtection(id1);
        boolean secondIsProtection = isProtection(id2);

        if (firstIsProtection && secondIsProtection) {
            cir.setReturnValue(true);
        }
    }

    private static boolean isProtection(String id) {
        return id.equals(PROTECTION) || id.equals(FIRE_PROTECTION)
            || id.equals(BLAST_PROTECTION) || id.equals(PROJECTILE_PROTECTION);
    }
}
