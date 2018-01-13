package shakram02.ahmed.prola

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.google.zxing.client.android.Intents


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnBarcodeScanListener] interface
 * to handle interaction events.
 */
class CameraScanFragment : ScanFragment(R.layout.fragment_camera_scan) {


    @BindView(R.id.scan_frag_button) lateinit var scanButton: Button

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Scan cancelled
        if (data == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        val result = FragmentIntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        if (result.contents != null) {
            scanListener!!.onBarcodeScan(result.contents)
        }
    }

    @OnClick(R.id.scan_frag_button)
    fun onScanButtonClick() {
        val integrator = FragmentIntentIntegrator(this)
        integrator.captureActivity = BarcodeScanActivity::class.java
        integrator.addExtra(Intents.Scan.ONE_D_MODE, true)
        integrator.initiateScan()
    }

    override fun enableScan() {
        scanButton.isEnabled = true
    }

    override fun disableScan() {
        scanButton.isEnabled = false
    }
}
