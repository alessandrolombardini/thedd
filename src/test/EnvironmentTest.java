package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import thedd.model.roomevent.RoomEvent;
import thedd.model.roomevent.combatevent.CombatEvent;
import thedd.model.roomevent.floorchanger.Stairs;
import thedd.model.roomevent.interactableactionperformer.Contraption;
import thedd.model.roomevent.interactableactionperformer.InteractableActionPerformer;
import thedd.model.world.Difficulty;
import thedd.model.world.environment.Environment;
import thedd.model.world.environment.EnvironmentImpl;
import thedd.model.world.floor.Floor;
import thedd.model.world.floor.FloorDetailsFactory;
import thedd.model.world.floor.FloorDetailsFactoryImpl;
import thedd.model.world.floor.details.FloorDetails;
import thedd.model.world.room.Room;
import thedd.model.world.room.RoomFactory;
import thedd.model.world.room.RoomFactoryImpl;

/**
 * This class allows to test combat module.
 */
public class EnvironmentTest {

    private static final int NUMBER_OF_TEST = 10;

    /**
     * Test of new environment.
     */
    @Test
    public void testCreateEnvironement() {
        for (int i = 1; i < NUMBER_OF_TEST; i++) {
            final int numberOfRooms = i + i;
            final int numberOfFloors = i;
            final Environment environment = new EnvironmentImpl(numberOfFloors, numberOfRooms);
            assertEquals(environment.getCurrentFloorIndex(), 0);
            final Floor floor = environment.getCurrentFloor();
            assertTrue(floor.getCurrentRoomIndex() < 0);
            floor.nextRoom();
            assertEquals(floor.getCurrentRoomIndex(), 0);
            assertFalse(floor.checkToChangeFloor());
            assertTrue(floor.hasNextRoom());
        }
    }

    /**
     * Test of floor options.
     */
    @Test
    public void testFloorOptionsWithEasyMode() {
        this.testWithDifferentDifficulty(Difficulty.EASY, true);
        this.testWithDifferentDifficulty(Difficulty.NORMAL, true);
        this.testWithDifferentDifficulty(Difficulty.HARD, true);
        this.testWithDifferentDifficulty(Difficulty.EASY, false);
        this.testWithDifferentDifficulty(Difficulty.NORMAL, false);
        this.testWithDifferentDifficulty(Difficulty.HARD, false);
    }

    private void testWithDifferentDifficulty(final Difficulty difficulty, final boolean boss) {
        final FloorDetailsFactory factory = new FloorDetailsFactoryImpl();
        final int roomForBoss = boss ? 1 : 0;
        for (int i = 1; i < NUMBER_OF_TEST; i++) {
            final FloorDetails details = factory.createFloorDetails(difficulty, i, boss);
            assertEquals(details.isBossFloor(), boss);
            assertTrue(details.getNumberOfEnemies() <= RoomFactoryImpl.MAX_ENEMIES_PER_ROOM * (i - roomForBoss));
            assertTrue(details.getNumberOfEnemies() >= RoomFactoryImpl.MIN_ENEMIES_PER_ROOM * (i - roomForBoss));
            assertEquals(details.getNumberOfRooms(), i);
            assertTrue(details.getNumberOfContraptions() + details.getNumberOfTreasures() 
                        >= RoomFactoryImpl.MIN_INTERACTABLE_ACTION_PER_ROOM * (i - roomForBoss));
            assertTrue(details.getNumberOfContraptions() + details.getNumberOfTreasures() 
                        >= RoomFactoryImpl.MIN_INTERACTABLE_ACTION_PER_ROOM * (i - roomForBoss));
        }
    }

