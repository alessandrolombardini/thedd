package thedd.model.combat.action.effect;

import java.util.Objects;

import thedd.model.character.BasicCharacter;
import thedd.model.combat.actor.ActionActor;
import thedd.model.item.Item;
import thedd.model.item.ItemFactory;

/**
 * ActionEffect specialization
 * which allow to add an Item to a target inventory.
 *
 */
public final class ItemGiverEffect extends AbstractActionEffect {
    //If a luck-like statistic is implemented,
    //then the item can change based on that value with method updateEffectByTarget
    private final Item itemGiven;

    /**
     * Create a new effect which gives a random item to the target. 
     */
    public ItemGiverEffect() {
        super();
        itemGiven = Objects.requireNonNull(ItemFactory.getRandomItem());
    }

    private ItemGiverEffect(final Item itemGiven) {
        super();
        this.itemGiven = Objects.requireNonNull(itemGiven);
    }


    @Override
    public void apply(final ActionActor target) {
        if (target instanceof BasicCharacter) {
            ((BasicCharacter) target).getInventory().addItem(itemGiven);
        } else {
            throw new IllegalArgumentException("This effect can be applied only to ActionActor of type Character");
        }
    }

    @Override
    public String getLogMessage() {
        return "Found " + itemGiven.getName() + ".";
    }

    @Override
    public String getDescription() {
        return "Gives an item to the target.";
    }

    @Override
    public String getPreviewMessage() {
        return getDescription();
    }

    @Override
    public ActionEffect getSpecializedCopy() {
        return new ItemGiverEffect(itemGiven);
    }

}
