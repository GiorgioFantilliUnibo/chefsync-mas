package model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import view.KitchenView;

/**
 * Concrete implementation of the {@link KitchenModel} interface extending Jason's {@link GridWorldModel}.
 */
public class KitchenModelImpl extends GridWorldModel implements KitchenModel {

    public static final int GSize = 12;

    private Map<String, Workstation> stations = new HashMap<>();
    private KitchenView customView;
    private Map<Integer, String> agentNames = new ConcurrentHashMap<>();
    private int fps = 2;

    public KitchenModelImpl() {
        super(GSize, GSize, 10);
        
        addWorkstation(new WorkstationImpl("grill", 2, 3));
        addWorkstation(new WorkstationImpl("oven", 9, 4));
        addWorkstation(new WorkstationImpl("prep_counter", 5, 9));
    }

    @Override
    public void addWorkstation(Workstation ws) {
        stations.put(ws.getName(), ws);
        this.add(GridWorldModel.OBSTACLE, ws.getX(), ws.getY());
    }

    @Override
    public Collection<Workstation> getWorkstations() {
        return stations.values();
    }

    @Override
    public void addAgent(int agId, String name, int x, int y) {
        agentNames.put(agId, name);
        this.setAgPos(agId, x, y);
    }

    @Override
    public String getAgentName(int agId) {
        return agentNames.get(agId);
    }

    @Override
    public Workstation getWorkstationAt(int x, int y) {
        for (Workstation ws : stations.values()) {
            if (ws.getX() == x && ws.getY() == y) {
                return ws;
            }
        }
        return null;
    }

    @Override
    public boolean lock(String station, String agName) {
        Workstation ws = stations.get(station);
        if (ws != null && ws.lock(agName)) {
            if (customView != null) customView.updateView();
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock(String station, String agName) {
        Workstation ws = stations.get(station);
        if (ws != null && ws.unlock(agName)) {
            if (customView != null) customView.updateView();
            return true;
        }
        return false;
    }

    @Override
    public boolean moveTowards(int agId, int destX, int destY) {
        Location r1 = this.getAgPos(agId);
        if (r1.x < destX) r1.x++;
        else if (r1.x > destX) r1.x--;

        if (r1.y < destY) r1.y++;
        else if (r1.y > destY) r1.y--;

        this.setAgPos(agId, r1);
        
        if (customView != null) {
            customView.updateView();
        }
        
        return true;
    }

    @Override
    public String getLockOwner(String station) {
        Workstation ws = stations.get(station);
        return ws != null ? ws.getLockOwner() : null;
    }

    @Override
    public void setView(KitchenView view) {
        this.customView = view;
    }

    @Override
    public int getFPS() {
        return fps;
    }

    @Override
    public void setFPS(int fps) {
        this.fps = fps > 0 ? fps : 1;
    }
}
