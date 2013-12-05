/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.application.io.external.interfaces.ImportCOntology;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.contology.model.COntology;
import coolmap.utils.Tools;
import coolmap.utils.bioparser.gseagmt.GmtEntry;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportCOntologyFromGMT implements ImportCOntology {

    public Collection<COntology> importFromFile(File[] files) throws Exception {

        HashSet<COntology> ontologies = new HashSet<COntology>();

        for (File file : files) {
            try {
                GmtEntry obj = GmtEntry.parse(file.getName(), GmtEntry.ID_TYPE.ENTREZ_ID, new FileInputStream(file));

//        obj.printStructure();
                COntology ontology = new COntology(Tools.removeFileExtension(file.getName()), null);
                Set<String> geneSets = obj.getGenesetNames();

                for (String geneSetString : geneSets) {

//            Set<String> genes = obj.getGenesetGenes(geneSetString);
                    ArrayList<String> genes = new ArrayList<String>(obj.getGenesetGenes(geneSetString));
                    Collections.sort(genes);

                    for (String gene : genes) {
                        ontology.addRelationshipNoUpdateDepth(geneSetString, gene);
                        if (Thread.interrupted()) {
                            return null;
                        }
                    }

                    COntology.setAttribute(geneSetString, "GMT.Description", obj.getDescription(geneSetString));

                }

                //remove internal loops
                ontology.validate(); //remove loop, compute depth
                ontology.setName(Tools.removeFileExtension(file.getName()));
                ontologies.add(ontology);
            } catch (Exception e) {
                CMConsole.logError("failed to load ontology from " + file);
            }
//        COntologyUtils.printOntology(ontology);  
        }
        return ontologies;
    }

    @Override
    public String getLabel() {
        return "GSEA gmt";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("GSEA gmt", "gmt", "txt");
    }
}
