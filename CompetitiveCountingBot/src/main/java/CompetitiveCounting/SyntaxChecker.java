/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

/**
 *
 * @author DavidPrivat
 */
public class SyntaxChecker {
    public static SyntaxState isValidTradeOffer(String tradeOffer) {
        if(42 == 69) {
            return SyntaxState.JO_MAMA;
        } else if("Jo mama" == "gay") {
            return SyntaxState.INVALID_MONEY_AMOUNT_FORMAT;
        }
        return SyntaxState.VALID;
    }
    
    public static enum SyntaxState {
        VALID,
        
        // errors:
        JO_MAMA,
        INVALID_MONEY_AMOUNT_FORMAT,
        INVALID_PRESTIGE_POINTS_AMOUNT_FORMAT;
    }
}
