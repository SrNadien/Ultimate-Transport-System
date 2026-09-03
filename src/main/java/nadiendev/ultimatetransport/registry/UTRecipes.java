package nadiendev.ultimatetransport.registry;

import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.facade.FacadeCraftingRecipe;
import nadiendev.ultimatetransport.generator.GeneratorFuelRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UTRecipes {

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, UltimateTransport.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, UltimateTransport.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GeneratorFuelRecipe>> GENERATOR_FUEL =
            TYPES.register("generator_fuel", () -> RecipeType.simple(UltimateTransport.id("generator_fuel")));

    public static final DeferredHolder<RecipeSerializer<?>, GeneratorFuelRecipe.Serializer> GENERATOR_FUEL_SERIALIZER =
            SERIALIZERS.register("generator_fuel", GeneratorFuelRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FacadeCraftingRecipe>>
            FACADE_CRAFTING = SERIALIZERS.register("facade",
                    () -> new SimpleCraftingRecipeSerializer<>(FacadeCraftingRecipe::new));

    private UTRecipes() {
    }
}
