/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.geosoft;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.LinkedHashMultimap;
import java.util.Arrays;

/**
 *
 * @author sugang
 */
public class ArrayTable {

    private Integer rowCount;
    private Integer columnCount;
    private LinkedHashMultimap<String, String> symbolToID;
    private Double[][] data;
    private String[] rowNames;
    private String[] columnNames;
    //The attributes can also be loaded as a two-way table
    private HashBasedTable<String, String, Object> attributes;

    private ArrayTable() {
        this(null, null);
    }

    @Override
    public String toString(){
        return "Rows: " + rowCount + " Columns:" + columnCount + " Attributes?" + (attributes.isEmpty()?"No":"Yes");
    }
    
    public ArrayTable(Integer rowCount, Integer columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        symbolToID = LinkedHashMultimap.create();
        data = new Double[rowCount][columnCount];
        rowNames = new String[rowCount];
        columnNames = new String[columnCount];
        attributes = HashBasedTable.create();
    }

    ;
    
    public void addMapping(String geneSymbol, String geneID) {
        symbolToID.put(geneSymbol, geneID);
    }

    public void setValue(int row, int column, Double value) {
        data[row][column] = value;
    }

    public void setColumnName(int column, String name) {
        columnNames[column] = name;
    }

    public void setRowNames(int row, String name) {
        rowNames[row] = name;
    }

    public void printData() {
        System.out.println("DATA row:" + rowCount + " column:" + columnCount);
        System.out.print("R/C\t");
        for (String column : columnNames) {
            System.out.print("\t" + column);
        }
        System.out.println();


        for (int i = 0; i < rowCount; i++) {

            System.out.print(rowNames[i]);

            for (int j = 0; j < columnCount; j++) {
                System.out.print("\t" + data[i][j]);
            }
            System.out.println();
        }
    }

    public void printMapping() {
        for (String key : symbolToID.keySet()) {
            System.out.println(key + "->" + Arrays.toString(symbolToID.get(key).toArray()));
        }
    }

    public void printAttribute() {
        Object[] attributeColumnNames = attributes.columnKeySet().toArray();
        Object[] attributeRowNames = attributes.rowKeySet().toArray();

        System.out.println("Attribute row:" + attributeRowNames.length + " column:" + attributeColumnNames.length);
        System.out.print("R/C");
        for (Object column : attributeColumnNames) {
            System.out.print("\t" + column);
        }

        System.out.println();

//        for (int i = 0; i < attributeRowNames.length; i++) {
//
//            System.out.print(attributeRowNames[i]);
//
//            for (int j = 0; j < attributeColumnNames.length; j++) {
//                System.out.print("\t" + );
//            }
//            System.out.println();
//        }
        for(Object rLabel : attributeRowNames){
            System.out.print(rLabel);
            for(Object cLabel : attributeColumnNames){
                System.out.print("\t" + attributes.get(rLabel.toString(), cLabel.toString()));
            }
            System.out.println();
        }

    }

    public void addAttribute(String probeName, String attributeName, Object value) {
        if (probeName != null && probeName.length() > 0
                && attributeName != null && attributeName.length() > 0
                && value != null) {
            attributes.put(probeName, attributeName, value);
        }

    }
}
