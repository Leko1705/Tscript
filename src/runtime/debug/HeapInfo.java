package runtime.debug;

import java.util.Collection;

public interface HeapInfo extends DebugInfo {


    String getName();


    Collection<DataInfo> getData();

    @Override
    default <P, R> R process(DebugHandler<P, R> handler, P p) {
        return handler.handleHeapInfo(this, p);
    }
}
