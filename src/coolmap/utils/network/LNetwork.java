/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils.network;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author gangsu
 */
public class LNetwork {

    private HashSet<Node> nodeList = new HashSet<Node>();
    private HashSet<Edge> edgeList = new HashSet<Edge>();
    private HashMap<Node, double[]> positions = new HashMap<Node, double[]>();
    private boolean recomputedLayout = false;

    public LNetwork() {
    }

    public HashSet<Node> getNodes() {
        return nodeList;
    }

    public HashSet<Edge> getEdges() {
        return edgeList;
    }

    public void addNode(Node node) {
        if (node == null) {
            return;
        }
        nodeList.add(node);
    }

    public void addEdge(Edge edge) {
        if (edge == null && edge.startNode != null && edge.endNode != null) {
            return;
        }
        edgeList.add(edge);
        nodeList.add(edge.startNode);
        nodeList.add(edge.endNode);
        edge.startNode.weight++;
        edge.endNode.weight++;
    }

    public int getEdgeCount() {
        return edgeList.size();
    }

    public int getNodeCount() {
        return nodeList.size();
    }

    public void recomputeLayout(int iterations) {
        //randommize initial
        positions.clear();
        for (Node node : nodeList) {
            positions.put(node, new double[]{Math.random(), Math.random()});
        }

        MinimizerClassic minimizerClassic = new MinimizerClassic(nodeList, edgeList, 0.0, 3.0, 0.15, 2);
        minimizerClassic.minimizeEnergy(positions, iterations);
        recomputedLayout = true;
    }

    public BufferedImage drawNetwork(int width, int height) {
        //need to figure out min max
        if(recomputedLayout == false){
            //unless force too
            recomputeLayout(50);
        }
        
        
        double xMin, xMax, yMin, yMax;
        xMin = yMin = Double.MAX_VALUE;
        xMax = yMax = -Double.MAX_VALUE;
        for (Map.Entry<Node, double[]> entry : positions.entrySet()) {
            //System.out.println(entry.getKey() + "-->" + Arrays.toString(entry.getValue()));
            if (xMin > entry.getValue()[0]) {
                xMin = entry.getValue()[0];
            }
            if (yMin > entry.getValue()[1]) {
                yMin = entry.getValue()[1];
            }
            if (xMax < entry.getValue()[0]) {
                xMax = entry.getValue()[0];
            }
            if (yMax < entry.getValue()[1]) {
                yMax = entry.getValue()[1];
            }
        }

        //
        double xWidth = xMax - xMin;
        double yWidth = yMax - yMin;
        
        ///////////////////////////
        

        //factor
        double factorX = width / xWidth;
        double factorY = height / yWidth;
        
        double centerX = -xMin * factorX;
        double centerY = -yMin * factorY;


        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2D = image.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setColor(Color.WHITE);
        for (Map.Entry<Node, double[]> entry : positions.entrySet()) {
            g2D.fillOval((int) Math.round(entry.getValue()[0] * factorX + centerX) - 2, (int) Math.round(entry.getValue()[1] * factorY + centerY) - 2, 4, 4);
        }
        g2D.setColor(new Color(255, 255, 255, 50));
        for (Edge e : edgeList) {
            double[] p1 = positions.get(e.startNode);
            double[] p2 = positions.get(e.endNode);
            g2D.drawLine((int)(p1[0]*factorX + centerX), (int)(p1[1]*factorY + centerY), (int)(p2[0]*factorX + centerX), (int)(p2[1]*factorY + centerY));
        }

        return image;
    }

    @Override
    public String toString() {
        return "Nodes:" + getNodeCount() + " Edges:" + getEdgeCount();
    }
    
    
}
