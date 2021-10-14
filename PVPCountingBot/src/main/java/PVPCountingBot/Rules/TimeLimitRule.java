/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PVPCountingBot.Rules;

import PVPCountingBot.Counter;
import PVPCountingBot.CountingBot;
import PVPCountingBot.CountingStreak;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

/**
 *
 * @author DavidPrivat
 */
public class TimeLimitRule implements Rule{
    private String ownerId;
    private CountingStreak streak;
    private int time = 10;
    private AnimationThread thread;
    private boolean hasLost = false;
    public TimeLimitRule(String ownerId, CountingStreak streak) {
        this.ownerId = ownerId;
        this.streak = streak;

    }
    
    public void applyTimerToMessage(Message message, Counter loser) {
        if(thread != null) {
            thread.shouldStop = true;
        }
        thread = new AnimationThread(message, loser);
        thread.start();
    }
    
    public boolean hasLost() {
        return hasLost;
    }
    
    @Override
    public String toString() {
        return "Zwischen 2 Zahlen dürfen maximal " + time + " Sekunden vergehen";
    }
    
    private class AnimationThread extends Thread {
            public boolean shouldStop = false;
            private Message message;
            private Counter loser;
            public AnimationThread(Message message, Counter loser) {
                this.message = message;
                this.loser = loser;
            }
            @Override
            public void run() {
                int timer = time + 1;
                ReactionEmoji one,two,three,bolt;
                one = ReactionEmoji.unicode("\u0031\u20E3");
                two = ReactionEmoji.unicode("\u0032\u20E3");
                three = ReactionEmoji.unicode("\u0033\u20E3");
                bolt = ReactionEmoji.unicode("\u26A1");
                message.addReaction(ReactionEmoji.of(Long.parseLong("805121814296133653"), "kekmark", false)).subscribe();
                message.addReaction(bolt).subscribe();
                while(timer >= 0) {
                    if(shouldStop) {
                        message.removeSelfReaction(three).subscribe();
                        message.removeSelfReaction(one).subscribe();
                        message.removeSelfReaction(two).subscribe();
                        message.removeSelfReaction(bolt).subscribe();
                        message.addReaction(ReactionEmoji.of(Long.parseLong("805121814296133653"), "kekmark", false)).subscribe();
                        return;
                    }
                    switch(timer) {
                        case 1:
                            message.removeSelfReaction(two).subscribe();
                            message.addReaction(one).subscribe();
                            break;
                        case 2:
                            message.removeSelfReaction(three).subscribe();
                            message.addReaction(two).subscribe();
                            break;
                        case 3:
                            message.addReaction(three).subscribe();
                            break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    timer -= 1;                    
                }
                if(shouldStop) {
                        message.removeSelfReaction(three).subscribe();
                        message.removeSelfReaction(one).subscribe();
                        message.removeSelfReaction(two).subscribe();
                        message.removeSelfReaction(bolt).subscribe();
                        return;
                }
                hasLost = true;
                message.removeSelfReaction(one).subscribe();
                message.removeSelfReaction(bolt).subscribe();
                streak.timeLimitLost(ownerId, message, loser);
                CountingBot.getInstance().removeStreak(streak.getKey());
                

                
            }
        }

    @Override
    public String getOwnerId() {
        return ownerId;
    }
    
}
