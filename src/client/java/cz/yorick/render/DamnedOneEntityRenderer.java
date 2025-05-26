package cz.yorick.render;

import cz.yorick.LastRites;
import cz.yorick.entity.DamnedOneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DamnedOneEntityRenderer extends MobEntityRenderer<DamnedOneEntity, DamnedOneEntityModel> {
    private static final Identifier TEXTURE = Identifier.of(LastRites.MOD_ID, "textures/entity/damned_one.png");
    public DamnedOneEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DamnedOneEntityModel(context.getPart(DamnedOneEntityModel.LAYER)), 0.3F);
    }

    @Override
    protected int getBlockLight(DamnedOneEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public Identifier getTexture(DamnedOneEntity entity) {
        return TEXTURE;
    }
}
