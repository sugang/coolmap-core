/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.application.io.external.interfaces.ImportCOntology;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.edgeattributes.COntologyEdgeAttributeImpl;
import coolmap.utils.Tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author gangsu
 */
public class ImportCOntologyFromSimpleTwoColumn implements ImportCOntology {
    
    private final HashSet<COntology> ontologies = new HashSet<COntology>();

    public void importFromFiles(File... files) throws Exception {
        for (File file : files) {
            try {
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
                            if (Thread.interrupted()) {
                                ontologies.clear();
                                return;
                            }
                        }
                    } catch (Exception e) {
//                System.out.println(line + " malformed");
                    }
                }
                reader.close();
                ontology.validate();
                ontology.setName(Tools.removeFileExtension(file.getName()));
                ontologies.add(ontology);

            } catch (Exception e) {
                CMConsole.logError("failed to load ontology from " + file);
            }
        }
        return;
    }

    @Override
    public String getLabel() {
        return "Simple two column (sif)";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Simple two column (sif) tsv", "tsv", "txt");
    }

    @Override
    public void importFromFile(File file) throws Exception {
        importFromFiles(new File[]{file});
    }

    @Override
    public Set<COntology> getImportedCOntology() {
        return ontologies;
    }

    @Override
    public void configure(File... file) {
    }

    @Override
    public boolean onlyImportFromSingleFile() {
        return false;
    }
}
