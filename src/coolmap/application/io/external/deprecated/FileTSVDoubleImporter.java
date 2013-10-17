package coolmap.application.io.external.deprecated;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package coolmap.application.io.external.impl;
//
//import au.com.bytecode.opencsv.CSVReader;
//import coolmap.application.io.external.FileImporter;
//import coolmap.canvas.datarenderer.renderer.impl.DoubleToColor;
//import coolmap.canvas.datarenderer.renderer.model.ViewRenderer;
//import coolmap.canvas.sidemaps.impl.ColumnLabels;
//import coolmap.canvas.sidemaps.impl.ColumnTree;
//import coolmap.canvas.sidemaps.impl.RowLabels;
//import coolmap.canvas.sidemaps.impl.RowTree;
//import coolmap.data.CoolMapObject;
//import coolmap.data.aggregator.impl.DoubleDoubleMean;
//import coolmap.data.cmatrix.impl.DoubleCMatrix;
//import coolmap.data.cmatrixview.model.VNode;
//import coolmap.utils.Tools;
//import coolmap.utils.graphics.UI;
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.filechooser.FileNameExtensionFilter;
//
///**
// *
// * @author gangsu
// */
//public class FileTSVDoubleImporter extends FileImporter{
//    
//    private int _currentLine = 1;
//    private char _sep = UI.tab;
//
//    @Override
//    public FileFilter getFileFilter() {
//        return new FileNameExtensionFilter("tsv,txt", "txt", "tsv");
//    }
//
//    @Override
//    public CoolMapObject importFromStream(InputStream stream, Object... params) {
//        try{
//            s
//            
//            LineNumberReader r = new LineNumberReader(new InputStreamReader(stream));
//            String c = r.readLine();
//            if(c == null){
//                throw new Exception("Empty File");
//            }
//            String firstRow = r.readLine();
//            if(firstRow == null){
//                throw new Exception("Empty File");
//            }
//            
//            String colHeaderRaw[] = c.trim().split(_sep+"", -1);
//            String firstRowE[] = firstRow.split(_sep+"", -1);
//            
//            String colHeaders[];
//            if(colHeaderRaw.length == firstRowE.length){
//                //Data
//                //skip first
//                colHeaders = new String[colHeaderRaw.length-1];
//                for(int i=0; i<colHeaders.length; i++){
//                    colHeaders[i] = colHeaderRaw[i+1];
//                }
//            }
//            else if(colHeaderRaw.length == firstRowE.length-1){
//                colHeaders = new String[firstRowE.length-1];
//                for(int i=0; i<colHeaders.length; i++){
//                    colHeaders[i] = colHeaderRaw[i];
//                }
//            }
//            else{
//                throw new Exception("Column header or first row malformed");
//            }
//            
//            //r.skip(Long.MAX_VALUE);
//            //skip empty lines
//            String l;
//            int skip = 0;
//            while((l = r.readLine())!=null){
//                if(l.length()==0){
//                    skip++;
//                }
//            }
//            
//            int numRow = r.getLineNumber() - skip - 1;
//            String rowHeaders[] = new String[numRow];
//            int numCol = colHeaders.length;
////            System.out.println("Cols:" + colHeaders.length);
////            System.out.println("Rows:" + rowHeaders.length);
//            
//
//            
////            ObjectLocalCMatrix<Double> cmatrix = new ObjectLocalCMatrix<Double>(file.getName().replaceAll("\\..*$", ""), numRow, numCol, Double.class);
////            cmatrix.printMatrix();
//            
////            System.out.println(cmatrix.getName());
//            String name;
//            if(params != null && params.length > 0 && params[0] != null){
//                name = Tools.removeFileExtension(params[0].toString());
//            }
//            else{
//                name = "Untitled";
//            }
//            
//            DoubleCMatrix cmatrix = new DoubleCMatrix(name, numRow, numCol);
//            System.out.println(cmatrix + " rows:" + numRow + " cols:" + numCol);
//            
//            _currentLine = 0;
//            
//            
//            CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(stream)), _sep);
//            
//            String[] elements;
//            skip = 0;
//            System.out.println("WTF");//it did not read.
//            
//            while((elements = reader.readNext())!=null){
//                System.out.println("elements are here:" + Arrays.toString(elements));
//                
//                
//                if(_currentLine==0){
//                    _currentLine++;
//                    continue;
//                }
//                
//                
//                
//                //System.out.println(Arrays.toString(elements) + " " + colHeaders.length);
//                //System.out.println(Arrays.toString(elements));
//                
//                if(elements.length==1 && elements[0].equals("")){
//                    _currentLine++;
//                    skip++;
//                    continue;//skip empty line
//                }
//                
//                
//                
//                if(elements.length-1 != colHeaders.length || elements[0] == null || elements[0].equals("")){
//                    //System.out.println(elements.length);
//                    throw new Exception("Malformed Row @ " + (_currentLine + 1) + ". Please check source data");
//                }
//                else{
//                    rowHeaders[_currentLine-1-skip] = elements[0];
//                }
//                
//                Double d;
//                for(int i=1; i<elements.length; i++){
//                    try{
//                        d = Double.parseDouble(elements[i]);
//                        cmatrix.setValue(_currentLine-1-skip, i-1, d);
//                    }
//                    catch(Exception e){
//                        d = Double.NaN;
//                        cmatrix.setValue(_currentLine-1-skip, i-1, Double.NaN);
//                        //System.err.print("NaN detected @ line " + _currentLine);
//                    }
//                }
//                
//                _currentLine++;
//            }
//            
//            //System.out.println("Loading completed");
//            
//            //obj.printMatrix();
////            System.out.println(Arrays.toString(rowHeaders));
//            cmatrix.setRowLabels(rowHeaders);
//            cmatrix.setColLabels(colHeaders);
//            
//            System.out.println(Arrays.toString(rowHeaders));
//            
////            cmatrix.printMatrix();
//            
////            System.out.println(cmatrix);
//            
//            CoolMapObject obj = new CoolMapObject();
//            obj.addBaseCMatrix(cmatrix);
//            
////          No ontology, add the default nodes
//            ArrayList<VNode> nodes = new ArrayList<VNode>();
//            for(String label : rowHeaders){
//                nodes.add(new VNode(label));
//            }
//            obj.insertRowNodes(nodes);
//            
//            nodes.clear();
//            for(String label : colHeaders){
//                nodes.add(new VNode(label));
//            }
//            obj.insertColumnNodes(nodes);
//            
//            
//            System.out.println(obj.getID() + ":" + obj.getViewNumColumns() + " " + obj.getViewNumRows());
//            
////            obj.setName("");
////            use unique class name to get
//            //set defaults
////            obj.setAggregator(CoolMap.getAggregator("Double:Mean"));
////            obj.setViewRenderer(
////                    CoolMap.createNewViewRenderer("Double to Color"));
//            obj.setAggregator(new DoubleDoubleMean());
//            obj.setViewRenderer(new DoubleToColor());
//            obj.getCoolMapView().addRowMap(new RowLabels(obj));
//            obj.getCoolMapView().addRowMap(new RowTree(obj));
//            obj.getCoolMapView().addColumnMap(new ColumnLabels(obj));
//            obj.getCoolMapView().addColumnMap(new ColumnTree(obj));
//            
//            return obj;
//        }
//        catch(Exception e){
//            //Messenger.showStackTrace(e, "File Import Exception at line " + (_currentLine+1));
//            e.printStackTrace();
//            return null;
//        }        
//    }
//
//    @Override
//    public String getDisplayName() {
//        return "Numeric(Double) [tsv]";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Create a numeric CoolMap from a numeric matrix file, tab delimited";
//    }
//    
//}
