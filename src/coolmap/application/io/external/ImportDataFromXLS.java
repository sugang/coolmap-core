/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import cern.colt.Arrays;
import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.interfaces.ImportData;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.CoolMapObject;
import coolmap.data.contology.model.COntology;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
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

    private File inFile;
    private int sheetIndex;
    private int rowStart;
    private int columnStart;
    private boolean importOntology;

    @Override
    public void importFromFile(File... file) throws Exception {
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
                
                Iterator<Row> rowIterator = sheet.rowIterator();
                
                while(rowIterator.hasNext()){
                    Row row = rowIterator.next();
                    
                    
                    if(rowCounter < rowStart){
                        rowCounter++;
                        continue;
                        
                        //skip first rows
                    }
                    
                    
                    
                    
                    for(int i=columnStart; i<row.getLastCellNum(); i++){
                        Cell cell = row.getCell(i);
                        System.out.print(cell + " ");
                    }
                    
                    System.out.println("");
                    
                }
                
                
                
                
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("File error");
            }

        }

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
        return null;
    }

    @Override
    public Set<COntology> getImportedCOntology() {
        return null;
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
                    CoolMapMaster.getCMainFrame(), configPanel, "Import from Excel", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

            if (returnVal == JOptionPane.OK_OPTION) {
                proceed = true;

                inStream.close();
                workbook = null;

                //set parameters
                this.inFile = file[0];
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

                    if (isSelected || row + 1 < ((Integer) rowStartSpinner.getValue()).intValue() || column + 1 < ((Integer) columnStartSpinner.getValue()).intValue()) {
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

            toolBar.add(new JLabel("Row start"));
            toolBar.add(rowStartSpinner);
            toolBar.addSeparator();

            toolBar.add(new JLabel("Column start"));
            toolBar.add(columnStartSpinner);

            toolBar.addSeparator();

            JLabel label = new JLabel("Ontology");
            label.setToolTipText("Import rows and columns before starting locations as ontologies");
            toolBar.add(label);

            ontologyImport.setSelected(false);
            toolBar.add(ontologyImport);

        }//end of constructor

    }

}
