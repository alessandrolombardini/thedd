package thedd.model.combat.encounter;

import java.util.Set;

import thedd.model.combat.actionexecutor.ActionExecutor;
import thedd.model.combat.actor.ActionActor;

/**
 * A container holding information about the combat that has to start.
 * <p>
 * Used for generating combat encounters, it holds a combat logic entity,
 * and a collection of the enemies encountered in this combat by the player.
 */
public interface HostileEncounter {

    /**
     * Sets the combat logic of this encounter.
     * @param combatLogic the combat logic to be used
     */
    void setCombatLogic(ActionExecutor combatLogic);

    /**
     * Gets the logic of the combat that will start.
     * If the logic is not set, a NoSuchElementException is thrown.
     * 
     * @return the combat logic
     * @throws IllegalStateException if there isn't combat logic
     */
    ActionExecutor getCombatLogic();

    /**
     * Gets the list of hostile NPCs that are going to
     * participate in the skirmish.
     * The iteration order of this list is predictable.
     * @return the list of hostile NPCs
     */
    Set<ActionActor> getNPCs();

    /**
     * Adds an actor to the party opposed to the player.
     * @param npc the actor to be added
     */
    void addNPC(ActionActor npc);

    /**
     * Adds one or more actors to the party oppose to the player.
     * @param npcs the actors to be added
     */
    void addAll(Set<ActionActor> npcs);

}
