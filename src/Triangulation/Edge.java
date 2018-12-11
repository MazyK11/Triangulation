/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Triangulation;

// class, which representing data structure Edge

public class Edge {

    //variables
    protected Point3D p1;
    protected Point3D p2;
    protected boolean thickness;
    protected double random;

    // constructor
    public Edge(Point3D p1, Point3D p2) {
        this.p1 = p1;
        this.p2 = p2;
        thickness = false;
    }

    // method, which changes the orientation of the edge
    public void swap() {
        Point3D tmp;
        tmp = p1;
        p1 = p2;
        p2 = tmp;
    }

    @Override
    // method for comparing objects
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge)) {
            return false;
        }
        Edge e = (Edge) obj;
        if ((e.p1 == p1) && (e.p2 == p2)) {
            return true;
        }
        return false;
    }

    // method which returns new edge with changed orientation
    public Edge swappedEdge() {
        return new Edge(p2, p1);
    }
    
}
