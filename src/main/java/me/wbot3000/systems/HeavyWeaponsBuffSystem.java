package me.wbot3000.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.ABILITY_FLAGS;
import me.wbot3000.components.BerserkerAbilities;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;


//NOTE: The index referenced here is the thing GETTING DAMAGED, not the thing DOING damage
//To get the damager, you need to use damage.getSource()
//Increases the damage of heavy weapons (Battleaxes and Maces) depending on the rank of the ability
public class HeavyWeaponsBuffSystem extends DamageEventSystem {

    Query<EntityStore> query;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    //Get entities with stats
    public HeavyWeaponsBuffSystem() {
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
        if (_player == null) { //Damage not done by a player, don't apply this
            return;
        }

        BerserkerAbilities _abilities = store.getComponent(damager_ref, BerserkerAbilities.getComponentType());
        if (_abilities == null) { //Doesn't have any Berserker Abilities, so this can't apply
            return;
        }

        ItemStack _item_in_hand = _player.getInventory().getActiveHotbarItem();
        String _item_id = _item_in_hand == null ? "" : _item_in_hand.getItemId();
        if (_item_id.contains("Weapon_Battleaxe") || _item_id.contains("Weapon_Mace")) { //See if the weapon used to attack is a battleaxe or mace
            float damage_multiplier = 1.0F;
            long hwgI = ABILITY_FLAGS.HEAVY_WEAPONS_GUY_I.enumToLong();
            if ((_abilities.get_current_abilities_bitfield() & hwgI) == hwgI) {
                damage_multiplier += 0.2F; //20% increase in damage
            }
            //LOGGER.atInfo().log(String.valueOf(damage.getAmount()));
            damage.setAmount(damage.getAmount() * damage_multiplier);
            //LOGGER.atInfo().log(String.valueOf(damage.getAmount()));
        }

    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return this.query;
    }
}
