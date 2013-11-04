/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.application.utils;

/**
 *
 * @author sugang
 */
public class Logger{
    
    private Logger _logger = null;
    
    public Logger getInstance(){
        if(_logger == null){
            _logger = new Logger();
        }
        return _logger;
    }
    
    private Logger(){
        
    }
    
    public void log(Object... object){
        
    }
    
    
}
