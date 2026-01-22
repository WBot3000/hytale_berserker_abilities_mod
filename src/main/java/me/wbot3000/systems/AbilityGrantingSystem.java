package me.wbot3000.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.ABILITY_FLAGS;
import me.wbot3000.components.AbilityGranter;
import me.wbot3000.components.BerserkerAbilities;
import me.wbot3000.components.Rage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

//Handles the granting of Berserker abilities to players. Alters the ability bitfield and does any additional setup needed when the ability is granted.
public class AbilityGrantingSystem extends RefChangeSystem<EntityStore, AbilityGranter> {

    private final ComponentType<EntityStore, AbilityGranter> abilityGranterComponentType = AbilityGranter.getComponentType();

    @Nonnull
    private final Query<EntityStore> query;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    //Modifiers for abilities
    private static final StaticModifier BULKY_I_MODIFIER = new StaticModifier(Modifier.ModifierTarget.MAX, StaticModifier.CalculationType.ADDITIVE, 50);


    public AbilityGrantingSystem() {
        this.query = BerserkerAbilities.getComponentType();
    }


    @Nonnull
    public ComponentType<EntityStore, AbilityGranter> componentType() {
        return this.abilityGranterComponentType;
    }


    @Nonnull
    public Query<EntityStore> getQuery() {
        return this.query;
    }


    //NOTE: Heavy Weapons Guy I does all of it's work in the HeavyWeaponsBuffSystem component, which is why it isn't done here
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AbilityGranter granter, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        BerserkerAbilities abilities = store.getComponent(ref, BerserkerAbilities.getComponentType());

        assert abilities != null;
        long old_abilities_bitfield = abilities.get_current_abilities_bitfield();
        long ability_as_long = granter.get_ability();
        boolean give_ability = granter.should_give_ability();

        if(give_ability) {
            abilities.set_ability_flag(ability_as_long);
        }
        else {
            abilities.clear_ability_flag(ability_as_long);
        }

        //Bitfield remains the same, don't change anything
        //This check is necessary, otherwise the functions below would apply the same ability multiple times
        if (old_abilities_bitfield == abilities.get_current_abilities_bitfield()) {
            return;
        }

        //TODO: Bad way to do this, but having trouble with function handles in Java
        if (ability_as_long == ABILITY_FLAGS.RAGE.enumToLong()) {
            changeRageAbility(give_ability, ref, store, commandBuffer);
        }
        if (ability_as_long == ABILITY_FLAGS.BULKY_I.enumToLong()) {
            changeBulkyIAbility(give_ability, ref, store, commandBuffer);
        }
       //NOTE: Heavy Weapons Guy I doesn't need this, because all the work is done in the HeavyWeaponsBuffSystem

        commandBuffer.removeComponent(ref, this.abilityGranterComponentType);
    }

    @Override
    public void onComponentSet(@NonNullDecl Ref<EntityStore> ref, @NullableDecl AbilityGranter abilityGranter, @NonNullDecl AbilityGranter t1, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
    }

    @Override
    public void onComponentRemoved(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AbilityGranter abilityGranter, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
    }


    public void changeRageAbility(@Nonnull boolean giveAbility, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        if (giveAbility) { //Flag is present, apply ability
            //The component that enables Rage
            //NOTE: When adding or removing components, use commandBuffer. store works for getting components.
            commandBuffer.addComponent(ref, Rage.getComponentType());
        }
        else { //Flag is NOT present, remove ability
            commandBuffer.removeComponent(ref, Rage.getComponentType());
        }
    }

    public void changeBulkyIAbility(@Nonnull boolean giveAbility, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        EntityStatMap stats = store.getComponent(ref, EntityStatMap.getComponentType());
        //This is how you can reference the player's health stat
        int healthIdx = DefaultEntityStatTypes.getHealth();

        assert stats != null;
        if (giveAbility) { //Flag is present, apply ability
            stats.putModifier(healthIdx, "BerserkerAbilitiesBulkyI", BULKY_I_MODIFIER);
            LOGGER.atInfo().log("Modifier added");
        } else { //Flag is NOT present, remove ability
            stats.removeModifier(healthIdx, "BerserkerAbilitiesBulkyI");
            LOGGER.atInfo().log("Modifier removed");
        }
    }
}
