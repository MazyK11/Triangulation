/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

// class representing GUI
public class GUI extends javax.swing.JFrame {

    // variables
    protected GenerateWorker wo;
    protected CalculateWorker woc;
    protected JFileChooser choose;
    protected boolean generate;
    protected boolean scaled;
    protected int calculation;
    protected double heightDifference;
    protected int c;
    protected double Zmax;
    protected double Zmin;

    // constructor
    public GUI() {
        initComponents();
        wo = new GenerateWorker();
        woc = new CalculateWorker();
        choose = new JFileChooser();
        //setting File filter for txt files
        choose.setFileFilter(new FileNameExtensionFilter("", "txt"));
        generate = false;
        calculation = 0;
        scaled = false;
    }

    // class representing new thread 
    public class GenerateWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            // generates random points 
            if (generate == true) {
                drawPanel1.triangles = new LinkedList<>();
                drawPanel1.edges = new LinkedList<>();

                // gets number of points from text field
                int npoints = Integer.parseInt(pointCountField.getText());
                Point3D[] points = new Point3D[npoints];
                Random rnd = new Random();

                // generates random x, y ,z coordinates
                // z is multiply by 0.2 to get better results
                for (int i = 0; i < npoints; i++) {
                    points[i] = new Point3D(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble() * 0.2);
                }
                drawPanel1.point = points;

                // calculates height difference between min and max point
                heightDifference = Algorithms.heightdifference(drawPanel1.point);

            } // load a file
            else {
                try {
                    drawPanel1.triangles = new LinkedList<>();
                    drawPanel1.edges = new LinkedList<>();

                    // load a file
                    BufferedReader br;
                    br = new BufferedReader(new FileReader(choose.getSelectedFile()));
                    String str;
                    List<Point3D> p = new LinkedList<>();

                    // reads a lines
                    while ((str = br.readLine()) != null) {

                        // add point to the list
                        p.add(parse(str));
                    }

                    // list to array
                    drawPanel1.point = new Point3D[p.size()];
                    for (int i = 0; i < p.size(); i++) {
                        drawPanel1.point[i] = p.get(i);
                    }

                } catch (IOException e) {

                    // message for user
                    msgL.setText("Failed to load the file");
                }

                // calculates height difference between min and max point
                heightDifference = Algorithms.heightdifference(drawPanel1.point);

                // scales x and y coordinates
                // so it does not matter in which projection x y coordinates are
                drawPanel1.point = Algorithms.scaling(drawPanel1.point);
            }
            return null;
        }

        @Override
        protected void done() {

            // if random points are generated, set c to 100
            if (generate == true) {
                c = 100;
            } else {
                c = 1;
            }

            // rounds height difference and parse it to String
            DecimalFormat df = new DecimalFormat("00.0");

            // multiply by c, it looks better than number smaller than 1
            String result = df.format(heightDifference * c);
            result = result.replace(',', '.');

            // message for user, so user can choose proper contour interval
            difLbl.setText("Height difference between min and max point is: "
                    + result);

            // sets interval for user
            double a = Double.parseDouble(result) / 10;
            df = new DecimalFormat("0.00");
            result = df.format(a);
            result = result.replace(',', '.');
            interval.setText(result);

            //repaint the canvas
            drawPanel1.repaint();
        }

        // method, which splits the line and parses String to double
        private Point3D parse(String str) {
            String[] items;

            //gets delimiter
            String s = del.getText();
            items = str.split(s);
            double p[] = new double[items.length];

            for (int i = 0; i < items.length; i++) {
                p[i] = Double.parseDouble(items[i]);
            }

            return new Point3D(p[0], p[1], p[2]);
        }
    }

    // class representing new thread 
    public class CalculateWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {

            //calculates one of these processes
            switch (calculation) {
                case 0:

                    // calculates delaunay triangulation
                    drawPanel1.triangles = Algorithms.delaunay(drawPanel1.point);
                    break;

                case 1:

                    drawPanel1.point = new Point3D[0];

                    // gets an interval number from text field
                    String s = interval.getText();
                    double npoints = Double.parseDouble(s) / c;

                    // if file was load
                    if (generate == false) {

                        // if z coordinate was scaled
                        if (scaled == true) {

                            // scales it back to its original scale
                            drawPanel1.triangles = scalingZBack(drawPanel1.triangles);
                            scaled = false;
                        }
                    }

                    // calculates contours
                    drawPanel1.edges = Algorithms.calcContours(drawPanel1.triangles, npoints);
                    break;
                case 2:

                    drawPanel1.point = new Point3D[0];

                    // if file was loaded
                    if (generate == false) {

                        // if coordinate Z is not scaled yet
                        if (scaled == false) {

                            // scales z coor to 0 - 1 scale 
                            drawPanel1.triangles = scalingZ(drawPanel1.triangles);
                            scaled = true;
                        }
                    }

                    // calculates slope in every triangle
                    for (Triangle t : drawPanel1.triangles) {
                        
                        // converts it to degrees
                        t.slope = Math.toDegrees(t.getSlope());
                        t.exposition = -1;
                    }

                    double minZ = Double.MAX_VALUE;
                    double maxZ = -Double.MAX_VALUE;

                    // finds minimum and maximum slope 
                    for (Triangle t : drawPanel1.triangles) {
                        if (t.slope < minZ) {
                            minZ = t.slope;
                        }
                        if (t.slope > maxZ) {
                            maxZ = t.slope;
                        }

                        // converts it back to radians
                        double rad = Math.toRadians(t.slope);
                        t.slope = (int) ((255 * rad) / (Math.PI / 2));
                    }
                    drawPanel1.maxMin[0] = maxZ;
                    drawPanel1.maxMin[1] = minZ;

                    break;
                case 3:

                    drawPanel1.point = new Point3D[0];

                    // if file was loaded
                    if (generate == false) {

                        // if coordinate Z is not scaled yet
                        if (scaled == false) {

                            // scales z coor to 0 - 1 scale 
                            drawPanel1.triangles = scalingZ(drawPanel1.triangles);
                            scaled = true;
                        }
                    }

                    // calculates exposition for every triangle
                    for (Triangle t : drawPanel1.triangles) {
                        t.exposition = t.getExposition();
                        t.slope = -1;
                    }

                    break;
            }
            return null;
        }

        @Override
        protected void done() {

            //repaint the canvas
            drawPanel1.repaint();
        }

        // method, which scales z coordinates back to its original numbers
        public List<Triangle> scalingZBack(List<Triangle> tl) {
            double minZ = Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;

            // finds maximum and minimum of coordinates
            for (Triangle t : tl) {
                if (t.p1.getZ() < minZ) {
                    minZ = t.p1.getZ();
                }
                if (t.p1.getZ() > maxZ) {
                    maxZ = t.p1.getZ();
                }
                if (t.p2.getZ() < minZ) {
                    minZ = t.p2.getZ();
                }
                if (t.p2.getZ() > maxZ) {
                    maxZ = t.p2.getZ();
                }
                if (t.p3.getZ() < minZ) {
                    minZ = t.p3.getZ();
                }
                if (t.p3.getZ() > maxZ) {
                    maxZ = t.p3.getZ();
                }
            }

            // scales coordinates
            for (Triangle t : tl) {

                if (t.p1.getZ() < 1.01) {

                    t.p1.setZ((((t.p1.getZ() - minZ) / (maxZ - minZ))
                            * (Zmax - Zmin) + Zmin));
                }
                if (t.p2.getZ() < 1.01) {

                    t.p2.setZ((((t.p2.getZ() - minZ) / (maxZ - minZ))
                            * (Zmax - Zmin) + Zmin));
                }
                if (t.p3.getZ() < 1.01) {

                    t.p3.setZ((((t.p3.getZ() - minZ) / (maxZ - minZ))
                            * (Zmax - Zmin) + Zmin));
                }
            }

            return tl;
        }

        // method, which scales z coordinates to 0 - 1 scale 
        public List<Triangle> scalingZ(List<Triangle> tl) {
            Zmin = Double.MAX_VALUE;
            Zmax = -Double.MAX_VALUE;

            // finds maximum and minimum of coordinates
            for (Triangle t : tl) {
                if (t.p1.getZ() < Zmin) {
                    Zmin = t.p1.getZ();
                }
                if (t.p1.getZ() > Zmax) {
                    Zmax = t.p1.getZ();
                }
                if (t.p2.getZ() < Zmin) {
                    Zmin = t.p2.getZ();
                }
                if (t.p2.getZ() > Zmax) {
                    Zmax = t.p2.getZ();
                }
                if (t.p3.getZ() < Zmin) {
                    Zmin = t.p3.getZ();
                }
                if (t.p3.getZ() > Zmax) {
                    Zmax = t.p3.getZ();
                }
            }

            // scales coordinates
            for (Triangle t : tl) {

                if (t.p1.getZ() > 1.01) {
                    t.p1.setZ(1 * ((t.p1.getZ() - Zmin) / (Zmax - Zmin)));
                }

                if (t.p2.getZ() > 1.01) {
                    t.p2.setZ(1 * ((t.p2.getZ() - Zmin) / (Zmax - Zmin)));
                }

                if (t.p3.getZ() > 1.01) {
                    t.p3.setZ(1 * ((t.p3.getZ() - Zmin) / (Zmax - Zmin)));
                }
            }

            return tl;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        drawPanel1 = new Triangulation.drawPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        loadButton = new javax.swing.JButton();
        pointsButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pointCountField = new javax.swing.JTextField();
        msgL = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        del = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        interval = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        calcContourButt = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        difLbl = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        delaunayButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        slopeButt = new javax.swing.JButton();
        aspectButt = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(782, 650));

        javax.swing.GroupLayout drawPanel1Layout = new javax.swing.GroupLayout(drawPanel1);
        drawPanel1.setLayout(drawPanel1Layout);
        drawPanel1Layout.setHorizontalGroup(
            drawPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        drawPanel1Layout.setVerticalGroup(
            drawPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 509, Short.MAX_VALUE)
        );

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setText("Choose between two options:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 16;
        gridBagConstraints.ipady = 78;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 16, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        loadButton.setText("Load a file...");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 67;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        jPanel1.add(loadButton, gridBagConstraints);

        pointsButton.setText("Generate random points");
        pointsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 11;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 0, 0);
        jPanel1.add(pointsButton, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("or");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 6, 0, 0);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Number of points:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 9;
        gridBagConstraints.ipady = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        pointCountField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pointCountField.setText("100");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 71;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 10);
        jPanel1.add(pointCountField, gridBagConstraints);

        msgL.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        msgL.setText("No file choosen");
        msgL.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 22;
        gridBagConstraints.ipady = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 0, 0);
        jPanel1.add(msgL, gridBagConstraints);

        jLabel9.setText("Delimiter:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.ipadx = 9;
        gridBagConstraints.ipady = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        jPanel1.add(jLabel9, gridBagConstraints);

        del.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 0);
        jPanel1.add(del, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Contour interval:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 24;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        interval.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        interval.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        jPanel2.add(interval, gridBagConstraints);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setText("Contour:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 209;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        jPanel2.add(jLabel6, gridBagConstraints);

        calcContourButt.setBackground(new java.awt.Color(153, 51, 0));
        calcContourButt.setText("Calculate contour");
        calcContourButt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcContourButtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 37;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 4, 0, 0);
        jPanel2.add(calcContourButt, gridBagConstraints);

        jLabel5.setText("Create contour:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 29;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(jLabel5, gridBagConstraints);

        difLbl.setText("Height difference between min and max point is:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.ipadx = 46;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 10);
        jPanel2.add(difLbl, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        delaunayButton.setBackground(new java.awt.Color(0, 255, 153));
        delaunayButton.setText("Calculate triangulation");
        delaunayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delaunayButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 39, 0);
        jPanel3.add(delaunayButton, gridBagConstraints);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Delaunay Triangulation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 18;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        jPanel3.add(jLabel7, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel8.setText("Slope and Exposition:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        jPanel4.add(jLabel8, gridBagConstraints);

        slopeButt.setBackground(new java.awt.Color(102, 255, 204));
        slopeButt.setText("Calculate slope");
        slopeButt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slopeButtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 0);
        jPanel4.add(slopeButt, gridBagConstraints);

        aspectButt.setBackground(new java.awt.Color(255, 102, 51));
        aspectButt.setText("Calculate exposition");
        aspectButt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aspectButtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 11, 0);
        jPanel4.add(aspectButt, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(drawPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(drawPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // method, which generates random points when the "Generate random points"
    // button is pressed
    private void pointsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsButtonActionPerformed
        // sets boolean variable
        generate = true;

        // runs thread for generating points
        wo.execute();
        wo = new GenerateWorker();
    }//GEN-LAST:event_pointsButtonActionPerformed

    // method, which calculates delaunay triangulation when the 
    // "Calculate triangulation" button is pressed
    private void delaunayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delaunayButtonActionPerformed
        //sets variable, which representing specific process
        calculation = 0;

        // runs thread for calculating specific processes
        woc.execute();
        woc = new CalculateWorker();
    }//GEN-LAST:event_delaunayButtonActionPerformed

    // method, which calculates contours when the "Calculate contour" 
    // button is pressed
    private void calcContourButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcContourButtActionPerformed
        //sets variable, which representing specific process
        calculation = 1;

        // runs thread for calculating specific processes
        woc.execute();
        woc = new CalculateWorker();
    }//GEN-LAST:event_calcContourButtActionPerformed

    // method, which makes points from .txt file when the "Load a file..." 
    // button is pressed
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        int returnvalue = choose.showOpenDialog(this);
        if (returnvalue == JFileChooser.APPROVE_OPTION) {

            // sets boolean variable
            generate = false;
            msgL.setText("File choosen");

            // runs thread for generating points from file
            wo.execute();
            wo = new GenerateWorker();
        } else {
            msgL.setText("No file choosen");
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    // method, which calculates exposition when the "Calculate exposition" 
    // button is pressed
    private void aspectButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aspectButtActionPerformed
        //sets variable, which representing specific process
        calculation = 3;

        // runs thread for calculating specific processes
        woc.execute();
        woc = new CalculateWorker();
    }//GEN-LAST:event_aspectButtActionPerformed

    // method, which calculates slope when the "Calculate slope" 
    // button is pressed
    private void slopeButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slopeButtActionPerformed
        //sets variable, which representing specific process
        calculation = 2;

        // runs thread for calculating specific processes
        woc.execute();
        woc = new CalculateWorker();
    }//GEN-LAST:event_slopeButtActionPerformed

    // main method, which creates GUI and sets a title
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GUI cp = new GUI();

                // sets title
                cp.setTitle("Digital Terrain Model");
                cp.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aspectButt;
    private javax.swing.JButton calcContourButt;
    private javax.swing.JTextField del;
    private javax.swing.JButton delaunayButton;
    private javax.swing.JLabel difLbl;
    private Triangulation.drawPanel drawPanel1;
    private javax.swing.JTextField interval;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton loadButton;
    private javax.swing.JLabel msgL;
    private javax.swing.JTextField pointCountField;
    private javax.swing.JButton pointsButton;
    private javax.swing.JButton slopeButt;
    // End of variables declaration//GEN-END:variables
}
