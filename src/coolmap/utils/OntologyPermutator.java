/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.utils;

import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.COntologyUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * one way to create ontologies from column combinations
 * probably need the user to select certain levels in order to make the parsing work
 * 
 * generate | 
 * @author sugang
 */
public class OntologyPermutator {
    
    public static void parse(ArrayList<String[]> input, String[] base){
//        COntology ontology = new COntology("Parsed file", "Parsed file according to the input");
        
//        for(String[] s : input){
//            System.out.println(Arrays.toString(s));
//        }
//        System.out.println(Arrays.toString(base));
        
        //first generate all base level
        String[] baseLevels = new String[base.length];
        for(int j=0; j<base.length; j++){
            String label = "";
            for(String[] line : input){
                label += line[j] + "|";
            }
            baseLevels[j] = label.substring(0, label.length()-1);
        }
        
        
        
        //base levels.
//        System.out.println(Arrays.toString(baseLevels));
        
        //Then create membership possibilities
        COntology ontology = new COntology("ParsedOntology", null);
        for(int i=0; i < baseLevels.length; i++){
            ontology.addRelationshipNoUpdateDepth(baseLevels[i], base[i]);
        }
        
        for(String baseTerm : baseLevels){
            addRelationship(baseTerm, ontology);
        }
        
        
        
        
        COntologyUtils.printOntology(ontology);
    }
    
    private static void addRelationship(String term, COntology ontology){
        System.out.println(term);
        if(term.indexOf("|") < 0){
            return; //it's a single term
        }
        else{
            String ele[] = term.split("\\|", -1);
            for(int i=0; i<ele.length; i++){
//                String token = ele[i];
                StringBuilder sb = new StringBuilder();
                for(int j=0; j<ele.length; j++){
                    if(j==i)continue;
                    sb.append(ele[j]);
                    sb.append("|");
                }
                String newString = sb.toString().substring(0, sb.length()-1);
                ontology.addRelationshipNoUpdateDepth(newString, term); //new string is shorter; and it was added as the parent of the current term
                addRelationship(newString, ontology);
            }
        }
        
    }
    
    public static void main(String args[]){
        File f = new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/permute_ontology");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line;
            ArrayList<String[]> data = new ArrayList<>();
            while((line = reader.readLine()) != null){
                data.add(line.split("\\s+"));
            }
            parse(data, data.remove(data.size()-1));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OntologyPermutator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OntologyPermutator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
