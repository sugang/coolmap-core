/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.contology;

import coolmap.application.io.IOTerm;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.edgeattributes.COntologyEdgeAttributeImpl;
import coolmap.utils.graphics.UI;
import java.awt.Color;
import java.io.*;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author gangsu
 */
public class PrivateCOntologyStructureFileIO {
    
    public COntology readFromFolder(String ID, String name, String description, Color viewColor, File entryFolder) throws Exception{
        if(entryFolder != null && entryFolder.isDirectory()){
//            System.out.println(entryFolder);
            File file = new File(entryFolder + File.separator + IOTerm.FILE_CONTOLOGY_ENTRY);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            COntology ontology = new COntology(name, description, ID);
            ontology.setViewColor(viewColor);
            ontology.setDescription(description);
            while((line = reader.readLine())!=null){
                //System.out.println(line);
                try{
                    String[] elements = line.split("\t", -1);
                    ontology.addRelationshipNoUpdateDepth(elements[1], elements[0]);
                    if(elements.length > 2){
                        ontology.setEdgeAttribute(elements[1], elements[0], new COntologyEdgeAttributeImpl(Float.parseFloat(elements[2])));
                    }
                }
                catch(Exception e){
                    System.out.println(line + " malformed");
                }
            }
            reader.close();
            ontology.validate();
            return ontology;
        }
        else{
            System.out.println("Exception!!");
            return null;
        }
    }
    
    public void writeToFolder(COntology ontology, File entryFolder) throws Exception{
        if(entryFolder != null && entryFolder.isDirectory()){
            System.out.println();
            File of = new File(entryFolder.getAbsolutePath() + File.separator + IOTerm.FILE_CONTOLOGY_ENTRY);
            Set<String> childNodes = ontology.getAllNodesWithParents();
            BufferedWriter writer = new BufferedWriter(new FileWriter(of));
            for(String child : childNodes){
                List<String> parentNodes = ontology.getImmediateParents(child);
                for(String parent : parentNodes){
                    Float heightMultiple = ontology.getHeightDifference(parent, child);
                    writer.write(child + UI.tab + parent + UI.tab + heightMultiple + UI.newLine);
                }
            }
            writer.flush();
            writer.close();
        }
        else{
            throw new Exception("The contology output destination folder is either null or not a directory.");
        }
    }
    
}
