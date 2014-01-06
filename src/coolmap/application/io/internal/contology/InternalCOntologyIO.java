/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.internal.contology;

import coolmap.data.contology.model.COntology;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Set;

/**
 * This is used for read and write COntology
 * @author sugang
 */
public class InternalCOntologyIO {
    
    public static void dumpData(BufferedWriter writer, COntology ontology) throws Exception{
        
        System.out.println("Dumping COntology called...");
        
        if(writer == null || ontology == null)
            return;
        
        Set<String> parentNodes = ontology.getAllNodesWithChildren();
        for(String parentNode : parentNodes){
            writer.write(parentNode);
            ArrayList<String> immediateChildren = ontology.getImmediateChildren(parentNode);
            for(String childNode : immediateChildren){
                writer.write("\t");
                writer.write(childNode);
            }
            writer.write("\n");
        }
        
        writer.flush();
        writer.close();
    }
    
    public static void loadData(BufferedReader reader, COntology ontology) throws Exception{
        String line;
        while( (line = reader.readLine()) != null){
            String[] ele = line.split("\\t", -1);
            if(ele.length < 1){
                throw new Exception("Malformed ontology file");
            }
            
            String parentNode = ele[0].trim();
            for(int i=1; i < ele.length; i++){
                ontology.addRelationshipNoUpdateDepth(parentNode, ele[i]);
            }
            
        }
        ontology.validate();
    }
}
