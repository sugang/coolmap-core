/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.external;

import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrix.model.CMatrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author gangsu
 */
public class ImportDoubleCMatrixFromFile {

    public static CMatrix importFromFile(File file) throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        ArrayList<String> dataLines = new ArrayList<String>();

        String header = reader.readLine();

        String line = null;
        while ((line = reader.readLine()) != null) {
            dataLines.add(line);
        }

        String[] ele = header.split("\t", -1);

        DoubleCMatrix matrix = new DoubleCMatrix(file.getName(), dataLines.size(), ele.length - 1);

        String[] colLabels = new String[ele.length - 1];
        for (int i = 1; i < ele.length; i++) {
            colLabels[i - 1] = ele[i].trim();
        }
        matrix.setColLabels(colLabels);

        int counter = 0;
        Double value;
        for (String row : dataLines) {

            ele = row.split("\t");
            matrix.setRowLabel(counter, ele[0]);

            for (int i = 1; i < ele.length; i++) {
                try {
                    value = Double.parseDouble(ele[i]);
                } catch (Exception e) {
                    value = null;
                }
                matrix.setValue(counter, i - 1, value);
            }

            counter++;
        }


        //String[] ele = header.split("\\t", -1);
//        for (int i = 1; i < ele.length; i++) {
//            matrix.setColLabel(i - 1, ele[i].trim());
//        }
//
//
//        int counter = 0;
//        while ((line = reader.readLine()) != null) {
//            ele = line.split("\\t", -1);
//            matrix.setRowLabel(counter, ele[0]);
//            Double value;
//            for (int i = 1; i < ele.length; i++) {
//                try {
//                    value = Double.parseDouble(ele[i]);
//                } catch (Exception e) {
//                    value = Double.NaN;
//                }
//                matrix.setValue(counter, i - 1, value);
//            }
//            counter++;
//        }

        reader.close();
        return matrix;
    }
}
