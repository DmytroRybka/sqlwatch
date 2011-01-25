package sqlwatch4.ui.adapters;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dmitry.mamonov
 */

public class CountingMap<K> extends LinkedHashMap<K, AtomicInteger> {
    public CountingMap() {
        super();
    }

    public int put(K key){
        if (containsKey(key)==false){
            put(key,new AtomicInteger(0));
        }
        return get(key).incrementAndGet();
    }

    public void putAll(CountingMap<K> merge) {
        for(K key:merge.keySet()){
            if (containsKey(key)==false){
                put(key,new AtomicInteger(0));
            }
            get(key).addAndGet(merge.get(key).intValue());
        }
    }
}
