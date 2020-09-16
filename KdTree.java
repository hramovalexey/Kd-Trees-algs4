import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.ArrayList;

public class KdTree {
    // vertical or horizontal level?
    private static final boolean VERT = true;
    private static final boolean GOR = false;
    private Node root;
    private final ArrayList<Point2D> pointsList; // list of all points

    // construct an empty set of points
    public KdTree() {
        pointsList = new ArrayList<Point2D>();
    }


    private class Node {
        private Point2D p;
        private boolean or; // orientation
        private Node lb; // left/bottom subtree
        private Node rt; // right/top subtree
        private RectHV rect; // rectangle of this node
        private int count;

        public Node() {
            this.p = null;
        }

        public Node(Point2D inputP, boolean orientation, int countNum, RectHV rectangle) {
            this.p = inputP;
            this.count = countNum;
            this.or = orientation;
            this.rect = rectangle;
            pointsList.add(p);

        }
    }

    // is the set empty?
    public boolean isEmpty() {
        return root == null;
    }

    // number of points in the set
    public int size() {
        if (root == null) return 0;
        return size(root);
    }

    // Helper methods put&size&rect for insert
    private int size(Node x) {
        if (x == null) return 0;
        return x.count;
    }

    private RectHV rect(Node parentNode, Point2D p, boolean parentOr) {
        if (parentNode == null) {
            RectHV returnRect = new RectHV(0, 0, 1, 1);
            return returnRect;
        }

        double xmin = parentNode.rect.xmin();
        double xmax = parentNode.rect.xmax();
        double xmid = parentNode.p.x();
        double ymin = parentNode.rect.ymin();
        double ymax = parentNode.rect.ymax();
        double ymid = parentNode.p.y();
        if (parentOr == VERT) {
            if (p.x() < xmid) xmax = xmid;
            if (p.x() > xmid) xmin = xmid;
        }
        if (parentOr == GOR) {
            if (p.y() < ymid) ymax = ymid;
            if (p.y() > ymid) ymin = ymid;
        }
        if (xmax < xmin) {
            double xtemp = xmax;
            xmax = xmin;
            xmin = xtemp;
        }
        if (ymax < ymin) {
            double ytemp = ymax;
            ymax = ymin;
            ymin = ytemp;
        }

        RectHV returnRect = new RectHV(xmin, ymin, xmax, ymax);
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(0.01);
        returnRect.draw();
        StdDraw.show();
        return returnRect;
    }

    // helper comparator
    private int compare(Point2D thisP, Node thatN) {
        if (thisP.equals(thatN.p)) return 0;
        if (thatN.or == VERT) {
            if (thisP.x() > thatN.p.x()) return 1;
            if (thisP.x() < thatN.p.x() || thisP.x() == thatN.p.x()) return -1;
        }
        if (thatN.or == GOR) {
            if (thisP.y() > thatN.p.y()) return 1;
            if (thisP.y() < thatN.p.y() || thisP.y() == thatN.p.y()) return -1;
        }
        return 0;
    }

    private Node put(Node x, Point2D p, boolean or, Node parentNode) {
        if (x == null) {
            return new Node(p, or, 1, rect(parentNode, p, !or));
        }
        int cmp = compare(p, x);
        if (cmp < 0) x.lb = put(x.lb, p, !or, x);
        if (cmp > 0) x.rt = put(x.rt, p, !or, x);
        else if (cmp == 0) x.p = p;
        x.count = size(x.rt) + size(x.lb) + 1;
        return x;
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        root = put(root, p, VERT, null);
    }

    // does the set contains point p?
    public boolean contains(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        Node x = root;
        while (x != null) {
            int cmp = compare(p, x);
            if (cmp < 0) x = x.lb;
            if (cmp > 0) x = x.rt;
            if (cmp == 0) return true;
        }
        return false;
    }


    // draw all points to standard draw
    public void draw() {
        for (Point2D p : pointsList) p.draw();
    }

    private void range(Node x, RectHV rect, ArrayList<Point2D> insideRect) {
        if (x == null) return;

        if (rect.intersects(x.rect)) {
            range(x.lb, rect, insideRect);
            range(x.rt, rect, insideRect);
            if (rect.contains(x.p)) insideRect.add(x.p);
            // StdOut.println(x.p.toString());
        }
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new IllegalArgumentException();
        ArrayList<Point2D> insideRect = new ArrayList<Point2D>();
        range(root, rect, insideRect);
        return insideRect;
    }


    // helper for nearest
    // p - point with which we are looking the closest
    // distance - current shortest distance
    // nearestP - current nearest point
    private Point2D nearest(Point2D p, Node x, double distance, Point2D nearestP) {
        if (x.rect.distanceSquaredTo(p) > distance) {
            return nearestP;
        }
        double tempDist = p.distanceSquaredTo(x.p);
        if (tempDist < distance) {
            distance = tempDist;
            nearestP = x.p;
        }

        // kd-tree traversal processing
        // 1st case: query point is inside left or right kd tree branch
        if (x.rt != null && x.rt.rect.contains(p)) {
            nearestP = nearest(p, x.rt, distance, nearestP);
            if (x.lb != null) {
                distance = p.distanceSquaredTo(nearestP);
                nearestP = nearest(p, x.lb, distance, nearestP);
            }
        }

        else if (x.lb != null && x.lb.rect.contains(p)) {
            nearestP = nearest(p, x.lb, distance, nearestP);
            if (x.rt != null) {
                distance = p.distanceSquaredTo(nearestP);
                nearestP = nearest(p, x.rt, distance, nearestP);
            }
        }

        // 2nd case: query point is outside of both kd tree branches
        else if (x.lb != null && x.rt != null && x.rt.rect.distanceSquaredTo(p) <= x.lb.rect
                .distanceSquaredTo(p)) {
            nearestP = nearest(p, x.rt, distance, nearestP);
            distance = p.distanceSquaredTo(nearestP);
            nearestP = nearest(p, x.lb, distance, nearestP);

        }
        else if (x.lb != null && x.rt != null && x.rt.rect.distanceSquaredTo(p) > x.lb.rect
                .distanceSquaredTo(p)) {
            nearestP = nearest(p, x.lb, distance, nearestP);
            distance = p.distanceSquaredTo(nearestP);
            nearestP = nearest(p, x.rt, distance, nearestP);
        }

        // 3rd case: query point is outside and one of branches is null
        else if (x.rt != null) {
            nearestP = nearest(p, x.rt, distance, nearestP);
            if (x.lb != null) {
                distance = p.distanceSquaredTo(nearestP);
                nearestP = nearest(p, x.lb, distance, nearestP);
            }
        }
        else if (x.lb != null) {
            nearestP = nearest(p, x.lb, distance, nearestP);
        }
        return nearestP;
    }


    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null) throw new IllegalArgumentException();
        if (root == null) return null;
        if (this.contains(p)) return p;
        Point2D nearestP = root.p;
        double nearestDist = p.distanceSquaredTo(root.p);
        nearestP = nearest(p, root, nearestDist, nearestP);
        return nearestP;
    }


    public static void main(String[] args) {
        String filename = args[0];
        In in = new In(filename);
        KdTree kdtree = new KdTree();
        while (!in.isEmpty()) {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);
            kdtree.insert(p);
        }

        // process nearest neighbor queries
        StdDraw.enableDoubleBuffering();
        Point2D queryP = new Point2D(0.81, 0.45);

        // StdDraw.clear();
        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.02);
        kdtree.draw();

        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.04);
        queryP.draw();

        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(0.04);
        kdtree.nearest(queryP).draw();
        StdDraw.show();

    }
}
