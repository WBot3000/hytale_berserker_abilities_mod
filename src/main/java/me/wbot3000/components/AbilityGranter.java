package me.wbot3000.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.ABILITY_FLAGS;
import me.wbot3000.Main;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

//This component is used to give or take away abilities.
//This is used so that the BerserkerAbilities component (which keeps track of all abilities obtained) doesn't have to be repeatedly added/removed
//Just like how the Teleport component works
public class AbilityGranter implements Component<EntityStore> {

    @Nonnull
    public static final BuilderCodec<AbilityGranter> CODEC;

    private long ability_as_long = 1; //Defaults to Rage
    private boolean give_ability = true; //True gives the ability, false takes it away


    static {
        CODEC = (BuilderCodec.builder(AbilityGranter.class, AbilityGranter::new)
                .append(new KeyedCodec<Long>("AbilityAsLong", Codec.LONG),
                        (component, _ability) -> component.ability_as_long = _ability,
                        (component) -> component.ability_as_long)
                .add()
                .append(new KeyedCodec<Boolean>("GiveAbility", Codec.BOOLEAN),
                        (component, _give) -> component.give_ability = _give,
                        (component) -> component.give_ability)
                .add()).build();
    }


    public static ComponentType<EntityStore, AbilityGranter> getComponentType() {
        return Main.get().getAbilityGranterComponentType();
    }


    public AbilityGranter() {}

    public AbilityGranter(long _ability_as_long, boolean _give_ability) {
        ability_as_long = _ability_as_long;
        give_ability = _give_ability;
    }

    public AbilityGranter(long _ability_as_long) {
        ability_as_long = _ability_as_long;
    }

    public AbilityGranter(AbilityGranter _component_to_copy) {
        ability_as_long = _component_to_copy.ability_as_long;
        give_ability = _component_to_copy.give_ability;
    }

    public long get_ability() {
        return ability_as_long;
    }

    public boolean should_give_ability() {
        return give_ability;
    }


    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new AbilityGranter(ability_as_long, give_ability);
    }
}
