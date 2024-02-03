package runtime.debug;

import java.util.List;

public interface ThreadInfo extends DebugInfo {

    int getID();

    int getLine();

    List<FrameInfo> getFrameTrees();

    @Override
    default <P, R> R process(DebugHandler<P, R> handler, P p) {
        return handler.handleThreadInfo(this, p);
    }
}
