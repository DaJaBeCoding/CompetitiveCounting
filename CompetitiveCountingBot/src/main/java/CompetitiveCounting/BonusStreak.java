/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;
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
                CountingBot.write(message, "Begin your streak with 0!");
            } else if(!isTimeLegit()) {
                fail(message, "time");
            } else {
                fail(message, "num");
            }
        }
    }
    
    private void success(Message message, int count) {
        CountingBot.write(message, "Bonus received: You get " + (count*type.multiplier) + " money from your " + type.name() + " streak.");
    }
    
    private void fail(Message message, String arg) {
        switch(arg) {
            case "time":
                CountingBot.write(message, "Whoops! You messed up your " + type.name() + " streak at " + currentCount + " , because apparently you have no idea what " + type.name() + " means.");
                break;
            case "num":
                CountingBot.write(message, "Whoops! You messed up your " + type.name() + " streak at " + currentCount + " , because apparently you have no idea what comes after " + this.currentCount + ".");
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
    
    public int getCurrCount() {
        return currentCount;
    }
    


    public boolean isTimeLegit() {
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
