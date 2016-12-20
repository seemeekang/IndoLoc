package ch.joelniklaus.indoloc;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.meta.CVParameterSelection;
import weka.classifiers.trees.J48;
import weka.core.Utils;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class HyperParameterSearchTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAutoWeka() throws Exception {
        /*
        AutoWEKAClassifier autoweka = new AutoWEKAClassifier();
        autoweka.setTimeLimit(1); // in minutes
        autoweka.setMemLimit(1024); // in MB
        autoweka.setDebug(true);
        autoweka.setSeed(123);
        autoweka.setnBestConfigs(3);
        autoweka.buildClassifier(train);
        System.out.println(autoweka.getnBestConfigs());
        */
    }

    @Test
    public void testCVParameterSelection() throws Exception {
        J48 classifier = new J48();
        CVParameterSelection cvParameterSelection = new CVParameterSelection();
        cvParameterSelection.setClassifier(classifier);
        cvParameterSelection.buildClassifier(train);
        cvParameterSelection.setNumFolds(5);  // using 5-fold CV
        cvParameterSelection.addCVParameter("C 0.1 0.5 5");
        String[] classifierOptions = cvParameterSelection.getBestClassifierOptions();
        classifier.setOptions(classifierOptions);
        classifier.buildClassifier(train);
        System.out.println(Utils.joinOptions(classifierOptions));
    }

    @Test
    public void testGridSearch() throws Exception {

    }

    @Test
    public void testMultiSearch() throws Exception {

    }

}