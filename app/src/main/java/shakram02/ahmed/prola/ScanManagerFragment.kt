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

    override fun onStart() {
        super.onStart()
        setChildrenAreEnabled(false, scanManagerLayout)
    }

    override fun onBarcodeScan(barcode: String) {
        scanListener.onBarcodeScan(barcode) // Pass the event to parent
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder!!.unbind()
    }

    fun enableScan() {
        setChildrenAreEnabled(true, scanManagerLayout)
        scanManagerLayout.isEnabled = true
    }

    fun disableScan() {
        setChildrenAreEnabled(false, scanManagerLayout)
        scanManagerLayout.isEnabled = false
    }

    private fun setChildrenAreEnabled(state: Boolean, viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            child.isEnabled = state

            // Do DFS
            if (child is ViewGroup) setChildrenAreEnabled(state, child)
        }
    }
}
