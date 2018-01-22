package shakram02.ahmed.prola

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import shakram02.ahmed.prola.utils.FailedPacketAdapter


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnBarcodeScanListener] interface
 * to handle interaction events.
 */
class FailedCodesFragment : ScanFragment(R.layout.fragment_failed_codes) {
    @BindView(R.id.failed_codes_view) lateinit var barcodeRecyclerView: RecyclerView

    private val failedPacketAdapter = FailedPacketAdapter()
    private lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        // Get fragment container from parent activity
        fragmentView = activity.findViewById(R.id.error_codes_fragment_container)

        // Notify prent Activity when a code is pressed
        failedPacketAdapter.onBarcodeClicked += this::onBarcodeClicked

        barcodeRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        barcodeRecyclerView.adapter = failedPacketAdapter

        barcodeRecyclerView.addItemDecoration(DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL))

        return v
    }

    fun addFailedCode(code: String) {
        if (!failedPacketAdapter.addUniqueBarcode(code)) return

        if (fragmentView.visibility != View.VISIBLE) {
            fragmentView.visibility = View.VISIBLE
        }

        barcodeRecyclerView.scrollToPosition(0)
    }

    private fun onBarcodeClicked(barcode: String) {
        if (failedPacketAdapter.itemCount == 0) {
            fragmentView.visibility = View.GONE
        }

        scanListener!!.onBarcodeScan(barcode)
    }
}
