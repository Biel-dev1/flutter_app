package com.accessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var statusText: TextView
    
    companion object {
        private const val PICK_IMAGE = 1
        private const val READ_STORAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        statusText = findViewById(R.id.statusText)
        
        findViewById<Button>(R.id.btnPickPhoto).setOnClickListener { pickPhoto() }
        findViewById<Button>(R.id.btnPermission).setOnClickListener { openAccessibilitySettings() }
        findViewById<Button>(R.id.btnCheck).setOnClickListener { checkStatus() }

        checkStatus()
    }

    private fun pickPhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_STORAGE)
        } else {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE)
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun checkStatus() {
        val enabled = isAccessibilityEnabled()
        statusText.text = if (enabled) "✓ Acessibilidade ATIVADA" else "✗ Acessibilidade DESATIVADA"
        statusText.setTextColor(if (enabled) android.graphics.Color.parseColor("#4CAF50") else android.graphics.Color.parseColor("#F44336"))
    }

    private fun isAccessibilityEnabled(): Boolean {
        val services = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return services.contains("$packageName/${AccessibilityService::class.java.name}")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let {
                imageView.setImageURI(it)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE)
        }
    }
}
