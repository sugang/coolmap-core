/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external.deprecated;

import coolmap.data.CoolMapObject;
import java.io.InputStream;

/**
 *
 * @author gangsu
 */
public interface CoolMapImporter {
    
    public CoolMapObject importFromStream(InputStream stream, Object... params);
    public String getDisplayName();
    public String getDescription();
}
