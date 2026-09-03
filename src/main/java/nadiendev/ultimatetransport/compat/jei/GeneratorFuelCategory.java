package nadiendev.ultimatetransport.compat.jei;

import nadiendev.ultimatetransport.api.Numbers;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import nadiendev.ultimatetransport.UltimateTransport;
import nadiendev.ultimatetransport.generator.GeneratorType;
import nadiendev.ultimatetransport.registry.UTBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class GeneratorFuelCategory implements IRecipeCategory<GeneratorFuel> {
    private static final Map<GeneratorType, RecipeType<GeneratorFuel>> TYPES = new EnumMap<>(GeneratorType.class);

    static {
        for (GeneratorType type : GeneratorType.VALUES) {
            if (type.consumesItems()) {
                TYPES.put(type, RecipeType.create(UltimateTransport.MODID,
                        "fuel_" + type.getSerializedName(), GeneratorFuel.class));
            }
        }
    }

    public static RecipeType<GeneratorFuel> typeOf(GeneratorType type) {
        return TYPES.get(type);
    }

    public static Set<GeneratorType> burners() {
        return TYPES.keySet();
    }

    private final GeneratorType generator;
    private final IDrawable icon;

    public GeneratorFuelCategory(IGuiHelper helper, GeneratorType generator) {
        this.generator = generator;
        this.icon = helper.createDrawableItemStack(new ItemStack(
                UTBlocks.generators().get(generator.ordinal()).get()));
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public RecipeType<GeneratorFuel> getRecipeType() {
        return TYPES.get(generator);
    }

    @Override
    public Component getTitle() {
        return UTBlocks.generators().get(generator.ordinal()).get().getName();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GeneratorFuel recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 12)
                .addIngredients(VanillaTypes.ITEM_STACK, recipe.fuels());
        builder.addSlot(RecipeIngredientRole.CATALYST, 27, 12)
                .addItemStack(new ItemStack(recipe.generator()));
    }

    @Override
    public void draw(GeneratorFuel recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        var font = net.minecraft.client.Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.ultimatetransport.fuel.rate",
                Numbers.compact(recipe.rate())), 50, 8, 0x404040, false);
        graphics.drawString(font, Component.translatable("jei.ultimatetransport.fuel.duration",
                        recipe.durationLabel()).withStyle(ChatFormatting.DARK_GRAY),
                50, 20, 0x404040, false);
    }
}
