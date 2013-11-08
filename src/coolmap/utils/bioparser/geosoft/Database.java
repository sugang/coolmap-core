/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.geosoft;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author sugang
 */
public class Database {
    private String databaseID;
    private final LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
    
    private Database(){
        databaseID = null;
    }
    
    public Database(String databaseID){
        this.databaseID = databaseID;
    }
    
    public void addAttribute(String key, String attribute){
        attributes.put(key, attribute);
    }

    @Override
    public String toString() {
        return "Database:" + databaseID;
    }
    
    public void printDetails(){
        System.out.println("Database properties:");
        for( Entry<String, String> entry : attributes.entrySet()){
            System.out.println(entry.getKey() + " ==> " + entry.getValue());
        }
        System.out.println();
    }
    
    
}
