package com.dev.smsspamdetector


import android.app.DialogFragment
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat

class InfoDialog : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view: View = inflater.inflate(R.layout.dialog_layout, container, false)
        val text = "This project, entitled \"SMS Spam Detection using Machine Learning,\" was developed by <b>Kamal Douma</b> and <b>El Addouni Anass</b> under the guidance and supervision of Professor <b>Mourad Azhari</b>. The project aims to create an android app that utilizes machine learning algorithms to detect spam SMS messages and provide users with a visualization of the likelihood that a given SMS is spam." +
                "<br><br>To achieve this goal, the project team used the PySpark ML library and a dataset provided by the UCI Machine Learning Repository at <a href='https://archive.ics.uci.edu/ml/datasets/sms+spam+collection'>https://archive.ics.uci.edu/ml/datasets/sms+spam+collection</a>. The dataset contains over 5,000 SMS messages that have been labeled as either \"spam\" or \"not spam,\" and was used to train and test the machine learning algorithms." +
                "<br><br>The android app developed as part of this project allows users to input their own SMS messages and receive immediate feedback on the likelihood that the message is spam, as determined by multiple machine learning algorithms. By providing this information, the app aims to give users a better understanding of the problem of spam and the tools available for combating it."
        val textView = view.findViewById<TextView>(R.id.text_view)
        textView.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = LinkMovementMethod.getInstance()
        return view
    }
}

