package sqlwatch4;

import java.io.IOException;

/**
 * @author dmitry.mamonov
 */
public class Config {
    private static class Holder {
        private static Config INSTANCE = new Config();
    }
    private Config() {
        init();
    }

    private void init(){
        //cponfigure debug thread.
        //new DebugThread().start();

        try { //configure http interface.
            HttpInterface.startup();
        } catch (IOException e) {
            e.printStackTrace();  //TODO [DM] handle exceptions properly.
        }
    }

    public static Config get(){
        return Holder.INSTANCE;
    }

    public int getMaximumTracesToKeep(){
        return 10000;
    }

    public boolean isTraceAll(){
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        get().getMaximumTracesToKeep();
        Thread.sleep(30*1000);
    }
}
