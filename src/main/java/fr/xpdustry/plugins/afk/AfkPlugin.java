package fr.xpdustry.plugins.afk;

import arc.*;
import arc.struct.*;
import arc.util.*;

import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.game.EventType.*;

import static arc.util.Log.*;
import static mindustry.Vars.*;


@SuppressWarnings("unused")  // <- Only used for this template so IntelliJ stop screaming at me...
public class AfkPlugin extends Plugin{
    public static boolean enabled = true;
    public static int maxAfkPeriod = 10;
    public static final ObjectMap<Player, PosWatcher> kicker = new ObjectMap<>();
    /** Don't need to iterate every tick, every second is enough... */
    public static final Interval updater = new Interval();

    @Override
    public void init(){
        Events.on(PlayerJoin.class, e -> {
            kicker.put(e.player, new PosWatcher());
        });

        Events.on(PlayerLeave.class, e -> {
            kicker.remove(e.player);
        });

        Events.run(Trigger.update, () -> {
            if(enabled && updater.get(Time.toSeconds)){
                kicker.each((player, watcher) -> {
                    if(watcher.isAFK(player)){
                        player.kick("You have been AFK for " + maxAfkPeriod + "minute" + (maxAfkPeriod > 1 ? "s" : ""));
                    }
                });
            }
        });
    }

    public static class PosWatcher{
        /** Packed coordinates */
        private int lastPos = 0;
        public final Interval timer = new Interval();

        public boolean isAFK(Player player){
            int currentPos = (player.tileY() * world.height()) + player.tileX();
            if(currentPos != lastPos) timer.reset(0, 0);
            lastPos = currentPos;
            return timer.get(maxAfkPeriod * Time.toMinutes);
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("kicker", "<time/status> [arg]", "Manage the AFK kicker.", (args) -> {
            switch(args[0]){
                case "time":
                    if(args.length == 1){
                        info("The current maximum afk time is @ minute@.", maxAfkPeriod, (maxAfkPeriod > 1 ? "s" : ""));
                    }else{
                        try{
                            maxAfkPeriod = Integer.parseInt(args[1]);
                            info("The new maximum afk period is @ minute@.", maxAfkPeriod, (maxAfkPeriod > 1 ? "s" : ""));
                        }catch(NumberFormatException e){
                            err("You inputted an invalid number.");
                        }
                    }
                    break;

                case "status":
                    if(args.length == 1){
                        info("The AFK kicker is @.", enabled ? "enabled" : "disabled");
                    }else{
                        enabled = args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("enabled");
                        info("The AFK kicker is now @.", enabled ? "enabled" : "disabled");
                    }
                    break;

                default:
                    info("Your command is invalid.");
            }
        });
    }
}
