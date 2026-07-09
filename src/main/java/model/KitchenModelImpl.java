package model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import view.KitchenView;

/**
 * Concrete implementation of the {@link KitchenModel} interface extending Jason's {@link GridWorldModel}.
 */
public class KitchenModelImpl extends GridWorldModel implements KitchenModel {

    public static final int GSize = 16;

    private Map<String, Workstation> stations = new HashMap<>();
    private KitchenView customView;
    private Map<Integer, String> agentNames = new ConcurrentHashMap<>();
    private Runnable environmentListener;
    
    private Map<Integer, OrderRecord> orders = new ConcurrentHashMap<>();

    
    public KitchenModelImpl() {
        super(GSize, GSize, 10);
        
        addWorkstation(new WorkstationImpl("grill", 2, 3));
        addWorkstation(new WorkstationImpl("oven", 9, 4));
        addWorkstation(new WorkstationImpl("prep_counter", 5, 9));        
        addWorkstation(new WorkstationImpl("fryer", 14, 2));
        addWorkstation(new WorkstationImpl("salad_bar", 1, 14));
        addWorkstation(new WorkstationImpl("stove", 15, 12));
        addWorkstation(new WorkstationImpl("dessert_station", 12, 15));
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
        if (ws == null) return false;

        int agId = -1;
        for (Map.Entry<Integer, String> entry : agentNames.entrySet()) {
            if (entry.getValue().equals(agName)) {
                agId = entry.getKey();
                break;
            }
        }
        if (agId == -1) return false;
        
        Location agLoc = getAgPos(agId);
        if (agLoc == null) return false;
        
        if (Math.max(Math.abs(agLoc.x - ws.getX()), Math.abs(agLoc.y - ws.getY())) > 1) {
            return false;
        }

        if (ws.lock(agName)) {
            if (customView != null) customView.updateView();
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock(String station, String agName) {
        Workstation ws = stations.get(station);
        if (ws != null && ws.unlock(agName)) {
            ws.clearCompletedTask();
            if (customView != null) customView.updateView();
            return true;
        }
        return false;
    }

    @Override
    public boolean moveTowards(int agId, int destX, int destY) {
        Location r1 = this.getAgPos(agId);
        if (r1 == null) return false;
        if (r1.x == destX && r1.y == destY) return true;

        Location bestNext = null;
        int minDistance = Integer.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = r1.x + dx;
                int ny = r1.y + dy;
                
                if (nx < 0 || nx >= getWidth() || ny < 0 || ny >= getHeight()) continue;
                
                if (hasObject(GridWorldModel.AGENT, nx, ny)) continue;
                
                Workstation ws = getWorkstationAt(nx, ny);
                if (ws != null) {
                    String agName = getAgentName(agId);
                    if (!agName.equals(ws.getLockOwner())) {
                        continue;
                    }
                }
                
                int dist = Math.abs(nx - destX) + Math.abs(ny - destY);
                if (dist < minDistance) {
                    minDistance = dist;
                    bestNext = new Location(nx, ny);
                }
            }
        }

        if (bestNext != null) {
            this.setAgPos(agId, bestNext);
            if (customView != null) customView.updateView();
            return true;
        }
        
        return false;
    }

    @Override
    public boolean stepOff(int agId) {
        Location curr = getAgPos(agId);
        if (curr == null) return false;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = curr.x + dx;
                int ny = curr.y + dy;
                
                if (nx >= 0 && nx < getWidth() && ny >= 0 && ny < getHeight()
                    && !hasObject(GridWorldModel.AGENT, nx, ny) 
                    && getWorkstationAt(nx, ny) == null) {
                    
                    setAgPos(agId, nx, ny);
                    if (customView != null) customView.updateView();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getLockOwner(String station) {
        Workstation ws = stations.get(station);
        return ws != null ? ws.getLockOwner() : null;
    }

    @Override
    public boolean startCooking(int agId, String task, int timeMs) {
        Location loc = getAgPos(agId);
        if (loc == null) return false;
        Workstation ws = getWorkstationAt(loc.x, loc.y);
        if (ws == null) return false;
        
        ws.startCooking(task, timeMs, () -> {
            if (customView != null) customView.updateView();
            if (environmentListener != null) environmentListener.run();
        });
        return true;
    }

    @Override
    public void setEnvironmentListener(Runnable listener) {
        this.environmentListener = listener;
    }
    
    @Override
    public void addOrder(int orderId, String dish, List<String> taskNames) {
        List<TaskRecord> tasks = taskNames.stream()
                .map(name -> new TaskRecord(name, null, false))
                .collect(Collectors.toList());
        orders.put(orderId, new OrderRecord(orderId, dish, "PENDING", tasks));
        if (customView != null) customView.updateView();
    }

    @Override
    public void updateOrderStatus(int orderId, String status) {
        orders.computeIfPresent(orderId, (id, order) -> new OrderRecord(id, order.dish(), status, order.tasks()));
        if (customView != null) customView.updateView();
    }

    @Override
    public void assignTask(int orderId, String task, String chef) {
        orders.computeIfPresent(orderId, (id, order) -> {
            List<TaskRecord> updated = order.tasks().stream()
                    .map(t -> t.name().equals(task) ? new TaskRecord(t.name(), chef, t.completed()) : t)
                    .collect(Collectors.toList());
            return new OrderRecord(id, order.dish(), order.status(), updated);
        });
        if (customView != null) customView.updateView();
    }

    @Override
    public void completeTask(int orderId, String task) {
        orders.computeIfPresent(orderId, (id, order) -> {
            List<TaskRecord> updated = order.tasks().stream()
                    .map(t -> t.name().equals(task) ? new TaskRecord(t.name(), t.assignedTo(), true) : t)
                    .collect(Collectors.toList());
            return new OrderRecord(id, order.dish(), order.status(), updated);
        });
        if (customView != null) customView.updateView();
    }
    
    @Override
    public Collection<OrderRecord> getOrders() {
        return orders.values();
    }

    @Override
    public void setView(KitchenView view) {
        this.customView = view;
    }
}
