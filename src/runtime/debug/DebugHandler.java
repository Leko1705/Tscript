package runtime.debug;

public interface DebugHandler<P, R> {

    R handleVMInfo(VMInfo info, P p);

    R handleHeapInfo(HeapInfo info, P p);

    R handleThreadInfo(ThreadInfo info, P p);

    R handleFrameInfo(FrameInfo info, P p);

    R handleDataInfo(DataInfo info, P p);

}
