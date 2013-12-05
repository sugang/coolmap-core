/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.bioparser.geosoft;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 *
 * @author sugang
 */
public class GeoSOFT {

    private final Database database;
    private final Dataset dataset;
    private final LinkedHashSet<Subset> subsets = new LinkedHashSet<>();
    private final ArrayTable arrayTable;
    private final Annotation annotation;

    private GeoSOFT() {
        this(null, null, null, null, null);
    }

    public ArrayTable getArrayTable() {
        return arrayTable;
    }

    public GeoSOFT(Database database, Dataset dataset, Collection<Subset> subsets, ArrayTable arrayTable, Annotation annotation) {
        this.database = database;
        this.dataset = dataset;
        this.subsets.addAll(subsets);
        this.arrayTable = arrayTable;
        this.annotation = annotation;
    }

    public void printContents() {
        System.out.println(database);
        System.out.println(dataset);
        for (Subset subset : subsets) {
            System.out.println("  " + subset);
        }
        System.out.println(arrayTable);
        System.out.println(annotation == null ? "No annotation" : annotation);
    }

    public static GeoSOFT parse(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(file));

            String line;

            String currentField = null;
            ArrayList<Subset> subsets = new ArrayList<>();
            Database database = null;
            Dataset dataset = null;
            ArrayTable arrayTable = null;
            Subset currentSubset = null;
            Integer rowCount = null;
            Integer columnCount = null;
            int dataRowIndex = 0;
            String[] dataAttributes = null;
            Annotation annotation = null;

