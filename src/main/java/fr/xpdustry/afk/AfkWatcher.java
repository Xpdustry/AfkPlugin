package fr.xpdustry.afk;

import arc.util.*;
import mindustry.*;
import mindustry.gen.*;


/**
 * A class that monitors a player to check whether it is AFK or not.
 */
public class AfkWatcher{
    /** Packed coordinates to track player movement. */
    private int lastPos = 0;
    /** Tracks whether the player is typing or not. */
    private boolean lastTyping = false;
    private final Playerc player;
    private final Interval timer = new Interval();

    public AfkWatcher(Playerc player){
        this.player = player;
    }

    /**
     * A player is considered AFK if:
     * <ul>
     *     <li>It is not moving.</li>
     *     <li>It is not building something.</li>
     *     <li>It hasn't sent any message for a while.</li>
     * </ul>
     */
    public boolean isAfk(float duration){
        int currentPos = (player.tileY() * Vars.world.height()) + player.tileX();
        if(currentPos != lastPos || player.unit().isBuilding() || lastTyping != player.typing()) timer.reset(0, 0);

        lastPos = currentPos;
        lastTyping = player.typing();
        return timer.get(duration * Time.toMinutes);
    }

    public Playerc getPlayer(){
        return player;
    }

    public float getAfkTime(){
        return timer.getTime(0);
    }

    @Override
    public String toString(){
        return player.toString();
    }
}
