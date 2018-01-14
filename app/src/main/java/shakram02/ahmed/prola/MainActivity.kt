package shakram02.ahmed.prola

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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


class MainActivity : AppCompatActivity(), ScanFragment.OnBarcodeScanListener {
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiDialog: AlertDialog

    private val sender: CodeSender = CodeSender(TCP_PORT, UDP_PORT, 5000)
    private val connected: Boolean = false

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
    private lateinit var scanManagerFragment: ScanManagerFragment

    @BindView(R.id.connection_button) lateinit var connectionButton: Button
    @BindView(R.id.connect_spinner) lateinit var connectionSpinner: ProgressBar

    companion object {
        private const val TCP_PORT = 60111
        private const val UDP_PORT = 51213
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainToolbar = findViewById<View>(R.id.main_toolbar) as Toolbar
        this.setSupportActionBar(mainToolbar)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiDialog = createWifiDialog(this, wifiManager)

        ButterKnife.bind(this)
        scanManagerFragment = supportFragmentManager.findFragmentById(R.id.scan_fragment) as ScanManagerFragment

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.connect_app_menu_button -> {
                if (item.title == getString(R.string.disconnect_button_text)) {
                    connect()
                } else {
                    disconnect()
                }
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onBarcodeScan(barcode: String) {
        if (BuildConfig.DEBUG) {
            Log.i("BarcodeScanner", "Scanned $barcode")
        }

        scannedVal = barcode
        scanMachine.acceptEvent(Scan())
    }

    @OnClick(R.id.connection_button)
    fun onConnectionButtonClicked() {
        if (connectionButton.text == getString(R.string.disconnect_button_text)) {
            disconnect()
        } else {
            connect()
        }
    }

    private fun connect() {
        val wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (!wifiManager.isWifiEnabled || wifiInfo == null || !wifiInfo.isConnected) {
            showToast(this, "Make sure you're connected to a WiFi local network")
        } else {
            scanMachine.acceptEvent(ReqConnect())
        }
    }

    private fun disconnect() {
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
                        connectionButton.text = getString(R.string.connect_button_text)
                        connectionButton.isEnabled = true
                        scanManagerFragment.disableScan()
                    }
                }

                edge(ReqConnect(), PreConnect()) {
                    action {
                        sender.connect()
                        runOnUiThread {
                            // Show spin waiter, and disable connection button
                            connectionButton.isEnabled = false
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
                            scanManagerFragment.enableScan()
                            connectionButton.text = getString(R.string.disconnect_button_text)
                            connectionButton.isEnabled = true
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
