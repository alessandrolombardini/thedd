package model.item;

import java.util.Map;

import model.characters.Statistic;

/**
 * Abstract class that defines the methods which tell whether a item is of a
 * type or not.
 *
 */
public abstract class AbstractItem implements Item {

    private final int id;
    private final String name;
    private final Map<Statistic, Integer> effects;

    /**
     * 
     * @param id
     *          id of the object, used only for comparison and hashing purpose
     * @param name
     *          name of the object
     * @param effects
     *          effects of the object
     */
    public AbstractItem(final int id, final String name, final Map<Statistic, Integer> effects) {
        this.id = id;
        this.name = name;
        this.effects = effects;
    }

    /**
     * 
     * @return
     *          the item id
     */
    protected final int getId() {
        return this.id;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final boolean isEquipable() {
        return this instanceof EquipableItem;
    }

    @Override
    public final boolean isUsable() {
        return this instanceof UsableItem;
    }
    /**
     * 
     * @return
     *          the map of the effects of the item
     */
    protected final Map<Statistic, Integer> getEffects() {
        return effects;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractItem other = (AbstractItem) obj;
        return id == other.id;
    }

    @Override
    public abstract Item copy();

    @Override
    public abstract String toString();

}
