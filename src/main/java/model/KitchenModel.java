package model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashMap;
import java.util.Map;

public class KitchenModel extends GridWorldModel {

    public static final int GSize = 7;
    
    private Map<String, String> workstationLocks = new HashMap<>();


    public KitchenModel() {
        super(GSize, GSize, 2);
        
        this.setAgPos(0, 0, 0);
        this.setAgPos(1, 0, 1);
        
        workstationLocks.put("grill", null);
        workstationLocks.put("oven", null);
        workstationLocks.put("prep_counter", null);
    }

    public boolean lock(String station, String agName) {
        if (workstationLocks.containsKey(station) && workstationLocks.get(station) == null) {
            workstationLocks.put(station, agName);
            return true;
        }
        return false;
    }

    public boolean unlock(String station, String agName) {
        if (agName.equals(workstationLocks.get(station))) {
            workstationLocks.put(station, null);
            return true;
        }
        return false;
    }

    public boolean moveTowards(int agId, int destX, int destY) {
        Location r1 = this.getAgPos(agId);
        if (r1.x < destX) r1.x++;
        else if (r1.x > destX) r1.x--;

        if (r1.y < destY) r1.y++;
        else if (r1.y > destY) r1.y--;

        this.setAgPos(agId, r1);
        
        if (this.view != null) {
            this.view.update(r1.x, r1.y);
        }
        
        return true;
    }
    
    public String getLockOwner(String station) {
        return workstationLocks.get(station);
    }
}
