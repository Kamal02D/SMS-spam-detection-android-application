import findspark

findspark.init()
from pyspark.sql.functions import length
from pyspark.ml.feature import Tokenizer
from pyspark.ml.feature import StopWordsRemover
from pyspark.ml.feature import CountVectorizer
from pyspark.ml.feature import IDF
from pyspark.ml.feature import VectorAssembler
from pyspark.ml.feature import StringIndexer
from pyspark.sql import SparkSession
from pyspark.ml.classification import NaiveBayes
from pyspark.ml.classification import LogisticRegression
from pyspark.ml.classification import DecisionTreeClassifier
from pyspark.ml.classification import RandomForestClassifier
from pyspark.ml.classification import LinearSVC
import pyspark.sql.functions as F
# Evaluate the model's performance
from pyspark.ml.evaluation import MulticlassClassificationEvaluator

spark = None


def clean_features(data, tok=None, stop=None, count=None, idf_=None):
    data = data.withColumn('length', length(data['text']))
    # Cache the data to improve performance
    data = data.cache()
    if tok is None:
        tokenizer = Tokenizer(inputCol="text", outputCol="token_text")
    else:
        tokenizer = tok
    clean_up = tokenizer.transform(data)

    if stop is None:
        stopremove = StopWordsRemover(inputCol='token_text', outputCol='stop_tokens')
    else:
        stopremove = stop
    clean_up = stopremove.transform(clean_up)

    if count is None:
        count_vec = CountVectorizer(inputCol='stop_tokens', outputCol='c_vec').fit(clean_up)
    else:
        count_vec = count
    clean_up = count_vec.transform(clean_up)

    if idf_ is None:
        idf = IDF(inputCol="c_vec", outputCol="tf_idf").fit(clean_up)
    else:
        idf = idf_
    clean_up = idf.transform(clean_up)

    vec_asmb = VectorAssembler(inputCols=['tf_idf', 'length'], outputCol='features')
    clean_up = vec_asmb.transform(clean_up)
    return [clean_up, (tokenizer, stopremove, count_vec, idf)]


def clean_label(data, str_indexer=None):
    if str_indexer is None:
        ham_spam_to_num = StringIndexer(inputCol='class', outputCol='label').fit(data)
    else:
        ham_spam_to_num = str_indexer
    clean_up = ham_spam_to_num.transform(data)
    return [clean_up.select(["features", "label"]), ham_spam_to_num]


def initialize():
    print("initializing the machine learning algorithms...")
    # importing the necessary packages
    global spark
    spark = SparkSession.builder.appName('nlp').getOrCreate()
    spark.sparkContext.setLogLevel('ERROR')
    print("loading data set ...")
    data = spark.read.csv("SMSSpamCollection.csv", inferSchema=True, sep='\t')
    data = data.withColumnRenamed('_c0', 'class').withColumnRenamed('_c1', 'text')
    # Clean the data
    accuracies = []
    training, testing = data.randomSplit([0.7, 0.3])
    data_and_cleaners = clean_features(training)
    training_features = data_and_cleaners[0]
    cleaners = [cleaner for cleaner in data_and_cleaners[1]]
    data_and_str_indexer = clean_label(training_features)
    training_label = data_and_str_indexer[0]
    cleaners.append(data_and_str_indexer[1])
    # naive bayes
    print("initializing naive bayes algorithm ...")
    nb = NaiveBayes()
    nb_model = nb.fit(training_label)
    test_x = clean_features(testing, *(cleaners[:len(cleaners) - 1]))[0]
    test = clean_label(test_x, cleaners[-1])[0]
    accuracy = get_model_accuracy(nb_model, test)
    accuracies.append(accuracy * 100)
    print("naive bayes model's Accuracy:", accuracy*100)
    # logistic regression

    print("initializing logistic regression algorithm ...")
    lr = LogisticRegression()
    lr_model = lr.fit(training_label)
    test_x = clean_features(testing, *(cleaners[:len(cleaners) - 1]))[0]
    test = clean_label(test_x, cleaners[-1])[0]
    accuracy = get_model_accuracy(lr_model, test)
    accuracies.append(accuracy*100)
    print("logistic regression model's Accuracy:", accuracy*100)
    # Decision tree
    print("initializing Decision tree algorithm ...")
    dtc = DecisionTreeClassifier()
    dtc_model = dtc.fit(training_label)
    test_x = clean_features(testing, *(cleaners[:len(cleaners) - 1]))[0]
    test = clean_label(test_x, cleaners[-1])[0]
    accuracy = get_model_accuracy(dtc_model, test)
    accuracies.append(accuracy * 100)
    print("decision tree model's Accuracy:", accuracy*100)
    # random forest
    print("initializing random forest algorithm ...")
    rfc = RandomForestClassifier()
    rfc_model = rfc.fit(training_label)
    test_x = clean_features(testing, *(cleaners[:len(cleaners) - 1]))[0]
    test = clean_label(test_x, cleaners[-1])[0]
    accuracy = get_model_accuracy(rfc_model, test)
    accuracies.append(accuracy * 100)
    print("random forest model's Accuracy:", accuracy*100)
    # Linear Support Vector Machine
    print("initializing Linear Support Vector Machine algorithm ...")
    svm = LinearSVC()
    svm_model = svm.fit(training_label)
    test_x = clean_features(testing, *(cleaners[:len(cleaners) - 1]))[0]
    test = clean_label(test_x, cleaners[-1])[0]
    accuracy = get_model_accuracy(svm_model, test)
    accuracies.append(accuracy * 100)
    print("LinearSVC model's Accuracy:", accuracy*100)

    return [[nb_model, lr_model, dtc_model, rfc_model, svm_model], cleaners,accuracies]


