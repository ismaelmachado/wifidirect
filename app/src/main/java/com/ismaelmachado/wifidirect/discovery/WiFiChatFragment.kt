package com.ismaelmachado.wifidirect.discovery

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 */
class WiFiChatFragment : Fragment() {

    interface MessageTarget {
        val handler: Handler?
    }

    /**
     * ArrayAdapter to manage chat messages.
     */
    inner class ChatMessageAdapter(
        context: Context,
        textViewResourceId: Int,
        items: List<String>
    ) : ArrayAdapter<String?>(context, textViewResourceId, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: run {
                val vi = activity?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                vi.inflate(android.R.layout.simple_list_item_1, null)
            }
            val nameText = v?.findViewById<View>(android.R.id.text1) as TextView
            val message = items[position].takeIf { it.isNotEmpty() } ?: return v
            nameText.text = message
            nameText.setTextAppearance(
                if (message.startsWith("Me: ")) {
                    R.style.normalText
                } else {
                    R.style.boldText
                }
            )
            return v
        }

    }

    private var view: View? = null
    private var chatLine: TextView? = null
    private var listView: ListView? = null
    private var adapter: ChatMessageAdapter? = null
    private val items: List<String> = ArrayList()
    var chatManager: ChatManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatLine = view?.findViewById<View>(R.id.txtChatLine) as TextView
        listView = view?.findViewById<View>(android.R.id.list) as ListView
        adapter = ChatMessageAdapter(
            context ?: return view,
            android.R.id.text1,
            items
        )
        listView?.adapter = adapter
        view?.findViewById<View>(R.id.button1)?.setOnClickListener {
            if (chatManager != null) {
                chatManager?.write(chatLine?.text.toString())
                pushMessage("Me: " + chatLine?.text.toString())
                chatLine?.text = ""
                chatLine?.clearFocus()
            }
        }
        return view
    }

    fun pushMessage(readMessage: String) {
        adapter?.add(readMessage)
        adapter?.notifyDataSetChanged()
    }

}
