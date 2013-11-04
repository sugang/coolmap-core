/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.contology.model;

import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sugang
 */
public class COntologyAttribute {

    private final HashBasedTable<String, String, Object> _nodeAttrTable = HashBasedTable.create();
    private final HashMap<String, Class> _nodeAttrClassMap = new HashMap<String, Class>();

    public void addAttribute(String nodeString, String attributeString, Object attribute) {
        _nodeAttrTable.put(nodeString, attributeString, attribute);
    }

    public Object getAttribute(String nodeString, String attrString) {
        return _nodeAttrTable.get(nodeString, attrString);
    }

    public Set<String> getNodeStrings() {
        return new HashSet(_nodeAttrTable.rowKeySet());
    }

    public Set<String> getAttrbuteNames() {
        return new HashSet(_nodeAttrTable.columnKeySet());
    }

    public List<String> getNodeStringSorted() {
        ArrayList<String> list = new ArrayList<>(_nodeAttrTable.rowKeySet());
        Collections.sort(list);
        return list;
    }

    public ArrayList<String> getAttributeNamesSorted() {
        ArrayList<String> list = new ArrayList<>(_nodeAttrTable.columnKeySet());
        Collections.sort(list);
        return list;
    }
    
    public boolean containsNode(String nodeString){
        return _nodeAttrTable.rowKeySet().contains(nodeString);
    }
    
    public boolean containsAttribute(String attributeString){
        return _nodeAttrTable.columnKeySet().contains(attributeString);
    }
    
    public void clear(){
        _nodeAttrTable.clear();
        _nodeAttrClassMap.clear();
    }
    
    public void setAttributeClass(String attrString, Class cls){
        _nodeAttrClassMap.put(attrString, cls);
    }
    
    public Class getAttributeClass(String attrString){
        return _nodeAttrClassMap.get(attrString);
    }
    

}
