/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangulation;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Algorithm class, where almost all the counting is happening
public class Algorithms {

    // method, which calculates height difference between min and max point
    public static double heightdifference(Point3D[] points) {

        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        // finds maximum and minimum
        for (Point3D p : points) {
            if (p.getZ() < minZ) {
                minZ = p.getZ();
            }
            if (p.getZ() > maxZ) {
                maxZ = p.getZ();
            }
        }

        // returns the difference
        return (maxZ - minZ);
    }

    // method, which scales x and y coordinates to 0 - 1 scale 
    public static Point3D[] scaling(Point3D[] points) {

        // variables
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        // finds maximum and minimum of coordinates
        for (Point3D p : points) {
            if (p.getX() < minX) {
                minX = p.getX();
            }
            if (p.getX() > maxX) {
                maxX = p.getX();
            }
            if (p.getY() < minY) {
                minY = p.getY();
            }
            if (p.getY() > maxY) {
                maxY = p.getY();
            }
        }

        // scales and sets coordinates
        for (Point3D p : points) {
            double x = 1 * ((p.getX() - minX) / (maxX - minX));
            double y = 1 * ((p.getY() - minY) / (maxY - minY));
            p.setX(x);
            p.setY(y);
        }

        return points;
    }

    // variables
    protected static final double EPSILON = Double.MIN_VALUE;

    protected enum OrientationEnum {
        CW, COLINEAR, CCW
    }

    // method, which does the determinant test
    // and determinates orientation of third point based on a test
    public static OrientationEnum getOrientation(Point3D p1, Point3D p2, Point3D p3) {
        double val = (p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
                - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY());
        if (abs(val) < EPSILON) {
            return OrientationEnum.COLINEAR;
        } else if (val > 0) {
            return OrientationEnum.CW;
        } else {
            return OrientationEnum.CCW;
        }
    }

    // method which calculates distance between two points
    public static double dist(Point3D p1, Point3D p2) {
        return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
                + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
    }

    // method which calculates circle radius and middle point
    public static double circleRadius(Point3D p1, Point3D p2, Point3D p3) {
        double x1 = p1.getX();
        double x2 = p2.getX();
        double x3 = p3.getX();
        double y1 = p1.getY();
        double y2 = p2.getY();
        double y3 = p3.getY();

        double k1 = x1 * x1 + y1 * y1;
        double k2 = x2 * x2 + y2 * y2;
        double k3 = x3 * x3 + y3 * y3;
        double k4 = y1 - y2;
        double k5 = y1 - y3;
        double k6 = y2 - y3;
        double k7 = x1 - x2;
        double k8 = x1 - x3;
        double k9 = x2 - x3;
        double k10 = x1 * x1;
        double k11 = x2 * x2;
        double k12 = x3 * x3;

        double mnom = (k12 * (-k4) + k11 * k5 - (k10 + k4 * k5) * k6);
        double mdenom = (-k4) * x3 + x2 * k5 + x1 * (-k6);
        double m = 0.5 * mnom / mdenom;

        double nnom = k1 * (-k9) + k2 * k8 + k3 * (-k7);
        double ndenom = y1 * (-k9) + y2 * k8 + y3 * (-k7);

        double n = 0.5 * nnom / ndenom;

        // middle point
        Point3D middle = new Point3D(m, n, 0);

        // calculates distance
        double radius = dist(middle, p2);

        // if the middle point of a circle is on the other side than on a side, 
        // where the point is searched
        if (getOrientation(p1, p2, middle) == OrientationEnum.CW) {
            // convert radius to negative value, because the point 
            // is actually closer to the edge
            radius = -radius;
        }

        return radius;
    }

