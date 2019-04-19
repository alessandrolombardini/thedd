package thedd.model.character.types;

import java.util.ArrayList;
import java.util.List;

import thedd.model.character.BasicCharacterImpl;
import thedd.model.combat.action.effect.ActionEffect;
import thedd.model.combat.modifier.DamageModifier;
import thedd.model.combat.modifier.Modifier;
import thedd.model.combat.modifier.ModifierActivation;
import thedd.model.combat.requirements.tags.TagRequirement;
import thedd.model.combat.requirements.tags.TagRequirementType;
import thedd.model.combat.tag.EffectTag;
import thedd.model.combat.tag.Tag;

/**
 * Dark Destructor extension of {@link thedd.model.character.BasicCharacterImpl}.
 */
public class DarkDestructor extends BasicCharacterImpl {

    /**
     * DarkDestructor's constructor.
     * 
     * @param name       name of this Boss.
     * @param multiplier rate multiplied at the basic statistics.
     */
    public DarkDestructor(final String name, final double multiplier) {
        super(name, multiplier);
        // ret.addWeightedAction(new ActionImpl() , RandomActionPrority.DEFAULT);
        setPermanentModifiers();
    }

    private void setPermanentModifiers() {
        final ModifierActivation defensive = ModifierActivation.ACTIVE_ON_DEFENCE;
        List<Tag> requiredTags = new ArrayList<Tag>();
        List<Tag> allowedTags = new ArrayList<Tag>();

        //Resistance to physical damage
        final Modifier<ActionEffect> damageResistance = new DamageModifier(-0.3, true, defensive);
        allowedTags.add(EffectTag.NORMAL_DAMAGE);
        allowedTags.add(EffectTag.AP_DAMAGE);
        damageResistance.addRequirement(new TagRequirement<>(false, TagRequirementType.ALLOWED, allowedTags));
        addEffectModifier(damageResistance, true);

        //Weakness to holy damage
        final Modifier<ActionEffect> holyDmgWeakness = new DamageModifier(0.2, true, defensive);
        requiredTags.add(EffectTag.HOLY_DAMAGE);
        holyDmgWeakness.addRequirement(new TagRequirement<>(false, TagRequirementType.REQUIRED, requiredTags));
        addEffectModifier(holyDmgWeakness, true);
    }
}