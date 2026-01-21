package me.wbot3000.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.particle.config.Particle;
import com.hypixel.hytale.server.core.asset.type.particle.pages.ParticleSpawnPage;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.RAGE_STATE;
import me.wbot3000.components.Rage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//NOTE: Based off of https://hytalemodding.dev/en/docs/guides/plugin/player-input-guide
public class InputListener implements PacketWatcher {

    private static class InputData {
        long lastQuickPressTime = Instant.now().toEpochMilli();
        int numQuickUsePresses = 0;

        public InputData() {};
    }

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final long SUCCESSIVE_INPUT_THRESHOLD = 500; //Press succession threshold is 0.5 seconds (or 500 ms). Might make this configurable

    private static final Map<UUID, InputData> player_input_data = new HashMap<>();

    //private long lastInputTime = Instant.now().toEpochMilli();
    //private int numQuickUsePresses = 0; //Triggering rage will require 2 quick successive presses of Use, so you don't automatically use it when using Use

    @Override
    public void accept(PacketHandler packetHandler, Packet packet) {
        if (packet.getId() != 290) {
            return;
        }

        SyncInteractionChains interactionChains = (SyncInteractionChains) packet;
        SyncInteractionChain[] updates = interactionChains.updates;
        for (SyncInteractionChain item : updates) {
            PlayerAuthentication playerAuthentication = packetHandler.getAuth();
            assert playerAuthentication != null;
            UUID uuid = playerAuthentication.getUuid();
            InteractionType interactionType = item.interactionType;
            if (interactionType == InteractionType.Use) {
                // Get the player's captured input data, and if they pressed their Use key 2 times quickly, then attempt to rage.
                InputData data = player_input_data.get(uuid);
                if (data == null) {
                    data = new InputData();
                    player_input_data.put(uuid, data);
                }
                if (data.lastQuickPressTime + SUCCESSIVE_INPUT_THRESHOLD > Instant.now().toEpochMilli()) {
                    data.numQuickUsePresses = 0;
                }
                data.lastQuickPressTime = Instant.now().toEpochMilli();
                data.numQuickUsePresses++;
                if(data.numQuickUsePresses >= 2) {
                    attemptToTriggerRage(uuid);
                    data.numQuickUsePresses = 0;
                }

            }
        }
    }


    public void attemptToTriggerRage(UUID _player_uuid) {
        Universe universe = Universe.get();
        PlayerRef player_ref = universe.getPlayer(_player_uuid);
        if(player_ref == null) { return; }
        Ref<EntityStore> ref = player_ref.getReference();
        if(ref == null) { return; }

        UUID world_uuid = player_ref.getWorldUuid();
        if(world_uuid == null) { return; }

        World world = universe.getWorld(world_uuid);
        if(world == null) { return; }

        Store<EntityStore> store = world.getEntityStore().getStore();

        world.execute(() -> {
            Rage rage_component = store.getComponent(ref, Rage.getComponentType());
            if(rage_component == null) { return; }

            RAGE_STATE rage_state = rage_component.getRageState();
            switch (rage_state) { //Enter rage state if ready to do so, else send a message detailing current rage state.
                case READY:
                    rage_component.setRageState(RAGE_STATE.ACTIVE); //Triggers rage

                    TransformComponent transform = store.getComponent(ref, EntityModule.get().getTransformComponentType());
                    assert transform != null;

                    //Spawn particles of RAGE!
                    ParticleUtil.spawnParticleEffect("Explosion_Medium", transform.getPosition(), store);

                    //Plays a cool sound of RAGE!
                    int rage_sound_idx = SoundEvent.getAssetMap().getIndex("SFX_Golem_Earth_Wake");
                    SoundUtil.playSoundEvent3dToPlayer(ref, rage_sound_idx, SoundCategory.SFX, transform.getPosition(), store);
                    break;
                case ACTIVE:
                    player_ref.sendMessage(Message.raw("You're already raging!"));
                    break;
                case COOLDOWN:
                    float time_until_next_rage = Rage.NUM_SECONDS_COOLDOWN - rage_component.getCooldownTimer();
                    player_ref.sendMessage(Message.raw("You're in cooldown. Try again in " + Math.round(time_until_next_rage) + " seconds."));
                    break;
                default:
                    player_ref.sendMessage(Message.raw("You're in an unknown rage state. If you see this message, there is a bug with the mod."));
                    break;
            }
        });
    }
}