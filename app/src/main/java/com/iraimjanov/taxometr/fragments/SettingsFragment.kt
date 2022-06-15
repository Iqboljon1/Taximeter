package com.iraimjanov.taxometr.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.iraimjanov.taxometr.databinding.FragmentSettingsBinding
import com.orhanobut.hawk.Hawk

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        binding.edtKmSumma.setText(Hawk.get("km/summa", "3000"))

        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.imageDone.setOnClickListener {
            val text = binding.edtKmSumma.text.toString().trim()
            if (text.isNotEmpty() && text.toInt() > 0) {
                Hawk.put("km/summa", binding.edtKmSumma.text.toString().trim())
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireActivity(),
                    "Incorrect source entered",
                    Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}