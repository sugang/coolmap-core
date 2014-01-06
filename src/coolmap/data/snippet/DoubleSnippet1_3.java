/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.snippet;

import java.text.DecimalFormat;

/**
 *
 * @author gangsu
 */
public class DoubleSnippet1_3 extends SnippetConverter {

    private final DecimalFormat _format = new DecimalFormat("#.###");
    
    @Override
    public String convert(Object obj) {
        if(obj == null){
            return null;
        }
        return _format.format(obj);
    }

    @Override
    public boolean canConvert(Class cls) {
        if(cls != null && Double.class.isAssignableFrom(cls)){
            return true;
        }
        else{
            return false;
        }
    }
    
}
