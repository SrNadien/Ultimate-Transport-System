package nadiendev.ultimatetransport.compat.jei;

import nadiendev.ultimatetransport.api.Numbers;
import nadiendev.ultimatetransport.generator.GeneratorType;
import nadiendev.ultimatetransport.generator.GeneratorFuelRecipe;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.registry.UTRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public record GeneratorFuel(GeneratorType type, List<ItemStack> fuels, int burnTicks, int rate) {
    public GeneratorFuel(GeneratorType type, List<ItemStack> fuels, int burnTicks) {
        this(type, fuels, burnTicks, type.rate());
    }

    public Block generator() {
        return UTBlocks.generators().get(type.ordinal()).get();
    }

    public Component durationLabel() {
        return Component.translatable("jei.ultimatetransport.fuel.ticks", Numbers.compact(burnTicks));
    }

    private static List<GeneratorFuel> fromRecipes() {
        List<GeneratorFuel> entries = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return entries;
        }
        for (RecipeHolder<GeneratorFuelRecipe> holder
                : minecraft.level.getRecipeManager().getAllRecipesFor(UTRecipes.GENERATOR_FUEL.get())) {
            GeneratorFuelRecipe recipe = holder.value();
            List<ItemStack> fuels = new ArrayList<>();
            for (ItemStack stack : recipe.fuel().getItems()) {
                if (!stack.isEmpty()) {
                    fuels.add(stack);
                }
            }
            if (!fuels.isEmpty()) {
                entries.add(new GeneratorFuel(recipe.generator(), fuels, recipe.burnTime(),
                        recipe.rateOr(recipe.generator().rate())));
            }
        }
        return entries;
    }

    public static List<GeneratorFuel> forType(GeneratorType wanted) {
        List<GeneratorFuel> matching = new ArrayList<>();
        for (GeneratorFuel entry : all()) {
            if (entry.type() == wanted) {
                matching.add(entry);
            }
        }
        return matching;
    }

    public static List<GeneratorFuel> all() {
        List<GeneratorFuel> entries = new ArrayList<>();
        entries.addAll(fromRecipes());

        java.util.Map<Integer, List<ItemStack>> byDuration = new java.util.TreeMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                continue;
            }
            int burn;
            try {
                burn = stack.getBurnTime(null);
            } catch (RuntimeException failed) {
                continue;
            }
            if (burn > 0) {
                byDuration.computeIfAbsent(burn, key -> new ArrayList<>()).add(stack);
            }
        }
        byDuration.forEach((burn, fuels) -> entries.add(new GeneratorFuel(GeneratorType.FUEL, fuels, burn)));
        entries.removeIf(entry -> entry.fuels().isEmpty() || entry.burnTicks() <= 0);
        return entries;
    }
}
