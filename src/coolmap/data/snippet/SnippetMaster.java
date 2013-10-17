/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.snippet;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author gangsu
 */
public class SnippetMaster {
    
    public static HashMap<String, SnippetConverter> _converters = new HashMap<String, SnippetConverter>();
    
    public static void addConverter(String name, SnippetConverter converter){
        _converters.put(name, converter);
    }
    
    public static SnippetConverter getConverter(String name){
        return _converters.get(name);
    }
    
    public static void initialize(){
        DoubleSnippet1_3 d13 = new DoubleSnippet1_3();
        addConverter("D13", d13);
    }
    
}
