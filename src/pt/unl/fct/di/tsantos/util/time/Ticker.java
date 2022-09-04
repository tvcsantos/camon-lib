package pt.unl.fct.di.tsantos.util.time;

import java.util.Observable;
import java.util.Observer;
import java.util.TimerTask;
import java.util.Vector;

public class Ticker extends TimerTask {

    private static class TickerObservable extends Observable {

        public TickerObservable() {
            super();
        }

        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }
    }
    
    protected final Vector<Runnable> pool;
    protected final Vector<Runnable> work;
    protected long tick;
    protected long curr;
    private TickerObservable obs;
    public static final int RUNNING = 1;
    public static final int IDLE = 2;

    public Ticker(long tick) {
        this(tick, false);
    }

    public Ticker(long tick, boolean start) {
        super();
        this.tick = tick;
        if (start) {
            this.curr = 1;
        } else {
            this.curr = tick;
        }
        this.pool = new Vector<Runnable>();
        this.work = new Vector<Runnable>();
        this.obs = new TickerObservable();
    }

    public void add(Observer observer) {
        synchronized (this) {
            obs.addObserver(observer);
        }
    }

    public void remove(Observer observer) {
        synchronized (this) {
            obs.deleteObserver(observer);
        }
    }

    public void setTick(long tick) {
        synchronized (this) {
            this.tick = tick;
        }
    }

    public void run() {
        synchronized (this) {
            if (work.isEmpty()) {
                if (curr <= 1) {
                    spawn(pool.toArray(new Runnable[pool.size()]));
                    if (work.isEmpty()) {
                        curr = tick;
                    }
                } else {
                    curr = curr - 1;
                }
                obs.setChanged();
                obs.notifyObservers(Ticker.this);
            }
        }
    }

    public void execute() {
        if (work.isEmpty()) {
            spawn(pool.toArray(new Runnable[pool.size()]));
            if (work.isEmpty()) {
                curr = tick;
            }
            obs.setChanged();
            obs.notifyObservers(Ticker.this);
        }
    }

    public boolean isRunning() {
        synchronized (this) {
            return !work.isEmpty();
        }
    }

    public long nextRun() {
        synchronized (this) {
            return curr;
        }
    }

    public boolean add(Runnable task) {
        synchronized (this) {
            return pool.add(task);
        }
    }

    public boolean remove(Runnable task) {
        synchronized (this) {
            return pool.remove(task);
        }
    }

    protected void spawn(Runnable[] pool) {
        synchronized (this) {
            for (final Runnable task : pool) {
                work.add(task);
                new Thread() {

                    @Override
                    public void run() {
                        task.run();
                        Ticker.this.taskEnded(task);
                    }
                }.start();
            }
            //if (work.isEmpty()) curr = tick;
            //obs.setChanged();
            //obs.notifyObservers(Tiker.this);
        }
    }

    protected void taskEnded(Runnable task) {
        synchronized (this) {
            work.remove(task);
            if (work.isEmpty()) {
                curr = tick;
                obs.setChanged();
                obs.notifyObservers(this);
            }
        }
    }
}
