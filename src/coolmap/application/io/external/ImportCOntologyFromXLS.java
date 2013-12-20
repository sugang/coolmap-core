/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.application.CoolMapMaster;
import coolmap.application.io.external.interfaces.ImportCOntology;
import coolmap.application.widget.impl.console.CMConsole;
import coolmap.data.contology.model.COntology;
import coolmap.utils.ColorLabel;
import coolmap.utils.Tools;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
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
public class ImportCOntologyFromXLS implements ImportCOntology {

    HashSet<COntology> ontologies = new HashSet<COntology>();
    private int previewNum = 100;

    private static int SIF = 0;
    private static int GMT = 1;
    private int rowStart;

    boolean proceed = true;
    private int columnStart;
    private int sheetIndex;
    private int formatIndex;

    @Override
    public void importFromFiles(File... file) throws Exception {
    }

    private void importSIF(Sheet sheet, COntology ontology) throws Exception {

        System.out.println("rowStart + columnStart:" + rowStart + " " + columnStart);

        int rowCounter = 0;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (rowCounter < rowStart) {
                rowCounter++;
                continue;
            }

            if (columnStart + 2 > row.getLastCellNum()) {
                continue;
            }

            Cell parentCell = row.getCell(columnStart);
            Cell childCell = row.getCell(columnStart + 1);

            System.out.println(parentCell + " " + childCell);

            if (parentCell == null || childCell == null || parentCell.toString().trim().length() == 0 || childCell.toString().trim().length() == 0) {
                continue;
            }

            ontology.addRelationshipNoUpdateDepth(parentCell.toString().trim(), childCell.toString().trim());
            rowCounter++;
        }
    }

    private void importGMT(Sheet sheet, COntology ontology) throws Exception {
        int rowCounter = 0;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (rowCounter < rowStart) {
                rowCounter++;
                continue;
            }

            if (columnStart + 2 > row.getLastCellNum()) {
                continue;
            }

            Cell parentCell = row.getCell(columnStart);
            Cell descriptionCell = row.getCell(columnStart + 1);

            if (descriptionCell != null && descriptionCell.toString().trim().length() != 0 && parentCell == null && parentCell.toString().trim().length() != 0) {
                COntology.setAttribute(parentCell.toString().trim(), "gmt.Description", descriptionCell.toString().trim());
            }

            if (parentCell == null || parentCell.toString().trim().length() == 0) {
                continue;
            }

            for (int j = columnStart + 2; j < row.getLastCellNum(); j++) {
                Cell childCell = row.getCell(j);
                if (childCell == null || childCell.toString().trim().length() == 0) {
                    continue;
                }
                ontology.addRelationshipNoUpdateDepth(parentCell.toString().trim(), childCell.toString().trim());
            }

            rowCounter++;
        }

    }

    @Override
    public void importFromFile(File inFile) throws Exception {
        if (!proceed) {
            throw new Exception("Import from excel was cancelled");
        }

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

            COntology ontology = new COntology(Tools.removeFileExtension(fileNameString), null);

            if (formatIndex == SIF) {
                importSIF(sheet, ontology);
            } else if (formatIndex == GMT) {
                importGMT(sheet, ontology);
            }

            ontology.validate();
            ontologies.add(ontology);
//            COntologyUtils.printOntology(ontology);

        } catch (Exception e) {
            throw new Exception("File error");
        }
    }

    @Override
    public String getLabel() {
        return "Microsoft Excel (xls, xlsx)";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("Microsoft Excel", "xls", "xlsx");
    }

    @Override
    public Set<COntology> getImportedCOntology() {
        return ontologies;
    }

    @Override
    public void configure(File... file) {
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

            }//end of loop sheets

            ConfigPanel configPanel = new ConfigPanel(sheetNames, previewData);
            int returnVal = JOptionPane.showConfirmDialog(
                    CoolMapMaster.getCMainFrame(), configPanel, "Import from Excel: " + inFile.getAbsolutePath(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

            if (returnVal == JOptionPane.OK_OPTION) {
                proceed = true;

                inStream.close();
                workbook = null;

                //set parameters
                inFile = file[0];
                rowStart = configPanel.getRowStart();
                columnStart = configPanel.getColumnStart();
                sheetIndex = configPanel.getSheetIndex();
                formatIndex = configPanel.getFormatIndex();

            } else {
                //mark operation cancelled
                proceed = false;
            }

        } catch (Exception e) {

        }

    }

    @Override
    public boolean onlyImportFromSingleFile() {
        return true;
    }

    private class ConfigPanel extends JPanel {

        private final ArrayList<DefaultTableModel> models;
        private JTable table = new JTable();
        private JSpinner rowStartSpinner, columnStartSpinner;
        private JCheckBox ontologyImport = new JCheckBox();
        private final JComboBox sheetCombo;
        private final JComboBox formatCombo;

        public int getColumnStart() {
            return (Integer) columnStartSpinner.getValue() - 1;
        }

        public int getSheetIndex() {
            return sheetCombo.getSelectedIndex();
        }

        public int getRowStart() {
            return (Integer) rowStartSpinner.getValue() - 1;
        }

        public int getFormatIndex() {
            return formatCombo.getSelectedIndex();
        }

        public ConfigPanel(String[] tableNames, ArrayList<ArrayList<ArrayList<Object>>> data) {
            models = new ArrayList<>();

            for (ArrayList<ArrayList<Object>> tableData : data) {

                int maxSize = 0;
                for (ArrayList row : tableData) {
                    if (maxSize < row.size()) {
                        maxSize = row.size();
                    }
                }

                Object[][] rawData = new Object[tableData.size()][maxSize]; //There can be a problem..need to longest one

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

            }//

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

            toolBar.add(new JLabel("Format"));
            formatCombo = new JComboBox(new String[]{"two column (sif)", "GSEA (gmt)"});
            toolBar.add(formatCombo);

            formatCombo.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {

                        if (formatCombo.getSelectedIndex() == 0) {
                            //two column
                        } else {
                            //gmt
                        }

                        table.repaint();

                    }
                }
            });

            toolBar.addSeparator();
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
                    if (isSelected || value == null) {
                        return component;
                    }

                    if (formatCombo.getSelectedIndex() == 0) {
                        //two column
                        if (row + 1 >= ((Integer) rowStartSpinner.getValue()).intValue()) {

                            if (column + 1 == ((Integer) columnStartSpinner.getValue()).intValue()) {
                                component.setBackground(UI.colorLightBlue0);
                            } else if (column == ((Integer) columnStartSpinner.getValue()).intValue()) {
                                component.setBackground(UI.colorLightGreen0);
                            }
                        }
                    } else if (formatCombo.getSelectedIndex() == 1) {

                        if (row + 1 >= ((Integer) rowStartSpinner.getValue()).intValue()) {
                            if (column + 1 == ((Integer) columnStartSpinner.getValue()).intValue()) {
                                component.setBackground(UI.colorLightBlue0);
                            } else if (column - 1 >= ((Integer) columnStartSpinner.getValue()).intValue()) {
                                component.setBackground(UI.colorLightGreen0);
                            } else if (column == ((Integer) columnStartSpinner.getValue()).intValue()) {
                                component.setBackground(UI.colorRedWarning);
                            }
                        }
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

            JToolBar bottomBar = new JToolBar();
            this.add(bottomBar, BorderLayout.SOUTH);
            bottomBar.setFloatable(false);

            ColorLabel cLabel = new ColorLabel(UI.colorLightBlue0);
            cLabel.setText("Parent node");
            bottomBar.add(cLabel);

            cLabel = new ColorLabel(UI.colorLightGreen0);
            cLabel.setText("Child node");
            bottomBar.add(cLabel);

            cLabel = new ColorLabel(UI.colorRedWarning);
            cLabel.setText("Description");
            bottomBar.add(cLabel);

        }

    }

}
