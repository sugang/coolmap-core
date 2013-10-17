/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

/**
 *
 * @author gangsu
 */
public class ServiceMaster {
    
    private static TaskEngine _taskEngine;
    
    private ServiceMaster(){
        
    }
    
    public static void initialize(){
        _taskEngine = new TaskEngine();
    }
    
    public static TaskEngine getTaskEngine(){
        return _taskEngine;
    }
}
