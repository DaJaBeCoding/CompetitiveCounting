/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author DavidPrivat
 */
public class ContractHandler {
    private transient Counter owner;
    private transient List<Contract> contracts;
    public ContractHandler(Counter owner) {
        this.owner = owner;
        contracts = owner.getContracts();
    }
    
    public void addContract(Counter getter, float percentage, int limit) {
        Contract add = new Contract(getter, percentage, limit);
        contracts.add(add);
        add.owner = owner;
        getter.getIncomingContracts().add(add);
        return;
    }
    
    public void removeContract(Contract contract) {
        contracts.remove(contract);
    }
    
    public double getCurrentTotalPerc() {
        float all = 0;
        for(Contract curr: contracts) {
            all += curr.percentage;
        }
        return all;
    }
    
    public int getNetto(int brutto) {
        int netto = brutto;
        for(Contract curr: contracts) {
            int pay = curr.getPaid(brutto);
            netto -= pay;
            CountingBot.getInstance().getCounter(curr.toId).addBonusScoreFromContract(pay);
        }
        return netto;
    }
    
}
