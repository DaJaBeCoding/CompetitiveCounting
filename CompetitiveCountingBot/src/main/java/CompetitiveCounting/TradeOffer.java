/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import CompetitiveCounting.Tradable.MoneyTrade;
import discord4j.core.object.entity.Message;

/**
 *
 * @author DavidPrivat
 */
public class TradeOffer {
    public final static String YOU_GET = "YOU_GET:";
    public final static String I_GET = "I_GET:";
    private String userId, userPing;
    private Tradable[] youGetTrades, iGetTrades;
    private Counter initCounter, requCounter;
    public TradeOffer(String content, Counter initC) { //in upper case
        initCounter = initC;
        String[] splitted = content.split(YOU_GET);
        String init = splitted[0];
        String offers = splitted[1];
        splitted = offers.split(I_GET);
        String youGet = splitted[0];
        String iGet;
        if(splitted.length > 1) {
            iGet = splitted[1];
        } else{
            iGet = "";
        }
        splitted = init.split(" ");
        userPing = splitted[1].replaceAll(" ", "");
        userId = userPing.substring(3,userPing.length()-1);
        requCounter = CountingBot.getInstance().getCounter(userId);
        try {
        youGetTrades = Tradable.generateTradables(youGet);
            iGetTrades = Tradable.generateTradables(iGet);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void fullfill() {
        for(Tradable currTr: youGetTrades) {
            giveTradableFromTo(initCounter, requCounter, currTr);
        }
        for(Tradable currTr: iGetTrades) {
            giveTradableFromTo(requCounter, initCounter, currTr);
        }
    }
    
    private void giveTradableFromTo(Counter from, Counter to, Tradable tradable) {
        if(tradable instanceof Tradable.MoneyTrade) {
            from.transferTo(to,((MoneyTrade)tradable).getAmount());
            return;
        }
    }
    
    public String getRequestedUserId() {
        return userId;
    }
    
    public String getUserPing() {
        return userPing;
    }
    
    public boolean isTradeOfferValid(Message message, Counter initiator, Counter requested) {
        //check money
        if(getTotalMoneyRequirement(iGetTrades) > requested.getScore()) {
            CountingBot.write(message, userPing + " doesn't have enough money in their bank!");
            return false;
        }
        if(getTotalMoneyRequirement(youGetTrades) > initiator.getScore()) {
            CountingBot.write(message, "You don't have enough money in your bank!");
            return false;
        }
        return true;
    }
    
    public String isTradeOfferValid() {
        //check money
        if(getTotalMoneyRequirement(iGetTrades) > requCounter.getScore()) {
            return "You don't have enough money in your bank!";
        }
        if(getTotalMoneyRequirement(youGetTrades) > initCounter.getScore()) {
            return initCounter.getPing() + " doesn't have enough money in their bank!";
        }
        return "VALID";
    }
    
    private int getTotalMoneyRequirement(Tradable[] tradables) {
        int money = 0;
        for(Tradable trad: tradables) {
            if(trad instanceof Tradable.MoneyTrade) {
                money += ((Tradable.MoneyTrade)trad).getAmount();
            }
        }
        return money;
    }
}
