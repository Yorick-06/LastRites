package cz.yorick.entity;

import cz.yorick.LastRites;
import cz.yorick.block.SoulAshBlock;
import cz.yorick.mixin.FallingBlockAccessor;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class DamnedOneEntity extends PathAwareEntity implements Ownable {
    private static final TrackedData<Animation> ANIMATION;
    static {
        TrackedDataHandler<Animation> handler = TrackedDataHandler.ofEnum(Animation.class);
        TrackedDataHandlerRegistry.register(handler);
        ANIMATION = DataTracker.registerData(DamnedOneEntity.class, handler);
    }
    private PlayerEntity owner;
    private boolean isCharging;
    public AnimationState animationState = new AnimationState();
    public DamnedOneEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
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
    public PlayerEntity getOwner() {
        return this.owner;
    }

    public void setOwner(PlayerEntity owner) {
        this.owner = owner;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        if(this.owner != null && !(this.owner.getAttacking() instanceof DamnedOneEntity)) {
            return this.owner.getAttacking();
        }

        return null;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new ChargeTargetGoal());
        this.goalSelector.add(4, new GrabEntityGoal());
        this.goalSelector.add(6, new AnimateAshGoal(this));
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

    //always take control
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        if (this.hasPassenger(passenger)) {
            Vec3d entityPos = getPos();
            double y = entityPos.getY() + this.getMountedHeightOffset() + passenger.getHeightOffset();
            double x = entityPos.getX() + Math.sin(Math.toRadians(this.bodyYaw));
            double z = entityPos.getZ() + Math.cos(Math.toRadians(this.bodyYaw));
            passenger.setYaw(this.getYaw() - 180);
            positionUpdater.accept(passenger, x, y, z);
        }
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.5;
    }

    public static DefaultAttributeContainer.Builder createDamnedOneAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    public void onTrackedDataSet(TrackedData<?> data) {
        if (ANIMATION.equals(data)) {
            this.animationState.start(this.age);
        }

        super.onTrackedDataSet(data);
    }

    public void setAnimation(Animation animation) {
        this.dataTracker.set(ANIMATION, animation);
    }

    public Animation getAnimation() {
        return this.dataTracker.get(ANIMATION);
    }

    public enum Animation {
        IDLE,
        MOVING,
        ATTACKING,
        ANIMATING,
        GRABBING;
    }


    @Override
    public void onDeath(DamageSource damageSource) {
        Vec3d pos = getBlockPos().toCenterPos();
        getWorld().spawnEntity(FallingBlockAccessor.init(getWorld(), pos.getX(), pos.getY(), pos.getZ(), LastRites.SOUL_ASH.getDefaultState().with(SoulAshBlock.LAYERS, 4)));
        super.onDeath(damageSource);
    }

    private class AnimateAshGoal extends MoveToTargetPosGoal {
        private final int requiredTime = 100;
        private int animatingTime;
        private boolean prevReached = false;
        public AnimateAshGoal(PathAwareEntity mob) {
            super(mob, 1, 32, 10);
        }

        @Override
        protected void startMovingToTarget() {
            Vec3d pos = this.targetPos.up().toCenterPos();
            DamnedOneEntity.this.moveControl.moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
        }

        @Override
        public void tick() {
            super.tick();
            if(hasReached()) {
                if(!this.prevReached) {
                    DamnedOneEntity.this.setAnimation(Animation.ANIMATING);
                    this.prevReached = true;
                }

                this.animatingTime++;
                if(this.animatingTime > this.requiredTime) {
                    //validate block again, can crash (possibly if two entities animate it in the exact same tick or idk)
                    if(getWorld() instanceof ServerWorld serverWorld && isTargetPos(serverWorld, this.targetPos)) {
                        SoulAshBlock.animate(serverWorld, this.targetPos, serverWorld.getBlockState(this.targetPos), getOwner());
                        this.animatingTime = 0;
                    }
                }
            } else {
                //it can overshoot the pos, so if it stopped but has not reached the pos try again
                if(!DamnedOneEntity.this.moveControl.isMoving()) {
                    startMovingToTarget();
                }

                this.prevReached = false;
                this.animatingTime = 0;
            }
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
            if (chunk == null) {
                return false;
            }

            BlockState state = chunk.getBlockState(pos);
            return state.isOf(LastRites.SOUL_ASH) && state.get(SoulAshBlock.LAYERS) > 3;
        }

        @Override
        public void stop() {
            DamnedOneEntity.this.setAnimation(Animation.IDLE);
        }
    }

    private class GrabEntityGoal extends ChargeTargetGoal {
        private boolean grabbed = false;
        private Vec3d dropPos = null;

        @Override
        public void tick() {
            if(!this.grabbed) {
                LivingEntity target = DamnedOneEntity.this.getTarget();
                if (target != null) {
                    if (DamnedOneEntity.this.getBoundingBox().intersects(target.getBoundingBox())) {
                        target.startRiding(DamnedOneEntity.this);
                        DamnedOneEntity.this.setAnimation(Animation.GRABBING);
                        this.grabbed = true;
                        this.dropPos = DamnedOneEntity.this.getPos().add(randomOffset(), 20, randomOffset());
                        DamnedOneEntity.this.moveControl.moveTo(this.dropPos.getX(), this.dropPos.getY(), this.dropPos.getZ(), 0.5);
                    } else {
                        double distanceToTarget = DamnedOneEntity.this.squaredDistanceTo(target);
                        if (distanceToTarget < 9.0) {
                            Vec3d vec3d = target.getEyePos();
                            DamnedOneEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
                        }
                    }
                }
            } else {
                //in position
                if(this.dropPos.isInRange(DamnedOneEntity.this.getPos(), 1)) {
                    DamnedOneEntity.this.removeAllPassengers();
                    DamnedOneEntity.this.isCharging = false;
                }
            }
        }

        private double randomOffset() {
            return (4 * DamnedOneEntity.this.random.nextDouble()) - 2;
        }

        @Override
        public boolean canStart() {
            LivingEntity target = DamnedOneEntity.this.getTarget();
            return target != null && target.isAlive() && !DamnedOneEntity.this.getMoveControl().isMoving() && DamnedOneEntity.this.random.nextInt(toGoalTicks(7)) == 0;
        }

        @Override
        public void stop() {
            super.stop();
            DamnedOneEntity.this.removeAllPassengers();
        }

        @Override
        public void start() {
            super.start();
            this.grabbed = false;
            this.dropPos = null;
        }
    }

    private static final TypeFilter<Entity, DamnedOneEntity> filter = TypeFilter.instanceOf(DamnedOneEntity.class);
    public static void ownerLost(ServerPlayerEntity player) {
        player.getServer().getWorlds().forEach(serverWorld ->
                serverWorld.getEntitiesByType(filter,
                        entity -> entity.getOwner() == player
                ).forEach(DamnedOneEntity::kill)
        );
    }

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
        if(this.owner != null && this.owner.isRemoved()) {
            this.kill();
        }
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

            //started moving and has the idle anim (dont override grab/animate)
            if(this.state == State.MOVE_TO && this.prevState != State.MOVE_TO && DamnedOneEntity.this.getAnimation() == Animation.IDLE) {
                DamnedOneEntity.this.setAnimation(Animation.MOVING);
            }

            //stopped moving and has the move anim (dont override grab/animate)
            if(this.state != State.MOVE_TO && this.prevState == State.MOVE_TO && DamnedOneEntity.this.getAnimation() == Animation.MOVING) {
                DamnedOneEntity.this.setAnimation(Animation.IDLE);
            }

            this.prevState = this.state;
        }
    }

    private class ChargeTargetGoal extends Goal {
        public ChargeTargetGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            LivingEntity target = DamnedOneEntity.this.getTarget();
            if (target != null && target.isAlive() && !DamnedOneEntity.this.getMoveControl().isMoving() && DamnedOneEntity.this.random.nextInt(toGoalTicks(4)) == 0) {
                return DamnedOneEntity.this.squaredDistanceTo(target) > 4.0;
            } else {
                return false;
            }
        }

        public boolean shouldContinue() {
            return DamnedOneEntity.this.moveControl.isMoving() && DamnedOneEntity.this.isCharging && DamnedOneEntity.this.getTarget() != null && DamnedOneEntity.this.getTarget().isAlive();
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
            DamnedOneEntity.this.setAnimation(Animation.IDLE);
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
