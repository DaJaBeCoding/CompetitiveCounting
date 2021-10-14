/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PVPCountingBot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

/**
 *
 * @author DavidPrivat
 */
public class CountingBot {

    private String commandIndicator = "~";
    private Storage storage;
    private HashMap<String, Counter> counters;
    private HashMap<String, CountingStreak> streaks;

    private static CountingBot instance;

    public CountingBot() {
        storage = new Storage();
        counters = storage.loadCounters();
        System.out.println(counters);
        streaks = new HashMap<>();
        instance = this;
    }

    public void message(Message message) {
        String content = message.getContent();
        addCounterOptionally(message.getAuthor().get());
        checkCommands(message);

        count(message);

    }

    private void checkCommands(Message message) {
        String content = message.getContent();
        if (content.startsWith(commandIndicator)) {
            addCounterOptionally(message.getAuthor().get());
            if (content.startsWith(commandIndicator + "help")) {
                message.getChannel().block().createMessage("IMAGINE du brauchst hilfe also ne, sowas gayes gibts hier nicht. \nWenn du help willst frag schnadolino ob sie dir eine schreibt. \n(imagine keine nudes von ihr zu haben hehe no)").subscribe();
            } else if (content.startsWith(commandIndicator + "scoreboard") || content.equals(commandIndicator + "top")) {
                message.getChannel().block().createMessage(scoreboard()).subscribe();
            } else if (content.startsWith(commandIndicator + "score")) {
                message.getChannel().block().createMessage("Du hast " + getScore(message.getAuthor().get()) + " Geld").subscribe();
            } else if (content.startsWith(commandIndicator + "num")) {
                String channelId = message.getChannelId().asString();
                if (streaks.containsKey(channelId)) {
                    message.getChannel().block().createMessage("Die letzte Zahl war " + (streaks.get(channelId).getLastNum())).subscribe();
                } else {
                    message.getChannel().block().createMessage("Du kannst mit '1' starten!").subscribe();
                }
            } else if (content.startsWith(commandIndicator + "addrule")) {
                addRule(message);
            } else if (content.startsWith(commandIndicator + "rules")) {
                rules(message);
            } else if (content.startsWith(commandIndicator + "unlock")) {
                unlock(message);
            } else if (content.equals(commandIndicator + "prestige")) {
                prestige(message);
            } else if (content.equals(commandIndicator + "bal") || content.equals(commandIndicator + "balance")) {
                balance(message);
            } else if (content.startsWith(commandIndicator + "bonus")) {
                bonus(message);
            }
        }
    }
    
    private void bonus(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        String[] splitted = message.getContent().split(" ");
        if(splitted.length != 3 || !Util.isNumber(splitted[2])) {
            bonusInfo(message);
            return;
        }
        switch(splitted[1]) {
            case "daily":
                author.bonus(message, BonusStreak.BonusCountType.DAILY, (int)Double.parseDouble(splitted[2]));
                break;
            default:
                bonusInfo(message);
                return;
        }
    }
    
    private void bonusInfo(Message message) {
        CountingBot.write(message, "Erhalte Geld fürs alleine Zaehlen mit dem Bonus Command!\nSyntax: '~bonus [type] [count]'\nTypes: daily");
    }
    
    private void balance(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if(author.getPrestiges() > 0) {
            CountingBot.write(message, "Du besitzt " + author.getScore() + " geld und " + author.getPrestigePoints() + " Prestigepunkt[e]");
        } else {
            CountingBot.write(message, "Du besitzt " + author.getScore() + " geld");
        }
    }

    private void prestige(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        if (author.prestige(message)) {
            CountingBot.write(message, "GG WP, du hast prestiged! Dein Fortschritt wurde zurückgesetzt, dafür erhältst du einen Prestigepunkt! Unlocke damit neue Zählsysteme, um in diesen bonusgeld beim Zählen zu bekommen!");
        } else {
            return;
        }
    }

    private void unlock(Message message) {
        Counter author = this.getCounter(this.generateKeyFromUser(message.getAuthor().get()));
        String[] splitted = message.getContent().split(" ");

        if (author.isUnlocked(Unlockable.UNLOCK_COMMAND)) {
            if (splitted.length != 2) {
                this.unlockInfo(message, author);
                return;
            }
            String toUnlock = splitted[1];
            for (int i = 0; i < Unlockable.values().length; i++) {
                Unlockable currUnlockable = Unlockable.values()[i];
                if (currUnlockable.getName().equals(toUnlock)) {
                    author.unlock(currUnlockable, message);
                    return;
                }
            }

            CountingBot.write(message, "Unlock nicht gefunden");
        } else {
            if (author.getScore() < Unlockable.UNLOCK_COMMAND.getPrize()) {
                this.unlockInfo(message, author);
                return;
            } else {
                author.unlock(Unlockable.UNLOCK_COMMAND, message);
            }
        }
    }

