/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.canvas.misc;

import coolmap.utils.graphics.UI;
import java.awt.Font;
import java.util.ArrayList;

/**
 * need one for x and one for y
 *
 * @author Gang
 */
public class ZoomControl {

    private int currentZoomIndex = 0;
    private float currentZoom = 0f;
    private Font currentPlainFont = null;
    private Font currentBoldFont = null;
    private ArrayList<Float> zoomLevels = new ArrayList<Float>();
    private ArrayList<Font> labelFonts = new ArrayList<Font>();
    private ArrayList<Font> labelFontsBold = new ArrayList<Font>();
    private String defaultFontName = "Monospaced"; //it seems that if the font can be found easily, then it will be initalized quickly. Otherwise
    private int defaultZoomIndex = 8;
    
    public int getNearestZoomIndex(float zoom) {
        int currentZoomIndex;
        if (zoom <= 0) {
            return 0;
        } else {
            for (int i = 0; i < zoomLevels.size() - 1; i++) {
                if (zoom >= zoomLevels.get(i) && zoom < zoomLevels.get(i + 1)) {
                    currentZoomIndex = i;
                    return i;
                }
            }
            return defaultZoomIndex;
        }

        
    }

    public ZoomControl() {
        _initDefault();
    }

    public int getCurrentZoomIndex() {
        return currentZoomIndex;
    }

//    public void setCurrentZoomIndex(int index){
//        if(index < 0 ){
//            index = 0;
//        }
//        if(index >= zoomLevels.size()){
//            index = zoomLevels.size()-1;
//        }
//        currentZoomIndex = index;
//        setZoom(currentZoomIndex);
//    }
    public float getCurrentZoom() {
        return zoomLevels.get(currentZoomIndex);
    }

    /**
     * zoom increase by 1
     */
    public float getNextZoom() {
        if ((currentZoomIndex + 1) < zoomLevels.size()) {
            currentZoomIndex++;
            setZoom(currentZoomIndex);
        } else {
            //do nothing
        }
        return currentZoom;
    }

    public boolean isLowestZoom(){
        if(currentZoomIndex == 0){
            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean isHighestZoom(){
        if(currentZoomIndex >= zoomLevels.size()-1){
            return true;
        }
        return false;
    }
    
    
    public float getPreviousZoom() {
        if ((currentZoomIndex - 1) >= 0) {
            currentZoomIndex--;
            setZoom(currentZoomIndex);
        } else {
            //do nothing
        }
        return currentZoom;
    }

    private void _initDefault() {
        //zoomLevels.add(0.5f);
        //minimal is 1px. maybe i will change this.
        //use previews then
        zoomLevels.add(0.1f);
        zoomLevels.add(0.25f);
        zoomLevels.add(0.5f);
        
        zoomLevels.add(1f);//Single pixel.. may caue serious issues.
        zoomLevels.add(1.5f);
        zoomLevels.add(3f);
        zoomLevels.add(5f);
        zoomLevels.add(8.33f);
        zoomLevels.add(10f);
        zoomLevels.add(12.5f);
        zoomLevels.add(16.67f);
        zoomLevels.add(20f);
        zoomLevels.add(25f);
        zoomLevels.add(33.3f);
        zoomLevels.add(50f);
        zoomLevels.add(66.67f);
        zoomLevels.add(100f);
        zoomLevels.add(150f);
        zoomLevels.add(200f);
        zoomLevels.add(300f);
        zoomLevels.add(400f);
        zoomLevels.add(500f);


        float size = 0;
        for (float i : zoomLevels) {
            size = i;
            if (i <= 2) {
                size = 2;
            } else if (i >= 14) {
                size = 14;
            } else {
                //System.out.println("Size should be small");
                size = i - 2;
            }

            //System.out.println(size);
            labelFonts.add(UI.fontPlain.deriveFont(size));//Prob won't even work
            labelFontsBold.add(UI.fontPlain.deriveFont(Font.BOLD).deriveFont(size));
        }

        //set default zoomlevel to 12
        setZoom(defaultZoomIndex);
    }

    public Font getPlainFont() {
        return this.currentPlainFont;
    }

    public Font getBoldFont() {
        return this.currentBoldFont;
    }

    public void setZoom(int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= zoomLevels.size()) {
            index = zoomLevels.size() - 1;
        }
        currentZoomIndex = index;
        currentZoom = zoomLevels.get(index);
        currentBoldFont = labelFontsBold.get(index);
        currentPlainFont = labelFonts.get(index);
        
        

    }

    public Float getZoom(int index) {
        try {
            return zoomLevels.get(index);
        } catch (Exception e) {
            return null;
        }
    }
    //center the selected region
}
