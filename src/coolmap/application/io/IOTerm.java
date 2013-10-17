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
    public final static String OBJECT_CMATRIX = "CMatrix";
    public final static String OBJECT_STATESNAPSHOT = "StateSnapshot";
    public final static String OBJECT_COOLMAPOBJECT = "CoolMapObject";
    public final static String OBJECT_CONTOLOGY = "COntology";
    //fields
    public final static String FIELD_NUMROW = "NumRow";
    public final static String FIELD_NUMCOLUMN = "NumColumn";
    public final static String FIELD_ID = "ID";
    public final static String FIELD_NAME = "Name";
    public final static String FIELD_CMATRIX_MEMBERCLASS = "MemberClass";
    public final static String FIELD_URI = "URI";
    public final static String FIELD_COOLMAPOBJECT_LINKEDCMATRICES = "LinkedCMatrices";
    public final static String FIELD_COOLMAPOBJECT_AGGREGATOR = "Aggregator";
    public final static String FIELD_COOLMAPOBJECT_VIEWRENDERER = "ViewRenderer";
    public final static String FIELD_COOLMAPOBJECT_ANNOTATIONRENDERER = "AnnotationRenderer";
    public final static String FIELD_COOLMAPOBJECT_SNIPPETCONVERTER = "SnippetConverter";
    public final static String FIELD_CMATRIX_ICMATRIXIO = "ICMatrixIO";
    public final static String FIELD_CMATRIX_CLASS = "CMatrixClass";
    public final static String FIELD_SOURCE = "Source";
    public final static String FIELD_DESCRIPTION = "Description";
    public final static String FIELD_COOLMAPVIEW_ZOOMLEVEL = "ZoomLevel";
    public final static String FIELD_COOLMAPVIEW_MAPANCHOR = "MapAnchor";
    public final static String FIELD_VIEWCOLOR = "ViewColor";
    public final static String FIELD_VNODE_CURRENTVIEWMULTIPLIER = "ViewMultiplier";
    public final static String FIELD_VNODE_DEFAULTVIEWMULTIPLIER = "DefaultMultiplier";
    public final static String FIELD_VNODE_VIEWLABEL = "ViewLabel";
    public final static String FIELD_VNODE_ISEXPANDED = "Expanded";
    public final static String FIELD_VNODE_ONTOLOGYID = "OntologyID";
    public final static String FIELD_CONTOLOGY_EDGETATTRIBUTECLASS = "EdgeAttributeClass";
    
    //file names
    public final static String FILE_PROJECT_INFO = "project.info";
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
    public final static String DIR_CoolMapObject = "coolMapObject";
    public final static String DIR_StateSnapshot = "state"; // this one is under coolmapobject
}
