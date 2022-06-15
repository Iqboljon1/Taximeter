package com.iraimjanov.taxometr.fragments

import android.animation.Animator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.iraimjanov.taxometr.R
import com.iraimjanov.taxometr.databinding.FragmentSplashBinding
import com.iraimjanov.taxometr.models.Users
import com.orhanobut.hawk.Hawk

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        Hawk.init(context).build();
        val user = Hawk.get("user", Users())
        binding.animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                if (user.username.isNotEmpty()) {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_createAccountFragment)
                }
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}
        })

        return binding.root
    }

}