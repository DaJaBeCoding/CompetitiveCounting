/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PVPCountingBot;

import discord4j.core.object.entity.Message;

/**
 *
 * @author DavidPrivat
 */
public class Counter {

    public final static int PRESTIGE_WORTH = 1000000;
    private final String key, name;
    private int score, prestiges, prestigePoints;

    private transient int currScoreAdd;
    private int[] unlocked;
    private int[] unlockedSystems;
    private BonusStreak[] bonusStreaks; 

    public Counter(String key, String name, int score, int prestiges, int prestigePoints, int[] unlocked, int[] unlockedSystems, BonusStreak[] bonusStreaks) {
        this.key = key;
        this.score = score;
        this.name = name;
        this.unlocked = unlocked;
        currScoreAdd = 0;
        this.prestiges = prestiges;
        this.prestigePoints = prestigePoints;
        this.unlockedSystems = unlockedSystems;
        this.bonusStreaks = bonusStreaks;
    }

    public void unlock(Unlockable unlockable, Message message) {
        if (unlockable.ordinal() >= Unlockable.BASE_1.ordinal()) {
            if (this.isUnlocked(unlockable)) {
                CountingBot.write(message, "Das hast du bereits unlocked!");
                return;
            }
            if (prestigePoints == 0) {
                CountingBot.write(message, "Du hast nicht genügend Prestigepunkte dafür!");
                return;
            }
            this.addUnlocked(unlockable);
            this.prestigePoints -= Math.abs(unlockable.getPrize());
            CountingBot.write(message, "Glühstrumpf! Du hast '" + unlockable.getName() + "' für " + Math.abs(unlockable.getPrize()) + " Prestigepunkt(e) freigeschalten! Zählst du in diesem System, erhältst du " + getFactFromSys(unlockedSystems[unlockedSystems.length-1]) + " mal mehr geld!");
            CountingBot.getInstance().safeCounters();
        } else {
            if (this.isUnlocked(unlockable)) {
                CountingBot.write(message, "Das hast du bereits unlocked!");
                return;
            }
            if (unlockable.getPrize() > this.score) {
                CountingBot.write(message, "Du hast nur " + this.score + "/" + unlockable.getPrize() + " geld!");
                return;
            }
            this.addUnlocked(unlockable);
            this.score -= unlockable.getPrize();
            CountingBot.write(message, "Glühstrumpf! Du hast '" + unlockable.getName() + "' für " + unlockable.getPrize() + " freigeschalten! Du hast jetzt " + this.getScore() + " geld!");
            CountingBot.getInstance().safeCounters();
        }
    }
    
    public double getFactFromSys(int base) {
        if(unlockedSystems == null) {
            unlockedSystems = new int[]{};
        }
        int x = 0;
        int currBase = 10;
        while(currBase != base && x < unlockedSystems.length) {
            currBase = unlockedSystems[x];
            x++;
        }
        int fact = (int)(1.0d + 0.25d*x);
        return fact;
    }

    public boolean isUnlocked(Unlockable unlockable) {
        int ord = unlockable.ordinal();
        for (int i = 0; i < this.unlocked.length; i++) {
            if (this.unlocked[i] == ord) {
                return true;
            }
        }
        return false;
    }

    public int getAccWorth() {
        int worth = 0;
        worth += score;
        for (int i = 0; i < unlocked.length; i++) {
            worth += Unlockable.values()[this.unlocked[i]].getPrize();
        }
        return worth;
    }

    public boolean prestige(Message message) {
        if (getAccWorth() >= PRESTIGE_WORTH) {
            prestiges++;
            prestigePoints++;
            score = 0;
            unlocked = new int[]{};
            CountingBot.getInstance().safeCounters();
            return true;
        } else {
            CountingBot.write(message, "Setze mit ~prestige deinen Fortschritt zurück und erhalte dafür einen wertvollen Prestigepunkt! \nDein geld + deine unlocks müssen zusammen mindestens " + PRESTIGE_WORTH + " geld wert sein bevor du das machen kannst! Dazu fehlt dir noch " + (PRESTIGE_WORTH - getAccWorth()) + " geld");
            return false;
        }
    }

