package pt.unl.fct.di.tsantos.util;

import java.util.Date;
import org.tiling.scheduling.ScheduleIterator;

public class ModifiableScheduleIterator implements ScheduleIterator {

    private long period;
    private Date next;

    public ModifiableScheduleIterator(long delay, long period) {
        long start = System.currentTimeMillis();
        this.period = period;
        this.next = new Date(start + delay);
    }

    public void setNext(Date next) {
        this.next = next;
    }

    public Date next() {
        Date res = next;
        //System.out.println("RESULT: " + res);
        next = new Date(res.getTime() + period);
        //System.out.println("NEXT: " + next);
        return res;
    }
}
