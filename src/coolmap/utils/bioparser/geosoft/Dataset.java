/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.geosoft;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author sugang
 */
public class Dataset {

    private String datasetID;
    private final LinkedHashMap<String, String> plainAttributes = new LinkedHashMap<String, String>();
    private final LinkedHashMap<String, String> sampleAttributes = new LinkedHashMap<String, String>();

    public Dataset(String datasetID) {
        this.datasetID = datasetID;
    }

    private Dataset() {
        this.datasetID = null;
    }

    public void addAttribute(String key, String value) {
        plainAttributes.put(key, value);
    }

    public String getAttribute(String key) {
        return plainAttributes.get(key);
    }

    public void clear() {
        plainAttributes.clear();
        sampleAttributes.clear();
    }

    /**
     * get an array representation
     *
     * @return
     */
    public Object[][] toArray() {
        if (plainAttributes.isEmpty()) {
            return null;
        } else {
            return null;
        }
    }

    public void addSampleAttribute(String column, String value) {
        sampleAttributes.put(column, value);
    }

    public String getSampleAttribute(String column) {
        return sampleAttributes.get(column);
    }

    @Override
    public String toString() {
        return "Dataset:" + datasetID;
    }

    public void printDetails() {
        System.out.println("Dataset properties:");
        for (Map.Entry<String, String> entry : plainAttributes.entrySet()) {
            System.out.println(entry.getKey() + " ==> " + entry.getValue());
        }
        System.out.println("\nDataset column properties:");
        for (Map.Entry<String, String> entry : sampleAttributes.entrySet()) {
            System.out.println(" " + entry.getKey() + " ==> " + entry.getValue());
        }
        System.out.println();
    }
}
