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
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sugang
 */
public class GOMapping {

    public static void main(String args[]) {

        try {

            HashMap<String, String> idToSymbolMap = new HashMap<String, String>();

            File f = new File("/Users/sugang/Dropbox/Research - Dropbox/CoolMap datasets/hgnc_complete_set.processed.txt");

            BufferedReader reader;

            reader = new BufferedReader(new FileReader(f));

            String l;

            while ((l = reader.readLine()) != null) {

                String ele[] = l.split("\\t", -1);

                idToSymbolMap.put(ele[0], ele[1]);
            }

            reader.close();

            reader = new BufferedReader(new FileReader(new File("/Users/sugang/Dropbox/Research - Dropbox/GO/10090")));

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/Users/sugang/Dropbox/Research - Dropbox/GO/10090.genesymbol.txt")));

                while ((l = reader.readLine()) != null) {

                    
                    
                    String[] ele = l.split("\\t", -1);
                    if(idToSymbolMap.get(ele[0]) == null)
                        continue;
                    writer.write(idToSymbolMap.get(ele[0]));
                    writer.write("\t");
                    writer.write(ele[1]);
                    writer.write("\n");
                }

                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            Logger.getLogger(GOMapping.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
