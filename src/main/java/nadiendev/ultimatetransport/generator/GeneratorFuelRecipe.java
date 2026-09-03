package nadiendev.ultimatetransport.generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatetransport.registry.UTRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record GeneratorFuelRecipe(GeneratorType generator, Ingredient fuel, int burnTime, int rate)
        implements Recipe<SingleRecipeInput> {

    public static final MapCodec<GeneratorFuelRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(GeneratorType::values).fieldOf("generator")
                    .forGetter(GeneratorFuelRecipe::generator),
            Ingredient.CODEC.fieldOf("fuel").forGetter(GeneratorFuelRecipe::fuel),
            com.mojang.serialization.Codec.INT.fieldOf("burn_time").forGetter(GeneratorFuelRecipe::burnTime),
            com.mojang.serialization.Codec.INT.optionalFieldOf("rate", 0).forGetter(GeneratorFuelRecipe::rate)
    ).apply(instance, GeneratorFuelRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GeneratorFuelRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(id -> GeneratorType.VALUES[id], GeneratorType::ordinal),
            GeneratorFuelRecipe::generator,
            Ingredient.CONTENTS_STREAM_CODEC, GeneratorFuelRecipe::fuel,
            ByteBufCodecs.VAR_INT, GeneratorFuelRecipe::burnTime,
            ByteBufCodecs.VAR_INT, GeneratorFuelRecipe::rate,
            GeneratorFuelRecipe::new);

    public int rateOr(int fallback) {
        return rate > 0 ? rate : fallback;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return fuel.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return UTRecipes.GENERATOR_FUEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return UTRecipes.GENERATOR_FUEL.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<GeneratorFuelRecipe> {

        @Override
        public MapCodec<GeneratorFuelRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GeneratorFuelRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
