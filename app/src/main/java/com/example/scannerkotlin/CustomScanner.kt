package com.example.scannerkotlin

import android.app.Activity
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.os.Bundle
import android.view.View
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.android.synthetic.main.activity_custom_scanner.*


class CustomScanner : Activity(), DecoratedBarcodeView.TorchListener {

    lateinit var capture : CaptureManager
    lateinit var barcodeView: DecoratedBarcodeView

    var isSwitchOn : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        if (!hasFlash()) {
            switch_flashlight.visibility = View.GONE
        }

        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        barcodeView.setTorchListener(this)

        capture = CaptureManager(this,barcodeView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        switch_flashlight.setOnClickListener{
            switchFlashlight()
        }

    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
            .hasSystemFeature(FEATURE_CAMERA_FLASH)
    }

    fun switchFlashlight() = if (isSwitchOn) barcodeView.setTorchOff() else barcodeView.setTorchOn()

    override fun onTorchOn() {
        switch_flashlight.setImageResource(R.drawable.ic_flash_on_white_36dp)
        isSwitchOn = true
    }

    override fun onTorchOff() {
        switch_flashlight.setImageResource(R.drawable.ic_flash_off_white_36dp)
        isSwitchOn = false
    }
}