    /**
     * Test boss room.
     */
    @Test
    public void testBossRoom() {
        final int numberOfRooms = 1;
        final int numberOfFloors = 1;
        final Environment environment = new EnvironmentImpl(numberOfFloors, numberOfRooms);
        final Floor floor = environment.getCurrentFloor();
        floor.nextRoom();
        final Room room = floor.getCurrentRoom();
        final CombatEvent event = (CombatEvent) room.getEvents().stream().findAny().get();
        assertEquals(event.getHostileEncounter().getNPCs().stream().findAny().get().getName(), "Dark Destructor");
    }

    /**
     * Test of new stairs.
     */
    @Test
    public void testStairs() {
        final int numberOfRooms = 1;
        final int numberOfFloors = 3;
        final Environment environment = new EnvironmentImpl(numberOfFloors, numberOfRooms);
        Floor floor = environment.getCurrentFloor();
        floor.nextRoom();
        assertTrue(floor.getCurrentRoom().getEvents().stream().findFirst().get() instanceof Stairs);
        environment.setNextFloor(environment.getFloorOptions().stream().findFirst().get());
        floor = environment.getCurrentFloor();
        floor.nextRoom();
        assertTrue(floor.getCurrentRoom().getEvents().stream().findFirst().get() instanceof Stairs);
        environment.setNextFloor(environment.getFloorOptions().stream().findFirst().get());
        floor = environment.getCurrentFloor();
        floor.nextRoom();
        assertTrue(floor.getCurrentRoom().getEvents().stream().findFirst().get() instanceof CombatEvent);
    }

    /**
     * Test of RoomFactory.
     */
    @Test
    public void testRoomFactory() {
        this.testRoomFactoryByDifficulty(Difficulty.EASY);
        this.testRoomFactoryByDifficulty(Difficulty.NORMAL);
        this.testRoomFactoryByDifficulty(Difficulty.HARD);
    }

    private void testRoomFactoryByDifficulty(final Difficulty diff) {
        final FloorDetailsFactory factory = new FloorDetailsFactoryImpl();
        for (int i = EnvironmentImpl.MIN_NUMBER_OF_ROOMS; i < NUMBER_OF_TEST; i++) {
            final FloorDetails details = factory.createFloorDetails(diff, i, false);
            final RoomFactory roomFactory = new RoomFactoryImpl(details);
            int numberOfEnemies = 0;
            int numberOfContraption = 0;
            int numberOfTreasure = 0;
            int numberOfInteractable = 0;
            for (int j = 0; j < i; j++) {
                final List<RoomEvent> events = roomFactory.createRoom().getEvents();
                numberOfEnemies += this.getNumberOfEnemies(events);
                numberOfContraption += this.getNumberOfContraption(events);
                numberOfTreasure += this.getNumberOfTreasure(events);
                numberOfInteractable += this.getNumberOfInteractable(events);
            }
            assertEquals(details.getNumberOfEnemies(), numberOfEnemies);
            assertEquals(details.getNumberOfTreasures(), numberOfTreasure);
            assertEquals(details.getNumberOfContraptions(), numberOfContraption);
            assertEquals(details.getNumberOfTreasures() + details.getNumberOfContraptions(), numberOfInteractable);

        }
    }

    private int getNumberOfContraption(final List<RoomEvent> events) {
        return (int) events.stream().filter(e -> e instanceof Contraption).count();
    }

    private int getNumberOfTreasure(final List<RoomEvent> events) {
        return (int) events.stream().filter(e -> !(e instanceof Contraption) 
                                                 && !(e instanceof Stairs) 
                                                 && !(e instanceof CombatEvent))
                                    .count();
    }


    private int getNumberOfEnemies(final List<RoomEvent> events) {
        return (int) events.stream().filter(e -> e instanceof CombatEvent)
                                    .map(e -> (CombatEvent) e)
                                    .map(e -> e.getHostileEncounter().getNPCs())
                                    .flatMap(e -> e.stream())
                                    .count();
    }

    private int getNumberOfInteractable(final List<RoomEvent> events) {
        return (int) events.stream().filter(e -> e instanceof InteractableActionPerformer).count();
    }

}
