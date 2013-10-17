/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.filter;

import coolmap.data.CoolMapObject;
import java.util.HashSet;
import javax.swing.JComponent;

/**
 *
 * @author gangsu
 */
public abstract class ViewFilter<VIEW> implements Filter<VIEW> {

    private String _description;
    private String _name;

    public void setDescription(String des) {
        _description = des;
    }

    public String getDescrpition() {
        return _description;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    @Override
    public abstract boolean canPass(CoolMapObject<?, VIEW> data, int row, int col);

    @Override
    public abstract boolean canFilter(Class<?> objectClass);

    public JComponent getConfigUI() {
        return null;
    }
    private CoolMapObject _coolMapObject;

    public void setParentObject(CoolMapObject object) {
        _coolMapObject = object;
    }

    public CoolMapObject getParentObject() {
        return _coolMapObject;
    }
    private HashSet<ViewFilterUpdatedListener> _viewFilterUpdatedListeners = new HashSet<ViewFilterUpdatedListener>();

    public void addViewFilterUpdatedListener(ViewFilterUpdatedListener lis) {
        if (lis != null) {
            _viewFilterUpdatedListeners.add(lis);
        }
    }

    public void notifyViewFilterUpdated() {
        for (ViewFilterUpdatedListener lis : _viewFilterUpdatedListeners) {
            lis.filterUpdated(this, _coolMapObject);
        }
    }

    public void destroy() {
        _viewFilterUpdatedListeners.clear();
        _coolMapObject = null;
    }

    public String toString() {
        return getName();
    }
}
