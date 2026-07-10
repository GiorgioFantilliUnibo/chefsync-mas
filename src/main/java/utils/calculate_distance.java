package utils;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;

/**
 * Custom Jason Internal Action to calculate the spatial distance between coordinates.
 * Encapsulates the Manhattan distance calculation logic.
 *
 * Usage: utils.calculate_distance(X1, Y1, X2, Y2, Distance)
 */
public class calculate_distance extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            int x1 = (int) ((NumberTerm) args[0]).solve();
            int y1 = (int) ((NumberTerm) args[1]).solve();
            int x2 = (int) ((NumberTerm) args[2]).solve();
            int y2 = (int) ((NumberTerm) args[3]).solve();

            int distance = Math.abs(x1 - x2) + Math.abs(y1 - y2);

            return un.unifies(args[4], new NumberTermImpl(distance));
        } catch (Exception e) {
            ts.getLogger().warning("Error executing ia.calculate_distance: " + e.getMessage());
            return false;
        }
    }
}
