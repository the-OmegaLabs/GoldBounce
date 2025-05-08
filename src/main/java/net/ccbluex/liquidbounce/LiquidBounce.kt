/*
 * GoldBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/bzym2/GoldBounce/
 */
package net.ccbluex.liquidbounce

import de.florianmichael.viamcp.ViaMCP
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.api.loadSettings
import net.ccbluex.liquidbounce.api.messageOfTheDay
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
import net.ccbluex.liquidbounce.features.module.modules.player.GApple
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
import net.ccbluex.liquidbounce.ui.sound.TipSoundManager
import net.ccbluex.liquidbounce.ui.sound.TipSoundPlayer
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.ClassUtils.hasForge
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.ClientUtils.disableFastRender
import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.client.SysUtils
import net.ccbluex.liquidbounce.utils.client.TrayUtils
import net.ccbluex.liquidbounce.utils.extensions.SharedScopes
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.utils.timing.TickedActions
import net.ccbluex.liquidbounce.utils.timing.WaitMsUtils
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import op.wawa.opacketfix.features.hytpacket.PacketManager
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
    const val CLIENT_WEBSITE = "pornhub.com"

    const val MINECRAFT_VERSION = "1.8.9"
    val clientVersionText = "b10"
    val clientVersionNumber = clientVersionText.substring(1).toIntOrNull() ?: 0 // version format: "b<VERSION>" on legacy
    val clientCommit = ""
    val clientBranch = "main"
    var inited = false
    var local: Boolean = false
    var clientSoundsEnabled = true
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
    lateinit var tipSoundManager: TipSoundManager

    // HUD & ClickGUI
    val hud = HUD

    val clickGui = ClickGui

    // Menu Background
    var background: Background? = null

    // Discord RPC
    val clientRichPresence = ClientRichPresence
    fun initializeShadowUtils() {
        try {
            val scaledResolution = ScaledResolution(mc)
            ShadowUtils.initShaderIfRequired(scaledResolution, 5.0f) // 初始化，强度可以调整
        } catch (e: Exception) {
            println("Error initializing ShadowUtils: ${e.message}")
            e.printStackTrace()
        }
    }
    /**
     * Execute if client will be started
     */
    fun startClient() {
        PacketManager().init()
        isStarting = true
        tipSoundManager = TipSoundManager()
        LOGGER.info("Starting $CLIENT_NAME $clientVersionText $clientCommit, by $CLIENT_AUTHOR")

        try {
            TrayUtils().start()
            // Load languages
            loadLanguages()
            ViaMCP.create()
            ViaMCP.INSTANCE.initAsyncSlider() // For top left aligned slider
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
            registerListener(GApple)
            SysUtils().copyToFontDir("Product Sans Regular.ttf")
            SysUtils().copyToFontDir("Product Sans Bold.ttf")
            SysUtils().copyToFontDir("HONORSansCN-Regular.ttf")
            SysUtils().copyToFontDir("iconnovo.ttf")
            SysUtils().copyToFontDir("NotoSansSC-Regular.ttf")
            SysUtils().copyToFontDir("NotoSansSC-Bold.ttf")
            SysUtils().copyToGameDir("background.png","background.png")
            SysUtils().copyToGameDir("logo_large.png", "logo_large.png")
            // Load client fonts
            loadFonts()

            // Load settings
            loadSettings(false) {
                LOGGER.info("Successfully loaded ${it.size} settings.")
            }

            // Register commands
            registerCommands()
            // Setup module manager and register modules
            registerModules()
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
                LOGGER.error("Failed to load scripts.", it)
            }

            // Load configs
            loadAllConfigs()
            initializeShadowUtils()
            // Update client window
            updateClientWindow()

            // Tabs (Only for Forge!)
            if (hasForge()) {
                BlocksTab()
                ExploitsTab()
                HeadsTab()
            }

            // Disable optifine fastrender
            disableFastRender()

            // Load alt generators
            loadActiveGenerators()

            // Load message of the day
            messageOfTheDay?.message?.let { LOGGER.info("Message of the day: $it") }

            // Setup Discord RPC
            if (showRPCValue) {
                SharedScopes.IO.launch {
                    try {
                        clientRichPresence.setup()
                    } catch (throwable: Throwable) {
                        LOGGER.error("Failed to setup Discord RPC.", throwable)
                    }
                }
            }

            // Login into known token if not empty
            if (CapeService.knownToken.isNotBlank()) {
                runCatching {
                    CapeService.login(CapeService.knownToken)
                }.onFailure {
                    LOGGER.error("Failed to login into known cape token.", it)
                }.onSuccess {
                    LOGGER.info("Successfully logged in into known cape token.")
                }
            }

            // Refresh cape service
            CapeService.refreshCapeCarriers {
                LOGGER.info("Successfully loaded ${CapeService.capeCarriers.size} cape carriers.")
            }

            // Load background
            FileManager.loadBackground()
        } catch (e: Exception) {
            LOGGER.error("Failed to start client ${e.message}")
        } finally {
            // Set is starting status
            isStarting = false

            callEvent(StartupEvent())
            LOGGER.info("Successfully started client")
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
