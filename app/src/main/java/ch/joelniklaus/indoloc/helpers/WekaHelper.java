package ch.joelniklaus.indoloc.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import ch.joelniklaus.indoloc.activities.CollectDataActivity;
import ch.joelniklaus.indoloc.models.DataPoint;
import ch.joelniklaus.indoloc.models.SensorsValue;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Bagging;
import weka.classifiers.pmml.consumer.SupportVectorMachineModel;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;

// Weka version = 3.7.3
// weka_old.jar

public class WekaHelper {

    private Context context;

    private Classifier classifier;

    public static final String TRAINING_SET_PERCENTAGE = "80";

    public WekaHelper(Context context) {
        this.context = context;
    }

    public void test(Instances test) throws Exception {
        for (int i = 0; i < test.numInstances(); i++) {
            double actualClass = test.instance(i).classValue();
            String actual = test.classAttribute().value((int) actualClass);

            double predictedClass = classifier.classifyInstance(test.instance(i));
            String predicted = test.classAttribute().value((int) predictedClass);

            alert("Predicted Room: " + predicted + "\nActual Room: " + actual);
        }
    }

    /**
     * Randomizes the data. Divides data into training set and test set according to
     * TRAINING_SET_PERCENTAGE. Trains the classifier using the training set. Returns the test set.
     *
     * @param data
     * @return test set
     * @throws Exception
     */
    public Instances train(Instances data) throws Exception {
        // Randomizing
        data.randomize(new Random());

        // Filtering
        RemovePercentage remove = new RemovePercentage();
        remove.setInputFormat(data);

        String[] optionsTrain = {"-P", TRAINING_SET_PERCENTAGE, "-V"};
        remove.setOptions(optionsTrain);
        Instances train = Filter.useFilter(data, remove);
        train.setClassIndex(0);

        String[] optionsTest = {"-P", TRAINING_SET_PERCENTAGE};
        remove.setOptions(optionsTest);
        Instances test = Filter.useFilter(data, remove);
        test.setClassIndex(0);

        trainKNN(train);

        alert("Model successfully trained: \n" + classifier.toString());

        return test;
    }

    // K-Nearest Neighbour
    private void trainKNN(Instances train) throws Exception {
        // train classifier
        classifier = new IBk();
        classifier.buildClassifier(train);
    }


    // Support Vector Machine
    private void trainSVM(Instances train) throws Exception {
        // train classifier
        classifier = new SupportVectorMachineModel();
        classifier.buildClassifier(train);
    }


    // Naive Bayes
    private void trainNB(Instances train) throws Exception {
        // train classifier
        classifier = new NaiveBayes();
        classifier.buildClassifier(train);
    }

    // Logistic Regression
    private void trainLR(Instances train) throws Exception {
        // train classifier
        classifier = new Logistic();
        classifier.buildClassifier(train);
    }

    // Bagging
    private void trainBagging(Instances train) throws Exception {
        // train classifier
        classifier = new Bagging();
        classifier.buildClassifier(train);
    }

    public void alert(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    public static Instances buildInstances(ArrayList<DataPoint> dataPoints) {
        // rooms + number of rss + number of sensors
        int numberOfAttributes = 1 + CollectDataActivity.NUMBER_OF_ACCESS_POINTS + CollectDataActivity.NUMBER_OF_SENSORS;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(numberOfAttributes);

        ArrayList<String> rooms = new ArrayList<String>();

        for (DataPoint dataPoint : dataPoints)
            if (!rooms.contains(dataPoint.getRoom()))
                rooms.add(dataPoint.getRoom());

        // class: room
        attributes.add(new Attribute("room", rooms));

        // rss
        for (int i = 0; i < dataPoints.get(0).getRssList().size(); i++)
            attributes.add(new Attribute("rss" + i, Attribute.NUMERIC));

        // sensors
        //attributes.add(new Attribute("ambient_temperature", Attribute.NUMERIC));
        //attributes.add(new Attribute("relative_humidity", Attribute.NUMERIC));
        attributes.add(new Attribute("light", Attribute.NUMERIC));
        attributes.add(new Attribute("pressure", Attribute.NUMERIC));

        Instances data = new Instances("TestInstances", attributes, dataPoints.size());

        double[] instanceValues = null;
        for (DataPoint dataPoint : dataPoints) {
            instanceValues = new double[data.numAttributes()];

            // room
            instanceValues[0] = rooms.indexOf(dataPoint.getRoom());

            // rss
            ArrayList<Integer> rssListTemp = dataPoint.getRssList();
            ;
            for (int i = 0; i < rssListTemp.size(); i++)
                instanceValues[1 + i] = rssListTemp.get(i);

            // sensors
            SensorsValue sensors = dataPoint.getSensors();
            //instanceValues[rssListTemp.size() + 1] = sensors.getAmbientTemperature();
            //instanceValues[rssListTemp.size() + 4] = sensors.getRelativeHumidity();
            instanceValues[rssListTemp.size() + 1] = sensors.getLight();
            instanceValues[rssListTemp.size() + 2] = sensors.getPressure();

            data.add(new DenseInstance(1.0, instanceValues));
        }

        data.setClassIndex(0);

        return data;
    }

}