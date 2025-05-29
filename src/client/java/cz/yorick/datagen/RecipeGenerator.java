package cz.yorick.datagen;

import cz.yorick.LastRites;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

import java.util.function.Consumer;

public class RecipeGenerator extends FabricRecipeProvider {
    public RecipeGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, LastRites.ELDRITCH_URN)
                .pattern("S S")
                .pattern("S S")
                .pattern("SSS")
                .input('S', Items.NETHERITE_SCRAP)
                .criterion(hasItem(Items.NETHERITE_SCRAP), conditionsFromItem(Items.NETHERITE_SCRAP))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, LastRites.CLAY_URN)
                .pattern(" B ")
                .pattern("B B")
                .pattern("BBB")
                .input('B', Items.BRICK)
                .criterion(hasItem(Items.BRICK), conditionsFromItem(Items.BRICK))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, LastRites.CURSEBLADE)
                .pattern(" A ")
                .pattern(" A ")
                .pattern(" S ")
                .input('A', LastRites.SOUL_ASH)
                .input('S', Items.STICK)
                .criterion(hasItem(LastRites.SOUL_ASH), conditionsFromItem(LastRites.SOUL_ASH))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, LastRites.BRAZIER)
                .pattern("   ")
                .pattern("BCB")
                .pattern("SSS")
                .input('B', Items.IRON_BARS)
                .input('C', Items.COAL_BLOCK)
                .input('S', Items.COBBLESTONE)
                .criterion(hasItem(Items.COBBLESTONE), conditionsFromItem(Items.COBBLESTONE))
                .offerTo(exporter);
    }
}
