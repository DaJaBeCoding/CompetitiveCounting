/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import discord4j.core.object.entity.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    private List<Contract> contracts;
    private transient List<Contract> incomingContracts;
    private transient HashMap<String, TradeOffer> tradeOffers = new HashMap<String, TradeOffer>();
    private transient ContractHandler contractHandler;

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
        init();
        initIncomingContracts(CountingBot.getInstance().getCounters());
    }

    public void init() {
        if (contractHandler == null) {
            contractHandler = new ContractHandler(this);
        }
        if (contracts == null) {
            contracts = new ArrayList<>();
        }
        if (incomingContracts == null) {
            incomingContracts = new ArrayList<>();
        }
        

    }
    
    public void initIncomingContracts(HashMap<String, Counter> counters) {
        if (incomingContracts.isEmpty()) {
            counters.forEach((String currId, Counter counter) -> {
                for (Contract currContract : counter.getContracts()) {
                    if (currContract.toId.equals(this.getId())) {
                        currContract.owner = counter;
                        incomingContracts.add(currContract);
                    }
                }
            });
        }
    }

    public void unlock(Unlockable unlockable, Message message) {
        if (unlockable.ordinal() >= Unlockable.BASE_1.ordinal()) {
            if (this.isUnlocked(unlockable)) {
                CountingBot.write(message, "You have already unlocked this.");
                return;
            }
            if (prestigePoints == 0) {
                CountingBot.write(message, "You don't have enough prestige points to do that.");
                return;
            }
            this.addUnlocked(message, unlockable);

        } else {
            if (this.isUnlocked(unlockable)) {
                CountingBot.write(message, "You have already unlocked this.");
                return;
            }
            if (unlockable.getPrize() > this.score) {
                CountingBot.write(message, "You only have " + this.score + " money, but you need " + unlockable.getPrize() + "!");
                return;
            }
            this.addUnlocked(message, unlockable);
            this.score -= unlockable.getPrize();
            CountingBot.write(message, "You unlocked '" + unlockable.getName() + "' and paid " + unlockable.getPrize() + " money. You have " + this.getScore() + " money left.");
            CountingBot.getInstance().safeCounters();
        }
    }

    public double getFactFromSys(int base) {
        if (unlockedSystems == null) {
            unlockedSystems = new int[]{};
        }
        int x = 0;
        int currBase = 10;
        while (currBase != base && x < unlockedSystems.length) {
            currBase = unlockedSystems[x];
            x++;
        }
        double fact = (1.0 + 0.25 * x);
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

    public void addTradeOffer(TradeOffer tradeOffer, String key) { //trading start
        if (tradeOffers == null) {
            tradeOffers = new HashMap<>();
        }
        tradeOffers.put(key, tradeOffer);
    }

    public String buttonClick(String customId) {
        if (tradeOffers == null) {
            tradeOffers = new HashMap<>();
        }
        if (customId.startsWith("-")) {  // DECLINE
            System.out.println("Tradeoffer declined!");
            String newId = customId.substring(1);
            if (tradeOffers.containsKey(newId)) {
                tradeOffers.remove(newId);
                return "Offer declined!";
            } else {
                return "This offer doesn't match any of yours";
            }
        } else if (tradeOffers.containsKey(customId)) {
            TradeOffer offer = tradeOffers.get(customId);
            String answ = offer.isTradeOfferValid();
            if (answ.toUpperCase().equals("VALID")) {
                offer.fullfill();
                tradeOffers.remove(customId);
                return "Accepted!";
            } else {
                return answ;
            }
        } else {
            return "This offer doesn't match any of yours";
        }
    }

    public void transferTo(Counter to, int amount) {
        to.addBonusScore(amount);
        subtractScore(amount);
        CountingBot.getInstance().safeCounters();

    }//trading end

    public boolean prestige(Message message) {
        if (getAccWorth() >= PRESTIGE_WORTH) {
            prestiges++;
            prestigePoints++;
            score = 0;
            unlocked = new int[]{};
            CountingBot.getInstance().safeCounters();
            return true;
        } else {
            CountingBot.write(message, "Reset all your progress with ~prestige and acquire a prestige point. \n Your net worth (wallet + unlocks) has to be " + PRESTIGE_WORTH + " or more before you can do this. You are still missing " + (PRESTIGE_WORTH - getAccWorth()) + " money.");
            return false;
        }
    }

    public int getPrestiges() {
        return prestiges;
    }

    public BonusStreak[] getBonusStreaks() {
        return bonusStreaks;
    }

    private void addUnlocked(Message message, Unlockable unlockable) {
        int[] newUnlocked = new int[unlocked.length + 1];
        for (int i = 0; i < this.unlocked.length; i++) {
            newUnlocked[i] = this.unlocked[i];
        }
        newUnlocked[this.unlocked.length] = unlockable.ordinal();
        this.unlocked = newUnlocked;

        if (unlockable.ordinal() >= Unlockable.BASE_1.ordinal()) {
            switch (unlockable) {
                case BASE_1:
                    unlockBase(message, "1");
                    break;
                case BASE_16:
                    unlockBase(message, "16");
                    break;
                case BASE_2:
                    unlockBase(message, "2");
                    break;
                case BASE_3:
                    unlockBase(message, "3");
                    break;
            }
        }

    }

    public void unlockBase(Message message, String base) {
        if (!BaseSystems.isNumInSystem(base, 10) || Integer.parseInt(base) > 1000) {
            CountingBot.write(message, "Invalid base! (note: base can not exceed 1000.)");
            return;
        }
        int system = Integer.parseInt(base);
        if (isBaseUnlocked(system)) {
            CountingBot.write(message, "You have already unlocked this base.");
            return;
        }

        if (unlockedSystems == null) {
            unlockedSystems = new int[]{};
        }
        int[] newUnlockedSys = new int[unlockedSystems.length + 1];
        for (int i = 0; i < this.unlockedSystems.length; i++) {
            newUnlockedSys[i] = unlockedSystems[i];
        }

        newUnlockedSys[unlockedSystems.length] = system;

        this.unlockedSystems = newUnlockedSys;
        this.prestigePoints -= Math.abs(Unlockable.BASE_N.getPrize());
        CountingBot.write(message, "You unlocked the 'base-" + base + "-system' and paid " + Math.abs(Unlockable.BASE_N.getPrize()) + " prestige points. If you count in this system, you will earn " + getFactFromSys(unlockedSystems[unlockedSystems.length - 1]) + " times the normal money!");
        CountingBot.getInstance().safeCounters();

    }

    public void bonus(Message message, BonusStreak.BonusCountType type, int count) {
        boolean exists = false;
        BonusStreak streak = null;
        for (BonusStreak currStreak : this.bonusStreaks) {
            if (currStreak.getType().equals(type)) {
                exists = true;
                streak = currStreak;
                break;
            }
        }
        if (!exists) {
            streak = generateBonusStreak(type);
        }
        streak.count(this, message, count);
        CountingBot.getInstance().safeCounters();
    }

    public BonusStreak generateBonusStreak(BonusStreak.BonusCountType type) {
        BonusStreak streak = new BonusStreak(type);
        BonusStreak[] newStreaks = new BonusStreak[this.bonusStreaks.length + 1];
        for (int i = 0; i < this.bonusStreaks.length; i++) {
            newStreaks[i] = this.bonusStreaks[i];
        }
        newStreaks[newStreaks.length - 1] = streak;
        this.bonusStreaks = newStreaks;
        return streak;
    }

    public void contractInfo(Message message) {
        String mess = "";
        if (contracts.size() == 0 && incomingContracts.size() == 0) {
            CountingBot.write(message, "You don't have any active contracts!");
            return;
        }
        if (contracts.size() == 1) {
            mess += "You pay to\n" + CountingBot.getInstance().getCounter(contracts.get(0).toId).getName() + ": " + contracts.get(0).toString();
        } else if (contracts.size() > 1) {
            mess += "You pay " + contractHandler.getCurrentTotalPerc() + " of your income to";
            for (Contract curr : contracts) {
                mess += "\n" + CountingBot.getInstance().getCounter(contracts.get(0).toId).getName() + ": " + curr.toString();
            }
        }
        if (incomingContracts.size() > 0) {
            mess += "\n\nYou get from";
            for (Contract curr : incomingContracts) {
                mess += "\n" + curr.owner.getName() + ": " + curr.toString();
            }
        }
        CountingBot.write(message, mess);
    }

    public void cancelContractsTo(Counter to) {
        Iterator<Contract> it = contracts.iterator();
        while (it.hasNext()) {
            Contract next = it.next();
            if (to.getId().equals(next.toId)) {
                it.remove();
            }
        }
        it = to.getIncomingContracts().iterator();
        while (it.hasNext()) {
            Contract next = it.next();
            if (getId().equals(next.toId)) {
                it.remove();
            }
        }
    }

    public void notifyCount(int number, int base) {
        currScoreAdd += Math.round(number * getFactFromSys(base));
    }

    public void notifyWin(int win, int base) {
        score += (int) (win);
    }

    public void succeed(int base) {
        addBonusScore(currScoreAdd);
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
        this.score += contractHandler.getNetto(score);
    }

    public void addBonusScoreFromContract(int score) {
        int taxed = (int) (score / 2);
        if (taxed != 0) {
            addBonusScore(taxed);
        }
        this.score += score - taxed;

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

    public String getPing() {
        return "<@!" + getId() + ">";
    }

    public ContractHandler getContractHandler() {
        return contractHandler;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    public List<Contract> getIncomingContracts() {
        return incomingContracts;
    }
}
