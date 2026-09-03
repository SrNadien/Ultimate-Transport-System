package nadiendev.ultimatetransport.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.cable.UpgradeTier;
import nadiendev.ultimatetransport.cell.EnergyCellTier;
import nadiendev.ultimatetransport.generator.GeneratorType;
import nadiendev.ultimatetransport.client.FilterScreen;
import nadiendev.ultimatetransport.registry.UTBlocks;
import nadiendev.ultimatetransport.tube.TubeRegistration;
import nadiendev.ultimatetransport.registry.UTItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Everything in this mod is crafted with ordinary recipes, so there is no machine category to add.
 * What JEI is good for here is explaining the pieces whose use is not obvious from a recipe, and
 * letting a rule be filled by dragging an item out of JEI onto the editor.
 */
@JeiPlugin
public class UTJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = UltimateTransport.id("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (GeneratorType type : GeneratorFuelCategory.burners()) {
            registration.addRecipeCategories(
                    new GeneratorFuelCategory(registration.getJeiHelpers().getGuiHelper(), type));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var generator : UTBlocks.generators()) {
            GeneratorType type = generator.get().type();
            if (type.consumesItems()) {
                registration.addRecipeCatalyst(new ItemStack(generator.get()),
                        GeneratorFuelCategory.typeOf(type));
            }
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (GeneratorType type : GeneratorFuelCategory.burners()) {
            registration.addRecipes(GeneratorFuelCategory.typeOf(type), GeneratorFuel.forType(type));
        }
        for (var generator : UTBlocks.generators()) {
            info(registration, new ItemStack(generator.get()),
                    "generator." + generator.get().type().getSerializedName());
        }
        info(registration, new ItemStack(UTItems.CONFIGURATOR.get()), "configurator");
        info(registration, new ItemStack(UTItems.FACADE.get()), "facade");
        info(registration, new ItemStack(UTItems.DESTINATION_TOOL.get()), "destination_tool");
        info(registration, new ItemStack(TubeRegistration.TUBE_ITEM.get()), "player_tube");
        info(registration, new ItemStack(TubeRegistration.STATION_ITEM.get()), "station");

        for (var cable : UTBlocks.cables()) {
            info(registration, new ItemStack(cable.get()), "cable");
        }
        for (UpgradeTier tier : UpgradeTier.VALUES) {
            if (tier != UpgradeTier.NONE) {
                info(registration, new ItemStack(UTItems.UPGRADES.get(tier).get()), "upgrade");
            }
        }
        for (EnergyCellTier tier : EnergyCellTier.VALUES) {
            info(registration, new ItemStack(UTItems.CELLS.get(tier).get()), "energy_cell");
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(FilterScreen.class, new FilterGhostHandler());
    }

    private static void info(IRecipeRegistration registration, ItemStack stack, String key) {
        registration.addIngredientInfo(stack, VanillaTypes.ITEM_STACK,
                Component.translatable("jei.ultimatetransport." + key));
    }
}
