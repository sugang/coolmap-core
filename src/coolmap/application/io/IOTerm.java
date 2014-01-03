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
    //fields
    public final static String ATTR_NUMROW = "num_row";
    public final static String ATTR_NUMCOLUMN = "num_column";
    public final static String ATTR_ID = "id";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_MEMBERCLASS = "member_class";
    public final static String ATTR_CLASS = "class";
    public final static String FIELD_URI = "URI";
    public final static String FIELD_COOLMAPOBJECT_LINKEDCMATRICES = "LinkedCMatrices";
    public final static String FIELD_COOLMAPOBJECT_AGGREGATOR = "Aggregator";
    public final static String FIELD_COOLMAPOBJECT_VIEWRENDERER = "ViewRenderer";
    public final static String FIELD_COOLMAPOBJECT_ANNOTATIONRENDERER = "AnnotationRenderer";
    public final static String FIELD_COOLMAPOBJECT_SNIPPETCONVERTER = "SnippetConverter";
    public final static String FIELD_CMATRIX_ICMATRIXIO = "ICMatrixIO";

    public final static String FIELD_SOURCE = "Source";
    public final static String ATTR_DESCRIPTION = "description";
    public final static String ATTR_ZOOM = "ZoomLevel";
    public final static String FIELD_COOLMAPVIEW_MAPANCHOR = "MapAnchor";
    public final static String ATTR_COLOR = "color";
    public final static String FIELD_VNODE_CURRENTVIEWMULTIPLIER = "ViewMultiplier";
    public final static String FIELD_VNODE_DEFAULTVIEWMULTIPLIER = "DefaultMultiplier";
    public final static String FIELD_VNODE_VIEWLABEL = "ViewLabel";
    public final static String FIELD_VNODE_ISEXPANDED = "Expanded";
    public final static String FIELD_VNODE_ONTOLOGYID = "OntologyID";
    public final static String FIELD_CONTOLOGY_EDGETATTRIBUTECLASS = "EdgeAttributeClass";

    //file names
    public final static String FILE_PROJECT_INFO = "project.info";
    public final static String FILE_PROPERTY = "properties";
    public final static String FILE_DATA = "data";
    
    
    public final static String FILE_CONTOLOGY_ENTRY = "data.ont";
    public final static String FILE_CMATRIX_ENTRY = "data.cmx";
    public final static String FILE_COOLMAPOBJECT_ENTRY = "data.cbj";
    public final static String FILE_STATESNAPSHOT_TREE_ROW = "rtree.stt";
    public final static String FILE_STATESNAPSHOT_TREE_COLUMN = "ctree.stt";
    public final static String FILE_STATESNAPSHOT_NODE_ROWBASE = "rbnodes.stt";
    public final static String FILE_STATESNAPSHOT_NODE_COLUMNBASE = "cbnodes.stt";
    public final static String FILE_STATESNAPSHOT_NODE_ROWTREE = "rtnodes.stt";
    public final static String FILE_STATESNAPSHOT_NODE_COLUMNTREE = "ctnodes.stt";
    //folder names
    public final static String DIR_CMATRIX = "cmatrix";
    public final static String DIR_COntology = "contology";
    public final static String DIR_CONTOLOGY_ATTRIBUTE = "contology_attr";
    public final static String DIR_CoolMapObject = "coolMapObject";
    public final static String DIR_StateSnapshot = "state"; // this one is under coolmapobject

    //property file
    public final static String PROJECT_FIELD_DATE = "date";
    public final static String PROJECT_FIELD_SESSION_NAME = "session_name";

}
