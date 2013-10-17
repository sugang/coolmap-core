/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.widget.impl;

import com.javadocking.dockable.DockableState;
import coolmap.application.widget.Widget;
import coolmap.utils.graphics.UI;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.text.DecimalFormat;
import javax.swing.*;

/**
 *
 * @author gangsu
 */
public class WidgetMemoryUsage extends Widget {
    
    private MemoryTracker _tracker;
    private JProgressBar _progressBar = new JProgressBar();
//    private Double[][] data = new Double[15000][15000];
    private JLabel _label = new JLabel("Memory tracker stopped.");
    private DecimalFormat _format = new DecimalFormat("#.#");
    
    public WidgetMemoryUsage() {
        super("Memory Usage", W_MODULE, L_LEFTTOP, UI.getImageIcon("dashboard"), "A widget that tracks memory usage.");
        JComponent component = getContentPane();
        component.setLayout(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        component.add(toolBar, BorderLayout.NORTH);
        
        _progressBar = new JProgressBar();
        _progressBar.setValue(0);
        _progressBar.setStringPainted(false);
        _progressBar.setEnabled(false);
        component.add(_progressBar, BorderLayout.CENTER);
        
        JButton button1 = new JButton("Start", UI.getImageIcon("play"));
        button1.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (_tracker == null || !_tracker.isAlive()) {
                    _tracker = new MemoryTracker();
                    _tracker.start();
                }
            }
        });
        
        
        JButton button2 = new JButton("Stop", UI.getImageIcon("pause"));
        button2.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (_tracker != null && _tracker.isAlive()) {
                    _tracker.interrupt();
                }
            }
        });
        
        
        JButton button3 = new JButton("Clean", UI.getImageIcon("pacman"));
        button3.setToolTipText("Submit a system garbage collection process to release more memory.");
        button3.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
//                Thread t = new Thread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        System.gc();
//                    }
//                });
//                t.start();
                System.gc();
            }
        });
        
        toolBar.add(button1);
        toolBar.add(button2);
        toolBar.addSeparator();
        toolBar.add(button3);
        toolBar.setFloatable(false);
        _progressBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        
        component.add(_label, BorderLayout.SOUTH);
        
    }
    
    private class MemoryTracker extends Thread {
        
        public MemoryTracker() {
            this.setDaemon(true);
        }
        
        @Override
        public void run() {
            _progressBar.setValue(0);
            _progressBar.setStringPainted(true);
            _progressBar.setEnabled(true);
            MemoryPoolMXBean tenuredGenPool = null;
            for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
                if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
                    tenuredGenPool = pool;
                }
            }
            
            while (!Thread.currentThread().isInterrupted() && tenuredGenPool != null) {
                try {
//                    System.out.println(tenuredGenPool.getUsage().getUsed());
//                    System.out.println(tenuredGenPool.getUsage().getCommitted());
//                    System.out.println(tenuredGenPool.getUsage().getMax());
//                    System.out.println(tenuredGenPool.getUsage());
                    
                    float used = tenuredGenPool.getUsage().getUsed();
                    float committed = tenuredGenPool.getUsage().getCommitted();
                    float max = tenuredGenPool.getUsage().getMax();
                    
                    _progressBar.setValue((int) (100.0 * used / committed));

                    //System.out.println((int)(100.0 /( used / committed )));
                    
                    _label.setText("Used:" + _format.format(1.0 * used / (1024 * 1024)) + "Mb, Committed:" + _format.format(1.0 * committed / (1024 * 1024)) + "Mb, Max:" + _format.format(1.0 * max / (1024 * 1024)) + "Mb");
                    
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            _progressBar.setStringPainted(false);
            _progressBar.setEnabled(false);
            
            _label.setText("Memory tracker stopped.");
        }
    }
}
