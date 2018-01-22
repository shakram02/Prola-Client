package shakram02.ahmed.prola

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.OnClick
import shakram02.ahmed.prola.utils.FailedPacketAdapter
import java.util.*
import android.support.v7.widget.DividerItemDecoration


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ScanFragment.OnBarcodeScanListener] interface
 * to handle interaction events.
 */
class FailedCodesFragment : ScanFragment(R.layout.fragment_failed_codes) {
    private val codeStack = Stack<String>()
    private val failedPacketAdapter = FailedPacketAdapter(codeStack)
    @BindView(R.id.failed_codes_view) lateinit var view: RecyclerView
    private val rand = Random()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        // Notify prent Activity when a code is pressed
        failedPacketAdapter.onBarcodeClicked +=
                { barcode -> scanListener!!.onBarcodeScan(barcode) }

        view.layoutManager = LinearLayoutManager(this.activity)
        view.adapter = failedPacketAdapter

        view.addItemDecoration(DividerItemDecoration(activity,
                DividerItemDecoration.VERTICAL))
        return v
    }

    @OnClick(R.id.test_btn_add)
    fun testAdd() {
        addFailedCode((rand.nextInt(5000)).toString())
    }

    fun addFailedCode(code: String) {
        codeStack.push(code)

        // The newly inserted code goes to the top of the list because it's the most recent
        // item, the view doesn't scroll up by default, so it needs to be told to do so
        failedPacketAdapter.notifyItemInserted(0)
        view.scrollToPosition(0)
    }
}
