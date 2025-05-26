package cz.yorick;

import cz.yorick.item.CurseBladeItem;
import cz.yorick.render.DamnedOneEntityModel;
import cz.yorick.render.DamnedOneEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class LastRitesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		//clamped between 0 and 1
		ModelPredicateProviderRegistry.register(LastRites.CURSEBLADE, Identifier.of(LastRites.MOD_ID, "stage"), (stack, world, entity, seed) -> CurseBladeItem.getStage(stack)/4F);

		EntityModelLayerRegistry.registerModelLayer(DamnedOneEntityModel.LAYER, DamnedOneEntityModel::getTexturedModelLayer);
		EntityRendererRegistry.register(LastRites.DAMNED_ONE_ENTITY_TYPE, DamnedOneEntityRenderer::new);

		BlockRenderLayerMap.INSTANCE.putBlock(LastRites.BRAZIER, RenderLayer.getCutout());
	}
}