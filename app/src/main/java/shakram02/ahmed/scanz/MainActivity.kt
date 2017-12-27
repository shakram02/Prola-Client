package shakram02.ahmed.scanz

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import fsm.BaseEvent
import fsm.BaseState
import fsm.StateMachine


class MainActivity : Activity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiDialog: AlertDialog

    private val sender: CodeSender = CodeSender(TCP_PORT, UDP_PORT, 5000)

    @BindView(R.id.connect_button) lateinit var connectButton: Button
    @BindView(R.id.disconnect_button) lateinit var disconnectButton: Button
    @BindView(R.id.scan_button) lateinit var scanButton: Button

    companion object {
        private const val TCP_PORT = 60111
        private const val UDP_PORT = 51213
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiDialog = createWifiDialog(this, wifiManager)

        ButterKnife.bind(this)

        scanMachine.initialize()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        if (result.contents != null) {
            Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
            scannedVal = result.contents
            scanMachine.acceptEvent(Scan())
        }
    }

    @OnClick(R.id.scan_button)
    fun scanCode() {
        val integrator = IntentIntegrator(this)
        integrator.addExtra(Intents.Scan.ONE_D_MODE, true)
        integrator.initiateScan()
    }

    @OnClick(R.id.connect_button)
    fun connectInternet() {
        if (!wifiManager.isWifiEnabled) {
            showToast(this, "Couldn't connect, please enable WiFi")
        } else {
            scanMachine.acceptEvent(Connect())
        }
    }

    @OnClick(R.id.disconnect_button)
    fun disconnect() {
        scanMachine.acceptEvent(Disconnect())

    }

    class Disconnect : BaseEvent()
    class Disconnected : BaseState()
    class Connect : BaseEvent()
    class Connected : BaseState()
    class Scan : BaseEvent()

    private lateinit var scannedVal: String
    private val scanMachine = StateMachine.buildStateMachine(Disconnected()) {
        state(Disconnected()) {
            action {
                disconnectButton.isEnabled = false
                scanButton.isEnabled = false
                connectButton.isEnabled = true
            }
            edge(Connect(), Connected()) {
                action {
                    sender.start()
                }
            }
        }
        state(Connected()) {
            action {
                disconnectButton.isEnabled = true
                scanButton.isEnabled = true
                connectButton.isEnabled = false
            }

            edge(Disconnect(), Disconnected()) {
                action {
                    sender.close()
                }
            }

            edge(Scan(), Connected()) {
                action { sender.send(scannedVal) }
            }
        }
    }
}
