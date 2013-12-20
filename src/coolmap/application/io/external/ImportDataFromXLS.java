/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.interfaces.ImportData;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.canvas.datarenderer.renderer.impl.NumberToColor;
import coolmap.canvas.sidemaps.impl.ColumnLabels;
import coolmap.canvas.sidemaps.impl.ColumnTree;
import coolmap.canvas.sidemaps.impl.RowLabels;
import coolmap.canvas.sidemaps.impl.RowTree;
import coolmap.data.CoolMapObject;
import coolmap.data.aggregator.impl.DoubleDoubleMean;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrixview.model.VNode;
import coolmap.data.contology.model.COntology;
import coolmap.data.contology.utils.COntologyUtils;
import coolmap.data.snippet.DoubleSnippet1_3;
import coolmap.utils.ColorLabel;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author sugang
 */
public class ImportDataFromXLS implements ImportData {

    private int previewNum = 100;
    private boolean proceed = true;

    private int sheetIndex;
    private int rowStart; //need to recompute this
    private int columnStart;
    private boolean importOntology;

    private final HashSet<CoolMapObject> importedCoolMaps = new HashSet<CoolMapObject>();
    private final HashSet<COntology> importedOntologies = new HashSet<COntology>();

    @Override
    public void importFromFile(File inFile) throws Exception {
                //Ignore the file, choose only a single file
        //I actually don't know the row count
        if (!proceed) {
            throw new Exception("Import from excel was cancelled");
        } else {
            try {
                String fileNameString = inFile.getName().toLowerCase();
                FileInputStream inStream = new FileInputStream(inFile);
                Workbook workbook = null;
                if (fileNameString.endsWith("xls")) {
                    workbook = new HSSFWorkbook(inStream);
                } else if (fileNameString.toLowerCase().endsWith("xlsx")) {
                    workbook = new XSSFWorkbook(inStream);
                }

                Sheet sheet = workbook.getSheetAt(sheetIndex);

                int rowCounter = 0;

                //need to first copy the file over
                ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();

                Iterator<Row> rowIterator = sheet.rowIterator();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

//                    if (rowCounter < rowStart) {
//                        rowCounter++;
//                        //import ontology rows
//                        
//                        continue;
//
//                        //skip first rows
//                    }
                    ArrayList<Object> rowData = new ArrayList<Object>();

                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i);
//                        System.out.print(cell + " ");
//                        now add data
                        try {
                            if (cell == null) {
                                rowData.add(null);
                            } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                rowData.add(null);
                            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                rowData.add(cell.getStringCellValue());
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                rowData.add(cell.getNumericCellValue());
                            } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                                rowData.add(cell.getBooleanCellValue());
                            } else {
                                rowData.add(cell.toString());
                            }
                        } catch (Exception e) {
                            //
                            CMConsole.logError(" error parsing excel cell: " + cell + ", [" + row + "," + i + "]");
                            rowData.add(null);
                        }

                    }

//                    System.out.println("");
                    data.add(rowData);

                }

                System.out.println("Row start:" + rowStart + " Column start:" + columnStart);

                //now I have row data
                int rowCount = data.size() - rowStart - 1;
                int columnCount = data.get(0).size() - columnStart - 1;

                DoubleCMatrix matrix = new DoubleCMatrix(Tools.removeFileExtension(inFile.getName()), rowCount, columnCount);
                String[] rowNames = new String[rowCount];
                String[] columnNames = new String[columnCount];

                for (int i = rowStart; i < data.size(); i++) {
                    ArrayList row = data.get(i);
                    if (i == rowStart) {
                        //first row contains names
                        for (int j = columnStart + 1; j < row.size(); j++) {
                            try {
                                columnNames[j - columnStart - 1] = row.get(j).toString();
                            } catch (Exception e) {
                                columnNames[j - columnStart - 1] = "Untitled " + Tools.randomID();
                            }
                        }
                        continue;
                    }

                    for (int j = columnStart; j < row.size(); j++) {
                        Object cell = row.get(j);
                        if (j == columnStart) {
                            try {
                                rowNames[i - rowStart - 1] = cell.toString();
                            } catch (Exception e) {
                                rowNames[i - rowStart - 1] = "Untitled" + Tools.randomID();
                            }
                        } else {
                            //set values
                            try {
                                Object value = (Double) row.get(j);
                                if (value == null) {
                                    matrix.setValue(i - rowStart - 1, j - columnStart - 1, null);
                                } else if (value instanceof Double) {
                                    matrix.setValue(i - rowStart - 1, j - columnStart - 1, (Double) value);
                                } else {
                                    matrix.setValue(i - rowStart - 1, j - columnStart - 1, Double.NaN);
                                }
                            } catch (Exception e) {
                                matrix.setValue(i - rowStart - 1, j - columnStart - 1, null);
                            }

                        }
                    }//end of iterating columns

                }//end of iterating rows

                System.out.println(Arrays.toString(rowNames));
                System.out.println(Arrays.toString(columnNames));

