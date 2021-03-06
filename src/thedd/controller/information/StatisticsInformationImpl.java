package thedd.controller.information;

import java.util.Collections;
import java.util.List;

import thedd.model.character.BasicCharacter;
import thedd.model.character.statistics.Statistic;
import thedd.model.combat.status.Status;

/**
 * Implementation of {@link StatisticsInformation}.
 */
public final class StatisticsInformationImpl implements StatisticsInformation {

    private BasicCharacter character;
    // This static int represent the number of characters to skip in class path name
    // in getType() method.
    private static final int SUBSTRING_STEPS = 28;

    /**
     * StatisticsInformationImpl's constructor.
     * 
     * @param character the character.
     */
    public StatisticsInformationImpl(final BasicCharacter character) {
        this.character = character;
    }

    @Override
    public List<Status> getCharacterStatuses() {
        return Collections.unmodifiableList(this.character.getStatuses());
    }

    @Override
    public void setCharacter(final BasicCharacter character) {
        this.character = character;
    }

    @Override
    public String getHealthPointValue() {
        return String.valueOf(this.character.getStat(Statistic.HEALTH_POINT).getActual());
    }

    @Override
    public String getConstitutionValue() {
        return String.valueOf(this.character.getStat(Statistic.CONSTITUTION).getActual());
    }

    @Override
    public String getStrengthValue() {
        return String.valueOf(this.character.getStat(Statistic.STRENGTH).getActual());
    }

    @Override
    public String getAgilityValue() {
        return String.valueOf(this.character.getStat(Statistic.AGILITY).getActual());
    }

    @Override
    public String getHealthPointMaxValue() {
        return String.valueOf(this.character.getStat(Statistic.HEALTH_POINT).getMax());
    }

    @Override
    public String getConstitutionMaxValue() {
        return String.valueOf(this.character.getStat(Statistic.CONSTITUTION).getMax());
    }

    @Override
    public String getStrengthMaxValue() {
        return String.valueOf(this.character.getStat(Statistic.STRENGTH).getMax());
    }

    @Override
    public String getAgilityMaxValue() {
        return String.valueOf(this.character.getStat(Statistic.AGILITY).getMax());
    }

    @Override
    public String getCharacterType() {
        return this.character.getClass().getName().substring(SUBSTRING_STEPS);
    }
}
