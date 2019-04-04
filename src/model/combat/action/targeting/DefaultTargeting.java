package model.combat.action.targeting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import model.combat.action.Action;
import model.combat.action.TargetType;
import model.combat.actor.ActionActor;
import model.combat.instance.ActionExecutionInstance;

/**
 *  Default implementation of the {@link ActionTargeting}
 *  interface.
 */
public class DefaultTargeting implements ActionTargeting {

    /**
     * It targets only the main provided target.
     */
    @Override
    public List<ActionActor> getTargets(final ActionActor target, final List<ActionActor> availableTargets) {
        final List<ActionActor> targets = new ArrayList<>();
        targets.add(Objects.requireNonNull(target));
        return targets;
    }

    /**
     * Returns as valid targets:<br>
     * -The party of the {@link ActionActor} executing the
     *  action if the {@link TargetType} is ALLY<br>
     * -The party opposed to the one of the actor executing
     *  the action if the TargetType is FOE<br>
     * -Every actor in the instance if the TargetType is
     *  EVERYONE<br>
     * -The actor executing the action if the TargetType is
     *  SELF.
     */
    @Override
    public List<ActionActor> getValidTargets(final ActionExecutionInstance combatInstance, final Action sourceAction) {
        final ActionActor source = sourceAction.getSource().get();
        final TargetType targetType = sourceAction.getTargetType();
        switch (targetType) {
        case ALLY:
            return combatInstance.getNPCsParty().contains(source) ? combatInstance.getPlayerParty() 
                            : combatInstance.getNPCsParty();
        case EVERYONE:
            return combatInstance.getAllParties();
        case FOE:
            return combatInstance.getPlayerParty().contains(source) ? combatInstance.getNPCsParty()
                            : combatInstance.getPlayerParty();
        case SELF:
            return Collections.singletonList((ActionActor) source);
        default:
            throw new IllegalStateException("Target type of the action was not found");
        }
    }

}
