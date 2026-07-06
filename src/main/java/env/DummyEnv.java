package env;

import jason.asSyntax.Structure;
import jason.environment.Environment;

public class DummyEnv extends Environment {

    @Override
    public void init(String[] args) {
        System.out.println("Dummy Environment initialized.");
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        if (action.getFunctor().equals("test_action")) {
            System.out.println("Action 'test_action' successfully executed by " + agName);
            return true;
        }
        return false;
    }
}