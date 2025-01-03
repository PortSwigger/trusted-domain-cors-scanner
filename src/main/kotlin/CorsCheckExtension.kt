import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu

class CorsCheckExtension : BurpExtension {
    companion object {
        var unloaded = false
        var scanCheckEnabled = true
    }

    override fun initialize(api: MontoyaApi?) {
        if (api == null) {
            return
        }

        val name = "Trusted Domain CORS Scanner"
        api.extension().setName(name)
        api.logging().logToOutput("Loaded $name")

        //Register top-level menu bar
        val topMenu = JMenu(name)

        val disableScanCheck = JCheckBoxMenuItem("Enable CORS active scan check", scanCheckEnabled)

        disableScanCheck.addActionListener {
            scanCheckEnabled = !scanCheckEnabled

            if (!scanCheckEnabled) {
                api.logging().logToOutput("Disabled scan check.")
            } else {
                api.logging().logToOutput("Enabled scan check.")
            }
        }

        topMenu.add(disableScanCheck)


        api.userInterface().menuBar().registerMenu(topMenu)

        //Create thread pool
        val threadPool = ThreadPoolExecutor(4, 4, 60L, TimeUnit.SECONDS, LinkedBlockingQueue())

        //Scan checks
        api.scanner().registerScanCheck(CorsScannerCheck(api, threadPool))

        //Register context menu interface
        api.userInterface().registerContextMenuItemsProvider(CustomContextMenuItemsProvider(api, threadPool))

        //Register unloading handler
        api.extension().registerUnloadingHandler {
            unloaded = true
            threadPool.shutdown()

            api.logging().logToOutput("Unloading Extension...")
        }
    }
}