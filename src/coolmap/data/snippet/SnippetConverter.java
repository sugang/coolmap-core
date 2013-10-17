/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.snippet;

/**
 *
 * @author gangsu
 */
public interface SnippetConverter<T> {
    
    public String convert(T obj);
    public boolean canConvert(Class cls);
}
