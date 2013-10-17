/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import com.sun.media.jai.codec.PNGEncodeParam;
import coolmap.application.CoolMapMaster;
import coolmap.utils.graphics.UI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.*;

/**
 *
 * @author gangsu
 */
public class TaskEngine {

    private Thread _workerThread = null;
    private Thread _monitorThread = null;
    private JDialog _progressDialog;
    private String _taskName;
    private TaskPanel _taskPanel;
    private final ArrayDeque<LongTask> _tasks = new ArrayDeque<LongTask>();

    public TaskEngine() {
        _initUI();
    }

    public synchronized void submitTask(LongTask task) {
        if (task == null) {
            return;
        }
        submitTask(task, task.getName());
    }

    private synchronized void submitTask(LongTask task, String name) {
        _tasks.add(task);

        if (_workerThread != null && _workerThread.isAlive()) {
            //do nothing
        } else {
            _nextTask();
        }
    }

    private void _initUI() {
        _progressDialog = new JDialog(CoolMapMaster.getCMainFrame(), "Executing Task", true);

        _taskPanel = new TaskPanel();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_taskPanel, BorderLayout.CENTER);
        JButton button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (_workerThread != null && _workerThread.isAlive()) {
                    _workerThread.interrupt();
                    _monitorThread.interrupt();
                } else {
                    _progressDialog.setVisible(false);
                }
            }
        });

        panel.add(button, BorderLayout.SOUTH);
        _progressDialog.setContentPane(panel);
//        _progressDialog.setPreferredSize(new Dimension(400,300));
        _progressDialog.setSize(new Dimension(250, 140));
        _progressDialog.setAlwaysOnTop(true);
        _progressDialog.setUndecorated(true);
        //_progressDialog.setVisible(true);
        _progressDialog.setLocationRelativeTo(CoolMapMaster.getCMainFrame());

    }

    public void destroy() {
        if (_workerThread != null && _workerThread.isAlive()) {
            _workerThread.interrupt();
        }
    }

    public static void main(String args[]) {
        TaskEngine exe = new TaskEngine();
    }

    public void showModularScreen() {
        _progressDialog.setVisible(true);
        _progressDialog.pack();
    }

    private class TaskPanel extends JPanel {

        private Image blockloader = UI.blockLoader;
        private Font font = UI.fontPlain.deriveFont(11f).deriveFont(Font.BOLD);

        public TaskPanel() {
        }

        @Override
        protected void paintComponent(Graphics grphcs) {
            super.paintComponent(grphcs);
            grphcs.drawImage(blockloader, getWidth() / 2 - blockloader.getWidth(this) / 2, getHeight() / 2 - blockloader.getHeight(this) / 2 - 10, this);
            Graphics2D g = (Graphics2D) grphcs.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(font);

            int width = g.getFontMetrics().stringWidth("Running: " + _taskName);
            g.setColor(UI.colorBlack3);
            g.drawString("Running: " + _taskName, getWidth() / 2 - width / 2, 100);
        }
    }

    private class MonitorThread extends Thread {

        private long _startTime;

        public MonitorThread() {
            _startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
//            super.run();
            try {
                Thread.sleep(500);
                if (_workerThread != null && _workerThread.isAlive()) {
                    _progressDialog.setVisible(true);
                    while (!Thread.currentThread().isInterrupted()) {

                        if (_workerThread == null || !_workerThread.isAlive() || _workerThread.isInterrupted()) {
                            Thread.currentThread().interrupt();
                        } else {
                            _progressDialog.setVisible(true);
                        }

                        Thread.sleep(200);
                    }
                    _progressDialog.setVisible(false);
                }

            } catch (InterruptedException e) {
                _progressDialog.setVisible(false);
            }
        }
    }

    private class WorkerThread extends Thread {

        private LongTask _task;

        public WorkerThread(LongTask task) {
            super(task);
            this._task = task;
        }

        @Override
        public void run() {
            super.run();

            if (_task != null) {
                _tasks.remove(_task);
            }

            _nextTask();



//            try {
//                if (_monitorThread != null && _monitorThread.isAlive()) {
//                    _monitorThread.interrupt();
//                    throw new InterruptedException();
//                }
//            } catch (InterruptedException e) {
//                //Thread is interrupted
//                System.out.println("Worker interrupted");
//                //cancel all other tasks
////                for(LongTask longTask : _tasks){
////                    
////                }
//                _tasks.clear();
//            } catch (Exception e) {
//                //any other error may have occured. Just mute it or pass it on?
//                e.printStackTrace();
//            } finally {
//                if (_task != null) {
//                    _tasks.remove(_task);
//                }
//                ////////////////////////
//                LongTask nextTask = _tasks.pollFirst();
//                if (nextTask) {
//                                System.out.println("Worker thread started");
//            _workerThread = new WorkerThread(task);
//            _workerThread.start();
//
//            if (name == null || name.length() == 0) {
//                name = "task";
//            }
//
//            _taskName = name;
//
//            _monitorThread = new MonitorThread();
//            _monitorThread.start();
//                }
//            }

        }
    }

    private synchronized void _nextTask() {
        if (_tasks.size() > 0) {
            try {
                LongTask nextTask = _tasks.pollFirst();
                System.out.println("Worker thread started");
                _workerThread = new WorkerThread(nextTask);
                _workerThread.start();

                if (nextTask.getName() == null || nextTask.getName().length() == 0) {
                    _taskName = "task";
                }
                _taskName = nextTask.getName();
                _monitorThread = new MonitorThread();
                _monitorThread.start();
            } catch (Exception e) {
//                e.printStackTrace();
                e.printStackTrace();
            }
        } else {
            if (_monitorThread != null && _monitorThread.isAlive()) {
                //stop the monitor thread, all submitted tasks are done
                _monitorThread.interrupt();
            }
        }
    }
}
