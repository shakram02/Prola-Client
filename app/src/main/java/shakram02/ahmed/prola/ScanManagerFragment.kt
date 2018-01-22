package shakram02.ahmed.prola

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 */
class ScanManagerFragment : Fragment(), ScanFragment.OnBarcodeScanListener {
    private lateinit var scanListener: ScanFragment.OnBarcodeScanListener
    private var unBinder: Unbinder? = null

    @BindView(R.id.scan_manager) lateinit var scanManagerLayout: ViewGroup

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_scan_manager, container, false)
        unBinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ScanFragment.OnBarcodeScanListener) {
            scanListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnBarcodeScanListener")
        }
    }

    override fun onBarcodeScan(barcode: String) {
        scanListener.onBarcodeScan(barcode) // Pass the event to parent
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder!!.unbind()
    }

    fun enableScan() {
        setVisible(true)
    }

    fun disableScan() {
        setVisible(false)
    }

    private fun setVisible(state: Boolean) {
        // View.GONE -> Don't take any space
        val visibility = if (state) View.VISIBLE else View.GONE
        scanManagerLayout.visibility = visibility
    }
}
