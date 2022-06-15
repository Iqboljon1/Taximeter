package com.iraimjanov.taxometr.activities

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.iraimjanov.taxometr.databinding.ActivityMainBinding
import com.iraimjanov.taxometr.utils.ConnectionStateMonitor

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ConnectionStateMonitor(this).enable(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.attachBaseContext(newBase)
    }

}