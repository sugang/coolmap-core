/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import coolmap.utils.Tools;
import java.util.UUID;

/**
 *
 * @author gangsu
 */
public abstract class LongTask implements Runnable {
    
    private String _name;
    private final String _ID;
    private String _message;
    private Double _progress;
    
    public String getMessage(){
        return _message;
    };
    
    public double getProgress(){
        return _progress;
    };
    
    
    public LongTask(String name){
        _name = name;
        _ID = Tools.randomID();
    }
    
    private LongTask(){
        this(null);
    }
    
    public String getName(){
        return _name;
    }
    
    public String getID(){
        return _ID;
    }
    
    public void setMessage(String message){
        _message = message;
    }
    
    public void setProgress(Double progress){
        if(progress == null){
            _progress = null;
        }
        else{
            if(progress < 0){
                _progress = 0d;
            }
            else if(progress > 1){
                _progress = 1d;
            }
            else{
                _progress = progress;
            }
        }
    }
}
