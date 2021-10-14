/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PVPCountingBot;

/**
 *
 * @author DavidPrivat
 */
public enum Unlockable {
    UNLOCK_COMMAND(10000,"Unlock command", "Die Berechtigung, mit ~unlock neue Dinge freizuschalten"),
    DIV_RULE(20000, "div_rule", "Vielfache von einer Zahle müssen übersprungen werden"),
    DIGSUM_RULE(35000, "digsum_rule", "Zahlen mit einer bestimmten Quersumme müssen übersprungen werden"),
    SLOWMODE_RULE(50000, "slowmode_rule", "Bevor der nächste zählen darf, muss erst eine bestimmte Zeit vergehen"),
    TIMELIMIT_RULE(100000, "timelimit_rule", "Zwischen zwei Zahlen darf maximal eine bestimmte Zeit vergehen"),
    
    BASE_1(-1, "base1", "Zählen wie die Möven aus findet Nemo"),
    BASE_2(-1, "base2", "Zählen im Binärsystem!"),
    BASE_3(-1, "base3", "Zählen im Trinärsystem"),
    BASE_16(-1, "base16", "Zählen im Hexadezimalsystem");
    private final int prize;
    private String name, description;
    Unlockable(int prize, String name, String description) {
        this.prize = prize;
        this.name = name;
        this.description = description;
    }
    
    public int getPrize() {
        return prize;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}
