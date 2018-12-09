/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangulation;

// class, which representing data structure Point3D
public class Point3D {

    // variables
    private double x;
    private double y;
    private double z;

    // constructor
    public Point3D(double ax, double ay, double az) {
        x = ax;
        y = ay;
        z = az;
    }

    // getters and setters
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

}

