package shakram02.ahmed.prola

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import fsm.BaseEvent
import fsm.BaseState
import fsm.StateMachine


class MainActivity : AppCompatActivity(), ScanFragment.OnBarcodeScanListener {
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager

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
    private lateinit var scanManagerFragment: ScanManagerFragment

    lateinit var connectionButton: MenuItem
    @BindView(R.id.connect_spinner) lateinit var connectionSpinner: ProgressBar

    companion object {
        private const val TCP_PORT = 60111
        private const val UDP_PORT = 51213
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        val mainToolbar = findViewById<View>(R.id.main_toolbar) as Toolbar
        this.setSupportActionBar(mainToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        connectionButton = menu.findItem(R.id.connect_app_menu_button)
        connectionButton.title = getString(R.string.connect_button_text)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.connect_app_menu_button -> {
                if (item.title == getString(R.string.disconnect_button_text)) {
                    disconnect()
                } else {
                    connect()
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
                        connectionButton.title = getString(R.string.connect_button_text)
                        connectionButton.icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_phonelink_ring_black_24dp)
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
                            connectionButton.title = getString(R.string.disconnect_button_text)
                            connectionButton.icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_phonelink_erase_black_24dp)
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
