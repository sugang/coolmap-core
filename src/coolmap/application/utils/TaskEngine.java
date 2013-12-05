/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package coolmap.application.utils;

import coolmap.application.CoolMapMaster;
import java.util.ArrayDeque;

/**
 *
 * @author gangsu
 */
public class TaskEngine {

    private Thread _workerThread = null;
    private Thread _monitorThread = null;
    private String _taskName;
    private final ArrayDeque<LongTask> _tasks = new ArrayDeque<LongTask>();
    private static TaskEngine _taskEngine;

    public static TaskEngine getInstance() {
        if (_taskEngine == null) {
            _taskEngine = new TaskEngine();
        }
        return _taskEngine;
    }

    private TaskEngine() {
    }

    public synchronized void submitTask(LongTask task) {
        if (task == null) {
            return;
        }
        submitTask(task, task.getName());
    }

    //can add task to the queue
    private synchronized void submitTask(LongTask task, String name) {
        _tasks.add(task);

        if (_workerThread != null && _workerThread.isAlive()) {
            //do nothing
        } else {
            _nextTask();
        }
    }

    public void destroy() {
        if (_workerThread != null && _workerThread.isAlive()) {
            _workerThread.interrupt();
        }
    }

    public static void main(String args[]) {
        TaskEngine exe = new TaskEngine();
    }

//    public void showModularScreen() {
//        CoolMapMaster.getCMainFrame().showBusyDialog(true);
//    }


    public synchronized void cancelcurrentTask() {
        if (_workerThread != null && _workerThread.isAlive()) {
            _workerThread.interrupt();
            _nextTask();
        } else {
            CoolMapMaster.getCMainFrame().hideBusyDialog();
        }
    }

    public synchronized void cancelAll() {
        if (_workerThread != null && _workerThread.isAlive()) {
            _workerThread.interrupt();
            _monitorThread.interrupt();
            _tasks.clear(); //Just clear -> they should not be started yet
        } else {
            CoolMapMaster.getCMainFrame().hideBusyDialog();
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
                Thread.sleep(1000);
                if (_workerThread != null && _workerThread.isAlive()) {
                    CoolMapMaster.getCMainFrame().showBusyDialog(_taskName);
                    while (!Thread.currentThread().isInterrupted()) {

                        if (_workerThread == null || !_workerThread.isAlive() || _workerThread.isInterrupted()) {
                            Thread.currentThread().interrupt();
                        } else {
                            CoolMapMaster.getCMainFrame().showBusyDialog(_taskName);
                        }

                        Thread.sleep(500);
                    }
                    CoolMapMaster.getCMainFrame().hideBusyDialog();
                }

            } catch (InterruptedException e) {
                CoolMapMaster.getCMainFrame().hideBusyDialog();
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

        }
    }

    private synchronized void _nextTask() {
        if (_tasks.size() > 0) {
            try {
                LongTask nextTask = _tasks.pollFirst();
//                System.out.println("Worker thread started");
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