            while ((line = reader.readLine()) != null) {
                if (Thread.interrupted()) {
                    return null;
                }
                //skip blank lines
                if (line.trim().equals("")) {
                    continue;
                }

                //push to current subset
                if (line.startsWith("^") && currentField != null && currentField.equals(FieldConstants.SUBSET)
                        && currentSubset != null) {
                    subsets.add(currentSubset);
                }

                //starts with ^
                if (line.startsWith("^")) {

                    if (line.startsWith("^" + FieldConstants.DATABASE)) {
                        //create database
                        currentField = FieldConstants.DATABASE;
                        String[] keyVal = line.split(" = ", -1);
                        if (database == null) {
                            if (keyVal.length > 1) {
                                database = new Database(keyVal[1].trim());
                            } else {
                                database = new Database(null);
                            }
                        }

//                        System.out.println(database);
                    } else if (line.startsWith("^" + FieldConstants.DATASET)) {
                        currentField = FieldConstants.DATASET;
                        String[] keyVal = line.split(" = ", -1);
                        if (dataset == null) {
                            if (keyVal.length > 1) {
                                dataset = new Dataset(keyVal[1]);
                            } else {
                                dataset = new Dataset(null);
                            }
                        }

//                        System.out.println(dataset);
                    } else if (line.startsWith("^" + FieldConstants.SUBSET)) {
                        //start a new subset
                        currentField = FieldConstants.SUBSET;
                        String[] keyVal = line.split(" = ", -1);
                        if (keyVal.length > 1) {
                            currentSubset = new Subset(keyVal[1]);
                        } else {
                            currentSubset = new Subset(null);
                        }

//                        System.out.println(currentSubset);
                    } else if (line.startsWith("^" + FieldConstants.ANNOTATION)) {
                        currentField = FieldConstants.ANNOTATION;
                        if (annotation == null) {
                            annotation = new Annotation();
                        }
                    }

                } else if (line.startsWith("!")) {

                    if (line.startsWith("!" + FieldConstants.dataset_table_begin)) {
                        //Get the info for rows and columns for arrayTable
                        //arrayTable = new ArrayTable();
                        currentField = FieldConstants.dataset_table_begin;
//                        System.out.println("Row count: " + rowCount + " Column count:" + columnCount);
                        if (rowCount != null && columnCount != null) {
                            arrayTable = new ArrayTable(rowCount, columnCount);
                        } else {
                            arrayTable = null;
                        }

                    } else if (line.startsWith("!" + FieldConstants.dataset_table_end)) {
                        //dataset ended, no need to parse again
                        break;
                    } //Then do the regular
                    else {
                        line = line.substring(1);
                        String ele[] = line.split(" = ", -1);
                        try {
                            ele[0] = ele[0].trim();
                            ele[1] = ele[1].trim();
                            switch (currentField) {
                                case FieldConstants.DATABASE:
                                    database.addAttribute(ele[0], ele[1]);
                                    break;
                                case FieldConstants.DATASET:
                                    dataset.addAttribute(ele[0], ele[1]);
                                    break;
                                case FieldConstants.SUBSET:
                                    currentSubset.addAttribute(ele[0], ele[1]);
                                    break;
                                case FieldConstants.ANNOTATION:
                                    annotation.addAttribute(ele[0], ele[1]);
                                    break;

                            }

                            if (ele[0].equals(FieldConstants.dataset_feature_count)) {
                                rowCount = Integer.parseInt(ele[1]);
                            }

                            if (ele[0].equals(FieldConstants.dataset_sample_count)) {
                                columnCount = Integer.parseInt(ele[1]);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

//                    //also try to parse rows and columns
//                    ////
//                    if(line.startsWith("^dataset_feature_count")){
//                        
//                    }
                } else if (line.startsWith("#")) {
                    line = line.substring(1);
                    String ele[] = line.split(" = ", -1);
                    if (currentField.equals(FieldConstants.DATASET)) {
                        try {

                            dataset.addSampleAttribute(ele[0], ele[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (currentField.equals(FieldConstants.dataset_table_begin) && arrayTable != null) {
                        //
                        if (line.trim().equals("")) {
                            continue;
                        } else if (line.startsWith("ID_REF")) {
                            String[] headers = line.split("\t", -1);
                            //System.out.println(headers.length);
                            if (headers.length == columnCount + 2) {
                                //It is a plain soft file
                                for (int i = 2; i < headers.length; i++) {
                                    arrayTable.setColumnName(i - 2, headers[i]);
                                }
                                //just continue
                            } else {
                                //also parse the additional attributes; 
                                //
                                //This will be extended to load additional attributes
                                /////////////////////////////////////////////////////
                                //place holders

//                                System.out.println("Additional Annotation spotted: this is a full soft file");
//                                additional data attributes
                                dataAttributes = new String[headers.length - 2 - columnCount];
                                //System.out.println(dataAttributes.length);
                                for (int i = 2 + columnCount; i < headers.length; i++) {
                                    dataAttributes[i - 2 - columnCount] = headers[i];
                                }

                            }
                        } else {
                            //These are data lines
                            String[] dataLine = line.split("\t", -1);
                            try {
                                //add row name
                                arrayTable.setRowNames(dataRowIndex, dataLine[0].trim());

                                //add mapping
                                if (dataLine[1] != null && dataLine[1].trim().length() > 0) {
                                    arrayTable.addMapping(dataLine[1].trim(), dataLine[0].trim());
                                }

                                for (int j = 0; j < columnCount; j++) {
                                    try {
                                        //set Value
                                        arrayTable.setValue(dataRowIndex, j, Double.parseDouble(dataLine[j + 2]));
                                    } catch (Exception e) {
                                        //set Value
                                        arrayTable.setValue(dataRowIndex, j, null);
                                    }
                                }

                                if (dataAttributes != null) {
                                    //has attributes
                                    for (int j = columnCount + 2; j < dataLine.length; j++) {
                                        try {
                                            arrayTable.addAttribute(dataLine[0].trim(), dataAttributes[j - columnCount - 2], dataLine[j]);
                                        } catch (Exception e) {
                                            //anything wrong with the loop itself
                                        }
                                    }

                                }

                            } catch (Exception e) {
                                //skip a line if line is malformed
                                e.printStackTrace();
                            }

                            dataRowIndex++;
                        }
                    }
                }

            }
            //End of iterate the entire file
            //arrayTable.printData();
            //arrayTable.printMapping();
            //arrayTable.printAttribute();
            //database.printDetails();
            //dataset.printDetails();

//            for(Subset subset : subsets){
//                subset.printDetails();
//            }
            GeoSOFT soft = new GeoSOFT(database, dataset, subsets, arrayTable, annotation);

            return soft;
        } catch (Exception e) {
            //System.out.println("Error @ line" + reader.getLineNumber());
            e.printStackTrace();
            return null;
        }
    }
}
