package shakram02.ahmed.prola

import android.support.v4.app.Fragment
import android.widget.EditText
import butterknife.BindView
import butterknife.OnClick


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnBarcodeScanListener] interface
 * to handle interaction events.
 */
class ManualScanFragment : ScanFragment(R.layout.fragment_manual_entry) {
    @BindView(R.id.barcode_text) lateinit var barcodeText: EditText

    @OnClick(R.id.manual_scan_frag_button)
    fun onSendBarcodeButtonClick() {
        scanListener!!.onBarcodeScan(barcodeText.text.toString())
        barcodeText.text.clear()
    }
}
