package shakram02.ahmed.scanz

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * Scanning activity for 1D bar codes that uses a custom view
 */
class BarcodeScanActivity : Activity(), DecoratedBarcodeView.TorchListener {
    private var capture: CaptureManager? = null

    @BindView(R.id.zxing_barcode_scanner) lateinit var barcodeScannerView: DecoratedBarcodeView
    @BindView(R.id.switch_flashlight) lateinit var switchFlashlightButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)
        ButterKnife.bind(this)

        barcodeScannerView.setTorchListener(this)


        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            switchFlashlightButton.visibility = View.GONE
        }

        capture = CaptureManager(this, barcodeScannerView)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun switchFlashlight(view: View) {
        if (getString(R.string.turn_on_flashlight) == switchFlashlightButton.text) {
            barcodeScannerView.setTorchOn()
        } else {
            barcodeScannerView.setTorchOff()
        }
    }


    override fun onTorchOn() {
        switchFlashlightButton.setText(R.string.turn_off_flashlight)
    }


    override fun onTorchOff() {
        switchFlashlightButton.setText(R.string.turn_on_flashlight)
    }

}