/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.geosoft;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author sugang
 */
public class Subset {

//    public final String subsetDatasetID;
//    public final String subsetDescription;
//    public final String subsetType;
//    public final String[] subsetSamples;
//    
//    private Subset(){
//        this(null, null, null, null);
//    }
//    
//    public Subset(String subsetDatasetID, String subsetDescription, String subsetType, String[] subsetSamples){
//        this.subsetDatasetID = subsetDatasetID;
//        this.subsetDescription = subsetDescription;
//        this.subsetType = subsetType;
//        this.subsetSamples = subsetSamples;
//    }
    private String subsetID;
    private LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

    public Subset(String subsetID) {
        this.subsetID = subsetID;
    }

    private Subset() {
    }

    @Override
    public String toString() {
        return "Subset:" + subsetID;
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void printDetails() {
        System.out.println("Subset properties:");
        for (Entry<String, String> entry : attributes.entrySet()) {
            System.out.println(entry.getKey() + " ==> " + entry.getValue());
        }
        System.out.println();
    }
}
