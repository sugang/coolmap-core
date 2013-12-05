/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.external;

import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.COntologyUtils;
import coolmap.utils.Tools;
import coolmap.utils.bioparser.gseagmt.GmtEntry;
import java.io.File;
import java.io.FileInputStream;
import static java.lang.System.in;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author sugang
 */
public class ImportCOntologyFromGMT {

    public static COntology importFromFile(File file) throws Exception {
        
        GmtEntry obj = GmtEntry.parse(file.getName(), GmtEntry.ID_TYPE.ENTREZ_ID, new FileInputStream(file));
        
        
//        obj.printStructure();
        COntology ontology = new COntology(Tools.removeFileExtension(file.getName()), null);
        Set<String> geneSets = obj.getGenesetNames();
        
        for(String geneSetString : geneSets){
            
//            Set<String> genes = obj.getGenesetGenes(geneSetString);
            ArrayList<String> genes = new ArrayList<String>(obj.getGenesetGenes(geneSetString));
            Collections.sort(genes);
            
            for(String gene : genes){
                ontology.addRelationshipNoUpdateDepth(geneSetString, gene);
                if(Thread.interrupted()){
                    return null;
                }
            }
            
            COntology.setAttribute(geneSetString, "Description", obj.getDescription(geneSetString));
            
        }
        
        //remove internal loops
        ontology.validate(); //remove loop, compute depth
        
//        COntologyUtils.printOntology(ontology);  
        return ontology;
    }
}
