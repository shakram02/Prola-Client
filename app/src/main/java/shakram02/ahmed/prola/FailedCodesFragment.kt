package shakram02.ahmed.prola

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val barcodeList = mutableListOf<String>()
    private val failedPacketAdapter = FailedPacketAdapter(barcodeList)
    private lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        fragmentView = v!!

        // Notify prent Activity when a code is pressed
        failedPacketAdapter.onBarcodeClicked += this::onBarcodeClicked

        barcodeRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        barcodeRecyclerView.adapter = failedPacketAdapter

        barcodeRecyclerView.addItemDecoration(DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL))

        return fragmentView
    }

    fun addFailedCode(code: String) {
        if (barcodeList.contains(code)) return

        barcodeList.add(code)

        if (fragmentView.visibility != View.VISIBLE) {
            fragmentView.visibility = View.VISIBLE
        }

        // The newly inserted code goes to the top of the list because it's the most recent
        // item, the barcodeRecyclerView doesn't scroll up by default, so it needs to be told to do so
        failedPacketAdapter.notifyItemInserted(0)
        barcodeRecyclerView.scrollToPosition(0)
    }

    private fun onBarcodeClicked(barcode: String) {
        if (barcodeList.isEmpty()) {
            fragmentView.visibility = View.GONE
        }

        barcodeList.remove(barcode)
        scanListener!!.onBarcodeScan(barcode)
    }
}
