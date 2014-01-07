/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.io.internal.cmatrix;

import coolmap.application.io.IOTerm;
import coolmap.application.io.internal.cmatrix.interfaces.InternalCMatrixExporter;
import coolmap.data.cmatrix.model.CMatrix;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import org.json.JSONObject;

/**
 * Export a CMatrix using embedded object's toString method
 * @author sugang
 */
public class DefaultCMatrixExporter implements InternalCMatrixExporter{

    @Override
    public void dumpData(CMatrix matrix, TFile zipFolder) throws Exception {
        TFile outputFile = new TFile(zipFolder.getAbsolutePath() + File.separator + IOTerm.FILE_DATA);
        TFile propertyFile = new TFile(zipFolder.getAbsolutePath() + File.separator + IOTerm.FILE_PROPERTY);
        
        BufferedWriter propertyWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(propertyFile)));
        
        JSONObject cmatrixPropertyEntry = new JSONObject();
        cmatrixPropertyEntry.put(IOTerm.ATTR_ID, matrix.getID());
        cmatrixPropertyEntry.put(IOTerm.ATTR_NAME, matrix.getName());
        cmatrixPropertyEntry.put(IOTerm.ATTR_CMATRIX_NUMROW, matrix.getNumRows());
        cmatrixPropertyEntry.put(IOTerm.ATTR_CMATRIX_NUMCOLUMN, matrix.getNumColumns());
        cmatrixPropertyEntry.put(IOTerm.ATTR_CLASS, matrix.getClass().getName());
        cmatrixPropertyEntry.put(IOTerm.ATTR_CMATRIX_MEMBERCLASS, matrix.getMemberClass().getName());
        
//        System.out.println(cmatrixPropertyEntry);
        
        propertyWriter.write(cmatrixPropertyEntry.toString());
        
        propertyWriter.flush();
        propertyWriter.close();
        
        
        
        BufferedWriter dataWriter = new BufferedWriter(new OutputStreamWriter(new TFileOutputStream(outputFile)));
        
//        dataWriter.write("This is where cmatrix will be dumped");
//        part of the following code can be extracted for other use
//        dataWriter properties
        dataWriter.write("Row/Column");
        for(int i=0; i<matrix.getNumColumns(); i++){
            dataWriter.write("\t");
            String colLabelString = matrix.getColLabel(i);
            if(colLabelString == null)
                colLabelString = "";
            dataWriter.write(colLabelString);
        }
        dataWriter.write("\n");
        
        for(int i=0; i<matrix.getNumRows(); i++){
            
            String rowLabelString = matrix.getRowLabel(i);
            if(rowLabelString == null)
                rowLabelString = "";
            dataWriter.write(rowLabelString);
            
            for(int j=0; j<matrix.getNumColumns(); j++){
                dataWriter.write("\t");
                Object value = matrix.getValue(i, j);
                
                if(value != null){
                    dataWriter.write(value.toString());
                }
            }
            dataWriter.write("\n");
            
        }
        
        
        
        dataWriter.flush();
        dataWriter.close();
        
//        System.out.println("Dumping successful");
    }
    
}
