package shakram02.ahmed.prola.utils

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.junit.experimental.results.FailureList
import shakram02.ahmed.prola.R


/**
 * Displays a list of barcodes in reversed order
 */
class FailedPacketAdapter : RecyclerView.Adapter<FailedPacketAdapter.CodeHolder>() {
    private val barcodeList = mutableListOf<String>()
    val onBarcodeClicked = Event<String>()

    class CodeHolder(itemView: View, private val adapter: FailedPacketAdapter) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            val tvBarcode = v.findViewById(R.id.barcode_text_view) as TextView
            val barcode = tvBarcode.text.toString()
            adapter.removeBarcode(barcode)
            adapter.onBarcodeClicked.invoke(barcode)
        }
    }

    override fun onBindViewHolder(holder: CodeHolder?, position: Int) {
        if (holder == null) return
        if (position >= barcodeList.count()) throw IndexOutOfBoundsException("Trying to access item at $position")

        val tvBarcode = holder.itemView.findViewById(R.id.barcode_text_view) as TextView

        // Because we're displaying an inverted list
        val itemIndex: Int = (barcodeList.size - 1) - position
        tvBarcode.text = barcodeList[itemIndex]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.barcode_view, parent, false) as ViewGroup

        val codeHolder = CodeHolder(v, this)
        v.setOnClickListener(codeHolder)

        return codeHolder
    }

    override fun getItemCount(): Int {
        return barcodeList.count()
    }

    fun addUniqueBarcode(barcode: String): Boolean {
        if (barcodeList.contains(barcode)) return false
        barcodeList.add(barcode)

        // The newly inserted code goes to the top of the list because it's the most recent
        // item, the barcodeRecyclerView doesn't scroll up by default, so it needs to be told to do so
        this.notifyItemInserted(0)

        return true
    }

    private fun removeBarcode(barcode: String) {
        val index = barcodeList.indexOf(barcode)

        // Remove items using the reversed index, same as the way they were added
        val itemIndex: Int = (barcodeList.size - 1) - index
        notifyItemRemoved(itemIndex)

        // Remove the item at the end so the calculations above are correct
        barcodeList.remove(barcode)
    }
}
