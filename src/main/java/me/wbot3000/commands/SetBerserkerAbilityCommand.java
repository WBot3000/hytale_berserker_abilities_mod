package me.wbot3000.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.wbot3000.ABILITY_FLAGS;
import me.wbot3000.components.AbilityGranter;
import me.wbot3000.components.BerserkerAbilities;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SetBerserkerAbilityCommand extends AbstractPlayerCommand {

    final private ABILITY_FLAGS ability_flag;

    public SetBerserkerAbilityCommand(ABILITY_FLAGS _flag) {
        // Name, Description, Requires OP
        ability_flag = _flag;
        super("set" + _flag.getAbilityShorthand(), "Give (or remove) the " + _flag.getAbilityName() + " Berserker ability", false);
    }


    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {

        BerserkerAbilities abilities = store.getComponent(ref, BerserkerAbilities.getComponentType());
        final long LONG_FLAG = ability_flag.enumToLong();
        final String NAME = ability_flag.getAbilityName();
        AbilityGranter granter;

        if (abilities == null) { //If the player doesn't have any Berserker Abilities, you'll need to give them the component
            abilities = store.addComponent(ref, BerserkerAbilities.getComponentType());
        }

        if((abilities.get_current_abilities_bitfield() & LONG_FLAG) == 0) { //Give
            granter = new AbilityGranter(LONG_FLAG, true);
            store.addComponent(ref, AbilityGranter.getComponentType(), granter);
            world.sendMessage(Message.raw("You've been granted " + NAME));
        }
        else { //Take away
            granter = new AbilityGranter(LONG_FLAG, false);
            store.addComponent(ref, AbilityGranter.getComponentType(), granter);
            world.sendMessage(Message.raw(NAME + " has been removed"));
        }
    }
}
