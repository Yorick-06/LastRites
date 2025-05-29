package cz.yorick.datagen;

import cz.yorick.LastRites;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.ItemTags;

import java.util.function.Consumer;

public class RecipeGenerator extends FabricRecipeProvider {
    public RecipeGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, LastRites.ELDRITCH_URN)
                .pattern(" S ")
                .pattern("SUS")
                .pattern(" S ")
                .input('S', Items.NETHERITE_SCRAP)
                .input('U', LastRites.CLAY_URN)
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
                .pattern("ASA")
                .pattern(" A ")
                .input('A', LastRites.SOUL_ASH)
                .input('S', Items.DIAMOND_SWORD)
                .criterion(hasItem(LastRites.SOUL_ASH), conditionsFromItem(LastRites.SOUL_ASH))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, LastRites.BRAZIER)
                .pattern("   ")
                .pattern("BCB")
                .pattern("BBB")
                .input('B', Items.IRON_BARS)
                .input('C', ItemTags.COALS)
                .criterion(hasItem(Items.COBBLESTONE), conditionsFromItem(Items.COBBLESTONE))
                .offerTo(exporter);
    }
}
