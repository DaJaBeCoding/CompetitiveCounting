/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.Rules.*;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author DavidPrivat
 */
public class CountingStreak {
    private final String key;
    private int counter, lastCount;
    private Counter lastCounter;
    private HashMap<String,Counter> counters;
    private ArrayList<NumberRule> numberRules;
    private SlowModeRule slowModeRule;
    private TimeLimitRule timeLimitRule;
    private int currDivPrize = 50, divPrizeAdd = 50;
    private int currDigPrize = 100, digPrizeFact = 2;
    private int currRootPrize = 150;
    private int currTimePrize = 250, timePrizeFact = 2;
    private int currentBase;
    
    public CountingStreak(String key, int base) {
        this.key = key;
        counter = 1;
        lastCount = 0;
        counters = new HashMap<>();
        lastCounter = null;
        numberRules = new ArrayList<>();
        currentBase = base;
    }
    
    public boolean count(Message message, Counter user, String content) {
        if(!counters.containsKey(user.getId())) {
            counters.put(user.getId(), user);
        }
        int number = BaseSystems.toDecimal(content, currentBase);
        if(isNumCorrect(number, message) && (!user.equals(lastCounter))) {
            lastCount = number;
            incrementCounter();
            user.notifyCount(number, currentBase);
            
            
            
            
            if(slowModeRule != null) {
                slowModeRule.applyTimerToMessage(message);
            } else if(timeLimitRule != null) {
                timeLimitRule.applyTimerToMessage(message, lastCounter);
            } else {
                message.addReaction(ReactionEmoji.of(Long.parseLong("805121814296133653"), "kekmark", false)).subscribe();
            }
            lastCounter = user;
            return true;
        } else {
            fail(message, number, user);
            
            return false;
        }
    }
    
    private void fail(Message message, int number, Counter user) {
        message.addReaction(ReactionEmoji.unicode("\u274C")).subscribe();
            Rule winnerRule = getWinnerRule(number);
          
            
            
            String winnerName = "";
            if(winnerRule != null) {
                int loss = 0;
                if(winnerRule.getOwnerId().equals(user.getId())) {
                    loss = user.failFromOwn();
                    if(currentBase == 10) {
                        CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + lastCount + " due to his own rule '" + winnerRule.toString() + "' and lost " + loss + " money.");
                    } else {
                        CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") due to his own rule '" + winnerRule.toString() + "' and lost " + loss + " money.");
                    }
                } else {
                    loss = user.fail();
                    if(currentBase == 10) {
                        CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + lastCount + " and lost " + loss + " money.");
                    } else {
                        CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                    }
                    
