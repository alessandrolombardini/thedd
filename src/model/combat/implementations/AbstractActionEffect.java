package model.combat.implementations;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.combat.enums.ModifierActivation;
import model.combat.interfaces.ActionActor;
import model.combat.interfaces.ActionEffect;
import model.combat.interfaces.Tag;

/**
 * Abstract implementation of an ActionEffect containing all the standard
 * shared behavior of Effects.
 */
public abstract class AbstractActionEffect implements ActionEffect {

    private final Set<Tag> tags = new LinkedHashSet<>();
    private final Set<Tag> permanentTags = new LinkedHashSet<>();
    private final Set<Tag> targetTags = new LinkedHashSet<>();
    private final Set<Tag> sourceTags = new LinkedHashSet<>();

    @Override
    public abstract void apply(ActionActor target);

    @Override
    public abstract String getLogMessage();

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEffectByTarget(final ActionActor target) {
        targetTags.clear();
        targetTags.addAll(target.getTags());
        target.getEffectModifiers().stream()
                                    .filter(m -> m.getModifierActivation() == ModifierActivation.ACTIVE_ON_DEFENCE
                                            || m.getModifierActivation() == ModifierActivation.ALWAYS_ACTIVE)
                                    .filter(m -> m.accept(this))
                                    .forEach(m -> m.modify(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEffectBySource(final ActionActor source) {
        sourceTags.clear();
        sourceTags.addAll(source.getTags());
        source.getEffectModifiers().stream()
                                    .filter(m -> m.getModifierActivation() == ModifierActivation.ACTIVE_ON_ATTACK
                                            || m.getModifierActivation() == ModifierActivation.ALWAYS_ACTIVE)
                                    .filter(m -> m.accept(this))
                                    .forEach(m -> m.modify(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTags(final Set<Tag> tags, final boolean arePermanent) {
        if (arePermanent) {
            permanentTags.addAll(tags);
        } else {
            tags.addAll(tags.stream().filter(permanentTags::contains).collect(Collectors.toSet()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTag(final Tag tag, final boolean isPermanent) {
        if (isPermanent) {
            permanentTags.add(tag);
        } else if (!permanentTags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTargetTags() {
        return Collections.unmodifiableSet(targetTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getSourceTags() {
        return Collections.unmodifiableSet(sourceTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(Stream.concat(tags.stream(), permanentTags.stream())
                .collect(Collectors.toSet()));
    }

    /*/**
     * Equals override: confronts two effects based on their Tags,
     * their source/target Tags and their log message.
     */
    /*@Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ActionEffect)) {
            return false;
        }
        final ActionEffect o = ((ActionEffect) other);
        return getTags().equals(o.getTags())
                && getTargetTags().equals(o.getTargetTags())
                && getSourceTags().equals(o.getSourceTags())
                && getLogMessage().contentEquals(o.getLogMessage()); //BE CAREFUL
    }*/

    /*/**
     * hashCode override: hashes an effects based on their Tags,
     * their source/target Tags and their log message as per 
     * Objects.hash() method.
     */
    /*@Override
    public int hashCode() {
        return Objects.hash(getTags(), getTargetTags(), getSourceTags(), getLogMessage());
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeTag(final Tag tag) {
        return tags.remove(tag);
    }

}