    public int getPrestiges() {
        return prestiges;
    }

    private void addUnlocked(Unlockable unlockable) {
        int[] newUnlocked = new int[unlocked.length + 1];
        for (int i = 0; i < this.unlocked.length; i++) {
            newUnlocked[i] = this.unlocked[i];
        }
        newUnlocked[this.unlocked.length] = unlockable.ordinal();
        this.unlocked = newUnlocked;

        if (unlockable.ordinal() >= Unlockable.BASE_1.ordinal()) {
            if (unlockedSystems == null) {
                unlockedSystems = new int[]{};
            }
            int[] newUnlockedSys = new int[unlockedSystems.length + 1];
            for (int i = 0; i < this.unlockedSystems.length; i++) {
                newUnlockedSys[i] = unlockedSystems[i];
            }
            switch (unlockable) {
                case BASE_1:
                    newUnlockedSys[unlockedSystems.length] = 1;
                    break;
                case BASE_16:
                    newUnlockedSys[unlockedSystems.length] = 16;
                    break;
                case BASE_2:
                    newUnlockedSys[unlockedSystems.length] = 2;
                    break;
                case BASE_3:
                    newUnlockedSys[unlockedSystems.length] = 3;
                    break;
            }
            this.unlockedSystems = newUnlockedSys;
        }

    }
    
    public void bonus(Message message, BonusStreak.BonusCountType type, int count) {
        boolean exists = false;
        BonusStreak streak = null;
        for(BonusStreak currStreak : this.bonusStreaks) {
            if(currStreak.getType().equals(type)) {
                exists = true;
                streak = currStreak;
                break;
            }
        }
        if(!exists) {
            streak = generateBonusStreak(type);
        }
        streak.count(this, message, count);
        CountingBot.getInstance().safeCounters();
    }
    
    public BonusStreak generateBonusStreak(BonusStreak.BonusCountType type) {
        BonusStreak streak = new BonusStreak(type);
        BonusStreak[] newStreaks = new BonusStreak[this.bonusStreaks.length+1];
        for(int i = 0; i < this.bonusStreaks.length; i++) {
            newStreaks[i] = this.bonusStreaks[i];
        }
        newStreaks[newStreaks.length-1] = streak;
        this.bonusStreaks = newStreaks;
        return streak;
    }

    public void notifyCount(int number) {
        currScoreAdd += number;
    }

    public void notifyWin(int win, int base) {
        score += (int) (win * getFactFromSys(base));
    }

    public void succeed(int base) {
        score += (int) (currScoreAdd * getFactFromSys(base));
        currScoreAdd = 0;
    }

    public String getId() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int fail() {
        int couldHaveBeenPossible = getPossibleTotal();
        currScoreAdd /= 3.0d;
        score = (int) ((2.0d * (double) score / 3.0d));
        score += currScoreAdd;
        currScoreAdd = 0;
        return couldHaveBeenPossible - getScore();
    }

    public int failFromOwn() {
        int couldHaveBeenPossible = getPossibleTotal();
        currScoreAdd /= 4.0d;
        score = (int) ((2.0d * (double) score / 3.0d));
        score += currScoreAdd;
        currScoreAdd = 0;
        return couldHaveBeenPossible - getScore();
    }
    
    public void addBonusScore(int score) {
        this.score += score;
    }

    public int getScore() {
        return score;
    }

    public int getPossibleTotal() {
        return score + currScoreAdd;
    }

    public void subtractScore(int sub) {
        score -= sub;
    }

    public boolean isBaseUnlocked(int base) {
        if (base == 10) {
            return true;
        }

        if (unlockedSystems == null) {
            unlockedSystems = new int[]{};
            return false;
        }

        for (int i = 0; i < unlockedSystems.length; i++) {
            int currSys = unlockedSystems[i];
            if (currSys == base) {
                return true;
            }
        }
        return false;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }
}
