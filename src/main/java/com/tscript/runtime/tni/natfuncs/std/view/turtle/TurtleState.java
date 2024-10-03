package com.tscript.runtime.tni.natfuncs.std.view.turtle;

import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.tni.Environment;
import com.tscript.runtime.tni.natfuncs.std.view.ViewManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TurtleState {

    private record Key(String key, TscriptVM vm){
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key1 = (Key) o;
            return Objects.equals(key, key1.key) && Objects.equals(vm, key1.vm);
        }
        @Override
        public int hashCode() {
            return Objects.hash(key, vm);
        }
    }


    private static final Map<TscriptVM, TurtleState> pool = new HashMap<>();

    public static TurtleState getInstance(Environment env) {
        return pool.computeIfAbsent(env.getCurrentThread().getVM(), unused -> new TurtleState(env));
    }

    public static void dispose(Environment env) {
        pool.remove(env.getCurrentThread().getVM());
        ViewManager.dispose(env, "turtle");
    }


    private static final int BASE = -90;

    private final ViewManager viewManager;

    private boolean down = true;
    private int angle = BASE;
    private int x, y;

    public TurtleState(Environment env) {
        viewManager = ViewManager.getInstance(env, "turtle");
        viewManager.addDisposeListener(() -> dispose(env));
        x = viewManager.getWidth()/2;
        y = viewManager.getHeight()/2;
    }

    public void setColor(Color color){
        viewManager.setLineColor(color);
    }

    public void move(int distance){
        double x2 = Math.cos(Math.toRadians(angle)) * distance;
        double y2 = Math.sin(Math.toRadians(angle)) * distance;
        if (down)
            viewManager.drawLine(x, y, (int)(x+x2), (int)(y+y2));
        x += (int) x2;
        y += (int) y2;
    }

    public void setDown(boolean down){
        this.down = down;
    }

    public void turn(int angle){
        this.angle += angle;
    }

    public void reset(int x, int y, int angle, boolean down){
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.down = down;
    }

}
