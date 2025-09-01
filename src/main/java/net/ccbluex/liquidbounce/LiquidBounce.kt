/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce

//import cn.a114.idk.AutoMTFdotWIKI
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.ccbluex.liquidbounce.api.loadSettings
import net.ccbluex.liquidbounce.api.messageOfTheDay
import net.ccbluex.liquidbounce.bzym.GlobalFeatures
import net.ccbluex.liquidbounce.cape.CapeService
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.EventManager.callEvent
import net.ccbluex.liquidbounce.event.EventManager.registerListener
import net.ccbluex.liquidbounce.event.StartupEvent
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.command.CommandManager.registerCommands
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.ModuleManager.registerModules
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.settings.Sounds.playStartupSound
import net.ccbluex.liquidbounce.features.module.modules.world.scaffolds.Tower
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.features.special.ClientFixes
import net.ccbluex.liquidbounce.features.special.ClientRichPresence
import net.ccbluex.liquidbounce.features.special.ClientRichPresence.showRPCValue
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.FileManager.loadAllConfigs
import net.ccbluex.liquidbounce.file.FileManager.saveAllConfigs
import net.ccbluex.liquidbounce.lang.LanguageManager.loadLanguages
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.ScriptManager.enableScripts
import net.ccbluex.liquidbounce.script.ScriptManager.loadScripts
import net.ccbluex.liquidbounce.script.remapper.Remapper
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.skid.ClientSoundsPacketHandler
import net.ccbluex.liquidbounce.tabs.BlocksTab
import net.ccbluex.liquidbounce.tabs.ExploitsTab
import net.ccbluex.liquidbounce.tabs.HeadsTab
import net.ccbluex.liquidbounce.ui.client.GuiClientConfiguration.Companion.updateClientWindow
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager.Companion.loadActiveGenerators
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.font.Fonts.loadFonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.ClassUtils.hasForge
import net.ccbluex.liquidbounce.utils.ClientUtils.logger
import net.ccbluex.liquidbounce.utils.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.client.SysUtils
import net.ccbluex.liquidbounce.utils.client.TrayUtils
import net.ccbluex.liquidbounce.utils.extensions.SharedScopes
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister
import net.ccbluex.liquidbounce.utils.timing.TickedActions
import net.ccbluex.liquidbounce.utils.timing.WaitMsUtils
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.spongepowered.asm.mixin.Mixins
import java.util.concurrent.ExecutorService

object LiquidBounce {

    /**
     * Client Information
     *
     * This has all the basic information.
     */
    const val CLIENT_NAME = "GoldBounce"
    const val CLIENT_AUTHOR = "bzym2"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
    const val CLIENT_WEBSITE = "bzym.fun"

    const val MINECRAFT_VERSION = "1.8.9"
    val gf = GlobalFeatures()
    val clientVersionText = "b11"
    val clientBigVersionText = "Prev11"
    val clientVersionNumber = clientVersionText.substring(1).toIntOrNull() ?: 0 // version format: "b<VERSION>" on legacy
    val clientCommit = ""
    val clientBranch = "main"
    val development = true
    var inited = false
    var local: Boolean = false
    var clientSoundsEnabled = true
    var hasPlayedAnim = false
    private lateinit var executor: ExecutorService
    /**
     * Defines if the client is in development mode.
     * This will enable update checking on commit time instead of regular legacy versioning.
     */
    const val IN_DEV = true

    val clientTitle = "$CLIENT_NAME $clientVersionText "

    var isStarting = true

    // Managers
    public val moduleManager = ModuleManager
    val commandManager = CommandManager
    val eventManager = EventManager
    val fileManager = FileManager
    val scriptManager = ScriptManager

    // HUD & ClickGUI
    val hud = HUD

    val clickGui = ClickGui

    // Menu Background
    var background: Background? = null

