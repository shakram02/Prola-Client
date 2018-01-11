package shakram02.ahmed.prola

import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.widget.Toast

/**
 * Instantiates a Wifi control dialog
 */
internal fun createWifiDialog(context: Context, wifiManager: WifiManager): AlertDialog {

//    val integrator = IntentIntegrator(this)
//    integrator.addExtra(Intents.Scan.QR_CODE_MODE, true)
//    integrator.initiateScan()

    val alertDialogBuilder = AlertDialog.Builder(context)

    // set title
    alertDialogBuilder.setTitle("Wifi Settings")

    // set dialog message
    alertDialogBuilder
            .setMessage("Do you want to enable WIFI ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ -> wifiManager.isWifiEnabled = true }
            .setNegativeButton("No") { _, _ -> wifiManager.isWifiEnabled = false }

    // create alert dialog
    return alertDialogBuilder.create()
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}
