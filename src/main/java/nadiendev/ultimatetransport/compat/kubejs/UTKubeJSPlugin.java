package nadiendev.ultimatetransport.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import nadiendev.ultimatetransport.UltimateTransport;
import net.minecraft.world.item.crafting.Ingredient;

public class UTKubeJSPlugin implements KubeJSPlugin {

    public static final RecipeKey<String> GENERATOR = StringComponent.STRING.otherKey("generator");
    public static final RecipeKey<Ingredient> FUEL = IngredientComponent.INGREDIENT.inputKey("fuel");
    public static final RecipeKey<Integer> BURN_TIME = NumberComponent.INT.otherKey("burn_time");
    public static final RecipeKey<Integer> RATE = NumberComponent.INT.otherKey("rate").optional(0);

    public static final RecipeSchema GENERATOR_FUEL = new RecipeSchema(GENERATOR, FUEL, BURN_TIME, RATE);

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(UltimateTransport.id("generator_fuel"), GENERATOR_FUEL);
    }
}