                    int win = (int)(loss);
                    winnerName = counters.get(winnerRule.getOwnerId()).getName();
                    CountingBot.write(message, "Wrong number!\n" + winnerName + " has pulled a fast one on " + user.getName() + " with their '" + winnerRule.toString() + "' rule and got all of the victim's lost money, which is " + win + ".");
                    counters.get(winnerRule.getOwnerId()).notifyWin(win, currentBase);
                }
                
                
                
            } else {
                int loss = user.fail();
                if(currentBase == 10) {
                    CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + lastCount + " and lost " + loss + " money.");
                } else {
                    CountingBot.write(message, "Wrong number!\n" + user.getName() + " messed up after " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") and lost " + loss + " money.");
                }
            }
            
            counters.forEach((String key, Counter counter)->{
                if(!key.equals(user.getId())) {
                    counter.succeed(currentBase);
                }
            });
            CountingBot.getInstance().safeCounters();
    }
    
    private Rule getWinnerRule(int number) {
        for(NumberRule rule: numberRules) {
            if(!rule.numberAccepted(number)) {
                return rule;
            }
        }
        if(slowModeRule != null && slowModeRule.hasLost()) {
            return slowModeRule;
        }
        if(timeLimitRule != null && timeLimitRule.hasLost()) {
            return timeLimitRule;
        }
        return null;
    }
    
    private void incrementCounter() {
        do {
            counter++;
        } while(!numberAccepted());
    }
    
    private boolean canBuyDivRule(Counter author, Message message) {
        if(!author.isUnlocked(Unlockable.DIV_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if(currDivPrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + "out of the needed" + currDivPrize + " money to add this new rule.");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuyDigSumRule(Counter author, Message message) {
        if(!author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if(currDigPrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + "out of the needed" + currDigPrize + " money to add this new rule.");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuyRootRule(Counter author, Message message) {
        if(!author.isUnlocked(Unlockable.ROOT_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if(currRootPrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + "out of the needed" + currRootPrize + " money to add this new rule.");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuySlowmodeRule(Counter author, Message message) {
        if(!author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if(currTimePrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + "out of the needed" + currTimePrize + " money to add this new rule.");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuyTimelimitRule(Counter author, Message message) {

        if(!author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            CountingBot.write(message, "You have to unlock this rule before you can use it.");
            return false;
        }
        if(currTimePrize > author.getScore()) {
            CountingBot.write(message, "You only have " + author.getScore() + "out of the needed" + currTimePrize + " money to add this new rule.");
            return false;
        }
        return true;
    }
    
    
    private void addRuleInfo(Message message, Counter author) {
        String answer = "You can choose to add the following rules:\n";
        boolean anyRule = false;
        if(author.isUnlocked(Unlockable.DIV_RULE)) {
            answer += "\n'div': Numbers with the divisor n have to be skipped. (cost: " + currDivPrize + ")";
            anyRule = true;
        }
		if(author.isUnlocked(Unlockable.ROOT_RULE)) {
            answer += "\n'div': Numbers which have an integer nth root must be skipped. (cost: " + currRootPrize + ")";
            anyRule = true;
        }
        if(author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            answer += "\n'digsum': Numbers with digsum n must be skipped. (cost: " + currDigPrize + ")";
            anyRule = true;
        }
        if(author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            answer += "\n'slowmode': A certain time n has to pass between counts. (cost " + currTimePrize + ")";
            anyRule = true;
        }
        if(author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            answer += "\n'timelimit': The next number must have been counted before a certain time n has passed. (cost: " + currTimePrize + ")";
            anyRule = true;
        }
        answer += "\n\n syntax: '~addrule [name] [argument]";
        if(anyRule) {
        CountingBot.write(message, answer);
        }  else {
            CountingBot.write(message, "Unlock rules with ~unlock!");
        }
    }
    
    public void addRule(Message message, String ownerId) {
        if(!counters.containsKey(ownerId)) {
            counters.put(ownerId, CountingBot.getInstance().getCounter(ownerId));
        }
        String content = message.getContent();
        String[] splitted = content.split(" ");
        Counter author = counters.get(ownerId);
        
        if(splitted.length != 2 && splitted.length != 3) {
            addRuleInfo(message, author);
            return;
        }
        
        String ruleName;
        
        

        try {
            ruleName = splitted[1];
        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
        
        switch(ruleName) {
            case "div":
                if(!canBuyDivRule(author, message)) {
                    break;
                }
                if(splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int div = 0;
                try {
                    div = Integer.parseInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if(div < 2) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 1!");
                    return;
                }
                NumberRule.DividerRule add = new NumberRule.DividerRule(ownerId, div);
                addNumberRule(add);
                CountingBot.write(message, "You paid " + currDivPrize + " to add: " + add.toString());
                author.subtractScore(currDivPrize);
                currDivPrize += divPrizeAdd;
                break;
            case "digsum":
                if(!canBuyDigSumRule(author, message)) {
                    break;
                }
                if(splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int digsum = 0;
                try {
                    digsum = Integer.parseInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if(digsum < 1) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 0!");
                    return;
                }
                DigSumRule addDigSum = new DigSumRule(ownerId, digsum);
                addNumberRule(addDigSum);
                CountingBot.write(message, "You paid " + currDigPrize + " to add: " + addDigSum.toString());
                author.subtractScore(currDigPrize);
                currDigPrize *= digPrizeFact;
                break;
            case "root":
                if(!canBuyRootRule(author, message)) {
                    break;
                }
                if(splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int root = 0;
                try {
                    root = Integer.parseInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if(root < 2) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 1!");
                    return;
                }
                RootRule addRootRule = new RootRule(ownerId, root);
                addNumberRule(addRootRule);
                CountingBot.write(message, "You paid " + currRootPrize + " to add: " + addRootRule.toString());
                author.subtractScore(currRootPrize);
                break;
            case "slowmode":
                if(!canBuySlowmodeRule(author, message)) {
                    break;
                }
                if(splitted.length < 3) {
                    CountingBot.write(message, "Error: Please enter a number!");
                    return;
                }
                int slow = 0;
                try {
                    slow = Integer.parseUnsignedInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Error: Please enter an integer without special characters!");
                    return;
                }
                if(slow < 1) {
                    CountingBot.write(message, "Error: Please enter an integer greater than 0!");
                    return;
                }
                slowModeRule = new SlowModeRule(slow, ownerId);
                CountingBot.write(message, "You paid " + currTimePrize + " to add: " + slowModeRule.toString());
                if(timeLimitRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + timeLimitRule.toString());
                    timeLimitRule = null;
                }
                author.subtractScore(currTimePrize);
                currTimePrize *= timePrizeFact;
                break;
            case "timelimit":
                if(!canBuyTimelimitRule(author, message)) {
                    break;
                }
                timeLimitRule = new TimeLimitRule(ownerId, this);
                CountingBot.write(message,"You paid " + currTimePrize + " to add: " +  timeLimitRule.toString());
                if(slowModeRule != null) {
                    CountingBot.write(message, "This rule is being replaced: " + slowModeRule.toString());
                    slowModeRule = null;
                }
                author.subtractScore(currTimePrize);
                currTimePrize *= timePrizeFact;
                break;
            default:
                CountingBot.write(message, "This rule does not exist.");
        }
        if(!numberAccepted()) {
            incrementCounter();
        }
    }
    
    public void addNumberRule(NumberRule rule) {
        numberRules.add(rule);
    }
    
    public String getRulesRespond() {
        if(numberRules.isEmpty()) {
            return "No rules!";
        } else {
            String ret = "Active rules:";
            for(NumberRule rule: numberRules) {
                ret += "\n\t-" + rule.toString();
            }
            if(slowModeRule != null) {
                ret += "\n\t-" + slowModeRule.toString();
            }
            if(timeLimitRule != null) {
                ret += "\n\t-" + timeLimitRule.toString();
            }
            return ret;
        }
    }
    
    public String getBaseInfoRespond() {
        String ret = "Current base: " + currentBase;
        ret += "\nWith the characters:\n";
        if(currentBase == 1) {
            ret += "1&1";
        } else {
            for(int i = 0; i < currentBase; i++) {
                ret += BaseSystems.digitToChar(i) + " ";
            }
        }
        return ret;
    }
    
    private boolean numberAccepted() {
        for(NumberRule rule: numberRules) {
            if(!rule.numberAccepted(counter)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isNumCorrect(int num, Message message) {
        if(slowModeRule != null) {
            if(!slowModeRule.accepted(message)) {
               return false; 
            }
        }
        return num == (counter);
    }
    
    
    
    public int getLastNum() {
        return lastCount;
    }
    
    
    
    public String getKey() {
        return key;
    }

    public void timeLimitLost(String ownerId, Message message, Counter loser) {
       Message lostMessage = message.getChannel().block().createMessage("Whoops! Time ran out!").block();
       fail(lostMessage, lastCount, loser);
    }
    
    public int getBase() {
        return currentBase;
    }

   

   
    
}
