package me.wbot3000;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.commands.SetBerserkerAbilityCommand;
import me.wbot3000.components.AbilityGranter;
import me.wbot3000.components.BerserkerAbilities;
import me.wbot3000.components.Rage;
import me.wbot3000.listeners.InputListener;
import me.wbot3000.systems.AbilityGrantingSystem;
import me.wbot3000.systems.BloodbathSystem;
import me.wbot3000.systems.HeavyWeaponsBuffSystem;
import me.wbot3000.systems.RageSystems;

import javax.annotation.Nonnull;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends JavaPlugin {

    private static Main instance; //Allows direct access to the plugin instance. Based on Hytale's EntityModule
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ComponentType<EntityStore, BerserkerAbilities> berserkerAbilitiesComponentType;
    private ComponentType<EntityStore, AbilityGranter> abilityGranterComponentType;
    private ComponentType<EntityStore, Rage> rageComponentType;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    public static Main get() {
        return instance;
    }

    public ComponentType<EntityStore, BerserkerAbilities> getBerserkerAbilitiesComponentType() {
        return this.berserkerAbilitiesComponentType;
    }

    public ComponentType<EntityStore, AbilityGranter> getAbilityGranterComponentType() {
        return this.abilityGranterComponentType;
    }

    public ComponentType<EntityStore, Rage> getRageComponentType() {
        return this.rageComponentType;
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
        CommandRegistry commandRegistry = this.getCommandRegistry();


        //Register Components
        this.berserkerAbilitiesComponentType = entityStoreRegistry.registerComponent(BerserkerAbilities.class, "BerserkerAbilities", BerserkerAbilities.CODEC);
        this.abilityGranterComponentType = entityStoreRegistry.registerComponent(AbilityGranter.class, "AbilityGranter", AbilityGranter.CODEC);
        this.rageComponentType = entityStoreRegistry.registerComponent(Rage.class, "Rage", Rage.CODEC);

        //Register Systems
        entityStoreRegistry.registerSystem(new AbilityGrantingSystem());
        entityStoreRegistry.registerSystem(new HeavyWeaponsBuffSystem());
        entityStoreRegistry.registerSystem(new BloodbathSystem());
        //Systems for managing Rage state
        entityStoreRegistry.registerSystem(new RageSystems.RageOnTickSystem());
        entityStoreRegistry.registerSystem(new RageSystems.RageDamageBoostSystem());
        entityStoreRegistry.registerSystem(new RageSystems.RageProtectionBoostSystem());

        //Register Packet Listener (allows player input to trigger Rage ability)
        PacketAdapters.registerInbound(new InputListener());


        //Register Commands
        //Creates a set command for each Berserker Ability
        for (ABILITY_FLAGS ability : ABILITY_FLAGS.values()) {
            commandRegistry.registerCommand(new SetBerserkerAbilityCommand(ability));
        }
    }

}