//                matrix.printMatrix();

                //
                CoolMapObject object = new CoolMapObject();
                object.setName(Tools.removeFileExtension(inFile.getName()));
                object.addBaseCMatrix(matrix);
                ArrayList<VNode> nodes = new ArrayList<VNode>();
                for (Object label : matrix.getRowLabelsAsList()) {
                    nodes.add(new VNode(label.toString()));
                }
                object.insertRowNodes(nodes);

                nodes.clear();
                for (Object label : matrix.getColLabelsAsList()) {
                    nodes.add(new VNode(label.toString()));
                }
                object.insertColumnNodes(nodes);

                object.setAggregator(new DoubleDoubleMean());
                object.setSnippetConverter(new DoubleSnippet1_3());
                object.setViewRenderer(new NumberToColor(), true);

                object.getCoolMapView().addColumnMap(new ColumnLabels(object));
                object.getCoolMapView().addColumnMap(new ColumnTree(object));
                object.getCoolMapView().addRowMap(new RowLabels(object));
                object.getCoolMapView().addRowMap(new RowTree(object));

                importedCoolMaps.clear();
                importedCoolMaps.add(object);
                ////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////
                //
                //let's add COntologies
                if(columnStart > 0){
                    COntology columnOntology = new COntology(Tools.removeFileExtension(inFile.getName()) + " column ontology", null);
                    ArrayList<Object> columnLabels = data.get(rowStart); //these are column labels
                    
                    for(int i=0; i<rowStart; i++){
                        ArrayList ontologyColumn = data.get(i);
                        for(int j=columnStart+1; j<columnLabels.size();j++){
                            Object parent = ontologyColumn.get(j);
                            Object child = columnLabels.get(j);
                            
                            if(parent != null && child != null){
                                columnOntology.addRelationshipNoUpdateDepth(parent.toString(), child.toString());
                            }
                            
                            //Also need to create presets
                        }
                    }
                    
                    columnOntology.validate();
//                    COntologyUtils.printOntology(columnOntology);
                    importedOntologies.add(columnOntology);
                    
//                    need to finish the preset 
                }
                
                if(rowStart > 0){
                    COntology rowOntology = new COntology(Tools.removeFileExtension(inFile.getName()) + " row ontology", null);
                    
                    List rowLabels = Arrays.asList(rowNames);
                    
                    for(int j=0; j < columnStart; j++){
                    
                        for(int i=rowStart+1; i < data.size(); i++){
                            Object parent = data.get(i).get(j);
                            Object child = rowLabels.get(i - rowStart -1);
                            
                            if(parent != null && child != null){
                                rowOntology.addRelationshipNoUpdateDepth(parent.toString(), child.toString());
                            }
                        }
                        
                    }
                    
                    
                    
                    
                    rowOntology.validate();
                    
                    COntologyUtils.printOntology(rowOntology);
                    
                    importedOntologies.add(rowOntology);
                }
                
