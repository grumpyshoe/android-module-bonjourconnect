package com.grumpyshoe.module.bonjourconnect.sample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.grumpyshoe.module.bonjourconnect.BonjourConnect
import com.grumpyshoe.module.bonjourconnect.impl.BonjourConnectImpl
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val bonjourConnect: BonjourConnect by lazy { BonjourConnectImpl(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add clicklistener to button
        button.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            bonjourConnect.getServiceInfo(
                type = editText.text.toString(),
                onServiceInfoReceived = { networkService ->
                    Log.d("zzz", it.toString())
                    Handler(Looper.getMainLooper()).post {
                        textView.text = networkService.toString()
                        progressBar.visibility = View.GONE
                    }
                },
                onError = { errorType ->

                    Handler(Looper.getMainLooper()).post {
                        val value = when (errorType) {
                            BonjourConnect.ErrorType.UNKNOWN,
                            BonjourConnect.ErrorType.TIMEOUT -> "Service not Found"
                            else -> "$errorType"
                        }
                        textView.text = value
                        progressBar.visibility = View.GONE
                    }
                })
        }
    }

}
