package model.combat.actionexecutor;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import model.combat.action.Action;
import model.combat.action.ActionCategory;
import model.combat.action.TargetType;
import model.combat.action.result.ActionResult;
import model.combat.action.result.ActionResultImpl;
import model.combat.action.result.ActionResultType;
import model.combat.actor.ActionActor;
import model.combat.actor.automatic.AutomaticActionActor;
import model.combat.instance.ActionExecutionInstance;
import model.combat.instance.ExecutionInstanceImpl;
import model.combat.instance.CombatStatus;
import model.combat.status.Status;
import model.combat.tag.TagImpl;

/**
 *  Logic of a default combat<p>
 *  By default, actors are sorted comparing their priority, obtained via {@link ActionActor#getPriority()},
 *  if the priority is found to be equal and one actor is already placed in the queue, then that actor takes
 *  priority.<p>
 *  Actors' statuses are also updated at the start of every actor's turn, their provided actions are executed
 *  first and, at the end of the actor's turn, if expired, statuses are removed and their expiring action is
 *  executed before passing to the next queued actor.<p>
 *  The action selected by and actor which has successfully parried an action
 */
public class DefaultCombatActionExecutor implements ActionExecutor {

    private Optional<Action> currentAction = Optional.empty();
    private Optional<ActionActor> currentActor = Optional.empty();
    private ActionExecutionInstance combatInstance = new ExecutionInstanceImpl();
    private Optional<ActionResult> currentActionResult = Optional.empty();
    private final List<Action> actionsQueue = new LinkedList<>(); //A queue of actions that will be executed before the current actor's one
    private final List<ActionActor> actorsQueue = new LinkedList<>();
    private final Comparator<ActionActor> actorsSortingOrder = new Comparator<ActionActor>() {
        @Override
        public int compare(final ActionActor a, final ActionActor b) {
            final int aPriority = a.getPriority();
            final int bPriority = b.getPriority();
            if (aPriority == bPriority) {
                if (actorsQueue.contains(a) && !actorsQueue.contains(b)) {
                    return -1;
                }
                if (actorsQueue.contains(b) && !actorsQueue.contains(a)) {
                    return 1;
                }
            }
            return bPriority - aPriority;
        }
    };

    /**
     * Public constructor.
     */
    public DefaultCombatActionExecutor() {
        this(Collections.<ActionActor>emptyList(), Collections.<ActionActor>emptyList());
    }

    /**
     * Public constructor.
     * @param hostileNPCs the List of Actors to placed in the party opposed to player's
     */
    public DefaultCombatActionExecutor(final List<ActionActor> hostileNPCs) {
        this(hostileNPCs, Collections.<ActionActor>emptyList());
    }

