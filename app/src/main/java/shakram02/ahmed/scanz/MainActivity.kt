package shakram02.ahmed.scanz

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
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

    class Disconnected : BaseState()
    class Connected : BaseState()
    class PreConnect : BaseState()


    class Disconnect : BaseEvent()
    class Error : BaseEvent()
    class ReqConnect : BaseEvent()
    class Connect : BaseEvent()
    class Scan : BaseEvent()

    private lateinit var scannedVal: String
    private lateinit var scanMachine: StateMachine

    @BindView(R.id.connect_button) lateinit var connectButton: Button
    @BindView(R.id.disconnect_button) lateinit var disconnectButton: Button
    @BindView(R.id.scan_button) lateinit var scanButton: Button
    @BindView(R.id.connect_spinner) lateinit var connectionSpinner: ProgressBar

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


        scanMachine = buildScanMachine()
        sender.onConnected += { scanMachine.acceptEvent(Connect()) }
        sender.onSent += { v -> makeToast("Sent " + v) }
        sender.onError += { e ->
            // Whenever an error occurs, change state
            scanMachine.acceptEvent(Error())
            makeToast(e)
        }
        scanMachine.initialize()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return    // Scan cancelled

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        if (result.contents != null) {
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
            scanMachine.acceptEvent(ReqConnect())
        }
    }

    @OnClick(R.id.disconnect_button)
    fun disconnect() {
        scanMachine.acceptEvent(Disconnect())
    }

    private fun makeToast(s: String) {
        runOnUiThread { Toast.makeText(this, s, Toast.LENGTH_SHORT).show() }
    }

    private fun buildScanMachine(): StateMachine {
        return StateMachine.buildStateMachine(Disconnected()) {
            state(Disconnected()) {
                action {
                    disconnectButton.isEnabled = false
                    scanButton.isEnabled = false
                    connectButton.isEnabled = true
                }

                edge(ReqConnect(), PreConnect()) {
                    action {
                        sender.connect()
                        // Show spin waiter
                        connectButton.isEnabled = false
                        connectionSpinner.visibility = View.VISIBLE
                    }
                }
            }
            state(PreConnect()) {

                edge(Error(), Disconnected()) {
                    action {
                        sender.close()
                        connectionSpinner.visibility = View.GONE
                    }
                }
                edge(Connect(), Connected()) {
                    action {
                        // Stop spin waiter also
                        disconnectButton.isEnabled = true
                        scanButton.isEnabled = true
                        connectButton.isEnabled = false
                        connectionSpinner.visibility = View.GONE
                    }
                }

            }
            state(Connected()) {
                edge(Disconnect(), Disconnected()) { action { sender.close() } }
                edge(Error(), Disconnected()) { action { sender.close() } }
                edge(Scan(), Connected()) { action { sender.send(scannedVal) } }
            }
        }
    }
}
