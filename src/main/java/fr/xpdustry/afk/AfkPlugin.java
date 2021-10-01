package fr.xpdustry.afk;

import arc.*;
import arc.struct.*;
import arc.util.*;

import mindustry.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.game.EventType.*;

import java.util.*;


@SuppressWarnings("unused")
public class AfkPlugin extends Plugin{
    // Settings
    private static String message = "[scarlet]You have been kicked for being AFK.";
    private static boolean enabled = true;
    private static int duration = 10;
    // Internals
    private static final Interval updater = new Interval();
    private static final HashSet<AfkWatcher> kicker = new HashSet<>(8);

    @Override
    @SuppressWarnings("all")
    public void init(){
        // Load the settings
        message = Core.settings.getString("xpdustry-afk-message", message);
        enabled = Core.settings.getBool("xpdustry-afk-enabled", enabled);
        duration = Core.settings.getInt("xpdustry-afk-duration", duration);

        // Deploy the listeners
        Events.on(PlayerJoin.class, e -> kicker.add(new AfkWatcher(e.player)));
        Events.on(PlayerLeave.class, e -> kicker.remove(e.player));
        Events.run(Trigger.update, () -> {
            // Don't need to iterate every tick, every second is enough...
            if(enabled && updater.get(Time.toSeconds)){
                kicker.forEach(watcher -> {
                    if(!watcher.isAfk(duration));
                    else watcher.getPlayer().kick(Strings.format(message, duration));
                });
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        // Kicker commands :^)
        handler.register("afk", "<time/status/message> [arg...]", "Manage the AFK kicker.", (args) -> {
            switch (args[0].toLowerCase()){
                case "time":
                    if(args.length == 1){
                        Log.info("The current maximum afk time is @ minute@.", duration, (duration > 1 ? "s" : ""));
                    }else if(Strings.canParsePositiveInt(args[1])){
                        setDuration(Integer.parseInt(args[1]));
                    }else{
                        Log.err("You inputted an invalid number.");
                    }

                    break;

                case "status":
                    if(args.length == 1){
                        Log.info("The AFK kicker is @.", enabled ? "enabled" : "disabled");
                    }else{
                        setEnabled(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("enabled"));
                    }

                    break;

                case "message":
                    if(args.length == 1){
                        Log.info("The current kick message is '@'", message);
                    }else{
                        setMessage(args[1]);
                    }

                    break;

                default: Log.info("Your command is invalid.");
            }
        });
    }

    public static String getMessage(){
        return message;
    }

    public static void setMessage(String message){
        AfkPlugin.message = message;
        Log.info("The current kick message is '@'", message);
        Core.settings.put("xpdustry-afk-message", message);
    }

    public static boolean isEnabled(){
        return enabled;
    }

    public static void setEnabled(boolean enabled){
        AfkPlugin.enabled = enabled;
        Log.info("The AFK kicker is now @.", enabled ? "enabled" : "disabled");
        Core.settings.put("xpdustry-afk-enabled", enabled);
    }

    public static int getDuration(){
        return duration;
    }

    public static void setDuration(int duration){
        AfkPlugin.duration = duration;
        Log.info("The new maximum afk period is @ minute@.", duration, (duration > 1 ? "s" : ""));
        Core.settings.put("xpdustry-afk-duration", duration);
    }

    public static Seq<AfkWatcher> getAfkPlayers(){
        return Seq.with(kicker);
    }

    /** A player is considered AFK if he is not moving and not building something */
    public static class AfkWatcher{
        /** Packed coordinates */
        private int lastPos = 0;
        private final Interval timer = new Interval();
        private final Playerc player;

        public AfkWatcher(Playerc player){
            this.player = player;
        }

        public boolean isAfk(float duration){
            int currentPos = (player.tileY() * Vars.world.height()) + player.tileX();
            if(currentPos != lastPos || player.unit().isBuilding()) resetAfkTimer();

            lastPos = currentPos;
            return timer.get(duration * Time.toMinutes);
        }

        public void resetAfkTimer(){
            timer.reset(0, 0);
        }

        public Playerc getPlayer(){
            return player;
        }

        public float getAfkTime(){
            return timer.getTime(0);
        }

        @Override
        public int hashCode(){
            return player.hashCode();
        }

        @Override
        public String toString(){
            return player.toString();
        }
    }
}