//                create row and column complex combinatorial ontology (intersections)
                
                
                
                
                
                
                
                
                
                

            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("File error");
            }

        }
    }

    
    
    
    
    
    
    
    
    
    @Override
    public void importFromFiles(File... file) throws Exception {
    }

    @Override
    public String getLabel() {
        return "Numeric Microsoft Excel";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Microsoft Excel", "xls", "xlsx");
    }

    @Override
    public Set<CoolMapObject> getImportedCoolMapObjects() {
//        return null;
        return importedCoolMaps;
    }

    @Override
    public Set<COntology> getImportedCOntology() {
//        return null;
        return importedOntologies;
    }

    @Override
    public void configure(File... file) {
        //need to popup a secondary dialog; this must be done differently

        try {
            File inFile = file[0];
            String fileNameString = inFile.getName().toLowerCase();

            FileInputStream inStream = new FileInputStream(inFile);

            Workbook workbook = null;
            if (fileNameString.endsWith("xls")) {
                workbook = new HSSFWorkbook(inStream);
            } else if (fileNameString.toLowerCase().endsWith("xlsx")) {
                workbook = new XSSFWorkbook(inStream);
            }

            int sheetCount = workbook.getNumberOfSheets();

            String[] sheetNames = new String[sheetCount];

            for (int i = 0; i < sheetNames.length; i++) {
                String sheetName = workbook.getSheetAt(i).getSheetName();

                sheetNames[i] = sheetName == null || sheetName.length() == 0 ? "Untitled" : sheetName;
            }

            //get the sheet names
            final JComboBox sheetNameCombo = new JComboBox(sheetNames);

            System.out.println(Arrays.toString(sheetNames));

            //also need to get the top 100 rows + all columns
            DefaultTableModel tableModels[] = new DefaultTableModel[sheetCount];
            Cell cell;
            Row row;

            ArrayList<ArrayList<ArrayList<Object>>> previewData = new ArrayList();
            for (int si = 0; si < sheetCount; si++) {

                //The row iterator automatically skips the blank rows
                //so only need to figure out how many rows to skip; which is nice
                //columns, not the same though
                Sheet sheet = workbook.getSheetAt(si);
                Iterator<Row> rowIterator = sheet.rowIterator();

                int ri = 0;

                ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();

                while (rowIterator.hasNext()) {

                    row = rowIterator.next();
                    ArrayList<Object> rowData = new ArrayList<>();

                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        cell = row.getCell(j);

                        try {
                            if (cell == null) {
                                rowData.add(null);
                            } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                rowData.add(null);
                            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                rowData.add(cell.getStringCellValue());
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                rowData.add(cell.getNumericCellValue());
                            } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                                rowData.add(cell.getBooleanCellValue());
                            } else {
                                rowData.add(cell.toString());
                            }
                        } catch (Exception e) {
                            //
                            CMConsole.logError(" error parsing excel cell: " + cell + ", [" + ri + "," + j + "]");
                            rowData.add(null);
                        }

                    }

                    data.add(rowData);

                    ri++;

                    if (ri == previewNum) {
                        break;
                    }
                } //end 

