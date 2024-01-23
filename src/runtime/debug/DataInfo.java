package runtime.debug;

import java.util.List;

public interface DataInfo extends DebugInfo {

    List<DataInfo> getChildren();

    String toString();

    @Override
    default <P, R> R process(DebugHandler<P, R> handler, P p){
        return handler.handleDataInfo(this, p);
    }
}
