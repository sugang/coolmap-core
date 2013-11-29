/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package coolmap.utils.statistics.test;

/**
 *
 * @author sugang
 */
public class TestResult {
    
    private final double pValue;
    private final String title;
    private final String description;
    
    public TestResult(String title, String description, double pValue){
        this.title = title;
        this.pValue = pValue;
        this.description = description;
    }
    
    public double getPValue(){
        return pValue;
    };
    public String getTitle(){
        return title;
    };
    public String getDescription(){
        return description;
    };
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        sb.append("\n");
        sb.append("P-value: ").append(pValue).append("\n");
        sb.append("Details: ").append(description).append("\n\n");
        
        return sb.toString();
    }
}