//                System.out.println(data);
//                now the data is the data
//                ugh-> this is not a generic importer
                previewData.add(data);

            }//end of iterating all sheets

            ConfigPanel configPanel = new ConfigPanel(sheetNames, previewData);

            //int returnVal = JOptionPane.showMessageDialog(CoolMapMaster.getCMainFrame(), configPanel);
            int returnVal = JOptionPane.showConfirmDialog(
                    CoolMapMaster.getCMainFrame(), configPanel, "Import from Excel: " + inFile.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

            if (returnVal == JOptionPane.OK_OPTION) {
                proceed = true;

                inStream.close();
                workbook = null;

                //set parameters
                inFile = file[0];
                importOntology = configPanel.getImportOntology();
                rowStart = configPanel.getRowStart();
                columnStart = configPanel.getColumnStart();
                sheetIndex = configPanel.getSheetIndex();

            } else {
                //mark operation cancelled
                proceed = false;
            }

        } catch (Exception e) {
            CMConsole.logError(" failed to import numeric matrix data from: " + file);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onlyImportFromSingleFile() {
        return true; //To change body of generated methods, choose Tools | Templates.
    }

    private class ConfigPanel extends JPanel {

        private final String[] names;
        private final ArrayList<DefaultTableModel> models;
        private JTable table = new JTable();
        private JSpinner rowStartSpinner, columnStartSpinner;
        private JCheckBox ontologyImport = new JCheckBox();
        private final JComboBox sheetCombo;

        public int getRowStart() {
            return (Integer) rowStartSpinner.getValue() - 1;
        }

        public int getColumnStart() {
            return (Integer) columnStartSpinner.getValue() - 1;
        }

        public boolean getImportOntology() {
            return ontologyImport.isSelected();
        }

        public int getSheetIndex() {
            return sheetCombo.getSelectedIndex();
        }

        private ConfigPanel(String[] tableNames, ArrayList<ArrayList<ArrayList<Object>>> data) {

            names = tableNames;

            models = new ArrayList<>();

            for (ArrayList<ArrayList<Object>> tableData : data) {

                Object[][] rawData = new Object[tableData.size()][tableData.get(0).size()];

                for (int i = 0; i < tableData.size(); i++) {
                    ArrayList row = tableData.get(i);

                    for (int j = 0; j < row.size(); j++) {
                        try {
                            rawData[i][j] = row.get(j);
                        } catch (Exception e) {
                            System.err.println("parsing error");
                        }

                    }
                }

                DefaultTableModel tableModel = new DefaultTableModel(rawData, new Object[rawData[0].length]) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; //To change body of generated methods, choose Tools | Templates.;
                    }

                };
                models.add(tableModel);

            }

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            table.getTableHeader().setReorderingAllowed(false);

            setLayout(new BorderLayout());

            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);

            add(toolBar, BorderLayout.NORTH);

            JPanel panel = new JPanel();

            panel.setLayout(new BorderLayout());

            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            panel.setBorder(BorderFactory.createTitledBorder("Preview"));

            add(panel, BorderLayout.CENTER);

            toolBar.add(new JLabel("Choose sheet: "));

            sheetCombo = new JComboBox(tableNames);
            toolBar.add(sheetCombo);

            sheetCombo.setSelectedIndex(0);

            toolBar.addSeparator();

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    table.setModel(models.get(0));
                }
            });

            sheetCombo.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {

                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                table.setModel(models.get(sheetCombo.getSelectedIndex()));
                                //also cleer other data
                            }
                        });
                    }

                }
            });

            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JComponent component = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.

                    component.setBackground(UI.colorWhite);

                    if (isSelected || row + 1 < ((Integer) rowStartSpinner.getValue()).intValue() && column + 1 < ((Integer) columnStartSpinner.getValue()).intValue()) {

                        return component;
                    }

                    if (row + 1 < ((Integer) rowStartSpinner.getValue()).intValue() && column + 1 >= ((Integer) columnStartSpinner.getValue()).intValue()
                            || row + 1 >= ((Integer) rowStartSpinner.getValue()).intValue() && column + 1 < ((Integer) columnStartSpinner.getValue()).intValue()) {
                        //ontology cells
                        component.setBackground(UI.colorRedWarning);

                        return component;
                    }

                    if (value instanceof Double) {
                        component.setBackground(UI.colorLightBlue0);
                    } else {
                        component.setBackground(UI.colorWhite);
                    }

                    if (row + 1 == ((Integer) rowStartSpinner.getValue()).intValue() || column + 1 == ((Integer) columnStartSpinner.getValue()).intValue()) {
                        component.setBackground(UI.colorLightGreen0);
                    }

                    return component;
                }

            });

            rowStartSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
            columnStartSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

            rowStartSpinner.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    table.repaint();
                }
            });

            columnStartSpinner.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    table.repaint();
                }
            });

            JLabel label = new JLabel("Row start");
            label.setToolTipText("Index of the header row");

            toolBar.add(label);
            toolBar.add(rowStartSpinner);
            rowStartSpinner.setToolTipText("<html>Specifies the header row (row labels). <br/> Any rows before this row can be imported as ontological terms.</html>");
            toolBar.addSeparator();

            label = new JLabel("Column start");
            label.setToolTipText("Index of the header column");
            toolBar.add(label);
            toolBar.add(columnStartSpinner);
            columnStartSpinner.setToolTipText("<html>Specifies the header column (column labels). <br/> Any columns before this column can be imported as ontological terms.</html>");

            toolBar.addSeparator();

            label = new JLabel("Ontology");
            label.setToolTipText("Import rows and columns before starting locations as ontologies");
//            toolBar.add(label);

            ontologyImport.setSelected(false);
//            toolBar.add(ontologyImport);

            JToolBar bottomBar = new JToolBar();

            this.add(bottomBar, BorderLayout.SOUTH);
            bottomBar.setFloatable(false);

            ColorLabel cLabel = new ColorLabel(UI.colorLightBlue0);
            cLabel.setText("Numeric Data");

            bottomBar.add(cLabel);

            cLabel = new ColorLabel(UI.colorLightGreen0);
            cLabel.setText("Headers");
            bottomBar.add(cLabel);

            cLabel = new ColorLabel(UI.colorRedWarning);
            cLabel.setText("Ontologies");
            bottomBar.add(cLabel);

            cLabel = new ColorLabel(Color.WHITE);
            cLabel.setText("Etc");

            bottomBar.add(cLabel);

        }//end of constructor

    }

}
