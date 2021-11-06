package fr.xpdustry.afk;

import arc.*;
import arc.struct.*;
import arc.util.*;

import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.game.EventType.*;


@SuppressWarnings("unused")
public class AfkPlugin extends Plugin{
    // Settings
    private static String message = "[scarlet]You have been kicked for being AFK.";
    private static boolean enabled = true;
    private static boolean silentKick = true;
    private static int duration = 10;
    
    // Internals
    private static final Interval updater = new Interval();
    private static final ObjectMap<Player, AfkWatcher> kicker = new ObjectMap<>();

    @Override
    public void init(){
        // Load the settings
        message = Core.settings.getString("xpdustry-afk-message", message);
        enabled = Core.settings.getBool("xpdustry-afk-enabled", enabled);
        duration = Core.settings.getInt("xpdustry-afk-duration", duration);

        // Deploy the listeners
        Events.on(PlayerJoin.class, e -> kicker.put(e.player, new AfkWatcher(e.player)));
        Events.on(PlayerLeave.class, e -> kicker.remove(e.player));
        Events.run(Trigger.update, () -> {
            // Don't need to iterate every tick, every second is enough...
            if(enabled && updater.get(Time.toSeconds)){
                kicker.each((player, watcher) -> {
                    if(watcher.isAfk(duration)) {
                        if(silentKick){
                            netServer.admins.kickedIPs.put(player.ip(), Math.max(netServer.admins.kickedIPs.get(player.ip(), 0L), Time.millis() + 30 * 1000));
                            PlayerInfo info = netServer.admins.getInfo(player.uuid());
                            info.lastKicked = Math.max(Time.millis() + 30 * 1000, info.lastKicked);
                			
                            Call.infoMessage(player.con, Strings.format(message, duration));
                            player.con.close();
                			
                            netServer.admins.save();
                            player.con.kicked = true;
                            
                        } else player.kick(Strings.format(message, duration));
                    }
                });
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        // Kicker commands :^)
        handler.register("afk", "<time/status/message/type> [arg...]", "Manage the AFK kicker.", (args) -> {
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
                    
                case "type":
                	if(args.length == 1){
                        Log.info("The silent kick is '@'", enabled ? "enabled" : "disabled");
                    }else{
                        switch (args[1]){
                            case "on":
                                silentKick = true;
                                Log.info("silent kick enabled ...");
                        		
                            case "off":
                                silentKick = false;
                                Log.info("silent kick disabled ...");
                        	
                            default: Log.err("Invalid argument");
                        }
                    }
                	
                    break;

                default: Log.err("Your command is invalid.");
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

    public static boolean silentKickEnabled() {
    	return silentKick;
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

    public static ObjectMap<Player,AfkWatcher> getAfkPlayers(){
        return kicker.copy();
    }
}
