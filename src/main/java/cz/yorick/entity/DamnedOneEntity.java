package cz.yorick.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class DamnedOneEntity extends MobEntity implements Ownable {
    private static final TrackedData<Animation> ANIMATION;
    static {
        TrackedDataHandler<Animation> handler = TrackedDataHandler.ofEnum(Animation.class);
        TrackedDataHandlerRegistry.register(handler);
        ANIMATION = DataTracker.registerData(DamnedOneEntity.class, handler);
    }
    private PlayerEntity owner;
    private boolean isCharging;
    //private boolean isShooting;
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState moveAnimationState = new AnimationState();
    public AnimationState attackingAnimationState = new AnimationState();
    public AnimationState animatingAnimationState = new AnimationState();
    public AnimationState grabbingAnimationState = new AnimationState();
    public DamnedOneEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new VexMoveControl(this);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ANIMATION, Animation.IDLE);
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return this.owner;
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.owner != null ? this.owner.getAttacking() : null;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        //this.goalSelector.add(2, new ShootMissileGoal());
        this.goalSelector.add(4, new ChargeTargetGoal());
        this.goalSelector.add(8, new LookAtTargetGoal());
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
    }

    private BlockPos getBounds() {
        //makes the entity hover around the owner while idle
        if(this.owner != null) {
            return this.owner.getBlockPos().add(0, 2, 0);
        }

        return null;
    }

    public void onTrackedDataSet(TrackedData<?> data) {
        if (ANIMATION.equals(data)) {
            switch (this.getAnimation()) {
                case IDLE -> {
                    //System.out.println("Starting idle");
                    this.moveAnimationState.stop();
                    this.animatingAnimationState.stop();
                    this.grabbingAnimationState.stop();
                    this.idleAnimationState.start(this.age);
                }
                case MOVING -> {
                    //System.out.println("Starting move");
                    this.idleAnimationState.stop();
                    this.animatingAnimationState.stop();
                    this.grabbingAnimationState.stop();
                    this.moveAnimationState.start(this.age);
                }
                case ATTACKING -> {
                    //System.out.println("Starting attack");
                    this.idleAnimationState.stop();
                    this.moveAnimationState.stop();
                    this.animatingAnimationState.stop();
                    this.grabbingAnimationState.stop();
                    this.attackingAnimationState.start(this.age);
                }
                case ANIMATING -> {
                    System.out.println("Starting animating");
                    this.animatingAnimationState.start(this.age);
                }
                case GRABBING -> {
                    System.out.println("Starting grabbing");
                    this.grabbingAnimationState.start(this.age);
                }
            }
        }

        super.onTrackedDataSet(data);
    }

    public void setAnimation(Animation animation) {
        this.dataTracker.set(ANIMATION, animation);
    }

    public Animation getAnimation() {
        return this.dataTracker.get(ANIMATION);
    }

    private enum Animation {
        IDLE,
        MOVING,
        ATTACKING,
        ANIMATING,
        GRABBING;
    }
    /*
    private class ShootMissileGoal extends Goal {
        private final int maxCastTime = 2 * 20;
        private int castTime;
        public ShootMissileGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = DamnedOneEntity.this.getTarget();
            if (target != null && target.isAlive() && !DamnedOneEntity.this.getMoveControl().isMoving() && DamnedOneEntity.this.random.nextInt(toGoalTicks(7)) == 0) {
                return DamnedOneEntity.this.squaredDistanceTo(target) > 10.0;
            } else {
                return false;
            }
        }

        public boolean shouldContinue() {
            return DamnedOneEntity.this.isShooting && DamnedOneEntity.this.getTarget() != null && DamnedOneEntity.this.getTarget().isAlive() && this.castTime < this.maxCastTime;
        }

        @Override
        public void start() {
            this.castTime = 0;
            DamnedOneEntity.this.isShooting = true;
        }

        @Override
        public void stop() {
            this.castTime = 0;
            DamnedOneEntity.this.isShooting = false;
        }

        @Override
        public void tick() {
            this.castTime++;
            //finished casting
            if(this.castTime == this.maxCastTime) {
                DamnedOneEntity.this.getWorld().spawnEntity(new ShulkerBulletEntity(DamnedOneEntity.this.getWorld(), DamnedOneEntity.this, DamnedOneEntity.this.getTarget(), Direction.Axis.pickRandomAxis(getRandom())));
                DamnedOneEntity.this.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (DamnedOneEntity.this.random.nextFloat() - DamnedOneEntity.this.random.nextFloat()) * 0.2F + 1.0F);
            }
        }
    }*/

    //---------------------------------------------------------------------------------------------------------------------------
    //everything below is a vex copy - cannot extend VexEntity since it implements Ownable but forces the owner to be a MobEntity
    //---------------------------------------------------------------------------------------------------------------------------
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VEX_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height - 0.28125F;
    }

    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);
        this.checkBlockCollision();
    }

    public void tick() {
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);
    }

    private class VexMoveControl extends MoveControl {
        private State prevState = State.WAIT;
        public VexMoveControl(DamnedOneEntity owner) {
            super(owner);
        }

        public void tick() {
            if (this.state == State.MOVE_TO) {
                Vec3d vec3d = new Vec3d(this.targetX - DamnedOneEntity.this.getX(), this.targetY - DamnedOneEntity.this.getY(), this.targetZ - DamnedOneEntity.this.getZ());
                double d = vec3d.length();
                if (d < DamnedOneEntity.this.getBoundingBox().getAverageSideLength()) {
                    this.state = State.WAIT;
                    DamnedOneEntity.this.setVelocity(DamnedOneEntity.this.getVelocity().multiply(0.5));
                } else {
                    DamnedOneEntity.this.setVelocity(DamnedOneEntity.this.getVelocity().add(vec3d.multiply(this.speed * 0.05 / d)));
                    if (DamnedOneEntity.this.getTarget() == null) {
                        Vec3d vec3d2 = DamnedOneEntity.this.getVelocity();
                        DamnedOneEntity.this.setYaw(-((float) MathHelper.atan2(vec3d2.x, vec3d2.z)) * 57.295776F);
                        DamnedOneEntity.this.bodyYaw = DamnedOneEntity.this.getYaw();
                    } else {
                        double e = DamnedOneEntity.this.getTarget().getX() - DamnedOneEntity.this.getX();
                        double f = DamnedOneEntity.this.getTarget().getZ() - DamnedOneEntity.this.getZ();
                        DamnedOneEntity.this.setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776F);
                        DamnedOneEntity.this.bodyYaw = DamnedOneEntity.this.getYaw();
                    }
                }
            }

            //started moving
            if(this.state == State.MOVE_TO && this.prevState != State.MOVE_TO) {
                DamnedOneEntity.this.setAnimation(Animation.MOVING);
            }

            //stopped moving
            if(this.state != State.MOVE_TO && this.prevState == State.MOVE_TO) {
                DamnedOneEntity.this.setAnimation(Animation.IDLE);
            }

            this.prevState = state;
        }

        @Override
        public void moveTo(double x, double y, double z, double speed) {
            super.moveTo(x, y, z, speed);
        }
    }

    private class ChargeTargetGoal extends Goal {
        public ChargeTargetGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            LivingEntity target = DamnedOneEntity.this.getTarget();
            if (target != null && target.isAlive() && !DamnedOneEntity.this.getMoveControl().isMoving() && DamnedOneEntity.this.random.nextInt(toGoalTicks(7)) == 0) {
                return DamnedOneEntity.this.squaredDistanceTo(target) > 4.0;
            } else {
                return false;
            }
        }

        public boolean shouldContinue() {
            return DamnedOneEntity.this.getMoveControl().isMoving() && DamnedOneEntity.this.isCharging && DamnedOneEntity.this.getTarget() != null && DamnedOneEntity.this.getTarget().isAlive();
        }

        public void start() {
            LivingEntity livingEntity = DamnedOneEntity.this.getTarget();
            if (livingEntity != null) {
                Vec3d vec3d = livingEntity.getEyePos();
                DamnedOneEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
            }

            DamnedOneEntity.this.isCharging = true;
            DamnedOneEntity.this.playSound(SoundEvents.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
        }

        public void stop() {
            DamnedOneEntity.this.isCharging = false;
        }

        public boolean shouldRunEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity target = DamnedOneEntity.this.getTarget();
            if (target != null) {
                if (DamnedOneEntity.this.getBoundingBox().intersects(target.getBoundingBox())) {
                    DamnedOneEntity.this.tryAttack(target);
                    DamnedOneEntity.this.setAnimation(Animation.ATTACKING);
                    DamnedOneEntity.this.isCharging = false;
                } else {
                    double distanceToTarget = DamnedOneEntity.this.squaredDistanceTo(target);
                    if (distanceToTarget < 9.0) {
                        Vec3d vec3d = target.getEyePos();
                        DamnedOneEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
                    }
                }
            }
        }
    }

    private class LookAtTargetGoal extends Goal {
        public LookAtTargetGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            return !DamnedOneEntity.this.getMoveControl().isMoving() && DamnedOneEntity.this.random.nextInt(toGoalTicks(7)) == 0;
        }

        public boolean shouldContinue() {
            return false;
        }

        public void tick() {
            BlockPos boundPos = DamnedOneEntity.this.getBounds();
            if (boundPos == null) {
                boundPos = DamnedOneEntity.this.getBlockPos();
            }

            for(int i = 0; i < 3; ++i) {
                BlockPos randomPos = boundPos.add(DamnedOneEntity.this.random.nextInt(15) - 7, DamnedOneEntity.this.random.nextInt(11) - 5, DamnedOneEntity.this.random.nextInt(15) - 7);
                if (DamnedOneEntity.this.getWorld().isAir(randomPos)) {
                    DamnedOneEntity.this.moveControl.moveTo((double)randomPos.getX() + 0.5, (double)randomPos.getY() + 0.5, (double)randomPos.getZ() + 0.5, 0.25);
                    if (DamnedOneEntity.this.getTarget() == null) {
                        DamnedOneEntity.this.getLookControl().lookAt((double)randomPos.getX() + 0.5, (double)randomPos.getY() + 0.5, (double)randomPos.getZ() + 0.5, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}
