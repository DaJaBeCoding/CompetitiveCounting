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
            youGetTrades = Tradable.generateTradables(youGet, initCounter, requCounter);
            iGetTrades = Tradable.generateTradables(iGet, requCounter, initCounter);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public Counter getRequestedUser() {
        return requCounter;
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
        if(tradable instanceof Tradable.ContractTrade) {
            Tradable.ContractTrade trade = (Tradable.ContractTrade) tradable;
            from.getContractHandler().addContract(to, trade.getPercentage(), trade.getLimit());
        }
        if(tradable instanceof Tradable.ContractNullTrade) {
            to.cancelContractsTo(from);
        }
    }
    
    public String getRequestedUserId() {
        return userId;
    }
    
    public String getUserPing() {
        return userPing;
    }
    
    public boolean isTradeOfferValid(Message message) {
        //check money
        if(getTotalMoneyRequirement(iGetTrades) > requCounter.getScore()) {
            CountingBot.write(message, userPing + " doesn't have enough money in their bank!");
            return false;
        }
        if(getTotalMoneyRequirement(youGetTrades) > initCounter.getScore()) {
            CountingBot.write(message, "You don't have enough money in your bank!");
            return false;
        }
        
        // check contract < 100%
        if(getTotalContractPerc(iGetTrades) + requCounter.getContractHandler().getCurrentTotalPerc() > 1) {
            CountingBot.write(message, initCounter.getName() + " can't give away more than 100% of their earnings!");
            return false;
        }
        if(getTotalContractPerc(youGetTrades) + initCounter.getContractHandler().getCurrentTotalPerc() > 1) {
            CountingBot.write(message, "You can't give away more than 100% of your earnings!");
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
        
        //check contracts < 100%
        if(getTotalContractPerc(iGetTrades) + requCounter.getContractHandler().getCurrentTotalPerc() > 1) {
            return initCounter.getName() + " can't give away more than 100% of their earnings!";
        }
        if(getTotalContractPerc(youGetTrades) + initCounter.getContractHandler().getCurrentTotalPerc() > 1) {
            return "You can't give away more than 100% of your earnings!";
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
    
    private float getTotalContractPerc(Tradable[] tradables) {
        float tot = 0;
        for(Tradable trad: tradables) {
            if(trad instanceof Tradable.ContractTrade) {
                tot += ((Tradable.ContractTrade)trad).getPercentage();
            }
        }
        return tot;
    }
}
