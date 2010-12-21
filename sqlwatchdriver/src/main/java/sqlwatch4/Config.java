package sqlwatch4;

/**
 * @author dmitry.mamonov
 */
public class Config {
    private static class Holder {
        private static Config INSTANCE = new Config();
    }
    private Config() {
    }

    public static Config get(){
        return Holder.INSTANCE;
    }

    public int getMaximumTracesToKeep(){
        return 10000;
    }
}
