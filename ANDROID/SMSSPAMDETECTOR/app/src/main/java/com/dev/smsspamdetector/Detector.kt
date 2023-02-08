package com.dev.smsspamdetector


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.guilhe.views.CircularProgressView
import org.json.JSONObject


class Detector : AppCompatActivity() , OnResponseRecieved , GestureDetector.OnGestureListener{
    private val TAG = "detector_logger"
    private lateinit var ip : String
    private lateinit var progressBar  : ProgressBar
    private lateinit var btnValidate : Button
    private lateinit var txtInput : EditText
    private lateinit var holder : ViewAnimator
    private lateinit var img_info : ImageView
    private lateinit var left : ImageView
    private lateinit var right : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detector)
        val intent = intent
        ip = intent.getStringExtra("ip").toString()
        Log.d(TAG, "ip : $ip")

        progressBar = findViewById(R.id.progress_bar)
        btnValidate = findViewById(R.id.button_validate)
        txtInput = findViewById(R.id.txtInput)
        holder = findViewById(R.id.view_animator)
        img_info = findViewById(R.id.img_info)
        left = findViewById(R.id.left)
        right = findViewById(R.id.right)


        btnValidate.setOnClickListener {
            if (txtInput.text.isEmpty()) {
                txtInput.error = "this field shouldn't be empty"
                return@setOnClickListener
            }
            btnValidate.setBackgroundColor(resources.getColor(R.color.gray))
            btnValidate.isClickable = false
            progressBar.visibility = View.VISIBLE
            // sending the request
            val request = SendRequest(this)
            request.execute(ip, "5000", txtInput.text.toString())
        }

        val gestureDetector = GestureDetector(this, this)
        holder.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        img_info.setOnClickListener{
            val aboutDialogFragment = InfoDialog()
            aboutDialogFragment.show(fragmentManager, "about_dialog")

        }
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
       return true
    }

    override fun onLongPress(p0: MotionEvent?) {

    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (e1 == null || e2 == null) return false

        val deltaX = e2.x - e1.x
        if (Math.abs(deltaX) > 50) {
            if (deltaX > 0) {
                // Swipe right
                if (holder.displayedChild > 0) {
                    // Only show previous view if not at the first view
                    holder.showPrevious()
                    showHideDirectionIndicator(right)
                }
            } else {
                // Swipe left
                if (holder.displayedChild < holder.childCount - 1) {
                    // Only show next view if not at the last view
                    holder.showNext()
                    showHideDirectionIndicator(left)
                }
            }
            return true
        }
        return false
    }



    override fun onReceiveResponse(result: String, ip: String) {
        Log.d(TAG , "the response of the server : \n $result ")
        progressBar.visibility = View.GONE
        try{
            val listResults : List<Result> = parseResults(result)
            Log.d(TAG , listResults.toString())
            val dtc_view : View = createStaticsView("decision tree" , listResults[0])
            val lr_view : View = createStaticsView("logistic regression" , listResults[1])
            val nb_view : View = createStaticsView("naive bayes" , listResults[2])
            val svm_view : View = createStaticsView("support vector machine" , listResults[4])
            val rfc_view : View = createStaticsView("random forest" , listResults[3])
            holder.addView(lr_view)
            holder.addView(dtc_view)
            holder.addView(nb_view)
            holder.addView(svm_view)
            holder.addView(rfc_view)

            // turn the button the first state so that the user could send more requests
            btnValidate.setBackgroundColor(resources.getColor( R.color.purple_500))
            btnValidate.isClickable = true
        }catch (ex : Exception){
            Log.e(TAG , "error : ${ex.message}")
            Toast.makeText(baseContext, "the response of the server is unreadable", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError() {
        Log.e(TAG , "custom error : error sending the request to the server")
        Toast.makeText(baseContext, "something gone wrong pleas try again", Toast.LENGTH_SHORT).show()
    }



    fun parseResults(jsonString: String): List<Result> {
        val json = JSONObject(jsonString)
        val decisionTree = json.getJSONObject("decision_tree")
        val logisticRegression = json.getJSONObject("logistic_regression")
        val naiveBayes = json.getJSONObject("naive_bayes")
        val randomForest = json.getJSONObject("random_forest")
        val svm = json.getJSONObject("svm")


        val decisionTreeResult = Result(
            decisionTree.getDouble("ham_proba"),
            decisionTree.getString("result"),
            decisionTree.getDouble("spam_proba"),
            decisionTree.getDouble("acc")
        )
        val logisticRegressionResult = Result(
            logisticRegression.getDouble("ham_proba"),
            logisticRegression.getString("result"),
            logisticRegression.getDouble("spam_proba"),
            logisticRegression.getDouble("acc")
        )
        val naiveBayesResult = Result(
            naiveBayes.getDouble("ham_proba"),
            naiveBayes.getString("result"),
            naiveBayes.getDouble("spam_proba"),
            naiveBayes.getDouble("acc")
        )
        val randomForestResult = Result(
            randomForest.getDouble("ham_proba"),
            randomForest.getString("result"),
            randomForest.getDouble("spam_proba"),
            randomForest.getDouble("acc")
        )
        val svmResult = Result(
            svm.getDouble("ham_proba"),
            svm.getString("result"),
            svm.getDouble("spam_proba"),
            svm.getDouble("acc")
        )

        return listOf(
            decisionTreeResult,
            logisticRegressionResult,
            naiveBayesResult,
            randomForestResult,
            svmResult
        )
    }


    fun createStaticsView(algoName : String , result:Result) :  View{
        val inflater = LayoutInflater.from(baseContext)
        val view = inflater.inflate(R.layout.result, holder, false)
        view.findViewById<TextView>(R.id.txt_algo).text = algoName
        view.findViewById<CircularProgressView>(R.id.circularProgressView).setProgress(result.acc.toFloat())
        view.findViewById<CircularProgressView>(R.id.circularProgressView_2).setProgress(result.spamProba.toFloat())
        view.findViewById<CircularProgressView>(R.id.circularProgressView_3).setProgress(result.hamProba.toFloat())
        view.findViewById<TextView>(R.id.txt_pred).text =  result.result
        view.findViewById<TextView>(R.id.txt_accuracy).text = result.acc.toInt().toString()
        view.findViewById<TextView>(R.id.txt_spam_proba).text = result.spamProba.toString()
        view.findViewById<TextView>(R.id.txt_ham_proba).text = result.hamProba.toString()
        if (algoName == "support vector machine"){
            view.findViewById<TextView>(R.id.textView_ham).text = "note : the support vector machine algorithm is not a probabilistic model hence it doesn't use probabilities"
            view.findViewById<TextView>(R.id.textView_spam).visibility=View.GONE
            view.findViewById<CircularProgressView>(R.id.circularProgressView_2).visibility=View.GONE
            view.findViewById<CircularProgressView>(R.id.circularProgressView_3).visibility=View.GONE
            view.findViewById<TextView>(R.id.txt_spam_proba).visibility=View.GONE
            view.findViewById<TextView>(R.id.txt_ham_proba).visibility=View.GONE
        }
        val layoutParams = ConstraintLayout.LayoutParams(900, 950)
        view.layoutParams = layoutParams
        return view
    }

    fun showHideDirectionIndicator(imageView: ImageView){
        imageView.visibility = View.VISIBLE
        Handler().postDelayed({
        imageView.visibility = View.GONE
        }, 200)
    }
}