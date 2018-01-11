package shakram02.ahmed.prola

import android.support.v4.app.Fragment
import android.widget.Button
import butterknife.BindView


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnBarcodeScanListener] interface
 * to handle interaction events.
 */
class ManualEntryFragment : ScanFragment(R.layout.fragment_manual_entry) {
    @BindView(R.id.manual_scan_frag_button) lateinit var scanButton: Button

    override fun enableScan() {
        scanButton.isEnabled = true
    }

    override fun disableScan() {
        scanButton.isEnabled = false
    }


}
