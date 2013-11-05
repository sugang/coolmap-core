/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.utils.cluster;

/**
 *
 * @author sugang
 */
public class CoolMapClusterInterruptedException extends InterruptedException{

    public CoolMapClusterInterruptedException(String message) {
        super("Clustering interrupted " + message);
    }
    
}
