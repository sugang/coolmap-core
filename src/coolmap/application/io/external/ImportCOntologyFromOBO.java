/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import com.google.common.collect.HashMultimap;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.contology.model.COntology;
import coolmap.utils.Tools;
import coolmap.utils.bioparser.simpleobo.SimpleOBOEntry;
import coolmap.utils.bioparser.simpleobo.SimpleOBOTree;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportCOntologyFromOBO implements ImportCOntology {

    @Override
    public COntology importFromFile(File file) throws Exception {
        try {
            SimpleOBOTree tree = SimpleOBOTree.parse(Tools.removeFileExtension(file.getName()), new FileInputStream(file));

        //tree.printEntries();
            //Then add them:
            COntology ontology = new COntology(Tools.removeFileExtension(file.getName()), null);
            ontology.addRelationshipUpdateDepth(tree.getTree());

//            also add attributes
            for(SimpleOBOEntry entry : tree.getAllEntries()){
                COntology.setAttribute(entry.getID(), "OBO.Name", entry.getName());
                COntology.setAttribute(entry.getID(), "OBO.Namespace", entry.getNamespace());
                HashMultimap<String, String> otherAttr = entry.getOtherAttributes();
                for(String key : otherAttr.keySet()){
                    COntology.setAttribute(entry.getID(), "OBO." +key, otherAttr.get(key).toString());
                }
            }
            
            return ontology;
        } catch (Exception e) {
            CMConsole.logError(" failed to import ontology from: " + file);
            return null;
        }
    }

    @Override
    public String getLabel() {
        return "Simple obo";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Simple obo", "obo", "txt");
    }

}
