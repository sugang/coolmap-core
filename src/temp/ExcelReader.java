/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package temp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author sugang
 * 
 * This is the way to read from Excel!
 */
public class ExcelReader {

    public static void readFromXLSXFile(File file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));

            int sheetCount = workbook.getNumberOfSheets();

            System.out.println(sheetCount);

            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowItertor = sheet.iterator();

            while (rowItertor.hasNext()) {
                Row row = rowItertor.next();

                for (int i = 0; i < row.getLastCellNum(); i++) {
                    System.out.print(row.getCell(i) + "|"); //this you won't miss any cells! right way to do
                }
                
                System.out.println("");

            }

        } catch (IOException ex) {
            Logger.getLogger(ExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void readFromFile(File file) {
        try {

            //        XSSFWorkbook workbookXls;
            //        can load up every sheet as previews
            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
            System.out.println("Number of sheets:" + workbook.getNumberOfSheets());

            //say I want sheet 0
            HSSFSheet sheet = workbook.getSheetAt(5);

            Iterator<Row> rowIterator = sheet.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

//                Iterator<Cell> cellI = row.cellIterator();
//                
//                while(cellI.hasNext()){
//                    Cell cell = cellI.next();
//                    //This is wrong -> the blank cell should not return 0!
//                    //use toString would work; but it could cause the formulas to go wrong
//                    
//                    
//                    
//                    System.out.print(cell + "|");
//                }
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    System.out.print(row.getCell(i) + "|"); //this you won't miss any cells! right way to do
                }

                System.out.println("");
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExcelReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String args[]) {
//        readFromFile(new File("/Users/sugang/Dropbox/0 Current Tasks/0-gaofeng-mergedData1217.xls"));
        readFromXLSXFile(new File("/Users/sugang/Dropbox/0 Current Tasks/0-gaofeng-mergedData1217.xlsx"));
    }
}
