package com.tscript.runtime.debugger;

import com.tscript.runtime.debugger.states.FrameState;
import com.tscript.runtime.debugger.states.ObjectState;
import com.tscript.runtime.debugger.states.ThreadState;
import com.tscript.runtime.debugger.states.VMState;

import java.util.*;

public class ConsoleDebugger implements Debugger {

    private Deque<Object> stack;

    @Override
    public void onHalt(long threadId, VMState state, DebugActionObserver observer) {

        Scanner scanner = new Scanner(System.in);
        stack = new ArrayDeque<>(List.of(state));

        final String ANSI_YELLOW = "\u001B[33m";
        final String ANSI_RESET = "\u001B[0m";
        System.out.println(ANSI_YELLOW + "debug mode entered (called from thread-id: " + threadId + ")\nType 'help' for more information.");

        label:
        while (true) {
            System.out.print("~ ");
            String[] input = scanner.nextLine().split(" ");

            switch (input[0]) {
                case "s", "step":
                    System.out.print(ANSI_RESET);
                    observer.onAction(Action.STEP_OVER);
                    break label;
                case "o", "out":
                    System.out.print(ANSI_RESET);
                    observer.onAction(Action.STEP_OUT);
                    break label;
                case "r", "resume":
                    System.out.print(ANSI_RESET);
                    observer.onAction(Action.RESUME);
                    break label;
                case "q", "quit":
                    System.out.print(ANSI_RESET);
                    observer.onAction(Action.QUIT);
                    break label;

                case "h", "help":
                    help();
                    break;

                case "..":
                    if (stack.size() == 1){
                        System.out.println("can not step back -> already in root node");
                        break;
                    }
                    stack.pop();
                    plotInfo();
                    break;

                case "p", "plot":
                    plotInfo();
                    break;

                case "m", "move":
                    boolean success = move(input);
                    if (success) plotInfo();
                    break;

                case "":
                    // nothing happens
                    break;

                default:
                    System.out.println("invalid input");
            }
        }

        stack = null;
    }


    private void help(){
        Object currentInfo = stack.peek();

        System.out.print("""
                    h/help   ->  show help
                    s/step   ->  step to next instruction in debug mode
                    o/out    ->  step out of the current function
                    r/resume ->  resumes the program without debug mode
                    q/quit   ->  quits the program immediately and completely
                    ..       ->  move out of info
                    p/plot   ->  show current info
                    """);
        if (currentInfo instanceof VMState){
            System.out.print("""
                        m/move   ->  move into another info:
                            - t/thread <thread-id> -> the given thread-id  |  e.g.:  'm t 0' or 'move thread 0'
                        """);
        }
        else if (currentInfo instanceof ThreadState){
            System.out.print("""
                        m/move   ->  move into another info:
                            - f/frame <frame-index> -> the n-th top frame  |  e.g.:  'm f 0' or 'move frame 0'
                        """);
        }
    }

    private void plotInfo(){
        Object currentInfo = stack.peek();

        if (currentInfo instanceof VMState v){
            System.out.println("running threads: " + getRunningThreadsMsg(v));
        }

        else if (currentInfo instanceof ThreadState t){
            System.out.println("        id: " + t.getId());
            System.out.println("call-stack: " + getCallStackMsg(t));
        }

        else if (currentInfo instanceof FrameState f){
            System.out.println("  name: " + f.getName());
            System.out.println("  line: " + f.getLineNumber());
            System.out.println("locals: " + getDataListMsg(f.getLocals()));
            System.out.println(" stack: " + getDataListMsg(f.getStack()));
        }
    }


    private boolean move(String[] input){
        Object currentInfo = stack.peek();

        if (currentInfo instanceof VMState v){
            if (input[1].equals("t") || input[1].equals("thread")){

                if (!isInt(input[2])){
                    System.out.println("thread-id expected -> numeric value");
                    return false;
                }

                long id = Long.parseLong(input[2]);
                for (ThreadState threadInfo : v.getThreads()){
                    if (threadInfo.getId() == id) {
                        stack.push(threadInfo);
                        break;
                    }
                    System.out.println("thread-id " + id + " is not existent");
                    return false;
                }
            }
        }

        else if (currentInfo instanceof ThreadState t){
            if (input[1].equals("f") || input[1].equals("frame")){
                if (isInt(input[2])) {
                    int index = Integer.parseInt(input[2]);
                    if (index >= 0 && index < t.getFrames().size()){
                        stack.push(t.getFrames().get(index));
                    }
                    else {
                        System.out.println("frame index " + index + " is not existent");
                    }
                }
            }
        }

        return true;
    }

    private String getRunningThreadsMsg(VMState vmInfo){
        List<String> lst = new ArrayList<>();
        for (ThreadState info : vmInfo.getThreads())
            lst.add(Long.toString(info.getId()));
        return lst.toString();
    }

    private String getCallStackMsg(ThreadState vmInfo){
        List<String> lst = new ArrayList<>();
        for (FrameState info : vmInfo.getFrames())
            lst.add(info.getName());
        return lst.toString();
    }

    private String getDataListMsg(List<ObjectState> data){
        List<String> lst = new ArrayList<>();
        for (ObjectState info : data)
            lst.add(info != null ? info.toString() : "'undefined'");
        return lst.toString();
    }

    private boolean isInt(String s){
        try {
            Integer.parseInt(s);
            return true;
        }catch (Exception e){
            return false;
        }
    }


}
