package model;

/**
 * Represents the authorization roles in the kitchen brigade.
 * Used to implement Role-Based Access Control (RBAC) on environment actions.
 */
public enum Role {
    HEAD_CHEF("head_chef"), 
    STATION_CHEF("station_chef"), 
    WAITER("waiter");

    private final String prefix;

    Role(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Determines the role of an agent based on its name prefix.
     *
     * @param agName the name of the agent
     * @return the matched Role, or null if no match is found
     */
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
