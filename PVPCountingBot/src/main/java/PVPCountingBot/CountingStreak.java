/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PVPCountingBot;

import PVPCountingBot.Rules.DigSumRule;
import PVPCountingBot.Rules.NumberRule;
import PVPCountingBot.Rules.Rule;
import PVPCountingBot.Rules.SlowModeRule;
import PVPCountingBot.Rules.TimeLimitRule;
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
            user.notifyCount(number);
            
            
            
            
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
                        message.getChannel().block().createMessage(user.getName() + " hat nach " + lastCount + " an seiner eigenen Regel '" + winnerRule.toString() + "' gekackt und " + loss + " Geld verloren!").subscribe();
                    } else {
                        message.getChannel().block().createMessage(user.getName() + " hat nach " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") an seiner eigenen Regel '" + winnerRule.toString() + "' gekackt und " + loss + " Geld verloren!").subscribe();
                    }
                } else {
                    loss = user.fail();
                    if(currentBase == 10) {
                        message.getChannel().block().createMessage(user.getName() + " hat nach " + lastCount + " gekackt und " + loss + " Geld verloren!").subscribe();
                    } else {
                        message.getChannel().block().createMessage(user.getName() + " hat nach " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") gekackt und " + loss + " Geld verloren!").subscribe();
                    }
                    
                    int win = (int)(loss);
                    winnerName = counters.get(winnerRule.getOwnerId()).getName();
                    CountingBot.write(message, winnerName + " hat " + user.getName() + " mit der Regel '" + winnerRule.toString() + "' zum Verlieren gebracht und gewinnt " + win + " Geld von ihm!");
                    counters.get(winnerRule.getOwnerId()).notifyWin(win, currentBase);
                }
                
                
                
            } else {
                int loss = user.fail();
                if(currentBase == 10) {
                    message.getChannel().block().createMessage(user.getName() + " hat nach " + lastCount + " gekackt und " + loss + " Geld verloren!").subscribe();
                } else {
                    message.getChannel().block().createMessage(user.getName() + " hat nach " + BaseSystems.decimalToSystem(lastCount, currentBase) + " (=" + lastCount + ") gekackt und " + loss + " Geld verloren!").subscribe();
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
            CountingBot.write(message, "Schalte diese Regel erst mit ~unlock frei!");
            return false;
        }
        if(currDivPrize > author.getScore()) {
            CountingBot.write(message, "Du hast nur " + author.getScore() + "/" + currDivPrize + " Geld!");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuyDigSumRule(Counter author, Message message) {
        if(!author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            CountingBot.write(message, "Schalte diese Regel erst mit ~unlock frei!");
            return false;
        }
        if(currDigPrize > author.getScore()) {
            CountingBot.write(message, "Du hast nur " + author.getScore() + "/" + currDigPrize + " Geld!");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuySlowmodeRule(Counter author, Message message) {
        if(!author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            CountingBot.write(message, "Schalte diese Regel erst mit ~unlock frei!");
            return false;
        }
        if(currTimePrize > author.getScore()) {
            CountingBot.write(message, "Du hast nur " + author.getScore() + "/" + currTimePrize + " Geld!");
            return false;
        }
        
        return true;
    }
    
    private boolean canBuyTimelimitRule(Counter author, Message message) {

        if(!author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            CountingBot.write(message, "Schalte diese Regel erst mit ~unlock frei!");
            return false;
        }
        if(currTimePrize > author.getScore()) {
            CountingBot.write(message, "Du hast nur " + author.getScore() + "/" + currTimePrize + " Geld!");
            return false;
        }
        return true;
    }
    
    private void addRuleInfo(Message message, Counter author) {
        String answer = "Folgende rules kannst du hinzufügen:\n";
        boolean anyRule = false;
        if(author.isUnlocked(Unlockable.DIV_RULE)) {
            answer += "\n'div': Vielfache einer Zahl müssen übersprungen werden (für " + currDivPrize + " geld)";
            anyRule = true;
        }
        if(author.isUnlocked(Unlockable.DIGSUM_RULE)) {
            answer += "\n'digsum': Zahlen mit einer bestimmten Quersumme müssen übersprungen werden (für " + currDigPrize + " geld)";
            anyRule = true;
        }
        if(author.isUnlocked(Unlockable.SLOWMODE_RULE)) {
            answer += "\n'slowmode': Zwischen zwei Zahlen muss eine bestimmte Zeit gewartet werden (für " + currTimePrize + " geld)";
            anyRule = true;
        }
        if(author.isUnlocked(Unlockable.TIMELIMIT_RULE)) {
            answer += "\n'timelimit': Zwischen zwei Zahlen dürfen maximal 10 Sekunden vergehen (für " + currTimePrize + " geld)";
            anyRule = true;
        }
        answer += "\n\n syntax: '~addrule [name] [argument]";
        if(anyRule) {
        CountingBot.write(message, answer);
        }  else {
            CountingBot.write(message, "Schalte mit '~unlock' rules frei!");
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
                    CountingBot.write(message, "Bitte eine Zahl angeben!");
                    return;
                }
                int div = 0;
                try {
                    div = Integer.parseInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Bitte eine ganze Zahl ohne Sonderzeichen angeben!");
                    return;
                }
                if(div < 2) {
                    CountingBot.write(message, "Bitte eine ganze Zahl > 1 angeben!");
                    return;
                }
                NumberRule.DividerRule add = new NumberRule.DividerRule(ownerId, div);
                addNumberRule(add);
                CountingBot.write(message, "Du hast für " + currDivPrize + " geld hinzugefügt: " + add.toString());
                author.subtractScore(currDivPrize);
                currDivPrize += divPrizeAdd;
                break;
            case "digsum":
                if(!canBuyDigSumRule(author, message)) {
                    break;
                }
                if(splitted.length < 3) {
                    CountingBot.write(message, "Bitte eine Zahl angeben!");
                    return;
                }
                int digsum = 0;
                try {
                    digsum = Integer.parseInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Bitte eine ganze Zahl ohne Sonderzeichen angeben!");
                    return;
                }
                if(digsum < 1) {
                    CountingBot.write(message, "Bitte eine ganze Zahl > 0 angeben!");
                    return;
                }
                DigSumRule addDigSum = new DigSumRule(ownerId, digsum);
                addNumberRule(addDigSum);
                CountingBot.write(message, "Du hast für " + currDigPrize + " geld hinzugefügt: " + addDigSum.toString());
                author.subtractScore(currDigPrize);
                currDigPrize *= digPrizeFact;
                break;
            case "slowmode":
                if(!canBuySlowmodeRule(author, message)) {
                    break;
                }
                if(!counters.get(ownerId).isUnlocked(Unlockable.SLOWMODE_RULE)) {
                    CountingBot.write(message, "Diese rule muss erst freigeschaltet werden (~unlock slowmoderule)!");
                    return; 
                }
                if(splitted.length < 3) {
                    CountingBot.write(message, "Bitte eine Zahl angeben!");
                    return;
                }
                int slow = 0;
                try {
                    slow = Integer.parseUnsignedInt(splitted[2]);
                } catch(NumberFormatException e) {
                    CountingBot.write(message, "Bitte eine natürliche Zahl ohne Sonderzeichen angeben!");
                    return;
                }
                if(slow < 1) {
                    CountingBot.write(message, "Bitte eine ganze Zahl > 0 angeben!");
                    return;
                }
                if(slowModeRule != null && slowModeRule.getDuration() > slow) {
                    CountingBot.write(message, "Es gibt bereits einen slow mode, der mindestens so stark wie Deiner ist!");
                    return;
                }
                slowModeRule = new SlowModeRule(slow, ownerId);
                CountingBot.write(message, "Du hast für " + currTimePrize + " geld hinzugefügt: " + slowModeRule.toString());
                if(timeLimitRule != null) {
                    CountingBot.write(message, "Folgende Regel wird ersetzt: " + timeLimitRule.toString());
                    timeLimitRule = null;
                }
                author.subtractScore(currTimePrize);
                currTimePrize *= timePrizeFact;
                break;
            case "timelimit":
                if(!canBuyTimelimitRule(author, message)) {
                    break;
                }
                if(!counters.get(ownerId).isUnlocked(Unlockable.TIMELIMIT_RULE)) {
                    CountingBot.write(message, "Diese rule muss erst freigeschaltet werden (~unlock timelimitrule)!");
                    return; 
                }
                if(timeLimitRule != null) {
                    CountingBot.write(message, "Es gibt bereits ein time limit!");
                    return;
                }
                timeLimitRule = new TimeLimitRule(ownerId, this);
                CountingBot.write(message,"Du hast für " + currTimePrize + " geld hinzugefügt: " +  timeLimitRule.toString());
                if(slowModeRule != null) {
                    CountingBot.write(message, "Folgende Regel wird ersetzt: " + slowModeRule.toString());
                    slowModeRule = null;
                }
                author.subtractScore(currTimePrize);
                currTimePrize *= timePrizeFact;
                break;
            default:
                CountingBot.write(message, "Diese Regel gibt es nicht.");
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
            return "Keine Regeln!";
        } else {
            String ret = "Aktive Regeln:";
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
        Message lostMessage = message.getChannel().block().createMessage("Zeit abgelaufen!").block();
        fail(lostMessage, lastCount, loser);
    }
    
    public int getBase() {
        return currentBase;
    }

   

   
    
}
