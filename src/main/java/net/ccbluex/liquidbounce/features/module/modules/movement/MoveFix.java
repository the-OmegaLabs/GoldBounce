//// Optimized MoveFix.java
//package net.ccbluex.liquidbounce.features.module.modules.movement;
//
//import kotlin.jvm.internal.Intrinsics;
//import net.ccbluex.liquidbounce.event.EventTarget;
//import net.ccbluex.liquidbounce.event.StrafeEvent;
//import net.ccbluex.liquidbounce.event.UpdateEvent;
//import net.ccbluex.liquidbounce.features.module.Category;
//import net.ccbluex.liquidbounce.features.module.Module;
//import net.ccbluex.liquidbounce.utils.MinecraftInstance;
//import net.ccbluex.liquidbounce.utils.Rotation;
//import net.ccbluex.liquidbounce.utils.RotationUtils;
//import net.ccbluex.liquidbounce.utils.Vec3d;
//import net.ccbluex.liquidbounce.utils.extensions.MathExtensionsKt;
//import net.ccbluex.liquidbounce.value.BoolValue;
//import net.ccbluex.liquidbounce.value.ListValue;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.util.MathHelper;
//import org.jetbrains.annotations.NotNull;
//
//public final class MoveFix extends Module {
//
//    public static MoveFix INSTANCE = new MoveFix();
//
//    // Static fields remain unchanged for brevity
//    private static int groundTicksLocal;
//    private static double lastMotionY;
//    private static boolean wasClimbing;
//    private static boolean silentFix;
//    private static boolean doFix;
//    private static boolean isOverwrited;
//
//    private static int jumpfunny = 1;
//    private static long jumpticks = 0L;
//
//
//    private static BoolValue silentFixValue = new BoolValue("Silent", true, false, () -> true);
//
//    private static BoolValue spiderValue = new BoolValue("Spider", true, false, () -> true);
//
//    public ListValue mode = new ListValue("Mode", new String[]{"Grim", "Bloxd"}, "Grim", false, () -> true);
//
//    private NoaPhysics bloxdPhysics = new NoaPhysics();
//
//    public MoveFix() {
//        super("MoveFix", Category.MOVEMENT, -1, true, true, "Motion fix", "Move Fix", false, false, false);
//    }
//
//
//    @EventTarget
//    public final void onUpdate(@NotNull UpdateEvent event) {
//        Intrinsics.checkNotNullParameter(event, "event");
//        if (!isOverwrited) {
//            silentFix = (Boolean) silentFixValue.get();
//            doFix = this.getState();
//        }
//
//        EntityPlayerSP player = MinecraftInstance.mc.thePlayer;
//        if (player == null) return;
//
//        if ("Bloxd".equals(mode.get())) {
//            if (player.onGround) {
//                groundTicksLocal++;
//                if (groundTicksLocal > 5) jumpfunny = 0;
//            } else {
//                groundTicksLocal = 0;
//            }
//
//            if (player.isCollidedVertically && lastMotionY > (double) 0 && player.motionY <= (double) 0) {
//                bloxdPhysics.getVelocityVector().setY((double) 0.0F);
//                bloxdPhysics.getImpulseVector().setY((double) 0.0F);
//            }
//
//            lastMotionY = player.motionY;
//        }
//    }
//
//    public final void runStrafeFixLoop(boolean isSilent, @NotNull StrafeEvent event) {
//        Intrinsics.checkNotNullParameter(event, "event");
//
//        if (event.isCancelled()) return;
//
//        EntityPlayerSP player = MinecraftInstance.mc.thePlayer;
//        if (player == null) return;
//
//        Rotation rotation = RotationUtils.INSTANCE.getTargetRotation();
//        if (rotation == null) return;
//
//        float yaw = rotation.component1();
//        float strafe = event.getStrafe();
//        float forward = event.getForward();
//        float friction = event.getFriction();
//
//        // Early exit if no movement input
//        boolean hasInput = Math.abs(strafe) > 0.005f || Math.abs(forward) > 0.005f;
//        if (!hasInput) return;
//
//        float factor = strafe * strafe + forward * forward;
//        if (factor < 1.0E-4F) return;
//
//        // Calculate angle difference
//        float yawDiff = player.rotationYaw - yaw - 22.5F - 135.0F;
//        int angleDiff = (int) ((MathHelper.wrapAngleTo180_float(yawDiff) + 180.0F) / 45.0F);
//
//        // Compute final yaw based on silent mode
//        float calcYaw = isSilent ? yaw + 45.0F * angleDiff : yaw;
//
//        // Movement direction scaling logic
//        float moveDirAbs = Math.max(Math.abs(strafe), Math.abs(forward));
//        float calcMoveDirSq = moveDirAbs * moveDirAbs;
//        float divisor = Math.min(1.0F, calcMoveDirSq * 2.0F);
//        float calcMultiplier = (float) Math.sqrt(calcMoveDirSq / divisor);
//
//        // Apply multiplier adjustments only in silent mode
//        if (isSilent) {
//            boolean bothAxesUsed = Math.abs(forward) > 0.005f && Math.abs(strafe) > 0.005f;
//            switch (angleDiff) {
//                case 1:
//                case 3:
//                case 5:
//                case 7:
//                case 9:
//                    if (bothAxesUsed) {
//                        friction *= calcMultiplier;
//                    } else if (!bothAxesUsed) {
//                        friction /= calcMultiplier;
//                    }
//                    break;
//            }
//        }
//
//        // Normalize factor
//        factor = (float) Math.sqrt(factor);
//        factor = Math.max(factor, 1.0F);
//        factor = friction / factor;
//
//        strafe *= factor;
//        forward *= factor;
//
//        // Calculate sin/cos once
//        double radianYaw = MathExtensionsKt.toRadians(calcYaw);
//        float yawSin = MathHelper.sin((float) radianYaw);
//        float yawCos = MathHelper.cos((float) radianYaw);
//
//        // Apply motion
//        player.motionX += (strafe * yawCos - forward * yawSin);
//        player.motionZ += (forward * yawCos + strafe * yawSin);
//
//        event.cancelEvent();
//    }
//
//    @EventTarget
//    public final void onStrafe(@NotNull StrafeEvent event) {
//        Intrinsics.checkNotNullParameter(event, "event");
//        String currentMode = mode.get();
//
//        if ("Grim".equals(currentMode)) {
//            if (doFix) runStrafeFixLoop(silentFix, event);
//            return;
//        }
//
//        if (!"Bloxd".equals(currentMode)) return;
//
//        EntityPlayerSP player = MinecraftInstance.mc.thePlayer;
//        if (player == null) return;
//
//        if (player.onGround && bloxdPhysics.getVelocityVector().getY() < 0) {
//            bloxdPhysics.getVelocityVector().set(0, 0, 0);
//        }
//
//        if (player.onGround && player.motionY > 0.4199 && player.motionY < 0.4201) {
//            jumpfunny = Math.min(jumpfunny + 1, 3);
//            bloxdPhysics.getImpulseVector().add(0, 8, 0);
//        }
//
//        if (spiderValue.get()) {
//            boolean isMoving = Math.abs(event.getForward()) > 0.005 || Math.abs(event.getStrafe()) > 0.005;
//            if (player.isCollidedHorizontally && isMoving) {
//                bloxdPhysics.getVelocityVector().set(0, 8, 0);
//                wasClimbing = true;
//            } else if (wasClimbing) {
//                bloxdPhysics.getVelocityVector().set(0, 0, 0);
//                wasClimbing = false;
//            }
//        }
//
//        double speed = getBloxdSpeed();
//        Vec3d moveDir = getBloxdMoveVec(event.getForward(), event.getStrafe(), speed);
//        event.cancelEvent();
//
//        if (!MinecraftInstance.mc.theWorld.isBlockLoaded(player.getPosition()) || player.posY <= 0) {
//            player.motionX = player.motionY = player.motionZ = 0;
//            return;
//        }
//
//        player.motionX = moveDir.x;
//        player.motionZ = moveDir.z;
//        player.motionY = bloxdPhysics.getMotionForTick().getY() / 30;
//    }
//
//    private double getBloxdSpeed() {
//        EntityPlayerSP player = MinecraftInstance.mc.thePlayer;
//        if (player == null) return 0;
//
//        if (System.currentTimeMillis() < jumpticks) return 1;
//        if (player.isUsingItem()) return 0.06;
//
//        double finalSpeed = 0.26;
//        if (jumpfunny > 0) finalSpeed += 0.025 * jumpfunny;
//        return finalSpeed;
//    }
//
//    private Vec3d getBloxdMoveVec(float forwardIn, float strafeIn, double speed) {
//        EntityPlayerSP player = MinecraftInstance.mc.thePlayer;
//        if (player == null) return Vec3d.ZERO;
//
//        float forward = forwardIn;
//        float strafe = strafeIn;
//        float yaw = player.rotationYaw;
//
//        float sqrt = MathHelper.sqrt_float(forward * forward + strafe * strafe);
//        if (sqrt < 0.01F) return Vec3d.ZERO;
//
//        if (sqrt > 1.0F) {
//            forward /= sqrt;
//            strafe /= sqrt;
//        }
//
//        double yawRad = Math.toRadians(yaw);
//        double sinYaw = Math.sin(yawRad);
//        double cosYaw = Math.cos(yawRad);
//
//        double x = ((double) strafe * cosYaw - (double) forward * sinYaw) * speed;
//        double z = ((double) forward * cosYaw + (double) strafe * sinYaw) * speed;
//        return new Vec3d(x, 0, z);
//    }
//
//    public ListValue getMode() {
//        return this.mode;
//    }
//
//    // Optimized NoaPhysics class
//    public static final class NoaPhysics {
//        private MutableVec3d impulseVector = new MutableVec3d(0.0d, 0.0, 0.0);
//        private MutableVec3d forceVector = new MutableVec3d(0.0, 0.0, 0.0);
//        private MutableVec3d velocityVector = new MutableVec3d(0.0, 0.0, 0.0);
//        private MutableVec3d gravityVector = new MutableVec3d(0.0, -10.0, 0.0);
//        private double gravityMul = 2.0;
//        private final double mass = 1.0;
//        private final double delta = 1.0 / 30.0;
//
//        public void reset() {
//            impulseVector.set(0.0, 0.0, 0.0);
//            forceVector.set(0.0, 0.0, 0.0);
//            velocityVector.set(0.0, 0.0, 0.0);
//        }
//
//        public MutableVec3d getMotionForTick() {
//            double massDiv = 1.0 / mass;
//            forceVector.mul(massDiv);
//            forceVector.add(gravityVector);
//            forceVector.mul(gravityMul);
//            impulseVector.mul(massDiv);
//            forceVector.mul(delta);
//            impulseVector.add(forceVector);
//            velocityVector.add(impulseVector);
//            forceVector.set(0.0, 0.0, 0.0);
//            impulseVector.set(0.0, 0.0, 0.0);
//            return velocityVector;
//        }
//
//        public MutableVec3d getImpulseVector() {
//            return this.impulseVector;
//        }
//
//        public MutableVec3d getForceVector() {
//            return this.forceVector;
//        }
//
//        public MutableVec3d getVelocityVector() {
//            return this.velocityVector;
//        }
//
//        public MutableVec3d getGravityVector() {
//            return this.gravityVector;
//        }
//
//        public double getGravityMul() {
//            return this.gravityMul;
//        }
//
//        public double getMass() {
//            return this.mass;
//        }
//
//        public double getDelta() {
//            return this.delta;
//        }
//    }
//
//    // MutableVec3d optimizations
//    public static final class MutableVec3d {
//
//        private double x, y, z;
//
//        public MutableVec3d(double x, double y, double z) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//        }
//
//        public static MutableVec3dBuilder builder() {
//            return new MutableVec3dBuilder();
//        }
//
//
//        // Set xyz call with self return
//
//        public MutableVec3d setX(double x) {
//            this.x = x;
//            return this;
//        }
//
//        public MutableVec3d setY(double y) {
//            this.y = y;
//            return this;
//        }
//
//        public MutableVec3d setZ(double z) {
//            this.z = z;
//            return this;
//        }
//
//        public MutableVec3d set(double x, double y, double z) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//            return this;
//        }
//
//        public void add(double x, double y, double z) {
//            this.x += x;
//            this.y += y;
//            this.z += z;
//        }
//
//        public void mul(double factor) {
//            this.x *= factor;
//            this.y *= factor;
//            this.z *= factor;
//        }
//
//        public void add(MutableVec3d v) {
//            this.x += v.x;
//            this.y += v.y;
//            this.z += v.z;
//        }
//
//        public double getX() {
//            return this.x;
//        }
//
//        public double getY() {
//            return this.y;
//        }
//
//        public double getZ() {
//            return this.z;
//        }
//
//        public static class MutableVec3dBuilder {
//            private double x;
//            private double y;
//            private double z;
//
//            MutableVec3dBuilder() {
//            }
//
//            public MutableVec3dBuilder x(double x) {
//                this.x = x;
//                return this;
//            }
//
//            public MutableVec3dBuilder y(double y) {
//                this.y = y;
//                return this;
//            }
//
//            public MutableVec3dBuilder z(double z) {
//                this.z = z;
//                return this;
//            }
//
//            public MutableVec3d build() {
//                return new MutableVec3d(this.x, this.y, this.z);
//            }
//
//            public String toString() {
//                return "MoveFix.MutableVec3d.MutableVec3dBuilder(x=" + this.x + ", y=" + this.y + ", z=" + this.z + ")";
//            }
//        }
//    }
//}