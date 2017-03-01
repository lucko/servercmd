package me.lucko.servercmd;

import com.google.inject.Inject;

import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "servercmd", name = "servercmd", version = "1.0-SNAPSHOT", authors = {"Luck"}, description = "Adds /server on the backend server")
public class ServerCmd implements CommandExecutor {

    @Inject
    private Game game;

    private ChannelBinding.RawDataChannel channel = null;

    @Listener
    public void init(GameInitializationEvent event) {
        channel = game.getChannelRegistrar().createRawChannel(this, "BungeeCord");

        CommandSpec cmd = CommandSpec.builder()
                .description(Text.of("Switch servers"))
                .arguments(GenericArguments.string(Text.of("server")))
                .executor(this)
                .build();

        game.getCommandManager().register(this, cmd, "server");
    }

    @Listener
    public void stop(GameStoppingServerEvent event) {
        if (channel != null) {
            game.getChannelRegistrar().unbindChannel(channel);
        }
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Text.builder("You must be a player to use this command.").color(TextColors.RED).build());
            return CommandResult.empty();
        }

        Player player = ((Player) source);
        String server = context.<String>getOne("server").get();

        if (!source.hasPermission("servercmd.use." + server)) {
            throw new CommandPermissionException();
        }

        source.sendMessage(Text.builder("Connecting you to " + server).color(TextColors.RED).build());

        channel.sendTo(player, buf -> {
            buf.writeUTF("Connect");
            buf.writeUTF(server);
        });

        return CommandResult.affectedEntities(1);
    }
}
