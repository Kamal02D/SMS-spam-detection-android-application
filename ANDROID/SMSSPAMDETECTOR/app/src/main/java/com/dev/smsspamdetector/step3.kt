package com.dev.smsspamdetector

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import java.util.regex.Pattern

class step3 : Fragment() ,OnResponseRecieved{
    private lateinit var txtIpAddress : EditText
    private lateinit var imgNext : ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_step3, container, false)

        txtIpAddress = view.findViewById(R.id.input_field)
        imgNext = view.findViewById(R.id.img_ip)

        imgNext.setOnClickListener {
            imgNext.setImageResource(R.drawable.arrow_icon_gray)
            if (txtIpAddress.text != null && isValidIpAddress(txtIpAddress.text.toString()
                    .trim())
            ) {
                // launch the main screen
                val request = SendRequest(this)
                request.execute(txtIpAddress.text.toString().trim(), "5000", "handshake")
            } else {
                txtIpAddress.error = "the input needs to be a valid ip address"
                imgNext.setImageResource(R.drawable.arrow_icon)
            }
        }


        return  view
    }

    fun isValidIpAddress(ip: String): Boolean {
        val ipAddressRegex = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$"
        val pattern = Pattern.compile(ipAddressRegex)
        val matcher = pattern.matcher(ip)
        return matcher.matches() && ip.split(".").all { it.toInt() in 0..255 }
    }

    override fun onReceiveResponse(result: String, ip : String) {
        val intent = Intent(context , Detector::class.java)
        intent.putExtra("ip", ip)
        startActivity(intent)
        imgNext.setImageResource(R.drawable.arrow_icon)
    }

    override fun onError() {
        Toast.makeText(context, "the ip address has no functional server", Toast.LENGTH_LONG).show()
        imgNext.setImageResource(R.drawable.arrow_icon)
    }

}