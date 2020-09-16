import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.TreeSet;

public class PointSET {
    private final TreeSet<Point2D> ps; // my pointset

    // construct an empty set of points
    public PointSET() {
        ps = new TreeSet<Point2D>();
    }

    // is the set empty?
    public boolean isEmpty() {
        return ps.isEmpty();
    }

    // number of points in the set
    public int size() {
        return ps.size();
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        ps.add(p);
    }

    // does the set contain point p?
    public boolean contains(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        return ps.contains(p);
    }

    // draw all points to standard draw
    public void draw() {
        for (Point2D p : ps) p.draw();
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new IllegalArgumentException();
        return rangeInside(rect);
    }

    // Helper function for range: find all inside points
    private ArrayList<Point2D> rangeInside(RectHV rect) {
        ArrayList<Point2D> rangeInside = new ArrayList<Point2D>();
        for (Point2D p : ps) {
            if (rect.contains(p)) rangeInside.add(p);
        }
        return rangeInside;
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        if (ps.isEmpty()) return null;
        Point2D nearest = null; // = new Point2D(p.x(), p.y());
        for (Point2D that : ps) {
            if (nearest == null) {
                nearest = new Point2D(that.x(), that.y());
                continue;
            }
            if (p.distanceSquaredTo(that) < p.distanceSquaredTo(nearest)) nearest = that;
        }
        return nearest;
    }

    public static void main(String[] args) {
        RectHV rect = new RectHV(0.0, 0.0, 1.0, 1.0);
        StdDraw.enableDoubleBuffering();
        PointSET brute = new PointSET();
        while (true) {
            if (StdDraw.isMousePressed()) {
                double x = StdDraw.mouseX();
                double y = StdDraw.mouseY();
                StdOut.printf("%8.6f %8.6f\n", x, y);
                Point2D p = new Point2D(x, y);
                if (rect.contains(p)) {
                    StdOut.printf("%8.6f %8.6f\n", x, y);
                    brute.insert(p);
                    StdDraw.clear();
                    brute.draw();
                    StdDraw.show();
                }
            }
            StdDraw.pause(20);
        }

    }
}
