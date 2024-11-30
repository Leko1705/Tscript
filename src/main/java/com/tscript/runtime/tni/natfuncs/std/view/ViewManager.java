package com.tscript.runtime.tni.natfuncs.std.view;

import com.tscript.runtime.core.TscriptVM;
import com.tscript.runtime.tni.Environment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

public class ViewManager extends JFrame {

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


    private static final Map<Key, ViewManager> pool = new HashMap<>();

    public static ViewManager getInstance(Environment env, String usecase) {
        Key key = new Key(usecase, env.getCurrentThread().getVM());
        return pool.computeIfAbsent(key, unused -> new ViewManager(env, usecase));
    }

    public static void dispose(Environment env, String usecase) {
        Key key = new Key(usecase, env.getCurrentThread().getVM());
        pool.remove(key);
    }


    private final DynamicPrintPane canvas;

    private Color lineColor = Color.BLACK;
    private Color fillColor = Color.BLACK;

    private final Set<Runnable> disposeListeners = new HashSet<>();

    private ViewManager(Environment env, String usecase) {
        canvas = new DynamicPrintPane();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(100, 100, (int) dim.getWidth(), (int) dim.getHeight());

        getContentPane().add(canvas);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        canvas.paintImmediately(canvas.getBounds());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                dispose(env, usecase);
                for (Runnable r : disposeListeners) {
                    r.run();
                }
            }
        });
    }

    public void addDisposeListener(Runnable listener) {
        disposeListeners.add(Objects.requireNonNull(listener));
    }

    public void removeDisposeListener(Runnable listener) {
        disposeListeners.remove(listener);
    }


    public void drawLine(int x1, int y1, int x2, int y2) {
        canvas.setColor(lineColor);
        canvas.drawLine(x1, y1, x2, y2);
    }

    public void frameRect(int x, int y, int w, int h) {
        canvas.setColor(lineColor);
        canvas.frameRect(x, y, w, h);
    }

    public void fillRect(int x, int y, int w, int h) {
        canvas.setColor(fillColor);
        canvas.fillRect(x, y, w, h);
    }

    public void frameOval(int x, int y, int w, int h) {
        canvas.setColor(lineColor);
        canvas.frameOval(x, y, w, h);
    }

    public void fillOval(int x, int y, int w, int h) {
        canvas.setColor(fillColor);
        canvas.fillOval(x, y, w, h);
    }

    public void clear(){
        canvas.clear();
    }

    public void drawString(String str, int x, int y) {
        canvas.setColor(lineColor);
        canvas.drawString(x, y, str);
    }

    public void setFont(Font font) {
        canvas.setFont(font);
    }

    public void setLineColor(Color color) {
        this.lineColor = color;
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
    }

}
