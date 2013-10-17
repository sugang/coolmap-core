/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.network;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author gangsu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        // required time is to the squared
        // this may require a lot of time to render
        
        ArrayList<Node> nodeList = new ArrayList<Node>();
        for (int i = 0; i < 100; i++) {
            nodeList.add(new Node("Node" + i, 0));
        }

        ArrayList<Edge> edgetList = new ArrayList<Edge>();


        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = i+1; j < nodeList.size(); j++) {
                if (Math.random() > i * 1.0/nodeList.size()) {
                    edgetList.add(new Edge(nodeList.get(i), nodeList.get(j), 1));
                    nodeList.get(i).weight++;
                    nodeList.get(j).weight++;
                }
            }
        }

        System.out.println("Num of edges:" + edgetList.size());



        MinimizerClassic minimizerClassic = new MinimizerClassic(nodeList, edgetList, 0.0, 3.0, 0.15, 2);

        LinkedHashMap<Node, double[]> positions = new LinkedHashMap<Node, double[]>();
        for (Node node : nodeList) {
            positions.put(node, new double[]{Math.random(), Math.random()});
        }

        long t1 = System.currentTimeMillis();
        minimizerClassic.minimizeEnergy(positions, 100);
        long t2 = System.currentTimeMillis();

        System.out.println("Elapsed time:" + (t2 - t1));
        
        double xMin,xMax, yMin, yMax;
        xMin = yMin = Double.MAX_VALUE;
        xMax = yMax = -Double.MAX_VALUE;
        for( Entry<Node, double[]> entry : positions.entrySet()){
            //System.out.println(entry.getKey() + "-->" + Arrays.toString(entry.getValue()));
            if(xMin > entry.getValue()[0]){
                xMin = entry.getValue()[0];
            }
            if(yMin > entry.getValue()[1]){
                yMin = entry.getValue()[1];
            }
            if(xMax < entry.getValue()[0]){
                xMax = entry.getValue()[0];
            }
            if(yMax < entry.getValue()[1]){
                yMax = entry.getValue()[1];
            }
        }
        
        System.out.println(xMin + " " + xMax + "-->" + yMin + " " + yMax);
        
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2D = image.createGraphics();
        g2D.setColor(Color.WHITE);
        
        for(Entry<Node, double[]> entry : positions.entrySet()){
            g2D.fillRect((int)entry.getValue()[0] + 400, (int)entry.getValue()[1] + 300, 4, 4);
        }
        
        g2D.setColor(new Color(255,255,255,20));
        for(Edge e : edgetList){
            g2D.drawLine((int)positions.get(e.startNode)[0] + 400, (int)positions.get(e.startNode)[1] + 300, (int)positions.get(e.endNode)[0] + 400, (int)positions.get(e.endNode)[1] + 300);
        }
        
        g2D.dispose();
        try {
            ImageIO.write(image, "png", new File("/Users/gangsu/Desktop/network.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        


    }
}
