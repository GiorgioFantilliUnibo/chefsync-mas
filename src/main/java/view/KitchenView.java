package view;

/**
 * Interface representing the visual component of the kitchen environment.
 */
public interface KitchenView {

    /**
     * Notifies the view that a cell in the grid needs rendering update.
     *
     * @param x the X coordinate of the cell
     * @param y the Y coordinate of the cell
     */
    void update(int x, int y);
}
