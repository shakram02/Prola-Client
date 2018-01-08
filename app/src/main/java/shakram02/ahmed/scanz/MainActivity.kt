package shakram02.ahmed.scanz

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import fsm.BaseEvent
import fsm.BaseState
import fsm.StateMachine


class MainActivity : FragmentActivity(), ScanFragment.OnBarcodeScanListener {
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
    private lateinit var scanFragment: ScanFragment

    @BindView(R.id.connect_button) lateinit var connectButton: Button
    @BindView(R.id.disconnect_button) lateinit var disconnectButton: Button
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
        scanFragment = supportFragmentManager.findFragmentById(R.id.scan_fragment) as ScanFragment

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


    override fun onBarcodeScan(barcode: String) {
        if (BuildConfig.DEBUG) {
            Log.i("BarcodeScanner", "Scanned $barcode")
        }

        scannedVal = barcode
        scanMachine.acceptEvent(Scan())
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
                    runOnUiThread {
                        disconnectButton.isEnabled = false
                        scanFragment.disableScan()
                        connectButton.isEnabled = true
                    }
                }

                edge(ReqConnect(), PreConnect()) {
                    action {
                        sender.connect()
                        runOnUiThread {
                            // Show spin waiter
                            connectButton.isEnabled = false
                            connectionSpinner.visibility = View.VISIBLE
                        }
                    }
                }
            }
            state(PreConnect()) {

                edge(Error(), Disconnected()) {
                    action {
                        sender.close()
                        runOnUiThread { connectionSpinner.visibility = View.GONE }

                    }
                }
                edge(Connect(), Connected()) {
                    action {
                        runOnUiThread {
                            // Stop spin waiter also
                            disconnectButton.isEnabled = true
                            scanFragment.enableScan()
                            connectButton.isEnabled = false
                            connectionSpinner.visibility = View.GONE
                        }
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
