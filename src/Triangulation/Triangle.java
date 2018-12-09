/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangulation;

// class, which representing data structure Triangle
public class Triangle {

    // variables
    protected Point3D p1;
    protected Point3D p2;
    protected Point3D p3;
    protected double slope;
    protected double exposition;

    // constructor
    public Triangle(Point3D ap1, Point3D ap2, Point3D ap3) {
        p1 = ap1;
        p2 = ap2;
        p3 = ap3;
        slope = -1;
        exposition = -1;
    }

    // method which calculates perpendicular vector on a triangle plane
    public Point3D getNormalVec() {

        // denominator
        double denom = Algorithms.det(p1.getX(), p1.getY(), p2.getX(),
                p2.getY(), p3.getX(), p3.getY());

        // numerator
        double noma = Algorithms.det(p1.getY(), p1.getZ(), p2.getY(),
                p2.getZ(), p3.getY(), p3.getZ());

        double nomb = Algorithms.det(p1.getX(), p1.getZ(), p2.getX(),
                p2.getZ(), p3.getX(), p3.getZ());

        double nomc = Algorithms.det(p1.getX(), p1.getY(), p2.getX(),
                p2.getY(), p3.getX(), p3.getY());

        // point representing vector
        Point3D norm = new Point3D(noma / denom, nomb / denom, nomc / denom);
        return norm;
    }

    // method which calculates slope - angle between perpendicular vector on a 
    // triangle  plane and vertical vector 
    public double getSlope() {
        return Algorithms.angle(this.getNormalVec(), new Point3D(0, 0, 1));
    }

    // method which calculates Exposition
    public double getExposition() {
        Point3D norm = this.getNormalVec();
        double expositionn = Math.atan(norm.getY() / norm.getX());

        // makes all numbers positive
        if (norm.getX() < 0) {
            expositionn = expositionn + Math.PI;
        }

        // from 0 to 2PI
        return expositionn + Math.PI / 2;
    }
}
