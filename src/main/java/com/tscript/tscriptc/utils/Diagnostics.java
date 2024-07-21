package com.tscript.tscriptc.utils;

public class Diagnostics {

    public enum Kind {
        ERROR,
        WARNING
    }

    public abstract static class Diagnose {
        private final Kind kind;
        private final String message;
        private final Location location;
        public Diagnose(Kind kind, String msg, Location location){
            this.kind = kind;
            this.message = msg;
            this.location = location;
        }
        public Kind getKind() {
            return kind;
        }
        public String getMessage() {
            return message;
        }
        public Location getLocation() {
            return location;
        }
    }

    public static class Error extends Diagnose {
        private final Phase phase;
        public Error(String msg, Location location, Phase phase) {
            super(Kind.ERROR, msg, location);
            this.phase = phase;
        }
        public Phase getPhase() {
            return phase;
        }

        @Override
        public String toString() {
            return getMessage() + " (in line " + getLocation().line() + ")";
        }
    }

    public static class Warning extends Diagnose {
        public Warning(String msg, Location location) {
            super(Kind.WARNING, msg, location);
        }
    }

}
