package com.tscript.runtime.tni.natfuncs.std.view;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public class DynamicPrintPane extends JPanel {

    private Image image;
    private Graphics2D graphics2D;

    public DynamicPrintPane(){
        setDoubleBuffered(false);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponents(g);
        if (image == null){
            image = createImage(getSize().width, getSize().height);
            graphics2D = (Graphics2D) image.getGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(Color.BLACK);
            clear();
        }
        g.drawImage(image, 0, 0, null);
    }

    public void clear(){
        if (!isDrawable()) return;
        Color color = graphics2D.getColor();
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, getSize().width, getSize().height);
        graphics2D.setColor(color);
        repaint();
    }

    public void setColor(Color color){
        if (!isDrawable()) return;
        graphics2D.setColor(color);
    }

    public void drawLine(int x1, int y1, int x2, int y2){
        if (!isDrawable()) return;
        graphics2D.drawLine(x1, y1, x2, y2);
        repaint();
    }

    public void frameRect(int x, int y, int w, int h){
        if (!isDrawable()) return;
        graphics2D.drawRect(x, y, w, h);
        repaint();
    }

    public void fillRect(int x, int y, int w, int h){
        if (!isDrawable()) return;
        graphics2D.fillRect(x, y, w, h);
        repaint();
    }

    public void frameOval(int x, int y, int w, int h){
        if (!isDrawable()) return;
        graphics2D.drawOval(x, y, w, h);
        repaint();
    }

    public void fillOval(int x, int y, int w, int h){
        if (!isDrawable()) return;
        graphics2D.fillOval(x, y, w, h);
        repaint();
    }

    public void drawString(int x, int y, String s){
        if (!isDrawable()) return;
        graphics2D.drawString(s, x, y);
        repaint();
    }

    public void setFont(Font font){
        if (!isDrawable()) return;
        graphics2D.setFont(font);
    }

    public boolean isDrawable(){
        return image != null && graphics2D != null;
    }

}
