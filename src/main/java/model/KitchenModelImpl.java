package model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.HashMap;
import java.util.Map;
import view.KitchenView;

/**
 * Concrete implementation of the {@link KitchenModel} interface extending Jason's {@link GridWorldModel}.
 */
public class KitchenModelImpl extends GridWorldModel implements KitchenModel {

    public static final int GSize = 7;
    
    private Map<String, String> workstationLocks = new HashMap<>();
    private KitchenView customView;

    public KitchenModelImpl() {
        super(GSize, GSize, 2);
        
        this.setAgPos(0, 0, 0);
        this.setAgPos(1, 0, 1);
        
        workstationLocks.put("grill", null);
        workstationLocks.put("oven", null);
        workstationLocks.put("prep_counter", null);
    }

    @Override
    public boolean lock(String station, String agName) {
        if (workstationLocks.containsKey(station) && workstationLocks.get(station) == null) {
            workstationLocks.put(station, agName);
            return true;
        }
        return false;
    }

    @Override
    public boolean unlock(String station, String agName) {
        if (agName.equals(workstationLocks.get(station))) {
            workstationLocks.put(station, null);
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
            customView.update(r1.x, r1.y);
        }
        
        return true;
    }

    @Override
    public String getLockOwner(String station) {
        return workstationLocks.get(station);
    }

    @Override
    public void setView(KitchenView view) {
        this.customView = view;
    }
}
