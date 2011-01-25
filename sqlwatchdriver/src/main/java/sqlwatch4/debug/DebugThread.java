package sqlwatch4.debug;

import sqlwatch4.TracesSliceContainer;

/**
 * @author dmitry.mamonov
 */
public class DebugThread extends Thread {
    public DebugThread() {
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            String json = TracesSliceContainer.get().cutSlice().toJson();
            System.out.println("json="+json);
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
        new Exception("Exit").printStackTrace();

    }
}
