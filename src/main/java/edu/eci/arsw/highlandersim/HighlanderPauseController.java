package edu.eci.arsw.highlandersim;

public class HighlanderPauseController {

    private boolean paused = false;
    private int pausedCount = 0;
    private final int totalThreads;

    public HighlanderPauseController(int totalThreads) {
        this.totalThreads = totalThreads;
    }
    public synchronized void pauseAndAwaitAll() throws InterruptedException {
        paused = true;
        while (pausedCount < totalThreads) {
            wait();
        }
    }
    public synchronized void checkpoint() throws InterruptedException {
        if (paused) {
            pausedCount++;
            notifyAll();
            while (paused) {
                wait();
            }
        }
    }
    public synchronized void resumeAll() {
        paused = false;
        pausedCount = 0;
        notifyAll();
    }
}