    // method which searches a point which makes minimum bounding circle 
    public static Point3D minimalBoundingCircle(Edge e, Point3D[] points) {
        Point3D minPoint = null;
        double minradius = Double.MAX_VALUE;

        // find minimum radius
        for (Point3D p : points) {

            // if the points are the same as the ones creating the edge, 
            // continue
            if (p == e.p1 || p == e.p2) {
                continue;
            }

            // ignores points on the other side
            if (getOrientation(e.p1, e.p2, p) != OrientationEnum.CCW) {
                continue;
            }

            // calculates radius
            double radius = circleRadius(e.p1, e.p2, p);
            if (radius <= minradius) {
                minPoint = p;
                minradius = radius;
            }
        }
        return minPoint;
    }

    // method which checks if edge already exists in list or not
    private static void addToAel(List<Edge> ael, Edge edge) {

        // iterator
        Iterator<Edge> it = ael.iterator();

        // creates new edge with different orientation 
        Edge swapped = edge.swappedEdge();

        // iterates through list until it reaches its end
        while (it.hasNext()) {
            Edge e;

            // edge from list
            e = it.next();

            // if the swapped edge exists in the list
            if (e.equals(swapped)) {

                // remove edge from the list
                it.remove();
                return;
            }
        }
        ael.add(edge);
    }

    // method, which calculates delaunay triangulation
    public static List<Triangle> delaunay(Point3D[] points) {

        List<Triangle> ed = new LinkedList<>();

        // random point
        Point3D p1 = points[0];
        Point3D p2 = null;
        double mindist = Double.MAX_VALUE;

        // finds point with minimum distance
        for (Point3D p : points) {

            // continue if p is the elected random point
            if (p1 == p) {
                continue;
            }

            // distance between two points
            double a = dist(p1, p);

            // if distance is minimum
            if (a <= mindist) {
                p2 = p;
                mindist = a;
            }
        }

        // first edge
        Edge e = new Edge(p1, p2);

        // finds a point which makes minimum bounding circle
        Point3D p = minimalBoundingCircle(e, points);
        Edge e2;
        Edge e3;

        // if point which makes minimum bounding circle does not exist on the 
        // one side
        if (p == null) {

            // changes the orientation of the edge
            e.swap();

            // searches the point on the other side
            p = minimalBoundingCircle(e, points);

            // if the points are collinear or if there is only two points
            if (p == null) {
                System.out.println("Wrong input");
                System.exit(-1);
            }

            // creates edges
            e2 = new Edge(p1, p);
            e3 = new Edge(p, p2);
        } else {

            // creates edges
            e2 = new Edge(p2, p);
            e3 = new Edge(p, p1);
        }

        // first triangle
        ed.add(new Triangle(p1, p2, p));

        // Active edge list
        List<Edge> ael = new LinkedList<>();

        // adds edges to AEL
        ael.add(e);
        ael.add(e2);
        ael.add(e3);

        //iterates until AEL is not empty
        while (!ael.isEmpty()) {
            // iterator
            Iterator<Edge> it = ael.iterator();
            e = it.next();
            it.remove();
            // changes orientation of edge
            e.swap();

            // finds a point which makes minimum bounding circle
            p = minimalBoundingCircle(e, points);
            if (p == null) {
                continue;
            }
            
            // creates edges and triangle
            e2 = new Edge(e.p2, p);
            e3 = new Edge(p, e.p1);
            ed.add(new Triangle(e.p1, e.p2, p));

            //checks if the created edges are already in the AEL or not
            addToAel(ael, e3);
            addToAel(ael, e2);
        }

        return ed;
    }

    // method, which calculates determinant 3x3
    public static double det(double a1, double b1, double a2, double b2,
            double a3, double b3) {

        double a = a1;
        double b = b1;
        double c = 1;
        double d = a2;
        double e = b2;
        double f = 1;
        double g = a3;
        double h = b3;
        double i = 1;

        // equation
        double det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        return det;
    }

    // method, which calculates angle between two vectors
    public static double angle(Point3D u, Point3D v) {
        return Math.acos(dotProd(u, v) / (len(u) * len(v)));
    }

