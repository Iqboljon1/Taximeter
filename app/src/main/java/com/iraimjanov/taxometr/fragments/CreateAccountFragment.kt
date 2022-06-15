package com.iraimjanov.taxometr.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.iraimjanov.taxometr.R
import com.iraimjanov.taxometr.databinding.FragmentCreateAccountBinding
import com.iraimjanov.taxometr.models.Users
import com.iraimjanov.taxometr.utils.CheckUsers
import com.orhanobut.hawk.Hawk
import java.io.ByteArrayOutputStream


class CreateAccountFragment : Fragment() {
    private lateinit var binding: FragmentCreateAccountBinding
    private lateinit var realReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var uri: Uri? = null
    private var eye = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCreateAccountBinding.inflate(layoutInflater)
        CheckUsers().checkUsers()
        realReference = Firebase.database.getReference("users")
        storageReference = Firebase.storage.getReference("image/")
        Hawk.init(requireActivity()).build()
        binding.swipeRefreshLayout.isEnabled = false

        binding.tvSignIn.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                findNavController().navigate(R.id.action_createAccountFragment_to_signInFragment)
            }
        }

        binding.tvCreateAccount.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                checkInformation()
            }
        }

        binding.imageTexPassport.setOnClickListener {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                getImageContent.launch("image/*")
            }
        }

        settingETPassword()

        return binding.root
    }

    private fun checkInformation() {
        val username = binding.edtUsername.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val passwordConfirmation = binding.edtPasswordConfirmation.text.toString().trim()
        val number = binding.edtNumber.text.toString().trim()
        val address = binding.edtAddress.text.toString().trim()
        val patronymic = binding.edtPatronymic.text.toString().trim()
        val name = binding.edtName.text.toString().trim()
        val surname = binding.edtSurname.text.toString().trim()
        if (username.isNotEmpty() && password.isNotEmpty() && passwordConfirmation.isNotEmpty() && number.isNotEmpty() && address.isNotEmpty() && patronymic.isNotEmpty() && name.isNotEmpty() && surname.isNotEmpty() && uri != null) {
            if (password == passwordConfirmation) {
                if (CheckUsers.listUsernames.add(username)) {
                    binding.swipeRefreshLayout.isRefreshing = true
                    saveImage(Users(username, password, number, address, patronymic, name, surname))
                    realReference.child(username).setValue(Users(username, password))
                        .addOnSuccessListener {
                            Hawk.put("user", Users(username, password))
                            findNavController().navigate(R.id.action_createAccountFragment_to_homeFragment)
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                } else {
                    Toast.makeText(requireActivity(),
                        "There is a user with this name",
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireActivity(), "Passwords did not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireActivity(), "The information is not complete", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveImage(user: Users) {
        val bitmap: Bitmap = binding.imageTexPassport.drawable.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        storageReference.child(user.username).putBytes(byteArray)
            .addOnSuccessListener { it ->
                it.storage.downloadUrl.addOnSuccessListener {
                    user.imageLink = it.toString()
                    saveUser(user)
                }
            }
    }

    private fun saveUser(user: Users) {
        realReference.child(user.username).setValue(user)
            .addOnSuccessListener {
                Hawk.put("user", user)
                findNavController().navigate(R.id.action_createAccountFragment_to_homeFragment)
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

        binding.edtPasswordConfirmation.setOnTouchListener(View.OnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= binding.edtPasswordConfirmation.right - binding.edtPasswordConfirmation.compoundDrawables[2].bounds.width()
                ) {
                    if (eye) {
                        binding.edtPasswordConfirmation.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.edtPasswordConfirmation.setCompoundDrawablesWithIntrinsicBounds(0,
                            0,
                            R.drawable.eye_closed,
                            0)
                        eye = false
                    } else {
                        binding.edtPasswordConfirmation.inputType =
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.edtPasswordConfirmation.setCompoundDrawablesWithIntrinsicBounds(0,
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

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it ?: return@registerForActivityResult
        Glide.with(requireActivity()).load(it).centerCrop().into(binding.imageTexPassport)
        uri = it
    }

}