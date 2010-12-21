package sqlwatch4;

/**
 * @author dmitry.mamonov
 */
public class TraceDispatcher {
    private static class Holder {
        private static final TraceDispatcher INSATANCE = new TraceDispatcher();
    }

    private TraceDispatcher() {
    }


    public static TraceDispatcher get(){
        return Holder.INSATANCE;
    }

    public void spread(sqlwatch4.model.Trace trace){

    }
}
