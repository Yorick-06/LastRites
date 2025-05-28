package cz.yorick.datagen;

import cz.yorick.LastRites;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {
    public BlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(LastRites.BRAZIER);
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL).add(LastRites.BRAZIER);

        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).add(LastRites.SOUL_ASH);
    }
}
