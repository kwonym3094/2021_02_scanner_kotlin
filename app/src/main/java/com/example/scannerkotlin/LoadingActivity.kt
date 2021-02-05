package com.example.scannerkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import kotlinx.android.synthetic.main.activity_loading.*
import java.util.*
import kotlin.concurrent.schedule

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        Timer().schedule(30) {
            imageView.callOnClick()
        }

        Timer().schedule(1000) {
            finish()
        }

    }
}

