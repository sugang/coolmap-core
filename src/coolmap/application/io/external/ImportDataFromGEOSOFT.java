/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.application.io.external.interfaces.ImportData;
import coolmap.data.CoolMapObject;
import coolmap.data.contology.model.COntology;
import java.io.File;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author sugang
 */
public class ImportDataFromGEOSOFT implements ImportData {

    @Override
    public void importFromFile(File... file) throws Exception {
//        try {
//            GeoSOFT geoEntry = GeoSOFT.parse(file[0]);
//
//            ArrayTable arrayTable = geoEntry.getArrayTable();
//
////        arrayTable.printAttribute();
//            //This would also create a new COntology and add
//            String rowNames[] = arrayTable.getRowNames();
//            String columnNames[] = arrayTable.getColumnNames();
//
//            DoubleCMatrix matrix = new DoubleCMatrix(Tools.removeFileExtension(file.getName()), rowNames.length, columnNames.length);
//
//            matrix.setColLabels(columnNames);
//            matrix.setRowLabels(rowNames);
//
//            for (int i = 0; i < rowNames.length; i++) {
//                for (int j = 0; j < columnNames.length; j++) {
//                    matrix.setValue(i, j, arrayTable.getValue(i, j));
//                    if (Thread.interrupted()) {
//                        return null;
//                    }
//                }
//            }
//
//            if (Thread.interrupted()) {
//                return null;
//            }
//            return matrix;
//        } catch (Exception e) {
//            CMConsole.logError(" failed to import numeric matrix data from: " + file);
//            return null;
//        }
    }

    @Override
    public String getLabel() {
        return "NCBI.GEO soft";
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return new FileNameExtensionFilter("GEO soft", "soft", "txt");
    }

    @Override
    public Set<CoolMapObject> getImportedCoolMapObjects() {
        return null;
    }

    @Override
    public Set<COntology> getImportedCOntology() {
        return null;
    }



}
