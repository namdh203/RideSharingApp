package com.ridesharingapp.driversideapp.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.ridesharingapp.common.R
import com.ridesharingapp.common.databinding.FragmentChatBinding
import com.ridesharingapp.driversideapp.navigation.ChatKey
import com.zhuinden.simplestackextensions.fragments.KeyedFragment
import com.zhuinden.simplestackextensions.fragmentsktx.lookup
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

class ChatFragment : KeyedFragment(R.layout.fragment_chat) {

    private val viewModel by lazy { lookup<ChatViewModel>() }

    lateinit var binding: FragmentChatBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        binding.backIcon.setOnClickListener {
            viewModel.handleBackButton()
        }

        val channelId: String = (getKey() as ChatKey).channelId
        val messageListViewModel: MessageListViewModel by viewModels {
            MessageListViewModelFactory(cid = channelId)
        }

        val messageInputViewModel: MessageInputViewModel by viewModels {
            MessageListViewModelFactory(cid = channelId)
        }

        messageListViewModel.bindView(binding.messageListView, viewLifecycleOwner)
        messageInputViewModel.bindView(binding.messageInputView, viewLifecycleOwner)
    }
}