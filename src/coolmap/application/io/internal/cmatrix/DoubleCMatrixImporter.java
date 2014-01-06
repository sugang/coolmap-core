/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.cmatrix;

import coolmap.application.io.IOTerm;
import coolmap.application.io.internal.cmatrix.interfaces.InternalCMatrixImporter;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrix.model.CMatrix;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import org.json.JSONObject;

/**
 *
 * @author sugang
 */
public class DoubleCMatrixImporter implements InternalCMatrixImporter {

    @Override
    public CMatrix loadData(TFile matrixFolder, JSONObject matrixProperty) throws Exception {

        String id = matrixProperty.getString(IOTerm.ATTR_ID);
        String cls = matrixProperty.getString(IOTerm.ATTR_CLASS);
        String name = matrixProperty.optString(IOTerm.ATTR_NAME);

        int numColumn = matrixProperty.getInt(IOTerm.ATTR_CMATRIX_NUMCOLUMN);
        int numRow = matrixProperty.getInt(IOTerm.ATTR_CMATRIX_NUMROW);

        DoubleCMatrix matrix = new DoubleCMatrix(name, numRow, numColumn, id);

        //Then standard tsv
        BufferedReader reader = new BufferedReader(new InputStreamReader(new TFileInputStream(new TFile(matrixFolder + File.separator + IOTerm.FILE_DATA))));

        String header = reader.readLine();
        String[] ele = header.split("\t", -1);
        String[] colLabels = new String[ele.length - 1];
        for (int i = 1; i < ele.length; i++) {
            colLabels[i - 1] = ele[i].trim();
        }
        matrix.setColLabels(colLabels);

        String row;
        int counter = 0;
        Double value;
        while ((row = reader.readLine()) != null) {
           ele = row.split("\t");
           matrix.setRowLabel(counter, ele[0]);
           
           for(int i=1; i < ele.length; i++){
               try{
                   value = Double.parseDouble(ele[i]);
               }
               catch(Exception e){
                   value = null;
               }
               matrix.setValue(counter, i - 1, value);
               //Can consider return
               if(Thread.interrupted()){
                   throw new InterruptedException("Loading Operation Canceled");
               }
           }
           
           counter++;
        }
        
        reader.close();//Close IO
        
        return matrix;
    }

    @Override
    public Class<? extends CMatrix> getImportedClass() {

        return DoubleCMatrix.class;
    }

}