    // skibidi dumb dumb  yes yes
    var playTimeStart: Long = 0
    // Discord RPC
    val clientRichPresence = ClientRichPresence
    fun cpFiles(){
        SysUtils().copyToFontDir("Product Sans Regular.ttf")
        SysUtils().copyToFontDir("Product Sans Bold.ttf")
        SysUtils().copyToFontDir("iconnovo.ttf")
        SysUtils().copyToFontDir("NotoSansSC-Regular.ttf")
        SysUtils().copyToFontDir("NotoSansSC-Bold.ttf")
        SysUtils().copyToFontDir("HelveticaMedium.ttf")
        SysUtils().copyToFontDir("SF-UI-Display-Medium.ttf")
        SysUtils().copyToGameDir("background.png","background.png")
        SysUtils().copyToGameDir("logo_large.png", "logo_large.png")

    }
    fun isInDev(): String {
        if(development){
            return "Development"
        }else{
            return "Release"
        }
    }
    /**
     * Execute if client will be started
     */
    fun startClient() {

        isStarting = true
        logger.info("Starting $CLIENT_NAME $clientVersionText $clientCommit, by $CLIENT_AUTHOR")

        try {
            TrayUtils().start()
            // Load languages
            loadLanguages()
            // Register listeners
            registerListener(RotationUtils)
            registerListener(ClientFixes)
            registerListener(BungeeCordSpoof)
            registerListener(CapeService)
            registerListener(InventoryUtils)
            registerListener(MiniMapRegister)
            registerListener(TickedActions)
            registerListener(MovementUtils)
            registerListener(PacketUtils)
            registerListener(TimerBalanceUtils)
            registerListener(BPSUtils)
            registerListener(Tower)
            registerListener(WaitTickUtils)
            registerListener(SilentHotbar)
            registerListener(WaitMsUtils)
            registerListener(BPSUtils)
            registerListener(KillAura.CombatListener)
            KillAura.CombatListener.handleEvents()
            // Waiting for copy files
            runBlocking {
                cpFiles()
            }
            // Load client fonts
            runBlocking {
                loadFonts()
            }
            // Load settings
            loadSettings(false) {
                logger.info("Successfully loaded ${it.size} settings.")
            }

            // Register commands
            registerCommands()
            // Setup module manager and register modules
            runBlocking {
                registerModules()
            }
            runCatching {
                // Remapper
                loadSrg()

                if (!Remapper.mappingsLoaded) {
                    error("Failed to load SRG mappings.")
                }

                // ScriptManager
                loadScripts()
                enableScripts()
            }.onFailure {
                logger.error("Failed to load scripts.", it)
            }

            // Load configs
            loadAllConfigs()
            // Update client window
            updateClientWindow()

            // Tabs (Only for Forge!)
            if (hasForge()) {
                BlocksTab()
                ExploitsTab()
                HeadsTab()
            }
            playTimeStart = System.currentTimeMillis()
            // Disable optifine fastrender
            disableFastRender()

            // Load alt generators
            loadActiveGenerators()

            // Load message of the day
            messageOfTheDay?.message?.let { logger.info("Message of the day: $it") }

            // Setup Discord RPC
            if (showRPCValue) {
                SharedScopes.IO.launch {
                    try {
                        clientRichPresence.setup()
                    } catch (throwable: Throwable) {
                        logger.error("Failed to setup Discord RPC.", throwable)
                    }
                }
            }

            // Login into known token if not empty
            if (CapeService.knownToken.isNotBlank()) {
                runCatching {
                    CapeService.login(CapeService.knownToken)
                }.onFailure {
                    logger.error("Failed to login into known cape token.", it)
                }.onSuccess {
                    logger.info("Successfully logged in into known cape token.")
                }
            }
            // Refresh cape service
            CapeService.refreshCapeCarriers {
                logger.info("Successfully loaded ${CapeService.capeCarriers.size} cape carriers.")
            }
            // Load background
            FileManager.loadBackground()
            playStartupSound()
        } catch (e: Exception) {
            logger.error("Failed to start client ${e.message}")
        } finally {
            // Set is starting status
            isStarting = false

            callEvent(StartupEvent())
            logger.info("Successfully started client")
//              这玩意检测到Arch Linux用户
//              就自动打开https://mtf.wiki/en
//              太傻逼了我操
//            AutoMTFdotWIKI.init();
        }
    }
    @SubscribeEvent
    fun ServerJoinEvent(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        // Check if BPS is enabled
        local = event.isLocal
        if (clientSoundsEnabled) {
            // You don't need this on local server
            if (!local) {
                ClientSoundsPacketHandler.blocks.clear()
                // Create a netty pipeline handler
                val handler = ClientSoundsPacketHandler()
                // Register the handler before the Minecraft handler so that some packets can be ignored
                event.manager.channel().pipeline().addBefore("packet_handler", "asdfInHandler", handler)
                // Register the handler after the Minecraft handler to play sound
                event.manager.channel().pipeline().addAfter("packet_handler", "asdfOutHandler", handler)
                inited = true
            } else {
                executor.execute {
                    while (Minecraft.getMinecraft().thePlayer == null) {
                        try {
                            Thread.sleep(500)
                        } catch (e: InterruptedException) {
                        }
                    }
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(
                        ChatComponentText("BPS is automatically disabled on local servers.")
                    )
                }
            }
        } else {
            executor.execute {
                while (Minecraft.getMinecraft().thePlayer == null) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                    }
                }
                Minecraft.getMinecraft().thePlayer.addChatComponentMessage(
                    ChatComponentText("BPS was toggled off, do \"/bps toggle\" to toggle it back on.")
                )
            }
        }
    }

    // Triggered when you leave a server to remove the packet handler
    @SubscribeEvent
    fun ServerQuitEvent(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        if (inited) {
            inited = false
            val channel = event.manager.channel()
            channel.eventLoop().submit {
                channel.pipeline().remove("asdfInHandler")
                channel.pipeline().remove("asdfOutHandler")
                null
            }
        }
    }
    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        callEvent(ClientShutdownEvent())

        // Stop all CoroutineScopes
        SharedScopes.stop()

        // Save all available configs
        saveAllConfigs()

        // Shutdown discord rpc
        clientRichPresence.shutdown()
    }

}
