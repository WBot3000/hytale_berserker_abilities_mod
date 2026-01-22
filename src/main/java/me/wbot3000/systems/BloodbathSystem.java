package me.wbot3000.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.ABILITY_FLAGS;
import me.wbot3000.components.BerserkerAbilities;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

//Whenever you kill an entity, heal 10% of your health or the maximum health amount of the entity (whichever is lower)
public class BloodbathSystem extends DeathSystems.OnDeathSystem {
    Query<EntityStore> query;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    //Get entities with stats
    public BloodbathSystem() {
        this.query = EntityStatMap.getComponentType();
    }

    @Override
    public void onComponentAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl DeathComponent deathComponent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        Damage death_damage = deathComponent.getDeathInfo();
        Damage.Source damage_source = death_damage.getSource();
        if (!(damage_source instanceof Damage.EntitySource entity_source)) { //Doesn't apply to damage not done by entities
            return;
        }

        Ref<EntityStore> damager_ref = entity_source.getRef();
        Player _player = store.getComponent(damager_ref, Player.getComponentType());
        if (_player == null) { //Damage not done by a player, don't apply this
            return;
        }

        BerserkerAbilities _abilities = store.getComponent(damager_ref, BerserkerAbilities.getComponentType());
        if (_abilities == null) { //Doesn't have any Berserker Abilities, so this can't apply
            return;
        }

        //This is how you can reference the player's health stat
        int healthIdx = DefaultEntityStatTypes.getHealth();

        EntityStatMap dead_entity_stats = store.getComponent(damager_ref, EntityStatMap.getComponentType());
        EntityStatValue dead_entity_health = dead_entity_stats.get(healthIdx);
        float dead_entity_max_health = dead_entity_health.getMax();

        EntityStatMap damager_stats = store.getComponent(damager_ref, EntityStatMap.getComponentType());
        EntityStatValue damager_health = damager_stats.get(healthIdx);
        float damager_max_health = damager_health.getMax();

        float health_restoration_percentage = 0.0F;
        long bloodbathI = ABILITY_FLAGS.BLOODBATH_I.enumToLong();
        if ((_abilities.get_current_abilities_bitfield() & bloodbathI) == bloodbathI) {
            health_restoration_percentage += 0.1F;
        }

        if(health_restoration_percentage > 0.0F) { //This means that the damager has at least one variant of Bloodbath
            TransformComponent transform = store.getComponent(damager_ref, EntityModule.get().getTransformComponentType());
            assert transform != null;

            damager_stats.addStatValue(healthIdx, Math.min(dead_entity_max_health, damager_max_health * health_restoration_percentage));
            ParticleUtil.spawnParticleEffect("Health_Regen_Plus", transform.getPosition(), store);
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() { return query; }
}
