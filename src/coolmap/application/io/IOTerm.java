/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.io;

/**
 *
 * @author gangsu
 */
public class IOTerm {

    //data types
    public final static String OBJECT_CMATRIX_ID = "cmatrix_id";
    public final static String OBJECT_STATESNAPSHOT_ID = "StateSnapshot";
    public final static String OBJECT_COOLMAPOBJECT_ID = "coolmapobject_id";
    public final static String OBJECT_CONTOLOGY_ID = "contology_id";

    //matrix attributes
    public final static String ATTR_CMATRIX_NUMROW = "num_row";
    public final static String ATTR_CMATRIX_NUMCOLUMN = "num_column";
    public final static String ATTR_CMATRIX_MEMBERCLASS = "member_class";

    //matrix view attributes
    public final static String ATTR_VIEW_MATRICES = "linked_matrices";
    public final static String ATTR_VIEW_AGGREGATOR_CLASS = "aggregator";
    public final static String ATTR_VIEW_RENDERER_CLASS = "renderer";
    public final static String ATTR_VIEW_SNIPPETCONVERTER_CLASS = "snippet_converter";
    public final static String ATTR_VIEW_PANEL_COLUMN = "column_panels";
    public final static String ATTR_VIEW_PANEL_ROW = "row_panels";
    public final static String ATTR_VIEW_PANEL_CONTAINER_VISIBLE = "visible";
    public final static String ATTR_VIEW_PANEL = "panel";
    public final static String ATTR_VIEW_MAXIMIZED = "maximized";
    public final static String ATTR_VIEW_MINIMIZED = "minimized";
    public final static String ATTR_VIEW_BOUNDS = "bounds";
    
    

    //node attributes
    public final static String ATTR_NODE_ID = "i";
    public final static String ATTR_NODE_NAME = "n";
    public final static String ATTR_NODE_LABEL = "l";
    public final static String ATTR_NODE_VIEWMULTIPLIER = "v";
    public final static String ATTR_NODE_VIEWMULTIPLIER_DEFAULT = "d";
    public final static String ATTR_NODE_ISEXPANDED = "e";
    public final static String ATTR_NODE_ONTOLOGYID = "o";
    public final static String ATTR_NODE_COLOR = "c";
    public final static String ATTR_NODE_VIEWHEIGHT = "h";
    public final static String ATTR_NODE_VIEWHEIGHTDIFF = "f";

    //general attributes
    public final static String ATTR_SOURCE = "source";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_VIEW_ZOOM = "zoom";
    public final static String ATTR_VIEW_ANCHOR = "anchor";
    public final static String ATTR_COLOR = "color";
    public final static String ATTR_ID = "id";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_CLASS = "class";
    public final static String ATTR_URI = "uri";
    public final static String ATTR_CONFIG = "config";

    //project attributes
    public final static String ATTR_PROJECT_DATE = "date";
    public final static String ATTR_PROJECT_SESSION_NAME = "session_name";

    //file names
    public final static String FILE_PROJECT_INFO = "project.info";
    public final static String FILE_PROPERTY = "properties";
    public final static String FILE_DATA = "data";
    public final static String FILE_PROPERTY_AGGREGATOR = "properties.aggregator";
    public final static String FILE_PROPERTY_RENDERER = "properties.renderer";
    public final static String FILE_PROPERTY_SNIPPET = "properties.snippet";
    public final static String FILE_STATE_ROWTREE = "rtree";
    public final static String FILE_STATE_COLUMNTREE = "ctree";//column 
    public final static String FILE_STATE_NODE_ROWBASE = "rbnodes";//row base nodes
    public final static String FILE_STATE_NODE_COLUMNBASE = "cbnodes"; //column base nodes
    public final static String FILE_STATE_NODE_ROWTREE = "rtnodes"; //row tree nodes
    public final static String FILE_STATE_NODE_COLUMNTREE = "ctnodes"; //column tree nodes

    //folder names
    public final static String DIR_CMATRIX = "cmatrix";
    public final static String DIR_CONTOLOGY = "contology";
    public final static String DIR_CONTOLOGY_ATTRIBUTE = "contology_attr";
    public final static String DIR_COOLMAPOBJECT = "coolmapobject";
    public final static String DIR_StateSnapshot = "state"; // this one is under coolmapobject
    public final static String DIR_STATE = "state";
    public final static String DIR_WIDGET = "widget";
    public final static String DIR_PLUGIN = "plugin";

}
