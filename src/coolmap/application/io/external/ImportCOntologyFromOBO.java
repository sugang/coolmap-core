/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import com.google.common.collect.HashMultimap;
import coolmap.application.io.external.interfaces.ImportCOntology;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.contology.model.COntology;
import coolmap.utils.Tools;
import coolmap.utils.bioparser.simpleobo.SimpleOBOEntry;
import coolmap.utils.bioparser.simpleobo.SimpleOBOTree;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportCOntologyFromOBO implements ImportCOntology {

    private final HashSet<COntology> ontologies = new HashSet<COntology>();
    
    
    @Override
    public void importFromFiles(File... files) throws Exception {

            //tree.printEntries();
        //Then add them:
        for (File file : files) {

            try {
                SimpleOBOTree tree = SimpleOBOTree.parse(Tools.removeFileExtension(file.getName()), new FileInputStream(file));
                COntology ontology = new COntology(Tools.removeFileExtension(file.getName()), null);
                ontology.addRelationshipUpdateDepth(tree.getTree());

//            also add attributes
                for (SimpleOBOEntry entry : tree.getAllEntries()) {
                    COntology.setAttribute(entry.getID(), "OBO.Name", entry.getName());
                    COntology.setAttribute(entry.getID(), "OBO.Namespace", entry.getNamespace());
                    HashMultimap<String, String> otherAttr = entry.getOtherAttributes();
                    for (String key : otherAttr.keySet()) {
                        COntology.setAttribute(entry.getID(), "OBO." + key, otherAttr.get(key).toString());
                    }
                }
                ontology.validate();
                ontology.setName(Tools.removeFileExtension(file.getName()));
                ontologies.add(ontology);

            } catch (Exception e) {
                CMConsole.logError(" failed to import ontology from: " + file);
                ontologies.clear();
                return;
            }
        }
        return;
    }

    @Override
    public String getLabel() {
        return "Simple obo";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Simple obo", "obo", "tsv", "txt");
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
