package model;

import jason.environment.grid.Location;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Concrete implementation of the Workstation domain object.
 */
public class WorkstationImpl implements Workstation {

    private String name;
    private Location location;
    private String lockOwner;
    private boolean isOperational;
    
    private String completedTask;
    private Timer timer;

    public WorkstationImpl(String name, int x, int y) {
        this.name = name;
        this.location = new Location(x, y);
        this.lockOwner = null;
        this.isOperational = true;
        this.completedTask = null;
        this.timer = new Timer(name + "_timer", true);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public int getX() {
        return location.x;
    }

    @Override
    public int getY() {
        return location.y;
    }

    @Override
    public String getLockOwner() {
        return lockOwner;
    }

    @Override
    public boolean isLocked() {
        return lockOwner != null;
    }

    @Override
    public synchronized boolean lock(String agName) {
        if (lockOwner == null) {
            lockOwner = agName;
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean unlock(String agName) {
        if (agName.equals(lockOwner)) {
            lockOwner = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean isOperational() {
        return isOperational;
    }

    @Override
    public void setOperational(boolean operational) {
        this.isOperational = operational;
    }

    @Override
    public synchronized void startCooking(String task, int timeMs, Runnable onComplete) {
        this.completedTask = null;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (WorkstationImpl.this) {
                    completedTask = task;
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }, timeMs);
    }

    @Override
    public synchronized String getCompletedTask() {
        return completedTask;
    }

    @Override
    public synchronized void clearCompletedTask() {
        this.completedTask = null;
    }
}
