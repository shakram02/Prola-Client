package shakram02.ahmed.scanz

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import com.esotericsoftware.kryonet.Client
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import java.net.InetAddress


class MainActivity : Activity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiDialog: AlertDialog

    private val netThread: Thread = Thread({
        val client = Client()
        client.start()
        val hostAddress: InetAddress = client.discoverHost(UDP_PORT, 5000)
        client.connect(5000, hostAddress, TCP_PORT, UDP_PORT)
        runOnUiThread({ Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show() })

    })

    companion object {
        const val TCP_PORT = 60111
        const val UDP_PORT = 51213
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiDialog = createWifiDialog(this, wifiManager)

        ButterKnife.bind(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        if (result.contents != null) {
            Toast.makeText(this, result.contents, Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick(R.id.connect_button)
    fun connectInternet() {
        val integrator = IntentIntegrator(this)
        integrator.addExtra(Intents.Scan.QR_CODE_MODE, true)
        integrator.initiateScan()
//        if (!wifiManager.isWifiEnabled) {
//            showToast(this, "Couldn't connect, please enable WiFi")
//        } else {
//            netThread.start()
//        }
    }

}
