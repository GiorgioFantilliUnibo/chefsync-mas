package model;

public enum Role {
    HEAD_CHEF("head_chef"), 
    STATION_CHEF("station_chef"), 
    WAITER("waiter");

    private final String prefix;

    Role(String prefix) {
        this.prefix = prefix;
    }

    public static Role fromAgentName(String agName) {
        if (agName == null) return null;
        for (Role r : Role.values()) {
            if (agName.startsWith(r.prefix)) {
                return r;
            }
        }
        return null;
    }
}
