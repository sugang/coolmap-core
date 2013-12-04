/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.data.cmatrix.model;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import coolmap.utils.network.Edge;
import coolmap.utils.network.LNetwork;
import coolmap.utils.network.Node;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * numeric matrices
 *
 * @author gangsu
 */
public class NetworkCMatrix extends CMatrix<Double> {

    private SparseDoubleMatrix2D _edgeMatrix;
    private final ArrayList<String> _rowLabels = new ArrayList<String>();
    private final ArrayList<String> _colLabels = new ArrayList<String>();
    private final HashMap<String, Integer> _rowLabelToIndexMap = new HashMap<String, Integer>();
    private final HashMap<String, Integer> _colLabelToIndexMap = new HashMap<String, Integer>();
    private String _description = null;

    private NetworkCMatrix() {
        super(Double.class);
        _edgeMatrix = null;
        //things will be breaking, but it's ok
    }

    public NetworkCMatrix(int numRow, int numCol) {
        super(Double.class);
        _edgeMatrix = new SparseDoubleMatrix2D(numRow, numCol);
        _initDefaultLabels();
    }

    private void _initDefaultLabels() {
        for (int i = 0; i < getNumRows(); i++) {
            //setRowLabel(i, "R" + i);
            _rowLabels.add("RN" + i);
            _rowLabelToIndexMap.put("RN" + i, i);
        }
        for (int j = 0; j < getNumColumns(); j++) {
            //setColLabel(j, "C" + j);
            _colLabels.add("RN" + j);
            _colLabelToIndexMap.put("RN" + j, j);
        }
        
        
    }

    @Override
    public void destroy() {
        _edgeMatrix = null;
    }

    @Override
    public boolean isDestroyed() {
        if (_edgeMatrix == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Double getValue(int row, int col) {
        try {
            return _edgeMatrix.getQuick(row, col);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer getIndexOfRowName(String label) {
        return _rowLabelToIndexMap.get(label);
    }

    @Override
    public Integer getIndexOfColName(String label) {
        return _colLabelToIndexMap.get(label);
    }

    @Override
    public Integer getNumRows() {
        try {
            return _edgeMatrix.rows();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Integer getNumColumns() {
        try {
            return _edgeMatrix.columns();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setValue(int row, int col, Double value) {
        try {
            _edgeMatrix.setQuick(row, col, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    @Override
    public List<String> getRowLabelsAsList(Integer[] rowIndices) {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(_rowLabels);
        return list;
    }

    @Override
    public List<String> getColLabelsAsList(Integer[] colIndices) {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(_colLabels);
        return list;
    }

    @Override
    public void setRowLabel(int index, String label) {
        if (index < 0 || index >= getNumRows()) {
            return;
        } else {
            _rowLabels.set(index, label);
            _rowLabelToIndexMap.remove(_rowLabels.get(index));
            _rowLabelToIndexMap.put(label, index);
        }
    }

    @Override
    public void setColLabel(int index, String label) {
        if (index < 0 || index >= getNumColumns()) {
            return;
        } else {
            _colLabels.set(index, label);
            _colLabelToIndexMap.remove(_colLabels.get(index));
            _colLabelToIndexMap.put(label, index);
        }
    }

    @Override
    public String getColLabel(int index) {
        if (index < 0 || index >= getNumColumns()) {
            return null;
        } else {
            return _colLabels.get(index);
        }
    }

    @Override
    public String getRowLabel(int index) {
        if (index < 0 || index >= getNumRows()) {
            return null;
        } else {
            return _rowLabels.get(index);
        }
    }

    public void shrinkToSize() {
        try {
            _edgeMatrix.trimToSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get a network from rows or columns
     *
     * @param rowIndices
     * @param colIndices
     * @return
     */
    public LNetwork getLNetwork(Collection<Integer> rowIndices, Collection<Integer> colIndices) {
        HashSet<Integer> indices = new HashSet<Integer>();
        indices.addAll(rowIndices);
        indices.addAll(colIndices);
        double v;
        Edge e;
        LNetwork network = new LNetwork();
        HashMap<String, Node> nodesMap = new HashMap<String, Node>();
//        HashMap<String, Node> colNodes = new HashMap<String, Node>();

        String label;
        for (Integer i : indices) {
            label = getRowLabel(i);
            Node node = new Node(label, 1.0);
            nodesMap.put(label, node);
            network.addNode(node);
        }
        
       

//        for (Integer i : indices) {
//            Node firstNode = nodesMap.get(getRowLabel(i));
//            for (Integer j : indices) {
//                Node secondNode = nodesMap.get(getRowLabel(j));
//                v = _edgeMatrix.getQuick(i, j);
//                if (v > 0) {
//                    //an edge
//                    network.addEdge(new Edge(new Node(getRowLabel(i), 1), new Node(getColLabel(j), 1), 1));
//                }
//            }
//        }
        Integer[] indicesArray = new Integer[indices.size()];
        indices.toArray(indicesArray);

        
        
        for (int i = 0; i < indices.size(); i++) {
            Node firstNode = nodesMap.get(getRowLabel(indicesArray[i]));
            for (int j = i + 1; j < indices.size(); j++) {
                Node secondNode = nodesMap.get(getRowLabel(indicesArray[j]));
                v = _edgeMatrix.getQuick(i, j);
                if (v > 0) {
                    //an edge
                    network.addEdge(new Edge(firstNode, secondNode, 1));
                }
            }

        }


        return network;
    }

    public static void main(String args[]) {
        NetworkCMatrix mx = new NetworkCMatrix(20, 20);

        for (int i = 0; i < mx.getNumRows(); i++) {
            for (int j = 0; j < mx.getNumColumns(); j++) {
                if (Math.random() > 0.7) {
                    mx.setValue(i, j, 1.0);
                }
            }
        }

        mx.printMatrix();
        
//        System.out.println("Index map:" + mx.getIndexOfColName("RN1"));

        ArrayList<Integer> rowIndices = new ArrayList<Integer>();
        ArrayList<Integer> colIndices = new ArrayList<Integer>();

        for (int i = 0; i < mx.getNumRows(); i++) {
            rowIndices.add(i);
        }

        for (int i = 0; i < mx.getNumColumns(); i++) {
            colIndices.add(i);
        }

        LNetwork network = mx.getLNetwork(rowIndices, colIndices);

//        System.out.println(network.getNodeCount());
//        System.out.println(network.getEdgeCount());

        network.recomputeLayout(50);
        BufferedImage img = network.drawNetwork(80, 60);

        //
        try {
            ImageIO.write(img, "png", new File("/Users/gangsu/Desktop/network.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
