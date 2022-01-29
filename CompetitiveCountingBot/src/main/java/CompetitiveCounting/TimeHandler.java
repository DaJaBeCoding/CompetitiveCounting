/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CompetitiveCounting;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author DavidPrivat
 */
public class TimeHandler {
    
    public static boolean isYesterday(long epochDay) {
        LocalDate now = LocalDate.now();
        LocalDate test = LocalDate.ofEpochDay(epochDay);
        if(test.plusDays(1).equals(now)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static long nowInEpochDay() {
        return LocalDate.now().toEpochDay();
    }
    
}
