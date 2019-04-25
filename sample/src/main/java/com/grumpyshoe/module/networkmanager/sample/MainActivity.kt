package com.grumpyshoe.module.networkmanager.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.grumpyshoe.module.networkmanager.NetworkManager
import com.grumpyshoe.module.networkmanager.impl.NetworkManagerImpl
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val networkManager: NetworkManager by lazy { NetworkManagerImpl(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add clicklistener to button
        button.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            networkManager.getServiceInfo(
                type = editText.text.toString(),
                onServiceInfoReceived = {
                    Log.d("zzz", it.toString())
                    textView.post {
                        textView.text = it.toString()
                    }
                    progressBar.post {
                        progressBar.visibility = View.GONE
                    }
                },
                onError = {
                    Log.d("zzz", "error: $it")

                    textView.post {
                        val value = when(it){
                            NetworkManager.ERROR_TYPE.UNKNOWN,
                            NetworkManager.ERROR_TYPE.TIMEOUT -> "Service not Found"
                            else -> "$it"
                        }
                        textView.text = value
                    }
                    progressBar.post {
                        progressBar.visibility = View.GONE
                    }
                })
        }
    }

}
