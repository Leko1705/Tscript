package com.tscript.runtime.debug;

import java.util.*;

class ConsoleDebugger extends Debugger {

    private int threadId;
    private VMInfo info;

    @Override
    public void onDebug(int threadID, VMInfo info) {
        this.threadId = threadID;
        this.info = info;
        new Thread(new DebugProcess()).start();
    }


    private class DebugProcess implements Runnable {

        private final Deque<DebugInfo> stack = new ArrayDeque<>(List.of(info));

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);

            final String ANSI_YELLOW = "\u001B[33m";
            final String ANSI_RESET = "\u001B[0m";
            System.out.println(ANSI_YELLOW + "debug mode entered (called from thread-id: " + threadId + ")");
            plotInfo();

            label:
            while (true){
                System.out.print("~ ");
                String[] input = scanner.nextLine().split(" ");

                switch (input[0]) {
                    case "n", "next":
                        System.out.print(ANSI_RESET);
                        ConsoleDebugger.this.step();
                        break label;
                    case "r", "resume":
                        System.out.print(ANSI_RESET);
                        ConsoleDebugger.this.resume();
                        break label;
                    case "q", "quit":
                        System.out.print(ANSI_RESET);
                        ConsoleDebugger.this.quit();
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

                    case "s", "show":
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
        }

        private void help(){
            DebugInfo currentInfo = stack.peek();

            System.out.print("""
                    h/help   ->  show help
                    n/next   ->  step to next instruction in debug mode
                    r/resume ->  resumes the program without debug mode
                    q/quit   ->  quits the program immediately and completely
                    ..       ->  move out of info
                    s/show   ->  show current info
                    """);
            if (currentInfo instanceof VMInfo){
                System.out.print("""
                        m/move   ->  move into another info:
                            - t/thread <thread-id> -> the given thread-id  |  e.g.:  'm t 0' or 'move thread 0'
                            - h/heap               -> the used heap        |  e.g.:  'm h' or 'move heap'
                        """);
            }
            else if (currentInfo instanceof ThreadInfo){
                System.out.print("");
            }
        }

        private void plotInfo(){
            DebugInfo currentInfo = stack.peek();

            if (currentInfo instanceof VMInfo v){
                System.out.println("running threads: " + getRunningThreadsMsg(v));
                System.out.println("           heap: " + v.getHeapTree().getName());
            }

            else if (currentInfo instanceof ThreadInfo t){
                System.out.println("        id: " + t.getID());
                System.out.println("   in line: " + t.getLine());
                System.out.println("call-stack: " + getCallStackMsg(t));
            }

            else if (currentInfo instanceof FrameInfo f){
                System.out.println("  name: " + f.getName());
                System.out.println("locals: " + getDataListMsg(f.getLocals()));
                System.out.println(" stack: " + getDataListMsg(f.getStack()));
            }
        }

        private boolean move(String[] input){
            DebugInfo currentInfo = stack.peek();

            if (currentInfo instanceof VMInfo v){
                switch (input[1]){
                    case "t", "thread":
                        if (!isInt(input[2])){
                            System.out.println("thread-id expected -> numeric value");
                            return false;
                        }
                        int id = Integer.parseInt(input[2]);
                        for (ThreadInfo threadInfo : v.getThreadTrees()){
                            if (threadInfo.getID() == id) {
                                stack.push(threadInfo);
                                break;
                            }
                            System.out.println("thread-id " + id + " is not existent");
                            return false;
                        }

                        break;
                    case "h", "heap":
                        stack.push(v.getHeapTree());
                        break;
                }
            }

            else if (currentInfo instanceof ThreadInfo t){
                if (isInt(input[1])){
                    stack.push(t.getFrameTrees().get(Integer.parseInt(input[1])));
                }
                else {
                    for (FrameInfo threadInfo : t.getFrameTrees()){
                        if (input[1].equals(threadInfo.getName())) {
                            stack.push(threadInfo);
                            break;
                        }
                        System.out.println("frame " + input[1] + " is not existent");
                        return false;
                    }
                }
            }

            return true;
        }

        private String getRunningThreadsMsg(VMInfo vmInfo){
            List<String> lst = new ArrayList<>();
            for (ThreadInfo info : vmInfo.getThreadTrees())
                lst.add(Integer.toString(info.getID()));
            return lst.toString();
        }

        private String getCallStackMsg(ThreadInfo vmInfo){
            List<String> lst = new ArrayList<>();
            for (FrameInfo info : vmInfo.getFrameTrees())
                lst.add(info.getName());
            return lst.toString();
        }

        private String getDataListMsg(List<DataInfo> data){
            List<String> lst = new ArrayList<>();
            for (DataInfo info : data)
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

}
