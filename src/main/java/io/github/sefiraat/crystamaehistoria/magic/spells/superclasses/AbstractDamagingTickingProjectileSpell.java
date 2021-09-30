package io.github.sefiraat.crystamaehistoria.magic.spells.superclasses;

import io.github.sefiraat.crystamaehistoria.magic.SpellCastInformation;
import lombok.NonNull;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class AbstractDamagingTickingProjectileSpell extends AbstractDamagingTickingSpell implements CastableProjectile {

    private double projectileAoeRange;
    private double projectileKnockbackAmount;

    public AbstractDamagingTickingProjectileSpell() {
        this.projectileAoeRange = getProjectileAoeRange();
        this.projectileKnockbackAmount = getProjectileKnockbackForce();
    }

    @OverridingMethodsMustInvokeSuper
    public void cast(@NonNull SpellCastInformation spellCastInformation) {
        super.cast(spellCastInformation);
        registerTicker(spellCastInformation, getTickInterval(), getNumberTicks());
    }

    protected abstract double getProjectileAoeRange();

    protected abstract double getProjectileKnockbackForce();

}
