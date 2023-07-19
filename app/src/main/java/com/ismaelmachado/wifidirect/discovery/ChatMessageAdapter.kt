package com.ismaelmachado.wifidirect.discovery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ismaelmachado.wifidirect.discovery.databinding.ChatItemBinding

/**
 * ArrayAdapter to manage chat messages.
 */
class ChatMessageAdapter(
    context: Context,
    textViewResourceId: Int,
    private val items: List<String>
) : ArrayAdapter<String?>(context, textViewResourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            ChatItemBinding.bind(convertView)
        } else {
            ChatItemBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        val message = items[position]
        binding.chatText1.apply {
            text = message
            setTextAppearance(
                if (message.startsWith(context.getString(R.string.chat_me))) {
                    R.style.normalText
                } else {
                    R.style.boldText
                }
            )
        }
        return binding.root
    }

}