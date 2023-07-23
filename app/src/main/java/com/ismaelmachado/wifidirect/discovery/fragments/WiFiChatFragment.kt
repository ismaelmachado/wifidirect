package com.ismaelmachado.wifidirect.discovery.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ismaelmachado.wifidirect.discovery.R
import com.ismaelmachado.wifidirect.discovery.adapters.ChatMessageAdapter
import com.ismaelmachado.wifidirect.discovery.databinding.FragmentChatBinding
import com.ismaelmachado.wifidirect.discovery.managers.ChatManager

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 */
class WiFiChatFragment : Fragment() {

    interface MessageTarget {
        var handler: Handler
    }

    private lateinit var binding: FragmentChatBinding
    private var adapter: ChatMessageAdapter? = null
    private val items: List<String> = ArrayList()
    var chatManager: ChatManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater)
        val localContext = context ?: return binding.root

        binding.list.adapter = ChatMessageAdapter(localContext, R.id.chat_text1, items)
            .also { adapter = it }
        binding.button1.setOnClickListener { onButtonClick() }

        return binding.root
    }

    fun pushMessage(readMessage: String) {
        adapter?.add(readMessage)
        adapter?.notifyDataSetChanged()
    }

    private fun onButtonClick() {
        if (chatManager == null) return

        with(binding.txtChatLine) {
            chatManager?.write(text.toString())
            pushMessage(context.getString(
                R.string.chat_me_format,
                context.getString(R.string.chat_me),
                text
            ))
            setText("")
            clearFocus()
        }
    }

}
