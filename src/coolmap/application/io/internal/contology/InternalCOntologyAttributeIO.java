/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.internal.contology;

import com.google.common.collect.Table;
import coolmap.data.contology.model.COntology;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Set;

/**
 *
 * @author sugang
 */
public class InternalCOntologyAttributeIO {
    
    public static void dumpData(BufferedWriter writer) throws Exception{
        Set<Table.Cell> attributes = COntology.getAllAttributes();
        for(Table.Cell cell : attributes){
            writer.write(cell.getColumnKey().toString());
            writer.write("\t");
            writer.write(cell.getRowKey().toString());
            writer.write("\t");
            writer.write(cell.getValue().toString());
            writer.write("\n");
        }
        writer.flush();
        writer.close();
    }
    
    public static void loadData(BufferedReader reader) throws Exception{
        String line;
        while( (line = reader.readLine()) != null){
            String[] ele = line.split("\\t");
            if(ele.length < 1){
                throw new Exception("Malformed ontology attribute file");
            }
            COntology.setAttribute(ele[0], ele[1], ele[2]);
         }
    }
}
