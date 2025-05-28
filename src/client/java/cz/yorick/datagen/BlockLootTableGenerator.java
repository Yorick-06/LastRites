package cz.yorick.datagen;

import cz.yorick.LastRites;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class BlockLootTableGenerator extends FabricBlockLootTableProvider {
    public BlockLootTableGenerator(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        addDrop(LastRites.BRAZIER);
        addDrop(LastRites.CLAY_URN);
    }
}