    /**
     * Public constructor.
     * @param hostileNPCs the List of Actors to placed in the party opposed to player's
     * @param partyMembers the List of Actors to placed in the player's party
     */
    public DefaultCombatActionExecutor(final List<ActionActor> hostileNPCs, final List<ActionActor> partyMembers) {
        combatInstance.addNPCsPartyMembers(hostileNPCs);
        combatInstance.addPlayerPartyMembers(partyMembers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNextAction() {
        if (actionsQueue.isEmpty()) {
            if (!currentActor.isPresent()) {
                currentActor = Optional.of(actorsQueue.get(0));
                updateTurnStartStatuses(currentActor.get());
            }
            currentAction = currentActor.get().getNextQueuedAction();
        } else {
            currentAction = Optional.of(actionsQueue.get(0));
            actionsQueue.remove(0);
        }
        if (currentAction.isPresent()) {
            combatInstance.setCombatStatus(CombatStatus.ROUND_IN_PROGRESS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCurrentAction() {
        if (currentAction.isPresent() && currentActionResult.isPresent()) {
            final Action action = currentAction.get();
            currentActionResult.get()
                               .getResults()
                               .stream()
                               .filter(p -> p.getValue() == ActionResultType.HIT)
                               .forEach(p -> {
                                   final ActionActor target = p.getKey();
                                   action.applyEffects(target);
                                   //TODO: check if the target is k.o.
                                   if (canTargetParry(target)) {
                                       target.resetSelectedAction();
                                   }
                                   updateNewlyAppliedStatuses(target);
                                });
            final Optional<ActionActor> parryingActor = currentActionResult.get()
                                                                           .getResults()
                                                                           .stream()
                                                                           .filter(p -> p.getValue() == ActionResultType.PARRIED)
                                                                           .findFirst()
                                                                           .map(p -> p.getKey());
            if (parryingActor.isPresent()) {
                if (combatInstance.getNPCsParty().contains(parryingActor.get())) { //Maybe check if target is Automatic AND his behavior is active
                    setNextAIMove((AutomaticActionActor) parryingActor.get());
                } else {
                    combatInstance.setCombatStatus(CombatStatus.ROUND_PAUSED);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ActionResult> evaluateCurrentAction() {
        if (currentAction.isPresent()) {
            final Action action = currentAction.get();
            final ActionResult result = new ActionResultImpl(action);
            boolean interrupted = false;
            final List<ActionActor> targets = action.getTargets();
            for (int i = 0; i < targets.size() && !interrupted; i++) {
                final ActionActor target = targets.get(i);
                action.rollToHit(target);
                if (action.isTargetHit()) {
                    result.addResult(target, ActionResultType.HIT);
                } else if (canTargetParry(target)) {
                    result.addResult(target, ActionResultType.PARRIED);
                } else {
                    result.addResult(target, ActionResultType.MISSED);
                }
            }
            currentActionResult = Optional.of(result);
        } else {
            currentActionResult = Optional.empty();
        }
        return currentActionResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateExecutionStatus() {
        if (currentActor.isPresent() && currentActor.get().getActionQueue().isEmpty()) {
            final ActionActor actor = currentActor.get();
            updateExpiredStatuses(actor);
            if (actor.getActionQueue().isEmpty()) {
                actorsQueue.remove(actor);
                currentActor = Optional.empty();
            }
        }

        if (combatInstance.getCombatStatus() == CombatStatus.ROUND_PAUSED) {
            return;
        }

        if (actorsQueue.isEmpty() && actionsQueue.isEmpty() 
                && combatInstance.getCombatStatus() == CombatStatus.ROUND_IN_PROGRESS) {
            combatInstance.setCombatStatus(CombatStatus.ROUND_ENDED);
            combatInstance.getAllParties().forEach(ActionActor::resetSelectedAction);
            combatInstance.getAllParties().forEach(a -> a.getStatuses().forEach(s -> s.setIsUpdated(false)));
        }

        /*if(combatInstance.getAllies().stream().allMatch(c -> c.getCharacter().getHealth == 0)) {
            combatInstance.setCombatStatus(CombatStatus.PLAYER_LOST);
            //reset characters turnOrder variable and isInCombat value
        }

        if(combatInstance.getHostiles().stream().allMatch(c -> c.getCharacter().getHealth == 0)) {
            combatInstance.setCombatStatus(CombatStatus.PLAYER_WON);
            //reset characters turnOrder variable and isInCombat value
        }*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addActorToQueue(final ActionActor actor) {
        if (combatInstance.getCombatStatus() == CombatStatus.ROUND_PAUSED) {
            actionsQueue.add(0, actor.getSelectedAction().get());
        } else {
            actorsQueue.add(actor);
            actorsQueue.sort(actorsSortingOrder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startExecutor() { 
        combatInstance.setCombatStatus(CombatStatus.STARTED);
        combatInstance.getAllParties().forEach(a -> a.setIsInCombat(true));
        prepareNextRound();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareNextRound() {
        combatInstance.increaseRoundNumber();
        setNextAIMoves();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionInstance(final ActionExecutionInstance newInstance) {
        combatInstance = newInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CombatStatus getExecutionStatus() {
        return combatInstance.getCombatStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRoundReady() {
        return actorsQueue.size() >= combatInstance.getPlayerParty().size() + combatInstance.getNPCsParty().size() 
                || combatInstance.getCombatStatus() == CombatStatus.ROUND_PAUSED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ActionActor> getOrderedActorsList() {
        return Collections.unmodifiableList(combatInstance.getAllParties().stream()
                                                                           .sorted(actorsSortingOrder)
                                                                           .collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionExecutionInstance getExecutionInstance() {
        return combatInstance.getCopy();
    }

    /**
     * Tells all the actors in the hostile party to set their next
     * actions and targets.
     */
    protected void setNextAIMoves() {
        for (final ActionActor npc : combatInstance.getNPCsParty()) { //filter out those who cannot act (stunned and whatnot)
            if (npc instanceof AutomaticActionActor) {
                setNextAIMove((AutomaticActionActor) npc);
            } else {
                throw new IllegalStateException("Only AutomaticActionActors are allowed in the NPCs party");
            }
        }
    }

    /**
     * Tells an Actor to set its next action and targets.
     * @param actor the actor that will prepare its next move
     */
    protected void setNextAIMove(final AutomaticActionActor actor) {
        actor.selectNextMove(combatInstance.getCopy());
        addActorToQueue(actor);
    }

    /**
     * Determines whether a target actor can actively block the 
     * current action.
     * @param target the target to be tested
     * @return true of the target can parry the action, false otherwise
     */
    protected boolean canTargetParry(final ActionActor target) {
        return target.getSelectedAction().isPresent()
                && currentAction.get().getTargetType() != TargetType.SELF
                && currentAction.get().getCategory() != ActionCategory.STATUS
                && target.getSelectedAction().get().getTags().contains(new TagImpl("PARRY")) //&& (parryAction).canparry(sourceAction)
                && !actorsQueue.contains(target);
    }

    private void updateTurnStartStatuses(final ActionActor actor) {
        actor.getStatuses().stream()
                           .filter(s -> !s.isUpdated())
                           .forEach(s -> {
                               s.update(combatInstance);
                               if (s.getCurrentDuration() >= 0 || s.isPermanent()) {
                                   s.getAction().ifPresent(a -> {
                                           actor.insertActionIntoQueue(0, a, false);
                                   });
                               }
                           });
    }

    private void updateExpiredStatuses(final ActionActor actor) {
        final List<Status> toRemove = actor.getStatuses().stream()
                                                         .filter(s -> s.getCurrentDuration() <= 0)
                                                         .collect(Collectors.toList());
        toRemove.forEach(s -> {
            s.update(combatInstance);
            s.getAction().ifPresent(a -> {
                actor.addActionToQueue(a, false);
                actor.removeStatus(s);
            });
        });
    }

    private void updateNewlyAppliedStatuses(final ActionActor target) {
        target.getStatuses().stream()
                            .filter(s -> !s.isUpdated())
                            .filter(s -> s.getBaseDuration() == s.getCurrentDuration())//this means that they must have been applied just now
                            .forEach(s -> {
                                s.update(combatInstance);
                                s.getAction().ifPresent(actionsQueue::add);
                            });
    }

}
