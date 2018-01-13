package shakram02.ahmed.prola

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder

/**
 * Abstract fragment that can scan barcode
 */
abstract class ScanFragment(private val resId: Int) : Fragment() {
    private var unBinder: Unbinder? = null
    protected var scanListener: OnBarcodeScanListener? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(resId, container, false)
        unBinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnBarcodeScanListener) {
            scanListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnBarcodeScanListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        scanListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder!!.unbind()
    }

    abstract fun enableScan()
    abstract fun disableScan()

    interface OnBarcodeScanListener {
        fun onBarcodeScan(barcode: String)
    }
}