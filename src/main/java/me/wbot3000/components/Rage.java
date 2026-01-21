package me.wbot3000.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.Main;
import me.wbot3000.RAGE_STATE;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

import static me.wbot3000.RAGE_STATE.intToState;

public class Rage implements Component<EntityStore> {
    final public static float NUM_SECONDS_RAGE = 10.0F; //Rage state should last 10 seconds
    final public static float NUM_SECONDS_COOLDOWN = 60.0F; //Cooldown after using Rage when you can use it again

    @Nonnull
    public static final BuilderCodec<Rage> CODEC;

    @Nonnull
    private RAGE_STATE rage_state = RAGE_STATE.READY;

    private float rage_timer = 0.0F; //Used to keep track of how long rage has been active for
    private float cooldown_timer = 0.0F; //Used to keep track of how long cooldown has been


    static {
        CODEC = (BuilderCodec.builder(Rage.class, Rage::new)
                .append(new KeyedCodec<Integer>("RageState", Codec.INTEGER),
                        (component, _state) -> component.rage_state = intToState(_state),
                        (component) -> component.rage_state.stateToInt())
                .add()
                .append(new KeyedCodec<Float>("RageTimer", Codec.FLOAT),
                        (component, _timer) -> component.rage_timer = _timer,
                        (component) -> component.rage_timer)
                .add()
                .append(new KeyedCodec<Float>("CooldownTimer", Codec.FLOAT),
                        (component, _timer) -> component.cooldown_timer = _timer,
                        (component) -> component.cooldown_timer)
                .add()).build();
    }

    public static ComponentType<EntityStore, Rage> getComponentType() {
        return Main.get().getRageComponentType();
    }


    public RAGE_STATE getRageState() {
        return rage_state;
    }

    public void setRageState(RAGE_STATE _state) {
        rage_state = _state;
    }

    public float getRageTimer() {
        return rage_timer;
    }

    public void setRageTimer(float _timer) {
        rage_timer = _timer;
    }

    public float getCooldownTimer() {
        return cooldown_timer;
    }

    public void setCooldownTimer(float _timer) {
        cooldown_timer = _timer;
    }


    public Rage () {};

    public Rage (Rage _component_to_copy) {
        rage_state = _component_to_copy.rage_state;
        rage_timer = _component_to_copy.rage_timer;
        cooldown_timer = _component_to_copy.cooldown_timer;
    }


    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new Rage(this);
    }
}
