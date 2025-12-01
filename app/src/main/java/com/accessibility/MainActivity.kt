package com.accessibility

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var statusText: TextView
    private val PICK_IMAGE_REQUEST = 1
    private val READ_STORAGE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        statusText = findViewById(R.id.statusText)
        val pickImageBtn = findViewById<Button>(R.id.pickImageBtn)
        val permissionBtn = findViewById<Button>(R.id.permissionBtn)
        val statusBtn = findViewById<Button>(R.id.statusBtn)

        pickImageBtn.setOnClickListener { pickImage() }
        permissionBtn.setOnClickListener { requestAccessibilityPermission() }
        statusBtn.setOnClickListener { checkAccessibilityStatus() }

        checkAccessibilityStatus()
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_STORAGE_REQUEST)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun requestAccessibilityPermission() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun checkAccessibilityStatus() {
        val isAccessibilityEnabled = isAccessibilityServiceEnabled()
        statusText.text = if (isAccessibilityEnabled) "✓ Serviço de Acessibilidade ATIVADO" else "✗ Serviço de Acessibilidade DESATIVADO"
        statusText.setTextColor(if (isAccessibilityEnabled) android.graphics.Color.GREEN else android.graphics.Color.RED)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val thisServiceName = "$packageName/${AccessibilityMonitorService::class.java.name}"
        return enabledServices.contains(thisServiceName)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                imageView.setImageURI(it)
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }
}