    // method, which calculates dot product of two vectors
    public static double dotProd(Point3D u, Point3D v) {
        return u.getX() * v.getX() + u.getY() * v.getY() + u.getZ() * v.getZ();
    }

    // method, which calculates length of vector
    public static double len(Point3D u) {
        return sqrt(u.getX() * u.getX() + u.getY() * u.getY() + u.getZ() * u.getZ());
    }

    // method, which calculates intersection points of contours on the edge
    public static List<Point3D> calcContourPoints(Point3D p1, Point3D p2, double z) {
        Point3D lower;
        Point3D upper;

        // decides which point of the edge si upper point and lower point
        if (p1.getZ() < p2.getZ()) {
            lower = p1;
            upper = p2;
        } else {
            lower = p2;
            upper = p1;
        }

        // height difference
        double dh = upper.getZ() - lower.getZ();

        // distance between points
        double d = dist(lower, upper);

        // how many times is interval smaller than height of lower point
        double tmp = Math.floor(lower.getZ() / z);

        // height to first contour
        double dhtoCont = z - (lower.getZ() - (tmp * z));

        List<Point3D> pts = new LinkedList<>();
        int k = 0;

        // cycles until next contour is higher than height of upper point
        while (k * z + lower.getZ() + dhtoCont < upper.getZ()) {

            // scale
            double scale = (dhtoCont + z * k) / dh;

            // scales vector, shifts it to 0 and scales it back
            double x = (upper.getX() - lower.getX()) * scale + lower.getX();
            double y = (upper.getY() - lower.getY()) * scale + lower.getY();

            // new point
            pts.add(new Point3D(x, y, lower.getZ() + dhtoCont + z * k));
            k++;
        }
        return pts;
    }

    // method, which creates list of contours in a triangle 
    public static List<Edge> calcContours(Triangle t, double interval) {
        List<Edge> edges;
        edges = new LinkedList<>();
        List<Point3D> ptsp1p2;
        List<Point3D> ptsp2p3;
        List<Point3D> ptsp3p1;

        // calculates intersection points of contours for every edge of triangle
        ptsp1p2 = calcContourPoints(t.p1, t.p2, interval);
        ptsp2p3 = calcContourPoints(t.p2, t.p3, interval);
        ptsp3p1 = calcContourPoints(t.p3, t.p1, interval);
        
        // cycle through a lists of intersection points and creates a line
        // from points with the same height 
        for (Point3D p : ptsp1p2) {
            for (Point3D p2 : ptsp2p3) {
                if (p.getZ() == p2.getZ()) {
                    Edge e = new Edge(p, p2);
                    
                    // highlighted contour
                    if ((p.getZ() / interval) % 5 == 0) {
                        e.thickness = true;
                    }
                    edges.add(e);
                }
            }
        }

        for (Point3D p : ptsp1p2) {
            for (Point3D p2 : ptsp3p1) {
                if (p.getZ() == p2.getZ()) {
                    Edge e = new Edge(p, p2);
                    if ((p.getZ() / interval) % 5 == 0) {
                        e.thickness = true;
                    }
                    edges.add(e);
                }
            }
        }

        for (Point3D p : ptsp2p3) {
            for (Point3D p2 : ptsp3p1) {
                if (p.getZ() == p2.getZ()) {
                    Edge e = new Edge(p, p2);
                    if ((p.getZ() / interval) % 5 == 0) {
                        e.thickness = true;
                    }
                    edges.add(e);
                }
            }
        }

        return edges;
    }

    // method, which creates list of lines representing contours
    public static List<Edge> calcContours(List<Triangle> tl, double interval) {
        List<Edge> edges;
        edges = new LinkedList<>();

        // cycles through all triangles
        for (Triangle t : tl) {
            List<Edge> tedges;

            // calculates contours in a triangle
            tedges = calcContours(t, interval);

            // add all elements of the list to this list
            edges.addAll(tedges);
        }
        return edges;
    }
}
