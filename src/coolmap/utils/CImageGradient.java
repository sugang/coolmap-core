/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.utils;


import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.vecmath.Color3b;

/**
 *
 * @author sugang
 */
public class CImageGradient {

    private int length;
    private ArrayList<Color> colors = new ArrayList();
    private ArrayList<Double> pos = new ArrayList();
    private Color defaultColor = new Color(200, 150, 150);

    public ArrayList<Color> getColors(){
        return (ArrayList<Color>)(colors.clone());
    }

    public ArrayList<Double> getPos(){
        return (ArrayList<Double>)pos.clone();
    }
    
//    public void clearAll(){
//        colors.clear();
//        pos.clear();
//    }
    
    public void removeAt(int index){
        colors.remove(index);
        pos.remove(index);
    }    
    
    public enum InterType {
        Linear, HSB
    }

    public CImageGradient(int s) {
        length = s;
    }

    public void addColor(Color c, double p) {
        this.colors.add(c);
        this.pos.add(p);
    }

    public Color[] generateGradient(InterType t) {
        //System.out.println("Generate Gradient");
        int c = defaultColor.getRGB();
        Color[] gradients = new Color[length];
        if (colors.isEmpty()) {
            for (int i = 0; i < gradients.length; i++) {
                gradients[i] = defaultColor;
            }
        } else if (colors.size() == 1) {
            c = colors.get(0).getRGB();
            for (int i = 0; i < gradients.length; i++) {
                gradients[i] = defaultColor;
            }
        } else {
            //need to partition the colors
            //to match the gradient, actually it's better to use deque
            //all pos values should be between 0.0 - 1.0
            //insert 0 if not so
            if (pos.get(0) != 0.0) {
                colors.add(0, colors.get(0));
                pos.add(0, 0.0); //insert 0.0 as first element
            }
            //Append 1.0 if not so
            if (pos.get(pos.size() - 1) != 1.0) {
                colors.add(colors.get(colors.size() - 1));
                pos.add(1.0); //append 1.0 as last element
            }

            int[] blockSizes = new int[pos.size() - 1];



            for (int i = 0; i < pos.size() - 1; i++) {
                //System.out.println(pos.get(i));
                blockSizes[i] = (int) Math.round((pos.get(i + 1) - pos.get(i)) * length);
            }


            Color c1, c2;
            int c1R, c1G, c1B, c2R, c2G, c2B;
            int counter = 0;

            switch (t) {
                case Linear: //Linear Interpolation

                    int iR = 0,
                     iG = 0,
                     iB = 0,
                     iA = 255;

                    for (int i = 0; i < blockSizes.length; i++) {
                        c1 = colors.get(i);
                        c2 = colors.get(i + 1);

                        c1R = c1.getRed();
                        c1G = c1.getGreen();
                        c1B = c1.getBlue();

                        c2R = c2.getRed();
                        c2G = c2.getGreen();
                        c2B = c2.getBlue();

                        for (int j = 0; j < blockSizes[i]; j++) {
                            iR = (int) (c1R + 1.0 * (c2R - c1R) * (j) / blockSizes[i]);
                            iG = (int) (c1G + 1.0 * (c2G - c1G) * (j) / blockSizes[i]);
                            iB = (int) (c1B + 1.0 * (c2B - c1B) * (j) / blockSizes[i]);

                            //System.out.println(iR + " " + iG + " " + iB + " " + iA );
                            //System.out.println(new Color(iR, iG, iB, iA));
                            gradients[counter++] = (new Color(iR, iG, iB));
                        }

                    }

                    break;
                    
                case HSB: //hsb interpolation
                    float iH = 0f,
                     iS = 0f,
                     iL = 0f;
                    float[] c1f = new float[3],
                     c2f = new float[3];

                    for (int i = 0; i < blockSizes.length; i++) {
                        c1 = colors.get(i);
                        c2 = colors.get(i + 1);

                        c1R = c1.getRed();
                        c1G = c1.getGreen();
                        c1B = c1.getBlue();

                        c2R = c2.getRed();
                        c2G = c2.getGreen();
                        c2B = c2.getBlue();

                        Color.RGBtoHSB(c1R, c1G, c1B, c1f);
                        Color.RGBtoHSB(c2R, c2G, c2B, c2f);

                        for (int j = 0; j < blockSizes[i]; j++) {
                            iH = (float) (c1f[0] + (c2f[0] - c1f[0]) * j / blockSizes[i]);
                            iS = (float) (c1f[1] + (c2f[1] - c1f[1]) * j / blockSizes[i]);
                            iL = (float) (c1f[2] + (c2f[2] - c1f[2]) * j / blockSizes[i]);
                            gradients[counter++] = Color.getHSBColor(iH, iS, iL);
                        }
                    }

                    break;
            }



        }

        return gradients;

    }

//    public static BufferedImage generateSampleImage(int[] gradient) {
//        BufferedImage img = new BufferedImage(gradient.length, 50, BufferedImage.TYPE_INT_RGB);
//
//        //Look at the type of data
//        // System.out.println(img.getRaster().getDataBuffer().getClass().getName()); //DataBufferInt
//
//        int[] data = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData(); //I need to figure out this is column first or row first!
//
//        //The databuffer goes with row priority. Filling direction is horizontal i*width + j
//        for (int i = 0; i < img.getHeight(); i++) {
//            for (int j = 0; j < img.getWidth(); j++) {
//                data[i * img.getWidth() + j] = gradient[j]; //data is 2D, gradient is 1D
//                //data[j*img.getHeight()+i] = gradient[j];
//            }
//        }
//
//        return img;
//    }    
    /**
     * Generate the gradient from the color definitions.
     * 
     * Need to interpolate from other algorithms
     */
//    public int[] generateGradientHLS() {
//        
//        int[] gradients = new int[length];
//        int c = defaultColor.getRGB();
//        if (colors.isEmpty()) {
//            for (int i = 0; i < gradients.length; i++) {
//                gradients[i] = c;
//            }
//        } else if (colors.size() == 1) {
//            c = colors.get(0).getRGB();
//            for (int i = 0; i < gradients.length; i++) {
//                gradients[i] = c;
//            }
//        } else {
//            //interpolate from multiple colors
//            if (pos.get(0) != 0.0) {
//                colors.add(0, colors.get(0));
//                pos.add(0, 0.0); //insert 0.0 as first element
//            }
//            //Append 1.0 if not so
//            if (pos.get(pos.size() - 1) != 1.0) {
//                colors.add(colors.get(colors.size() - 1));
//                pos.add(1.0); //append 1.0 as last element
//            }
//
//            int[] blockSizes = new int[pos.size() - 1];
//            for (int i = 0; i < pos.size() - 1; i++) {
//                blockSizes[i] = (int) Math.round((pos.get(i + 1) - pos.get(i)) * length);
//            }
//
////            two loops
//            Color c1;
//            Color c2;
//            
//            ColorSpace hls = ColorSpace.getInstance(ColorSpace.TYPE_HLS);
//            float[] c1f = new float[3];
//            float[] c2f = new float[3];
//            
//            
//
//            int c1R, c1G, c1B, c1A, c2R, c2G, c2B, c2A, iR = 0, iG = 0, iB = 0, iA = 255;
//            int counter = 0;
//
//            for (int i = 0; i < blockSizes.length; i++) {
//                c1 = colors.get(i);
//                c2 = colors.get(i + 1);
//                
//                c1.getColorComponents(hls, c1f);
//                c2.getColorComponents(hls, c2f);  //this should be from 0 - 1
//                
//                
//
//                c1R = c1.getRed();
//                c1G = c1.getGreen();
//                c1B = c1.getBlue();
//                c1A = c1.getAlpha();
//
//                c2R = c2.getRed();
//                c2G = c2.getGreen();
//                c2B = c2.getBlue();
//                c2A = c2.getAlpha();
//
//                //each component is mixed between the two
//                for (int j = 0; j < blockSizes[i]; j++) {
//                    iR = (int) (c1R + 1.0 * (c2R - c1R) * (j) / blockSizes[i]);
//                    iG = (int) (c1G + 1.0 * (c2G - c1G) * (j) / blockSizes[i]);
//                    iB = (int) (c1B + 1.0 * (c2B - c1B) * (j) / blockSizes[i]);
//                    iA = (int) (c1A + 1.0 * (c2A - c1A) * (j) / blockSizes[i]);
//                    //System.out.println(iR + " " + iG + " " + iB + " " + iA );
//                    //System.out.println(new Color(iR, iG, iB, iA));
//                    gradients[counter++] = (new Color(iR, iG, iB, iA)).getRGB();
//                }//end inner for
//
//            }//end all blocks
//
//        }//end else
//
//
//        return gradients;
//    }
//
//    public int[] generateGradientRGB() {
//        //System.out.println("Generate Gradient");
//        int c = defaultColor.getRGB();
//        int[] gradients = new int[length];
//        if (colors.isEmpty()) {
//            for (int i = 0; i < gradients.length; i++) {
//                gradients[i] = c;
//            }
//        } else if (colors.size() == 1) {
//            c = colors.get(0).getRGB();
//            for (int i = 0; i < gradients.length; i++) {
//                gradients[i] = c;
//            }
//        } else {
//            //need to partition the colors
//            //to match the gradient, actually it's better to use deque
//            //all pos values should be between 0.0 - 1.0
//            //insert 0 if not so
//            if (pos.get(0) != 0.0) {
//                colors.add(0, colors.get(0));
//                pos.add(0, 0.0); //insert 0.0 as first element
//            }
//            //Append 1.0 if not so
//            if (pos.get(pos.size() - 1) != 1.0) {
//                colors.add(colors.get(colors.size() - 1));
//                pos.add(1.0); //append 1.0 as last element
//            }
//
//            int[] blockSizes = new int[pos.size() - 1];
////            blockSizes[0] = 0;
////            Iterator<Double> it = pos.iterator();
////            double value=0;
////            int counter=0;
////            while(it.hasNext()){                        
////                value = it.next();
//////                System.out.println(value);
////                blockSizes[counter++] = (int)(value * length);
////            }
//            //for(int i=0; i<pos.size()-1; i++){
//            //    System.out.println(pos.get(i+1) - pos.get(i)); //funny, 1.0 - 0.1 = 0.099999999...even though it's double type
//            //}
//
//
//            for (int i = 0; i < pos.size() - 1; i++) {
//                //System.out.println(pos.get(i));
//                blockSizes[i] = (int) Math.round((pos.get(i + 1) - pos.get(i)) * length);
//            }
//
//
////            System.out.println(Arrays.toString(blockSizes)); //block size is bascially
//
//
//            Color c1;
//            Color c2;
//
//            int c1R, c1G, c1B, c1A, c2R, c2G, c2B, c2A;
//            int iR = 0, iG = 0, iB = 0, iA = 255;
//            float iH = 0f, iS = 0f, iL = 0f;
//            int counter = 0;
//            float[] c1f = new float[3], c2f = new float[3];
//            
//            
//            for (int i = 0; i < blockSizes.length; i++) {
//                c1 = colors.get(i);
//                c2 = colors.get(i + 1);
//                System.out.println(c1 + "" + c2 + blockSizes.length);
//
//
//                c1R = c1.getRed();
//                c1G = c1.getGreen();
//                c1B = c1.getBlue();
//                c1A = c1.getAlpha();
//
//                c2R = c2.getRed();
//                c2G = c2.getGreen();
//                c2B = c2.getBlue();
//                c2A = c2.getAlpha();
//                
//                //System.out.println(Arrays.toString(Color.RGBtoHSB(c1R, c1G, c1B, a)));
//
//                //each component is mixed between the two
//                Color.RGBtoHSB(c1R, c1G, c1B, c1f);
//                Color.RGBtoHSB(c2R, c2G, c2B, c2f);
//                
//                for(int j=0; j<blockSizes[i]; j++){
//                    iH = (float)(c1f[0] + (c2f[0] - c1f[0]) * j / blockSizes[i]); 
//                    iS = (float)(c1f[1] + (c2f[1] - c1f[1]) * j / blockSizes[i]); 
//                    iL = (float)(c1f[2] + (c2f[2] - c1f[2]) * j / blockSizes[i]); 
//                    gradients[counter++] = Color.getHSBColor(iH, iS, iL).getRGB();
//                }
//                
//                //the HSL interpolation is even more misleading!
//                
//                
////                for (int j = 0; j < blockSizes[i]; j++) {
////                    iR = (int) (c1R + 1.0 * (c2R - c1R) * (j) / blockSizes[i]);
////                    iG = (int) (c1G + 1.0 * (c2G - c1G) * (j) / blockSizes[i]);
////                    iB = (int) (c1B + 1.0 * (c2B - c1B) * (j) / blockSizes[i]);
////                    iA = (int) (c1A + 1.0 * (c2A - c1A) * (j) / blockSizes[i]);
////                    //System.out.println(iR + " " + iG + " " + iB + " " + iA );
////                    //System.out.println(new Color(iR, iG, iB, iA));
////                    gradients[counter++] = (new Color(iR, iG, iB, iA)).getRGB();
////                }
//            }
//
//            //System.out.println(counter);
//
//
//        }
//
//        return gradients;
//    }
    public void reset() {
        colors.clear();
        pos.clear();
    }
//
//    public static void main(String[] args) {
//
//        CImageGradient cig = new CImageGradient(100);
//        cig.addColor(Color.RED, 0.0);
//        cig.addColor(Color.BLUE, 0.5);
//        //cig.addColor(Color.YELLOW, 0.9);
//        try {
//            ImageIO.write(CImageGradient.generateSampleImage(cig.generateGradientRGB()), "png", new File("C:/gradient.png"));
//
//
//
//
//
//
//            //System.out.println(Integer.toHexString(Color.red.getRGB()));
//
//
//            //int i = 0xffffffff;
//
//
//            //Integer a = Integer.parseInt("11111111111111111111111111111111", 2); //stupid, it can't even parse! if it's too long!
//            //Integer b = Integer.parseInt("FF");
//            //nteger.parse
//            //Integer b = Integer.parseInt("ff0000ff", 16);
//
//            //System.out.println(Integer.toHexString(a) + " " + Integer.toHexString(b));
//
//            //        double percentage = 0.5;
//            //        
//            //        int a = 0xffff0000; //red
//            //        int b = 0xff0000ff; //blue
//            //        
//            //        int mask1 = 0xffff00ff;
//            //        int mask2 = 0xff00ff00;
//            //        
//            //        int f1 = (int)(256 * percentage);
//            //        int f2 = 256 - f1;
//            //        
//            //        System.out.println(Integer.toHexString((a & mask1)));
//            //        System.out.println(Integer.toHexString((b & mask2)));
//            //        
//            //        System.out.println(Integer.toHexString((a & mask1)*f1));
//            //        System.out.println(Integer.toHexString((b & mask2)*f2));
//
//            //
//            //System.out.println(Integer.toHexString(  
//            //        ((((( a & mask1 ) * f1 ) + ( ( b & mask1 ) * f2 )) >> 8 ) & mask1 ) 
//            //  | ((((( a & mask2 ) * f1 ) + ( ( b & mask2 ) * f2 )) >> 8 ) & mask2 )
//            //        ));
//
//            //bitwise operator is ... still difficult
//        } catch (IOException ex) {
//            Logger.getLogger(CImageGradient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
}
