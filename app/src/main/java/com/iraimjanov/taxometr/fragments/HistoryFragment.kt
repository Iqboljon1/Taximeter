package com.iraimjanov.taxometr.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.taxometr.adapters.RVHistoryAdapter
import com.iraimjanov.taxometr.databinding.FragmentHistoryBinding
import com.iraimjanov.taxometr.models.History
import com.iraimjanov.taxometr.models.Users
import com.orhanobut.hawk.Hawk

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var realReference: DatabaseReference
    private var user: Users = Hawk.get("user", Users())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        binding.swipeRefreshLayout.isEnabled = false
        realReference = Firebase.database.getReference("history")

        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }

        getHistory()

        return binding.root
    }

    private fun getHistory() {
        binding.swipeRefreshLayout.isRefreshing = true
        realReference.child(user.username).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isVisible) {
                    val listHistory = ArrayList<History>()
                    for (child in snapshot.children) {
                        val history = child.getValue(History::class.java)
                        if (history != null) {
                            listHistory.add(history)
                        }
                    }
                    binding.rvHistory.adapter = RVHistoryAdapter(listHistory)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}