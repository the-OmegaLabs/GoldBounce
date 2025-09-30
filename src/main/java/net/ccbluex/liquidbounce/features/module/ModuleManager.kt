/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.event.EventManager.registerListener
import net.ccbluex.liquidbounce.event.EventManager.unregisterListener
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.features.command.CommandManager.registerCommand
import net.ccbluex.liquidbounce.features.module.modules.combat.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.*
import net.ccbluex.liquidbounce.features.module.modules.`fun`.AuraZoom
import net.ccbluex.liquidbounce.features.module.modules.`fun`.Derp
import net.ccbluex.liquidbounce.features.module.modules.`fun`.SkinDerp
import net.ccbluex.liquidbounce.features.module.modules.hud.*
import net.ccbluex.liquidbounce.features.module.modules.misc.*
import net.ccbluex.liquidbounce.features.module.modules.movement.*
import net.ccbluex.liquidbounce.features.module.modules.player.*
import net.ccbluex.liquidbounce.features.module.modules.render.*
import net.ccbluex.liquidbounce.features.module.modules.scriptbased.BetterRotation
import net.ccbluex.liquidbounce.features.module.modules.scriptbased.TargetHUD
import net.ccbluex.liquidbounce.features.module.modules.settings.BPSCalculation
import net.ccbluex.liquidbounce.features.module.modules.settings.Camera
import net.ccbluex.liquidbounce.features.module.modules.settings.CustomTag
import net.ccbluex.liquidbounce.features.module.modules.settings.Debugger
import net.ccbluex.liquidbounce.features.module.modules.settings.Interface
import net.ccbluex.liquidbounce.features.module.modules.settings.Sounds
import net.ccbluex.liquidbounce.features.module.modules.world.*
import net.ccbluex.liquidbounce.features.module.modules.world.Timer
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.ClientUtils.logger
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import java.util.*

object ModuleManager : Listenable {

    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()

    init {
        registerListener(this)
    }
    fun getModuleByCategory(category: Category): List<Module> {
        return modules.filter { it.category == category }
    }
    /**
     * Register all modules
     */
    fun registerModules() {
        logger.info("[ModuleManager] Loading modules...")

        // Register modules
        registerModules(
            AbortBreaking,
            Aimbot,
            AirJump,
            AirLadder,
            Ambience,
            Animations,
            AntiAFK,
            AntiBlind,
            AntiBot,
            AntiBounce,
            AntiCactus,
            AntiExploit,
            AntiHunger,
            AntiFireball,
            AntiVanish,
            AntiVoid,
            AtAllProvider,
            AttackEffects,
            AutoAccount,
            AutoArmor,
            AutoBow,
            AutoBreak,
            AutoClicker,
            AutoDisable,
            AutoFish,
            AutoProjectile,
            AutoPlay,
            AutoLeave,
            AutoPot,
            AutoRespawn,
            AutoRod,
            AutoSoup,
            AutoTool,
            AutoWalk,
            AutoWeapon,
            AvoidHazards,
            Backtrack,
            BedDefender,
            BedGodMode,
            BedPlates,
            BedProtectionESP,
            Blink,
            BlockESP,
            BlockOverlay,
            PointerESP,
            ProjectileAimbot,
            Breadcrumbs,
            BufferSpeed,
            CameraClip,
            CameraView,
            Chams,
            ChestAura,
            ChestStealer,
            CivBreak,
            ClickGUI,
            Clip,
            ComponentOnHover,
            ConsoleSpammer,
            Criticals,
            Damage,
            Derp,
            ESP,
            Eagle,
            FakeLag,
            FastBow,
            FastBreak,
            FastClimb,
            FastPlace,
            FastStairs,
            FastUse,
            FlagCheck,
            Fly,
            ForceUnicodeChat,
            FreeCam,
            Freeze,
            Fucker,
            Fullbright,
            GameDetector,
            Ghost,
            GhostHand,
            GodMode,
            HUD,
            HighJump,
            HitBox,
            IceSpeed,
            Ignite,
            InventoryCleaner,
            InventoryMove,
            ItemESP,
            ItemPhysics,
            ItemTeleport,
            KeepAlive,
            KeepContainer,
            KeepTabList,
            KeyPearl,
            Kick,
            KillAura,
            LadderJump,
            LiquidWalk,
            Liquids,
            LongJump,
            MidClick,
            MoreCarry,
            MultiActions,
            NameTags,
            Camera,
            NoClip,
            NoFOV,
            NoFall,
            NoFluid,
            NoFriends,
            NoHurtCam,
            NoJumpDelay,
            NoPitchLimit,
            NoRotateSet,
            NoSlotSet,
            NoSlow,
            NoSlowBreak,
            NoSwing,
            Notifier,
            NoWeb,
            Nuker,
            PacketDebugger,
            Parkour,
            PerfectHorseJump,
            Phase,
            PingSpoof,
            Plugins,
            PortalMenu,
            PotionSaver,
            PotionSpoof,
            Projectiles,
            ProphuntESP,
            Reach,
            Refill,
            Regen,
            ResourcePackSpoof,
            ReverseStep,
            Rotations,
            SafeWalk,
            Scaffold,
            ServerCrasher,
            SkinDerp,
            SlimeJump,
            Sneak,
            Spammer,
            Speed,
            Sprint,
            Step,
            StorageESP,
            Strafe,
            SuperKnockback,
            Teleport,
            TeleportHit,
            TNTBlock,
            TNTESP,
            TNTTimer,
            Teams,
            TimerRange,
            Timer,
            Tracers,
            TrueSight,
            VehicleOneHit,
            Velocity,
            WallClimb,
            WaterSpeed,
            XRay,
            Zoot,
            KeepSprint,
            Disabler,
            OverrideRaycast,
            TickBase,
            RotationRecorder,
            ForwardTrack,
            FreeLook,
            SilentHotbarModule,
            ClickRecorder,
            NLCrosshair,
            GORageBot,
            Hat,
            NoBooks,
            TNTTrails,
            NoRender,
            GORageTriggerBot,
            NoSpreadShoot,
            FastFall,
            AntiStaff,
            Clutch,
            NameProtect,
            AntiVoid,
            Gapple,
            FollowTargetHud,
            HitBubbles,
            FireFlies,
            ItemsDetector,
            Zoom,
            RawInput,
            MoreKB,
            HoldRight,
            LegitScaffold,
            BetterRotation,
            ACDetect,
            TargetHUD,
            RotationPathESP,
            TargetStrafe,
            Debugger,
            BPSCalculation,
            Insult(),
            CustomTag,
            WaterMark,
            Sounds,
            HalfLifeWarn,
            AuraZoom,
            MoveFix,
            MoreBends,
            NoC03,
            Arraylist,
            MotionBlur,
            NewGUI,
            Interface,
            Logo,
            PotionEffect,
            AutoL,
            AutoSave,
            Halo,
            ScaffoldHelper,
            VelocityBalancer
        )

        InventoryManager.startCoroutine()

        logger.info("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module

        generateCommand(module)
        registerListener(module)
    }

    /**
     * Register [moduleClass] with new instance
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: Throwable) {
            logger.error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Class<out Module>) = modules.forEach(this::registerModule)


    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Module) = modules.forEach(this::registerModule)

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        registerCommand(ModuleCommand(module, values))
    }

    /**
     * Get module by [moduleClass]
     */
    fun getModule(moduleClass: Class<*>) = moduleClassMap[moduleClass]!!

    operator fun get(clazz: Class<*>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    operator fun get(name: String) = getModule(name)

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.forEach { if (it.keyBind == event.key) it.toggle() }

    
}
