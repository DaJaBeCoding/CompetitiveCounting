/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PVPCountingBot;

import discord4j.core.object.entity.Message;
import java.time.LocalDate;

/**
 *
 * @author DavidPrivat
 */
public class BonusStreak {

    private int currentCount;
    private BonusCountType type;
    private long lastCountTime;

    public BonusStreak(BonusCountType type) {
        this.type = type;
        this.currentCount = -1;
        this.lastCountTime = -1;
    }

    public void count(Counter counter, Message message, int count) {
        if (isTimeLegit() && count == currentCount + 1) {
            currentCount++;
            lastCountTime = this.getTimeNow();
            counter.addBonusScore(currentCount * type.multiplier);
            success(message, count);
        } else {
            if(currentCount == -1) {
                CountingBot.write(message, "Starte deine Streak mit 0! (informatik 4 the win) (niin) (jhin) (h you check it 4 and jhin?)");
            } else if(!isTimeLegit()) {
                fail(message, "time");
            } else {
                fail(message, "num");
            }
        }
    }
    
    private void success(Message message, int count) {
        CountingBot.write(message, "Du erhaelst " + (count*type.multiplier) + " geld als " + type.name() + " Belohnung!");
    }
    
    private void fail(Message message, String arg) {
        switch(arg) {
            case "time":
                CountingBot.write(message, "Ha! Du hast deine " + type.name() + " streak bei " + currentCount + " gekackt, weil du anscheinend nicht verstanden hast was " + type.name() + " bedeutet du Versager");
                break;
            case "num":
                CountingBot.write(message, "Ha! Du hast deine " + type.name() + " streak bei " + currentCount + " gekackt, weil du anscheinend nicht weisst, was nach " + this.currentCount + " kommt du Neger!");
                break;
        }
        this.currentCount = -1;
        this.lastCountTime = -1;
    }
    
    public BonusCountType getType() {
        return type;
    }

    private long getTimeNow() {
        switch (type) {
            case DAILY:
                return TimeHandler.nowInEpochDay();

        }
        return -2;
    }

    private boolean isTimeLegit() {
        if (this.lastCountTime == -1) {
            return true;
        }
        switch (this.type) {
            case DAILY:
                if (TimeHandler.isYesterday(lastCountTime)) {
                    return true;
                } else {
                    return false;
                }
        }
        return false;
    }

    public static enum BonusCountType {
        DAILY(25);
        public final int multiplier;

        BonusCountType(int multiplier) {
            this.multiplier = multiplier;
        }
    }
}
