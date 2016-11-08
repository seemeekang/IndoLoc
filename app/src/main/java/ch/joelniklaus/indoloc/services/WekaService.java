package ch.joelniklaus.indoloc.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils;
import weka.core.converters.LibSVMLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemovePercentage;

public class WekaService extends Service {

    private Context context;

    public WekaService() {

    }

    public WekaService(Context context) {
        this.context = context;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // Weka version = 3.7.3

    public String testTrain() {
        try {
            // Load
            Instances data = loadArffFromAssets("supermarket.arff");

            // Filtering
            RemovePercentage remove = new RemovePercentage();
            remove.setInputFormat(data);
            String trainingSetPercentage = "99";

            String[] optionsTrain = {"-P", trainingSetPercentage};
            remove.setOptions(optionsTrain);
            Instances newData = Filter.useFilter(data, remove);
            saveArffToInternalStorage(newData, "train.arff");

            String[] optionsTest = {"-P", trainingSetPercentage, "-V"};
            remove.setOptions(optionsTest);
            newData = Filter.useFilter(data, remove);
            saveArffToInternalStorage(newData, "test.arff");


            // Train and test set
            Instances train = loadArffFromInternalStorage("train.arff");   // from internal storage
            train.setClassIndex(data.numAttributes() - 1);
            Instances test = loadArffFromInternalStorage("test.arff");    // from internal storage
            test.setClassIndex(data.numAttributes() - 1);
            // train classifier
            Classifier cls = new J48();
            cls.buildClassifier(train);
            // evaluate classifier and print some statistics
            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(cls, test);

            String result = eval.toSummaryString("\nResults\n======\n", false);

            System.out.println(result);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }

    public String testClassifier() {
        try {
            Instances data = loadArffFromAssets("weather.arff");

            // Classifier
            String[] options = new String[1];
            options[0] = "-U";            // unpruned tree
            J48 tree = new J48();         // new instance of tree
            tree.setOptions(options);     // set the options

            // Evaluation
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(tree, data, 10, new Random(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }

    public String testLoadAndSave() {
        try {
            // Load
            Instances data = loadArffFromAssets("weather.arff");

            // Filtering
            String[] options = new String[2];
            options[0] = "-R";                                    // "range"
            options[1] = "1";                                     // first attribute
            Remove remove = new Remove();                         // new instance of filter
            remove.setOptions(options);                           // set options
            remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
            Instances newData = Filter.useFilter(data, remove);   // apply filter

            // Save
            saveArffToInternalStorage(newData, "weather_new.arff");

            newData = loadArffFromInternalStorage("weather_new.arff");

            return newData.toSummaryString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "fail";
    }

    public String testSVM() {

        try {
            AbstractClassifier classifier = null;
            classifier = (AbstractClassifier) Class.forName(
                    "weka.classifiers.functions.LibSVM").newInstance();

            String options = ("-S 0 -K 0 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1");
            String[] optionsArray = options.split(" ");

            classifier.setOptions(optionsArray);


            Instances train = loadArffFromAssets("weather.arff");

            LibSVMLoader svmLoader = new LibSVMLoader();
            svmLoader.getDataSet();

            classifier.buildClassifier(train);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "fail";
    }

    public void saveArffToExternalStorage(Instances data, String fileName) throws IOException {
        if (isExternalStorageWritable()) {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            File directory = new File(root + "/locations");
            directory.mkdirs();
            saveArff(data, new File(directory + "/" + fileName));
        }
    }

    public Instances loadArffFromExternalStorage(String fileName) throws Exception {
        if (isExternalStorageReadable()) {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            File directory = new File(root + "/locations");
            return loadArff(directory + "/" + fileName);
        }
        return null;
    }

    public void saveArffToInternalStorage(Instances data, String fileName) throws IOException {
        saveArff(data, new File(context.getFilesDir() + "/" + fileName));
    }


    public Instances loadArffFromInternalStorage(String fileName) throws Exception {
        return loadArff(context.getFilesDir() + "/" + fileName);
    }

    public Instances loadArffFromAssets(String fileName) throws Exception {
        return new ConverterUtils.DataSource(context.getAssets().open(fileName)).getDataSet();
    }

    private void saveArff(Instances data, File file) throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(file);
        saver.writeBatch();
    }

    private Instances loadArff(String filePath) throws Exception {
        return new ConverterUtils.DataSource(filePath).getDataSet();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
