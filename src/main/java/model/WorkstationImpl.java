package model;

import jason.environment.grid.Location;

/**
 * Concrete implementation of the Workstation domain object.
 */
public class WorkstationImpl implements Workstation {

    private String name;
    private Location location;
    private String lockOwner;
    private boolean isOperational;

    public WorkstationImpl(String name, int x, int y) {
        this.name = name;
        this.location = new Location(x, y);
        this.lockOwner = null;
        this.isOperational = true;
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
}
