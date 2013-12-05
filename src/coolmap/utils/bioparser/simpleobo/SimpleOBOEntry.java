/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.simpleobo;


import com.google.common.collect.HashMultimap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sugang
 */
public class SimpleOBOEntry {

    public static String ID = "id";
    public static String NAME = "name";
    public static String NAMESPACE = "namespace";
    private String id = null;
    private String name = null;
    private String namespace = null;
    private HashSet<String> parents = new HashSet<>(5);
    private HashMultimap<String, String> otherAttributes = HashMultimap.create();

    public SimpleOBOEntry() {
    }

    public void setName(String nameString) {
        name = nameString;
    }

    public void setID(String idString) {
        id = idString;
    }

    public void setNamespace(String namespaceString) {
        namespace = namespaceString;
    }

    public void addAttribute(String attrName, String attrValue) {
        otherAttributes.put(attrName, attrValue);
    }

    public void addParent(String parentID) {
        parents.add(parentID);
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void print() {
        System.out.println("===");
        System.out.println(id);
        System.out.println(name);
        System.out.println(namespace);
        for (String key : otherAttributes.keySet()) {
            Set<String> values = otherAttributes.get(key);
            for (String value : values) {
                System.out.println("  " + key + ": " + value);
            }
        }
        System.out.println("");
    }
    
    public HashMultimap<String, String> getOtherAttributes(){
        return otherAttributes;
    }
    
    
}
