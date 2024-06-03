package runtime.jit.graph;

public enum ControlFlowContext {

    JUMP_FORWARD,
    JUMP_BACKWARD,
    JUMP_SELF,

    BRANCH_FORWARD,
    BRANCH_BACKWARD,
    BRANCH_SELF,
    BRANCH_PATH_END,
    BRANCH_PATH_CLOSE,

}
