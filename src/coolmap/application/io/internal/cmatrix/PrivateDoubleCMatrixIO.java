/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io.internal.cmatrix;

import coolmap.application.io.IOTerm;
import coolmap.data.cmatrix.impl.DoubleCMatrix;
import coolmap.data.cmatrix.model.CMatrix;
import coolmap.utils.graphics.UI;
import java.io.*;
import org.apache.commons.lang3.StringUtils;


/**
 *
 * @author gangsu
 */
public class PrivateDoubleCMatrixIO implements ICMatrixIO<Double> {

    @Override
    public CMatrix<Double> importFromDirectory(String ID, String name, int numRow, int numColumn,  File cmatrixDirectory, Class cmatrixClass) throws Exception {
        System.out.println("Trying to import from directory");
        File file = new File(cmatrixDirectory.getAbsolutePath() + File.separator + IOTerm.FILE_CMATRIX_ENTRY);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
       
        DoubleCMatrix matrix = new DoubleCMatrix(name, numRow, numColumn, ID);
       
        String header = reader.readLine();
        String[] ele = header.split("\\t", -1);
        for(int i = 1; i<ele.length;i++){
            matrix.setColLabel(i-1, ele[i].trim());
        }
        
        
        int counter = 0;
        while((line = reader.readLine()) != null){
            ele = line.split("\\t", -1);
            matrix.setRowLabel(counter, ele[0]);
            Double value;
            for(int i=1; i<ele.length; i++){
                try{
                    value = Double.parseDouble(ele[i]);
                }
                catch(Exception e){
                    value = Double.NaN;
                }
                matrix.setValue(counter, i-1, value);
            }
            counter ++;
        }
        
        reader.close();
        return matrix;
    }

    @Override
    public void exportToDirectory(CMatrix<Double> matrix, File entryDirectory) throws Exception{
        System.out.println("Attemp to export to directory");
        //It's own import must be able to read its own export
        if(entryDirectory != null && entryDirectory.isDirectory()){
            File of = new File(entryDirectory + File.separator + IOTerm.FILE_CMATRIX_ENTRY);
            BufferedWriter writer = new BufferedWriter(new FileWriter(of));
            
            writer.write("Row/Column" + UI.tab);
            writer.write(StringUtils.join(matrix.getColLabelsAsList().toArray(), UI.tab));
            writer.write(UI.newLine);
            
            int numRows = matrix.getNumRows();
            int numColumns = matrix.getNumColumns();
            
            for(int i=0; i<numRows; i++){
                writer.write(matrix.getRowLabel(i) + UI.tab);
                Double value;
                for(int j=0; j<numColumns-1; j++){
                    writer.write("" + matrix.getValue(i, j) + UI.tab);
                }
                writer.write("" + matrix.getValue(i, numColumns-1) + UI.newLine);
            }
            
            writer.flush();
            writer.close();
        }
        else{
            throw new Exception("The cmatrix output destination folder is either null or not a directory.");
        }
        
        
        
    }

    @Override
    public String getDisplayName() {
        return "Numeric(Double)";
    }


    
}
