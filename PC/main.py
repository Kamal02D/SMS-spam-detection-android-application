# Import the Flask library for creating a web server
from flask import Flask, request

# Create a new Flask app
app = Flask(__name__)

nb_model, lr_model, dtc_model, rfc_model, svm_model, \
tokenizer, stopWordsRemover, countVectorizer, idf, stringIndexer ,nb_acc,lr_acc,dtc_acc,rfc_acc,svm_Acc  = None,None,None,None,None,\
                                                                    None,None,None,None,None,None,None,None,None,None


# Define a route that processes requests to the /process URL
@app.route('/process')
def process():
    global nb_model, lr_model, dtc_model, rfc_model, svm_model, tokenizer, stopWordsRemover, \
        countVectorizer, idf, stringIndexer ,nb_acc,lr_acc,dtc_acc,rfc_acc,svm_Acc
    # Retrieve the "param" query string parameter from the request
    param = request.args.get('param')
    print("input : "+param)
    if param == "handshake":
        return "1"
    prediction = sms_spam_detector.predict_spam(param,nb_model, lr_model, dtc_model, rfc_model, svm_model, tokenizer,stopWordsRemover,countVectorizer, idf, stringIndexer,nb_acc,lr_acc,dtc_acc,rfc_acc,svm_Acc)
    return prediction


# Run the app on the specified host and port
if __name__ == '__main__':
    import sms_spam_detector

    models, cleaners , accuracies = sms_spam_detector.initialize()
    nb_model, lr_model, dtc_model, rfc_model, svm_model = models
    tokenizer, stopWordsRemover, countVectorizer, idf, stringIndexer = cleaners
    nb_acc, lr_acc, dtc_acc, rfc_acc, svm_Acc = accuracies
    app.run(host='0.0.0.0', port=5000)
