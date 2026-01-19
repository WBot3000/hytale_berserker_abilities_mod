package me.wbot3000.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.Main;


import javax.annotation.Nonnull;


public class BerserkerAbilities implements Component<EntityStore> {
    @Nonnull
    public static final BuilderCodec<BerserkerAbilities> CODEC;

    @Nonnull
    private long current_abilities_bitfield = 0;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();


    static {
        CODEC = (BuilderCodec.builder(BerserkerAbilities.class, BerserkerAbilities::new)
                        .append(new KeyedCodec<Long>("CurrentAbilitiesBitfield", Codec.LONG),
                                (component, _bitfield) -> component.current_abilities_bitfield = _bitfield,
                                (component) -> component.current_abilities_bitfield)
                        .add()).build();
    }


    public static ComponentType<EntityStore, BerserkerAbilities> getComponentType() {
        return Main.get().getBerserkerAbilitiesComponentType();
    }


    public BerserkerAbilities() {}

    public BerserkerAbilities(BerserkerAbilities _component_to_copy) {
        current_abilities_bitfield = _component_to_copy.current_abilities_bitfield;
    }


    public long get_current_abilities_bitfield() {
        return current_abilities_bitfield;
    }

    public void set_current_abilities_bitfield(long _new_abilities_bitfield) {
        current_abilities_bitfield = _new_abilities_bitfield;
    }

    public void set_ability_flag(long _bitflag) { //If I'm correct, changing the flag should update all the abilities in the BerserkerAbilitySystem
        current_abilities_bitfield = current_abilities_bitfield | _bitflag;
        LOGGER.atInfo().log("FLAG HAS BEEN SET");
    }

    public void clear_ability_flag(long _bitflag) {
        long reversed_bitflag = ~_bitflag;
        current_abilities_bitfield = current_abilities_bitfield & reversed_bitflag;
        LOGGER.atInfo().log("FLAG HAS BEEN REMOVED");
    }


    @Nonnull
    public Component<EntityStore> clone() {
        return new BerserkerAbilities(this);
    }
}
