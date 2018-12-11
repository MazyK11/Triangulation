/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;

// Drawing class
public class drawPanel extends javax.swing.JPanel {

    // variables
    protected Point3D[] point;
    List<Edge> edges;
    List<Triangle> triangles;
    protected Path2D poly;
    protected Path2D contour;
    protected double[] maxMin;

    // constructor
    public drawPanel() {
        point = new Point3D[0];
        poly = new Path2D.Double();
        contour = new Path2D.Double();
        edges = new LinkedList<>();
        triangles = new LinkedList<>();
        maxMin = new double[2];
        initComponents();
        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.white);
    }

    // method, where all the drawing is happening
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gfx = (Graphics2D) g;

        // get width and height of the canvas
        int width = this.getWidth();
        int height = this.getHeight() - 50;

        // draws the points as a cross 
        if (point.length != 0) {
            for (int i = 0; i < point.length; i++) {
                int x = (int) (point[i].getX() * width);
                int y = (int) ((1 - point[i].getY()) * height);
                gfx.drawLine(x - 5, y - 5, x + 5, y + 5);
                gfx.drawLine(x - 5, y + 5, x + 5, y - 5);
            }
        }

        // cycles through all triangles
        for (Triangle t : triangles) {

            // adds edges from the triangles to Path2D
            poly.moveTo(t.p1.getX(), t.p1.getY());
            poly.lineTo(t.p2.getX(), t.p2.getY());
            poly.lineTo(t.p3.getX(), t.p3.getY());
            poly.lineTo(t.p1.getX(), t.p1.getY());

            // scales Path2D
            poly = affineTransform(poly, width, height);

            // if slope was calculated
            if (triangles.get(0).slope != -1) {

                // sets color of triangle
                gfx.setColor(new Color((int) (t.slope), 255, (int) (255 - t.slope)));
                gfx.fill(poly);

                // converts minimum and maximum slope to String
                DecimalFormat df = new DecimalFormat("00.0");
                String min = df.format(maxMin[1]);
                String max = df.format(maxMin[0]);

                // colors representing start and end of gradient paint
                Color color2 = new Color(255, 255, 0);
                Color color1 = new Color(0, 255, 255);

                // creates GradientPaint, sets color and fill a rectangle
                GradientPaint gp = new GradientPaint(10, this.getHeight() - 12,
                        color1, 65, this.getHeight() - 32, color2);
                gfx.setPaint(gp);
                gfx.fillRect(10, this.getHeight() - 12, 75, -22);

                // draws min, max, and "Slope"
                gfx.setColor(Color.BLACK);
                gfx.drawString(min + "°", 5, this.getHeight() - 2);
                gfx.drawString(max + "°", 70, this.getHeight() - 2);
                gfx.drawString("Slope", 5, this.getHeight() - 36);
            }

            // if exposition is calculated
            if (triangles.get(0).exposition != -1) {

                // divides exposition to four parts 
                // 0° - 90°
                if (t.exposition > 0 && ((2.0 / 4.0) * Math.PI) > t.exposition) {

                    // scales exposition to  0 - 255 scale and sets color
                    int exp = colorScale(t.exposition, ((2.0 / 4.0) * Math.PI), 0);
                    gfx.setColor(new Color(exp, 255, 255));
                } // 90° - 180°
                else if (t.exposition > ((2.0 / 4.0) * Math.PI) && Math.PI >= t.exposition) {

                    // scales exposition to  0 - 255 scale and sets color
                    int exp = colorScale(t.exposition, Math.PI,
                            ((2.0 / 4.0) * Math.PI));
                    gfx.setColor(new Color(255, 255, 255 - exp));
                } // 180° - 270°
                else if (t.exposition > Math.PI && ((6.0 / 4.0) * Math.PI) >= t.exposition) {

                    // scales exposition to  0 - 255 scale and sets color
                    int exp = colorScale(t.exposition, (6.0 / 4.0 * Math.PI),
                            Math.PI);
                    gfx.setColor(new Color(255, 255 - exp, 0));
                } // 270° - 360°
                else {
                    
                    // scales exposition to  0 - 255 scale and sets color
                    int exp = colorScale(t.exposition, (2 * Math.PI),
                            (6.0 / 4.0 * Math.PI));
                    gfx.setColor(new Color(255 - exp, exp, exp));
                }

                gfx.fill(poly);

                // start colors
                Color colorN = new Color(0, 255, 255);
                Color colorE = new Color(255, 255, 255);
                Color colorS = new Color(255, 255, 0);
                Color colorW = new Color(255, 0, 0);
                Color[] col = {colorN, colorE, colorS, colorW};

                // strings for legend
                String[] s = {"N", "E", "S", "W"};

                Color color;
                int v = 115;
                int u = 0;

                // creates legend
                for (int i = 0; i < 4; i++) {

                    // sets end color 
                    if ("N".equals(s[i])) {
                        color = new Color(255, 255, 255);
                    } else if ("E".equals(s[i])) {
                        color = new Color(255, 255, 0);
                    } else if ("W".equals(s[i])) {
                        color = new Color(0, 255, 255);
                    } else {
                        color = new Color(255, 0, 0);
                    }

                    // creates GradientPaint, sets color and fill a rectangle
                    GradientPaint gp = new GradientPaint(10 + u, this.getHeight() - 12,
                            col[i], v, this.getHeight() - 32, color);
                    gfx.setPaint(gp);
                    gfx.fillRect(10 + u, this.getHeight() - 12, 105, -22);

                    // draws letter representing the world side
                    gfx.setColor(Color.BLACK);
                    gfx.drawString(s[i], 10 + u, this.getHeight() - 36);
                    v = v + 105;
                    u = u + 105;
                }
            }

            // draws Path2D
            gfx.setColor(Color.BLACK);
            gfx.draw(poly);
        }

        gfx.setColor(new Color(139, 69, 19));

        // cycles through all contours
        for (Edge e : edges) {

            // if the contour should be highlighted
            // sets wider stroke
            if (e.thickness == true) {
                gfx.setStroke(new BasicStroke(3));
            } else {
                gfx.setStroke(new BasicStroke(1));
            }

            // adds the edges to Path2D
            contour.moveTo(e.p1.getX(), e.p1.getY());
            contour.lineTo(e.p2.getX(), e.p2.getY());

            // get coordinates of A - the first end of the contour segment in scale
            double x1 = (e.p1.getX() * width);
            double y1 = ((1 - e.p1.getY()) * height);
            
             // get coordinates of B - the second end of the contour segment in scale
            double x2 = (e.p2.getX() * width);
            double y2 = ((1 - e.p2.getY()) * height);
            
            // calculate position of label (the center of segment line)
            int eAvgX = (int) Math.abs((x1 + x2) / 2);
            int eAvgY = (int) Math.abs((y1 + y2) / 2);

            // calculate angle between segment and x-axis
            double ux = (e.p2.getX() - e.p1.getX());
            double vx = 0.001;
            double uy = (e.p2.getY() - e.p1.getY());
            double vy = 0;
            double angle = Math.atan2(ux * vy - uy * vx, ux * vx + uy * vy);
            angle = angle * (180 / Math.PI);
            
            // load value of label from z-coordinate
            String text = String.valueOf ( Math.round ( e.p1.getZ() * 100.0 ) / 100.0 );
            
            if (e.random < 0.2) {
                // rotate label in specified angle      
                drawRotate(gfx, eAvgX, eAvgY, (int) angle, text);              
            }

            // scales Path2D and draws it
            contour = affineTransform(contour, width, height);
            gfx.draw(contour);
        }
    }

    // method, which transforms and scales Path2D to canvas size
    private static Path2D affineTransform(Path2D p, int width, int height) {
        AffineTransform at = AffineTransform.getScaleInstance(width, -height);
        p.transform(at);
        at = AffineTransform.getTranslateInstance(0, height);
        p.transform(at);
        return p;
    }

    // method, which scales number to 0 - 255 scale 
    private static int colorScale(double m, double max, double min) {
        double a = m - min;
        double b = max - min;
        double exp = (int) (255 * (a / b));
        return (int) exp;
    }
    
    // method, which rotate input string according to input angle in degrees
    public static void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) {
        g2d.translate((float) x, (float) y);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawString(text, 0, 0);
        g2d.rotate(-Math.toRadians(angle));
        g2d.translate(-(float) x, -(float) y);
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
