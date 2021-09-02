package fr.xpdustry.afk;

import arc.*;
import arc.struct.*;
import arc.util.*;

import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.game.EventType.*;

import static arc.util.Log.*;
import static mindustry.Vars.*;


@SuppressWarnings("unused")
public class AfkPlugin extends Plugin{
    // Settings
    public static String message = "[scarlet]You have been kicked for being AFK.";
    public static boolean enabled = true;
    public static int maxAfkPeriod = 10;
    
    public static final ObjectMap<Player, PosWatcher> kicker = new ObjectMap<>();
    public static final Interval updater = new Interval();

    @Override
    public void init(){
        // Load the settings
        if(Core.settings.has("xpdustry-afk-message")) message = Core.settings.getString("xpdustry-afk-message");
        if(Core.settings.has("xpdustry-afk-enabled")) enabled = Core.settings.getBool("xpdustry-afk-enabled");
        if(Core.settings.has("xpdustry-afk-duration")) maxAfkPeriod = Core.settings.getInt("xpdustry-afk-duration");

        // Deploy the listeners
        Events.on(PlayerJoin.class, e -> kicker.put(e.player, new PosWatcher()));
        Events.on(PlayerLeave.class, e -> kicker.remove(e.player));
        // The fun part :^)
        Events.run(Trigger.update, () -> {
            // Don't need to iterate every tick, every second is enough...
            if(enabled && updater.get(Time.toSeconds)){
                kicker.each((player, watcher) -> {
                    if (!watcher.isAFK(player)) return;
                    player.kick(Strings.format(message, maxAfkPeriod));
                });
            }
        });
    }

    /** A player is considered AFK if he is not moving and not building something */
    private static class PosWatcher{
        /** Packed coordinates */
        private int lastPos = 0;
        private final Interval timer = new Interval();

        private boolean isAFK(Player player){
            int currentPos = (player.tileY() * world.height()) + player.tileX();
            if(currentPos != lastPos || player.unit().isBuilding()) timer.reset(0, 0);

            lastPos = currentPos;
            return timer.get(maxAfkPeriod * Time.toMinutes);
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        // Kicker commands :^)
        handler.register("afk", "<time/status/message> [arg...]", "Manage the AFK kicker.", (args) -> {
            switch (args[0].toLowerCase()){
                case "time":
                    if(args.length == 1){
                        info("The current maximum afk time is @ minute@.", maxAfkPeriod, (maxAfkPeriod > 1 ? "s" : ""));
                    }else if(Strings.canParsePositiveInt(args[1])){
                        maxAfkPeriod = Integer.parseInt(args[1]);
                        info("The new maximum afk period is @ minute@.", maxAfkPeriod, (maxAfkPeriod > 1 ? "s" : ""));
                        Core.settings.put("xpdustry-afk-duration", maxAfkPeriod);
                    }else{
                        err("You inputted an invalid number.");
                    }

                    break;

                case "status":
                    if(args.length == 1){
                        info("The AFK kicker is @.", enabled ? "enabled" : "disabled");
                    }else{
                        enabled = args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("enabled");
                        info("The AFK kicker is now @.", enabled ? "enabled" : "disabled");
                        Core.settings.put("xpdustry-afk-enabled", enabled);
                    }

                    break;

                case "message":
                    if(args.length == 1){
                        info("The current kick message is '@'", message);
                    }else{
                        message = args[1];
                        info("The current kick message is '@'", message);
                        Core.settings.put("xpdustry-afk-message", message);
                    }

                    break;

                default: info("Your command is invalid.");
            }
        });
    }
}
