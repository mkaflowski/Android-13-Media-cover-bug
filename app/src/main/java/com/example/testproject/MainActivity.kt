package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    val TAG = "Test App"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonNameChange = findViewById<Button>(R.id.NAME_CHANGE)
        buttonNameChange.setOnClickListener { startWithNameChange() }

        val buttonOnlyCoverChange = findViewById<Button>(R.id.ONLY_COVER_CHANGE)
        buttonOnlyCoverChange.setOnClickListener { startWithNoNameChange() }
    }


    private fun startWithNameChange() {
        Log.d(TAG, "test")
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("ACTION","NAME_CHANGE")
        startService(intent)
    }

    private fun startWithNoNameChange() {
        Log.d(TAG, "test")
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("ACTION","ONLY_COVER_CHANGE")
        startService(intent)
    }
}