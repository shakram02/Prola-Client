package shakram02.ahmed.prola.utils

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import shakram02.ahmed.prola.BuildConfig
import shakram02.ahmed.prola.R
import java.util.*


/**
 * Displays items in a given list in reversed order
 */
class FailedPacketAdapter(private val items: List<String>) : RecyclerView.Adapter<FailedPacketAdapter.CodeHolder>() {
    val onBarcodeClicked = Event<String>()

    class CodeHolder(itemView: View, private val adapter: FailedPacketAdapter) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            val tvBarcode = v.findViewById(R.id.barcode_text_view) as TextView

            if (BuildConfig.DEBUG) {
                Toast.makeText(v.context, "Clicked! ${tvBarcode.text}, NOT SENDING DATA", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: remove the if statement when done
                adapter.onBarcodeClicked.invoke(tvBarcode.text.toString())
            }
        }
    }

    override fun onBindViewHolder(holder: CodeHolder?, position: Int) {
        if (holder == null) return
        if (position >= items.count()) throw IndexOutOfBoundsException("Trying to access item at $position")

        val tvBarcode = holder.itemView.findViewById(R.id.barcode_text_view) as TextView
        tvBarcode.text = items[(items.size - 1) - position] // Because we're displaying an inverted list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.barcode_view, parent, false) as ViewGroup

        // ViewHolder handles click events, not its content (textView)
        val codeHolder = CodeHolder(v, this)
        v.setOnClickListener(codeHolder)

        return codeHolder
    }

    override fun getItemCount(): Int {
        return items.count()
    }

}
