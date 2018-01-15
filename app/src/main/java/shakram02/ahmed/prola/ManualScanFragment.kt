package shakram02.ahmed.prola

import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
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
    @BindView(R.id.manual_scan_frag_button) lateinit var sendButton: Button

    override fun onResume() {
        super.onResume()

        barcodeText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // TODO check length
                sendButton.isEnabled = s!!.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    @OnClick(R.id.manual_scan_frag_button)
    fun onSendBarcodeButtonClick() {
        scanListener!!.onBarcodeScan(barcodeText.text.toString())
        barcodeText.text.clear()
    }
}
