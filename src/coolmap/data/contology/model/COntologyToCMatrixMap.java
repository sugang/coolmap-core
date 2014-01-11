/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * A map from ontology to a matrix's base row/columns
 */
package coolmap.data.contology.model;

import coolmap.data.cmatrix.model.CMatrix;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author gangsu
 */
public class COntologyToCMatrixMap {

    private final COntology _cOntology;
    private final ConcurrentHashMap<ItemKey, HashMap<String, Integer[]>> _baseIndexMaps = new ConcurrentHashMap<ItemKey, HashMap<String, Integer[]>>();
    private final ConcurrentHashMap<String, Integer[]> _tempBaseMap = new ConcurrentHashMap<String, Integer[]>();

    public void clear(){
        _baseIndexMaps.clear();
        _tempBaseMap.clear();
    }
    
    private COntologyToCMatrixMap() {
        _cOntology = null;
    }

    public COntologyToCMatrixMap(COntology ontology) {
        _cOntology = ontology;
    }

    /**
     * whether a ontology to matrix mapping has been established already
     *
     * @param matrix
     * @param direction
     * @return
     */
    private boolean _hasBaseIndexMap(CMatrix matrix, Integer direction) {
        if (matrix == null || direction == null) {
            return false;
        } else {
            return _baseIndexMaps.containsKey(new ItemKey(matrix, direction));
        }
    }

    /**
     * This method can't be called multiple times simultaneously, but it has a
     * lazy initalization
     *
     * @param matrix
     * @param direction
     * @param nodeName
     * @return
     */
    public synchronized Integer[] getBaseIndex(CMatrix matrix, Integer direction, String nodeName) {
        if (matrix == null || direction == null || (direction != COntology.ROW && direction != COntology.COLUMN)) {
            return null;
        }

        if (!_hasBaseIndexMap(matrix, direction)) {
            //lazy initialization
            _buildBaseIndexMap(matrix, direction);
        }

        //if after initalization there's still a problem, then something is wrong.
        HashMap<String, Integer[]> mapItem = _baseIndexMaps.get(new ItemKey(matrix, direction));
        if (mapItem != null) {
            return mapItem.get(nodeName);
        } else {
            return null;
        }
    }
    
    //force rebuilding the base index map
    public synchronized void rebuildBaseIndexMap(CMatrix matrix, Integer direction){
        _baseIndexMaps.remove(new ItemKey(matrix, direction));
        _buildBaseIndexMap(matrix, direction);
    }
    

    private synchronized void _buildBaseIndexMap(CMatrix matrix, Integer direction) {
        
        if (matrix == null || _cOntology == null) {
            //change stuff here later on
            return;
        }

        HashMap<String, Integer[]> baseMap;
        if(_hasBaseIndexMap(matrix, direction)){
            baseMap = _getBaseIndexMap(matrix, direction);
        }
        else{
            baseMap = _addEmptyBaseIndexMap(matrix, direction);
        }

        baseMap.clear(); //rebuild.
        _tempBaseMap.clear();//initialize
        
        Set<String> nodes = _cOntology.getAllNodes();
        
        for(String node : nodes){
            if(_tempBaseMap.containsKey(node)){
                continue;//already done
            }
            else{
                _buildBaseIndexMapForNode(node, matrix, direction);
            }
        }

        baseMap.putAll(_tempBaseMap);
        _tempBaseMap.clear();
    }
    
    private void _buildBaseIndexMapForNode(String node, CMatrix matrix, Integer direction){
        //Multiple checks to ensure program integrity
        if (node == null || matrix == null || direction == null || (direction != COntology.ROW && direction != COntology.COLUMN)){
            return;
        }
        
        List<String> childNodes = _cOntology.getImmediateChildren(node);
        if(childNodes == null){
            //debugging purposes
            if(direction == COntology.ROW && matrix.getIndexOfRowName(node)!=null || direction == COntology.COLUMN && matrix.getIndexOfColName(node)!=null){
                //node is a base node. skip
                return;
            }
            else{
                //node is an orphan. orphans are useless for a tree - should not be included in an ontology
                return;
            }
        }else{
            //It's a hashset. Therefore the order of adding is 
            //It will be the order that it is added
            LinkedHashSet<Integer> baseIndicesSet = new LinkedHashSet<Integer>();
            
            for (String childNode : childNodes) {
                Integer[] baseIndicesArray = _tempBaseMap.get(childNode);
                Integer rowIndex = matrix.getIndexOfRowName(childNode);
                Integer colIndex = matrix.getIndexOfColName(childNode);
                

                if (baseIndicesArray != null) {
                    baseIndicesSet.addAll(Arrays.asList(baseIndicesArray));//Which is more ... efficient?
                } else if (direction == COntology.ROW && rowIndex != null) {
                    baseIndicesSet.add(rowIndex);
                } else if (direction == COntology.COLUMN && colIndex != null) {
                    baseIndicesSet.add(colIndex);
                } else {
                    _buildBaseIndexMapForNode(childNode, matrix, direction);
                    baseIndicesArray = _tempBaseMap.get(childNode);
                    if (baseIndicesArray != null) {
                        baseIndicesSet.addAll(Arrays.asList(baseIndicesArray));
                    }
                }
            }//end of looping through all child nodes

            if (!baseIndicesSet.isEmpty()) {
                Integer[] indices = new Integer[baseIndicesSet.size()];
                baseIndicesSet.toArray(indices);
                _tempBaseMap.put(node, indices);
            } else {
                //orphan nodes, no concern
            }
            
        }
    }
    

    private HashMap<String, Integer[]> _addEmptyBaseIndexMap(CMatrix matrix, Integer direction) {
        HashMap<String, Integer[]> map = new HashMap<String, Integer[]>();
        _baseIndexMaps.put(new ItemKey(matrix, direction), map);
        return map;
    }

    private HashMap<String, Integer[]> _getBaseIndexMap(CMatrix matrix, Integer direction) {
        return _baseIndexMaps.get(new ItemKey(matrix, direction));
    }

    /**
     * this class is used to establish an equal, given an id of a matrix and its
     * direction
     */
    private class ItemKey {

        private final String _matrixID;
        private final Integer _direction;
        private final int _hashcode;

        public ItemKey(CMatrix matrix, Integer direction) {
            _matrixID = matrix == null ? null : matrix.getID();
            _direction = direction;
            _hashcode = Arrays.hashCode(new Object[]{_matrixID, _direction});
        }

        /**
         * Item key
         */
        private ItemKey() {
            _matrixID = null;
            _direction = null;
            _hashcode = Arrays.hashCode(new Object[]{_matrixID, _direction});
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ItemKey other = (ItemKey) obj;
//            if (this.ontology != other.ontology && (this.ontology == null || !this.ontology.equals(other.ontology))) {
//                return false;
//            }
            if (_matrixID != other._matrixID && (_matrixID == null || !_matrixID.equals(other._matrixID))) {
                return false;
            }

            if (_direction != other._direction && (_direction == null || !_direction.equals(other._direction))) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {

            return _hashcode;
//            Arrays.hashCode(os);
//            int hash = 3;
//            hash = 71 * hash + (_matrixID != null ? _matrixID.hashCode() : 0);
//            hash = 71 * hash + (_direction != null ? _direction.hashCode() : 0);
//            return hash;
        }
    }
}
