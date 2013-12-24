/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package temp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *
 * @author sugang
 */
public class GOExtractor {
    
    public static void main(String[] args) throws Exception{
        
        File f = new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/hgnc_complete_set.txt.txt");
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String l;
        
        File of = new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/hgnc_complete_set.processed.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(of));
        
        
        while( (l = reader.readLine()) != null){
            String ele[] = l.split("\\t", -1);
            try{
                ele[0] = ele[0].replaceAll("HGNC\\:", "");
                writer.write(ele[0].trim());
                writer.write("\t");
                writer.write(ele[1].trim());
                writer.write("\t");
                writer.write(ele[2].trim());
                writer.write("\n");
                
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
        writer.flush();
        writer.close();
    }
}
