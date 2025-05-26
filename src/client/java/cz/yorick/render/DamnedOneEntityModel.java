package cz.yorick.render;

import cz.yorick.LastRites;
import cz.yorick.entity.DamnedOneEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.WardenAnimations;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DamnedOneEntityModel extends SinglePartEntityModel<DamnedOneEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Identifier.of(LastRites.MOD_ID, "damned_one"), "main");
    private final ModelPart root;
    private final ModelPart main;
    private final ModelPart body;
    private final ModelPart leg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart head;
    private final ModelPart eyes;

    public DamnedOneEntityModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.root = root;
        this.main = root.getChild("main");
        this.body = this.main.getChild("body");
        this.leg = this.body.getChild("leg");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.head = this.body.getChild("head");
        this.eyes = this.head.getChild("eyes");
    }

    public static TexturedModelData getTexturedModelLayer() {
        ModelData modelData = new ModelData();
        ModelPartData rootData = modelData.getRoot();

        //ModelPartData main = rootData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 17.0F, 0.0F));
        ModelPartData main = rootData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 10.0F, 0.0F));

        ModelPartData body = main.addChild("body", ModelPartBuilder.create().uv(20, 30).cuboid(-4.0F, -6.0F, -3.0F, 8.0F, 12.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 1.0F, 0.0F));

        body.addChild("leg", ModelPartBuilder.create().uv(20, 47).cuboid(-7.0F, 0.0F, -0.5F, 14.0F, 17.0F, 0.0F, new Dilation(0.0F))
                .uv(46, 36).cuboid(-2.5F, 0.0F, -2.0F, 5.0F, 8.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 6.0F, 0.0F));

        ModelPartData rightArm = body.addChild("right_arm", ModelPartBuilder.create().uv(40, 0).cuboid(-3.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F, new Dilation(0.01F))
                .uv(0, 30).cuboid(-10.0F, -7.0F, 0.0F, 10.0F, 24.0F, 0.0F, new Dilation(0.01F)), ModelTransform.pivot(-4.0F, -4.0F, -0.5F));

        ModelPartData leftArm = body.addChild("left_arm", ModelPartBuilder.create().uv(40, 0).mirrored().cuboid(0.0F, -2.0F, -2.0F, 3.0F, 14.0F, 4.0F, new Dilation(0.01F)).mirrored(false)
                .uv(0, 30).mirrored().cuboid(0.0F, -7.0F, 0.0F, 10.0F, 24.0F, 0.0F, new Dilation(0.01F)).mirrored(false), ModelTransform.pivot(4.0F, -4.0F, -0.5F));

        ModelPartData head = body.addChild("head", ModelPartBuilder.create().uv(0, 15).cuboid(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-10.0F, -15.0F, 0.0F, 20.0F, 15.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -6.0F, 0.0F));

        head.addChild("eyes", ModelPartBuilder.create().uv(32, 23).cuboid(-4.0F, -3.0F, 0.0F, 8.0F, 6.0F, 0.0F, new Dilation(0.01F)), ModelTransform.pivot(0.0F, -4.0F, -4.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(DamnedOneEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        getPart().traverse().forEach(ModelPart::resetTransform);
        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
        this.updateAnimation(entity.idleAnimationState, DamnedOneAnimation.IDLE, animationProgress);
        this.updateAnimation(entity.moveAnimationState, DamnedOneAnimation.MOVE, animationProgress);
        this.updateAnimation(entity.attackingAnimationState, DamnedOneAnimation.ATTACK, animationProgress);
        this.updateAnimation(entity.animatingAnimationState, DamnedOneAnimation.ANIMATE_SOUL_ASH, animationProgress);
        this.updateAnimation(entity.grabbingAnimationState, DamnedOneAnimation.GRAB, animationProgress);
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
