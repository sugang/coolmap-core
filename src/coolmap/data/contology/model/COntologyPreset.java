/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author sugang
 */
public class COntologyPreset {

    private final String name;
    private final String description;
    private final ArrayList<String> labels = new ArrayList<String>();
    private final ArrayList<Integer> toExpandedIndices = new ArrayList<Integer>();

    public COntologyPreset(String name, String description, Collection<String> labels, Collection<Integer> toBeExpanded) {
        this.name = name;
        this.description = description;

        if (labels != null) {
            this.labels.addAll(labels);
        }
        if (toBeExpanded != null) {
            this.toExpandedIndices.addAll(toBeExpanded);
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getLabels() {
        return new ArrayList<String>(labels);
    }

    public ArrayList<Integer> getToBeExpandedIndices() {
        return new ArrayList<Integer>(toExpandedIndices);
    }

    @Override
    public String toString() {
        return getName();
    }
    
    

}
