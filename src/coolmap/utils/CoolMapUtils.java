/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;

import java.util.HashMap;

/**
 *
 * @author gangsu
 */
public class CoolMapUtils {
    private final static HashMap<Class<?>, String> _snippetMap = new HashMap<Class<?>, String>();
    
    
    public static String getSnippet(Object object){
        
        
        return _snippetMap.get(object.getClass());
    }

}
