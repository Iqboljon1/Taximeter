package com.iraimjanov.taxometr.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.taxometr.R
import com.iraimjanov.taxometr.databinding.FragmentSignInBinding
import com.iraimjanov.taxometr.models.Users
import com.orhanobut.hawk.Hawk


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var realReference: DatabaseReference
    private var eye = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        realReference = Firebase.database.getReference("users")
        Hawk.init(requireActivity()).build()
        binding.swipeRefreshLayout.isEnabled = false


        binding.tvCreateAccount.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                findNavController().navigate(R.id.action_signInFragment_to_createAccountFragment)
            }
        }

        binding.tvLogin.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                signIn()
            }
        }

        settingETPassword()

        return binding.root
    }

    private fun signIn() {
        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        if (username.isNotEmpty() && password.isNotEmpty()) {
            binding.swipeRefreshLayout.isRefreshing = true
            getAllUsers(username, password)
        } else {
            Toast.makeText(requireActivity(), "Reference is not enough", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getAllUsers(username: String, password: String) {
        realReference.get().addOnSuccessListener {
            var booleanUsername = true
            for (child in it.children) {
                val users = child.getValue(Users::class.java)
                if (users != null) {
                    if (users.username == username) {
                        comparisonUsers(users, username, password)
                        booleanUsername = true
                        break
                    } else {
                        booleanUsername = false
                    }
                }
            }
            if (!booleanUsername) {
                Toast.makeText(requireActivity(),
                    "There is no user with this name",
                    Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun comparisonUsers(users: Users, username: String, password: String) {
        if (users.password == password) {
            Hawk.put("user", users)
            findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
            binding.swipeRefreshLayout.isRefreshing = false
        } else {
            Toast.makeText(requireActivity(), "Password did not match", Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun settingETPassword() {
        binding.edtPassword.setOnTouchListener(View.OnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.edtPassword.right - binding.edtPassword.compoundDrawables[2].bounds.width()
                ) {
                    if (eye) {
                        binding.edtPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.eye_closed,
                            0)
                        eye = false
                    } else {
                        binding.edtPassword.inputType =
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.eye_open,
                            0)
                        eye = true
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }

}