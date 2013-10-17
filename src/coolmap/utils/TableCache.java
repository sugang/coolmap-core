/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author gangsu
 */
public final class TableCache<T> {
    
    //use unique string
    private LinkedHashMap<String, T> _keyCache = new LinkedHashMap<String, T>();
    //private HashBasedTable<String, String, T> _cache; // = HashBasedTable.create();
    
    private final int _capacity = 9000000;
    
    public TableCache(){
        //_cache = HashBasedTable.create();
    }
    
    public boolean contains(String row, String col){
        //return _cache.contains(row, col);
        return _keyCache.containsKey(row+col);
    }
    
    
    public T get(String row, String col){
        if(_keyCache.containsKey(row+col)){
            return _keyCache.get(row+col);
        }
        else{
            return null;
        }
    }
    
    public void clear(){
        _keyCache.clear();
    }
    
    public synchronized void put(String row, String col, T value){
        if(value == null){
            return;
        }
        if(_keyCache.size() > _capacity){
            _keyCache.clear(); //More work is needed to be done here.
        }
        
        _keyCache.put(row+col, value);
    }
}
