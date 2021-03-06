/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting.Rules;
/**
 *
 * @author DavidPrivat
 */
public class RootRule extends NumberRule{

    private final int root;
    public RootRule(String owner, int nthRoot) {
        super(owner);
        this.root = nthRoot;    
    }
    
    
    @Override
    public boolean numberAccepted(int number) {
        double rootVal = Math.pow(number, 1.0d/((double)root));
        double maxDiff = 0.00001d;
        double diff = Math.abs(rootVal - Math.round(rootVal));
        return  diff > maxDiff;
    }

    @Override
    public String toString() {
        return "Numbers with an integer " + root + "th root must be skipped!";
    }
    
}

