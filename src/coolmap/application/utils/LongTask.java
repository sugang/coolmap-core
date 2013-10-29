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
public abstract class LongTask implements Runnable{
    private final String name;
    
    public LongTask(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
}