    private void unlockInfo(Message message, Counter author) {
        if (author.isUnlocked(Unlockable.UNLOCK_COMMAND)) {
            String answ = "Schalte neue Features mit dem ~unlock Command frei!\nSyntax: ~unlock [unlock name]\n\nZum unlocken verfügbar:";
            boolean anyUnlockable = false;
            int currCount = 1;
            for (int i = 0; i < Unlockable.values().length; i++) {
                Unlockable currUnlockable = Unlockable.values()[i];
                if(i >= Unlockable.BASE_1.ordinal()) {
                    if(author.getPrestiges() == 0) {
                        continue;
                    }
                }
                if (!author.isUnlocked(currUnlockable)) {
                    answ += "\n" + String.valueOf(currCount) + ".  '" + currUnlockable.getName() + "': " + currUnlockable.getDescription();
                    currCount ++;
                    if(currUnlockable.getPrize() > 0) {
                        answ += " (" + currUnlockable.getPrize() + " geld)";
                    } else {
                        answ += " (" + Math.abs(currUnlockable.getPrize()) + " prestigepunkt[e])";
                    }
                    anyUnlockable = true;
                }
                
                
            }
            if (anyUnlockable) {
                CountingBot.write(message, answ);
            } else {
                CountingBot.write(message, "Du besitzt bereits alles!");
            }
        } else {
            CountingBot.write(message, "Schalte für " + Unlockable.UNLOCK_COMMAND.getPrize() + " den unlock command frei, um rules unlocken zu können! Dafür brauchst du noch " + (Unlockable.UNLOCK_COMMAND.getPrize() - author.getScore()) + "!");
        }
    }

    private void addRule(Message message) {
        String channelID = message.getChannelId().asString();
        if (streaks.containsKey(channelID)) {
            String content = message.getContent();
            streaks.get(channelID).addRule(message, generateKeyFromUser(message.getAuthor().get()));
        } else {
            write(message, "Starte eine counting streak, bevor du Regeln hinzufügst!");
        }
    }

    private void rules(Message message) {
        String channelId = message.getChannelId().asString();
        if (streaks.containsKey(channelId)) {
            write(message, streaks.get(channelId).getRulesRespond());
        } else {
            write(message, "Keine Regeln!");
        }
    }

    private String scoreboard() {
        ArrayList<Counter> countersSorted = new ArrayList<>();
        counters.forEach((String key, Counter counter) -> {
            countersSorted.add(counter);
        });
        countersSorted.sort(new Comparator<Counter>() {
            @Override
            public int compare(Counter arg0, Counter arg1) {    // arg0 > arg1 => 1
                if (arg1.getPrestiges() > arg0.getPrestiges() || (arg1.getPrestiges() == arg0.getPrestiges() && arg1.getPossibleTotal() > arg0.getPossibleTotal())) {
                    return 1;
                } else if (arg1.getPrestiges() == arg0.getPrestiges() && arg1.getPossibleTotal() == arg0.getPossibleTotal()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        String message = "Scoreboard:";
        for (Counter counter : countersSorted) {
            if (counter.getPrestiges() != 0) {
                message += "\n" + counter.getName() + " : " + counter.getPossibleTotal() + " score (" + counter.getPrestiges() + " mal prestiged)";
            } else {
                message += "\n" + counter.getName() + " : " + counter.getPossibleTotal() + " score";
            }
        }
        return message;
    }

    private void count(Message message) {
        User user = message.getAuthor().get();
        String channelKey = message.getChannelId().asString();
        String content = message.getContent();
        boolean deleteStreak = false;
        Counter author = null;
        if (counters.containsKey(generateKeyFromUser(user))) {
            author = counters.get(generateKeyFromUser(user));
        }
        if (streaks.containsKey(channelKey)) {
            CountingStreak streak = streaks.get(channelKey);
            if ((!BaseSystems.isNumInSystem(content, streak.getBase()))) {
                return;
            }
            if (author == null || (!author.isBaseUnlocked(streak.getBase()))) {
                CountingBot.write(message, "Du hast dieses System noch nicht freigeschalten!");
                return;
            }
            deleteStreak = !streak.count(message, counters.get(generateKeyFromUser(user)), content);
        } else {
            String[] splitted = content.split(" ");
            if (content.equals("1") || (splitted[0].equals("1") && splitted.length == 3 && splitted[1].equals("base") && Util.isNumber(splitted[2]))) {
                if (content.equals("1")) {
                    streaks.put(channelKey, new CountingStreak(channelKey, 10));
                } else {
                    if (author == null || (!author.isBaseUnlocked(Integer.parseInt(splitted[2])))) {
                        CountingBot.write(message, "Du hast dieses System noch nicht freigeschalten!");
                        return;
                    }
                    streaks.put(channelKey, new CountingStreak(channelKey, Integer.parseInt(splitted[2])));
                }
                deleteStreak = !streaks.get(channelKey).count(message, counters.get(generateKeyFromUser(user)), splitted[0]);
            }
        }
        if (deleteStreak) {
            streaks.remove(channelKey);
        }

    }

    public void removeStreak(String streakId) {
        streaks.remove(streakId);
    }

    public void safeCounters() {
        storage.safeCounters(counters);
    }

    public static void write(Message message, String s) {
        message.getChannel().block().createMessage(s).subscribe();
    }

    private int getScore(User user) {
        int score = counters.get(generateKeyFromUser(user)).getPossibleTotal();
        return score;
    }

    private void increaseScore(User user, int incr) {
        counters.get(generateKeyFromUser(user)).notifyCount(incr);
    }

    private void addCounterOptionally(User user) {

        String key = generateKeyFromUser(user);
        if (!counters.containsKey(key)) {
            counters.put(key, new Counter(key, user.getUsername(), 0, 0, 0 , new int[]{},new int[]{},new BonusStreak[]{}));
            storage.safeCounters(counters);
        }

    }

    private String generateKeyFromUser(User user) {
        return user.getId().asString();
    }

    public Counter getCounter(String id) {
        return counters.get(id);
    }

    public static CountingBot getInstance() {
        return instance;
    }

}
