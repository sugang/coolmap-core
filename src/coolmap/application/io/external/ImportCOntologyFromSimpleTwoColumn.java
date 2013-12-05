/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.edgeattributes.COntologyEdgeAttributeImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author gangsu
 */
public class ImportCOntologyFromSimpleTwoColumn implements ImportCOntology {

    public COntology importFromFile(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        COntology ontology = new COntology(file.getName(), null);
        while ((line = reader.readLine()) != null) {
            //System.out.println(line);
            try {
                String[] elements = line.split("\t", -1);
                ontology.addRelationshipNoUpdateDepth(elements[1], elements[0]);
                if (elements.length > 2 && elements[2].length() > 0) {
                    ontology.setEdgeAttribute(elements[1], elements[0], new COntologyEdgeAttributeImpl(Float.parseFloat(elements[2])));
                    if(Thread.interrupted()){
                        return null;
                    }
                }
            } catch (Exception e) {
//                System.out.println(line + " malformed");
            }
        }
        reader.close();
        ontology.validate();
        return ontology;
    }

    @Override
    public String getLabel() {
        return "Simple two column (sif)";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Simple two column (sif) tsv", "tsv", "txt");
    }
}
