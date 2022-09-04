package pt.unl.fct.di.tsantos.util.time;

import java.util.Date;

public class DateUtils {

    public static long difference(Date a, Date b) {
        return (a.getTime() - b.getTime());
    }
}
