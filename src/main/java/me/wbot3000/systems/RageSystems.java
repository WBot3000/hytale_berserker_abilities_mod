package me.wbot3000.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.ABILITY_FLAGS;
import me.wbot3000.RAGE_STATE;
import me.wbot3000.components.BerserkerAbilities;
import me.wbot3000.components.Rage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;

public class RageSystems {

    public static class RageOnTickSystem extends EntityTickingSystem<EntityStore> {
        @Override
        public void tick(float dt, int idx, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
            Rage rage_component = archetypeChunk.getComponent(idx, Rage.getComponentType());

            switch (rage_component.getRageState()) {
                case ACTIVE:
                    EntityStatMap stats = archetypeChunk.getComponent(idx, EntityStatMap.getComponentType());
                    int staminaIdx = DefaultEntityStatTypes.getStamina();

                    assert stats != null;
                    //Infinite stamina while raging
                    EntityStatValue stamina = stats.get(staminaIdx);
                    if(stamina.get() < stamina.getMax()) {
                        stats.setStatValue(staminaIdx, stamina.getMax());
                    }

                    float rage_timer = rage_component.getRageTimer();
                    rage_component.setRageTimer(rage_timer + dt);

                    if (rage_timer > Rage.NUM_SECONDS_RAGE) {
                        rage_component.setRageState(RAGE_STATE.COOLDOWN);
                        rage_component.setRageTimer(0.0F);
                        stats.setStatValue(staminaIdx, 0); //Instant stamina loss
                    }
                    break;
                case COOLDOWN:
                    float cooldown_timer = rage_component.getCooldownTimer();
                    rage_component.setCooldownTimer(cooldown_timer + dt);

                    if (cooldown_timer > Rage.NUM_SECONDS_COOLDOWN) {
                        rage_component.setRageState(RAGE_STATE.READY);
                        rage_component.setCooldownTimer(0.0F);
                    }
                    break;
                default:
                    break;
            }
        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return Rage.getComponentType();
        }
    }


    public static class RageDamageBoostSystem extends DamageEventSystem {
        Query<EntityStore> query;

        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

        //Get entities with stats
        public RageDamageBoostSystem() {
            this.query = EntityStatMap.getComponentType();
        }


        //Makes it so that this system runs BEFORE damage is dealt, otherwise it'll run after and do nothing
        @Nullable
        @Override
        public SystemGroup<EntityStore> getGroup() {
            return DamageModule.get().getGatherDamageGroup();
        }


        @Override
        public void handle(int idx, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl Damage damage) {
            Damage.Source damage_source = damage.getSource();
            if (!(damage_source instanceof Damage.EntitySource entity_source)) { //Doesn't apply to damage not done by entities
                return;
            }

            Ref<EntityStore> damager_ref = entity_source.getRef();
            Player _player = store.getComponent(damager_ref, Player.getComponentType());
            if (_player == null) { //Not a player, don't apply this
                return;
            }

            Rage rage_component = store.getComponent(damager_ref, Rage.getComponentType());
            if(rage_component != null && rage_component.getRageState() == RAGE_STATE.ACTIVE) {
                //50% outgoing damage increase for raging players
                damage.setAmount(damage.getAmount() * 1.5F);
            }
        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return this.query;
        }
    }


    public static class RageProtectionBoostSystem extends DamageEventSystem {
        Query<EntityStore> query;

        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

        //Get entities with Rage
        public RageProtectionBoostSystem() {
            this.query = Rage.getComponentType();
        }


        //Makes it so that this system runs BEFORE damage is dealt, otherwise it'll run after and do nothing
        @Nullable
        @Override
        public SystemGroup<EntityStore> getGroup() {
            return DamageModule.get().getGatherDamageGroup();
        }


        @Override
        public void handle(int idx, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl Damage damage) {
            Rage rage_component = archetypeChunk.getComponent(idx, Rage.getComponentType());
            if(rage_component != null && rage_component.getRageState() == RAGE_STATE.ACTIVE) {
                //50% incoming damage reduction for raging players
                //TODO: Might be over-powered, maybe tweak a bit
                damage.setAmount(damage.getAmount() * 0.5F);
            }
        }

        @NullableDecl
        @Override
        public Query<EntityStore> getQuery() {
            return this.query;
        }
    }
}
