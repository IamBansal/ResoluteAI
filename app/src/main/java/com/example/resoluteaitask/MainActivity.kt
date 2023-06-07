@file:Suppress("DEPRECATION")

package com.example.resoluteaitask

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
    }

    private lateinit var scannerView: ZXingScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(result: Result) {
        val scannedResult = result.text

        val progressBar = ProgressDialog(this)
        progressBar.setMessage("Updating the result: $scannedResult in db...")
        progressBar.show()

        FirebaseDatabase.getInstance().reference.child("Resolute").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(scannedResult).addOnCompleteListener {
            if (!it.isSuccessful) Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
//            else Toast.makeText(this, "Scanned result is: $scannedResult, and updated successfully.", Toast.LENGTH_SHORT).show()
            progressBar.dismiss()
        }

        scannerView.postDelayed({
            scannerView.resumeCameraPreview(this)
        }, 2000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scannerView.setResultHandler(this)
                scannerView.startCamera()
            } else {
                Log.d("QRCodeScannerActivity", "Camera permission denied")
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
