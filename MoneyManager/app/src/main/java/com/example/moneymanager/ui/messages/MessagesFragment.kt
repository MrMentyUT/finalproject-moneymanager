package com.example.moneymanager.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneymanager.databinding.FragmentMessagesBinding
import com.example.moneymanager.message.Message
import com.example.moneymanager.message.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private var receiverId: String = "SWV6Vp1NeAVoAgKBjC1cM2iD9E13"
    private var currentUserId: String? = null
    private lateinit var conversationId: String

    // Use ViewModel to store data
    private val viewModel: MessagesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the current user ID
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == receiverId) {
            receiverId = "ODNZCYCuUyTDLXQVeeOZZuMhg2E2"
        }
        conversationId = if (currentUserId!! < receiverId) {
            "$currentUserId-$receiverId"
        } else {
            "$receiverId-$currentUserId"
        }

        // Set up RecyclerView with adapter
        adapter = MessageAdapter(messages, currentUserId!!)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
            reverseLayout = false
        }
        binding.recyclerViewMessages.adapter = adapter

        // Observe messages from the ViewModel
        viewModel.messages.observe(viewLifecycleOwner, Observer { updatedMessages ->
            messages.clear()
            messages.addAll(updatedMessages)
            adapter.notifyDataSetChanged()
            binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
        })

        binding.messageEditText.addTextChangedListener {
            Log.d("Message", "Current input text: ${it.toString()}")
        }

        // Send button click listener
        binding.sendButton.setOnClickListener {
            val text = binding.messageEditText.text.toString()
            if (text.isNotBlank()) {
                sendMessage(text)
                binding.messageEditText.setText("") // Clear the input field
            }
        }

        // Listen for incoming messages
        listenForMessages()
    }

    private fun sendMessage(messageText: String) {
        val message = hashMapOf(
            "senderId" to currentUserId,
            "receiverId" to receiverId,
            "message" to messageText,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(message)
    }

    private fun listenForMessages() {
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && _binding != null) {
                    val newMessages = mutableListOf<Message>()
                    for (doc in snapshot.documents) {
                        doc.toObject(Message::class.java)?.let { newMessages.add(it) }
                    }

                    // Update the ViewModel with new messages
                    viewModel.setMessages(newMessages)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}