package sqlwatch4;

import sqlwatch4.model.Trace;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author dmitry.mamonov
 */
public class TraceDispatcher {
    private static class Holder {
        private static final TraceDispatcher INSTANCE = new TraceDispatcher();
    }

    private TraceDispatcher() {
        addListener(TracesSliceContainer.get());
    }


    public static TraceDispatcher get(){
        return Holder.INSTANCE;
    }


    private CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<Listener>();

    public void spread(Trace trace){
        for(Listener lst:listeners){
            try {
                lst.takeTrace(trace);
            } catch(Exception shouldNotHappen){
                System.out.println("Failed to dispatch trace: "+shouldNotHappen.getMessage());
            }
        }
    }

    public void addListener(Listener lst){
        listeners.add(lst);
    }

    public void removeListener(Listener lst){
        listeners.remove(lst);
    }

    public static interface Listener {
        void takeTrace(Trace trace);
    }
}
