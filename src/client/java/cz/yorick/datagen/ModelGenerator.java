package cz.yorick.datagen;

import cz.yorick.LastRites;
import cz.yorick.block.BrazierBlock;
import cz.yorick.block.SoulAshBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;

public class ModelGenerator extends FabricModelProvider {
    public ModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        registerAsh(blockStateModelGenerator);
        registerBrazier(blockStateModelGenerator);
    }

    private void registerAsh(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(LastRites.SOUL_ASH).coordinate(
                        BlockStateVariantMap.create(SoulAshBlock.LAYERS).register(
                                height -> BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(LastRites.SOUL_ASH, "_height" + height))
                        )
                )
        );
    }

    private void registerBrazier(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(LastRites.BRAZIER).coordinate(
                        BlockStateVariantMap.create(BrazierBlock.URN, BrazierBlock.FIRE).register(
                                (urn, fire) -> {
                                    String suffix = "";
                                    if(urn != BrazierBlock.UrnType.NONE) {
                                        suffix = suffix + "_" + urn.asString();
                                    }

                                    if(fire != BrazierBlock.FireType.NONE) {
                                        suffix = suffix + "_" + fire.asString();
                                    }

                                    return BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(LastRites.BRAZIER, suffix));
                                }
                        )
                )
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(LastRites.SOUL_ASH.asItem(), Models.GENERATED);
        itemModelGenerator.register(LastRites.ELDRITCH_URN, Models.GENERATED);
        itemModelGenerator.register(LastRites.CLAY_URN_ITEM, Models.GENERATED);
        //base one with overrides written manually
        itemModelGenerator.register(LastRites.CURSEBLADE, "2", Models.HANDHELD);
        itemModelGenerator.register(LastRites.CURSEBLADE, "3", Models.HANDHELD);
        itemModelGenerator.register(LastRites.CURSEBLADE, "4", Models.HANDHELD);
    }
}