def format_result(algorithm_name, algo_prediction , alog_acc):
    try:
        probability_spam = algo_prediction.select("probability").collect()[0][0][1]
        probability_spam = float(round(probability_spam * 100))
        probability_ham = 100 - probability_spam
    except:
        probability_spam = 0
        probability_ham = 0

    return {
        algorithm_name: {
            "result": str(algo_prediction.select("prediction_label").collect()[0][0]),
            "spam_proba": probability_spam,
            "ham_proba": probability_ham,
            "acc" : alog_acc
        }
    }


def get_model_accuracy(model, test_data):
    # Make predictions on the test data
    predictions = model.transform(test_data)
    evaluator = MulticlassClassificationEvaluator(predictionCol="prediction", labelCol="label", metricName="accuracy")
    accuracy = evaluator.evaluate(predictions)

    return accuracy


def predict_spam(text, nb_model, lr_model, dtc_model, rfc_model, svm_model,
                 tokenizer, stopWordsRemover, countVectorizer, idf, stringIndexer,
                 nb_acc,lr_acc,dtc_acc,rfc_acc,svm_Acc):
    # Pre-process the text
    data = spark.createDataFrame([(text,)], ["text"])
    features = clean_features(data, tokenizer, stopWordsRemover, countVectorizer, idf)[0]
    # features = features.select(["features"])


    # Make predictions using the trained models
    nb_prediction = nb_model.transform(features)
    lr_prediction = lr_model.transform(features)
    dtc_prediction = dtc_model.transform(features)
    rfc_prediction = rfc_model.transform(features)
    svm_prediction = svm_model.transform(features)

    index_to_label = {index: label for index, label in enumerate(stringIndexer.labels)}

    nb_prediction = nb_prediction.withColumn("prediction_label",
                                             F.udf(lambda index: index_to_label[index])("prediction"))
    lr_prediction = lr_prediction.withColumn("prediction_label",
                                             F.udf(lambda index: index_to_label[index])("prediction"))
    dtc_prediction = dtc_prediction.withColumn("prediction_label",
                                               F.udf(lambda index: index_to_label[index])("prediction"))
    rfc_prediction = rfc_prediction.withColumn("prediction_label",
                                               F.udf(lambda index: index_to_label[index])("prediction"))
    svm_prediction = svm_prediction.withColumn("prediction_label",
                                               F.udf(lambda index: index_to_label[index])("prediction"))


    result = {}
    result.update(format_result("naive_bayes", nb_prediction , nb_acc))
    result.update(format_result("decision_tree", dtc_prediction , dtc_acc))
    result.update(format_result("random_forest", rfc_prediction , rfc_acc))
    result.update(format_result("svm", svm_prediction , svm_Acc))
    result.update(format_result("logistic_regression", lr_prediction , lr_acc))
    print("output : "+ str(result))
    return result



