// Nathan Budzinski
// CSCI 331

package lab5;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.math.BigInteger;
import javax.sound.sampled.Line;
import javax.swing.*;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Random;
/**
 *
 * @author srosenbe
 */
public class VectorGraphics extends JComponent
{
    class Point extends Matrix
    {
        public double x() {return super.getEntry(0,0);}
        public double y() {return super.getEntry(1,0);}
        public double z() {return super.getEntry(2,0);}

        public void setx(double x) {super.setEntry(0, 0, x);}
        public void sety(double y) {super.setEntry(1, 0, y);}
        public void setz(double z) {super.setEntry(2, 0, z);}

        public Point(double xCoord, double yCoord, double zCoord)
        {
            super(3, 1);
            super.setEntry(0, 0, xCoord); super.setEntry(1, 0, yCoord); super.setEntry(2, 0, zCoord);
        }

        public Point(Matrix original) throws MatrixException
        {
            super(original);
            if(original.getNumberOfColumns() != 1 || original.getNumberOfRows() != 3)
            {
                throw new MatrixException("Cannot convert " + original.getNumberOfRows() + "x" + original.getNumberOfColumns()
                        + " matrix into Point");
            }
        }

        public Point subtract(Point Q)//override the inherited Matrix methods for speed
        {
            Point diff = new Point(x() - Q.x(), y() - Q.y(), z() - Q.z());
            return diff;
        }

        public Point add(Point Q)
        {
            Point sum = new Point(x() + Q.x(), y() + Q.y(), z() + Q.z());
            return sum;
        }

        public Point multiply(double scalar)
        {
            Point multiple = new Point(scalar * x(), scalar * y(), scalar * z());
            return multiple;
        }

        public double length()
        {
            return Math.sqrt(x()*x() + y()*y() + z()*z());
        }

        @Override public String toString()
        {
            return "(" + x() + "," + y() + "," + z() + ")";
        }

        @Override public Point clone()
        {
            Point copy = new Point(x(), y(), z());
            return copy;
        }
    }

    class PixelCoordinates
    {
        int x;
        int y;
    }

    class Plane
    {
        public Point representativePoint;
        public Point normalVector;

        public Plane()
        {
        }

        public Plane(Point A, Point B, Point C)
        {
            representativePoint = A;
            Point AB = B.subtract(A);
            Point AC = C.subtract(A);
            normalVector = CrossProduct(AB, AC);
        }
    }

    class LineSegment
    {
        public Point start;
        public Point end;

        public LineSegment(Point startPoint, Point endPoint)
        {
            start = startPoint; end = endPoint;
        }

        public LineSegment translate(Point translationVector)
        {
            LineSegment translation = clone();
            translation.start = translation.start.add(translationVector);
            translation.end = translation.end.add(translationVector);
            return translation;
        }

        @Override public LineSegment clone()
        {
            LineSegment copy = new LineSegment(start.clone(), end.clone());
            return copy;
        }
    }

    class RotationMatrix extends Matrix
    {
        public RotationMatrix()
        {
            super(3,3);
        }

        public RotationMatrix(Matrix m)
        {
            super(m);
        }

        Point multiply(Point vector)
        {
            Point result = new Point(0,0,0);
            try
            {
                Matrix matrixResult = super.multiply(vector);
                result = new Point(matrixResult);
            }
            catch(MatrixException e)
            {
                System.out.println(e);
            }
            return result;
        }
    }

    private final Point[] cubePoints =
            {
                    new Point(0, 0, 0),
                    new Point(10, 0, 0),
                    new Point(10, 10, 0),
                    new Point(0, 10, 0),
                    new Point(0, 0, 10),
                    new Point(10, 0, 10),
                    new Point(10, 10, 10),
                    new Point(0, 10, 10)
            };

    private final Point[] housePoints =
            {
                    new Point(450, 200, 0),//house base: front side
                    new Point(750, 200, 0),//house base: front side
                    new Point(750, 500, 0),//house base: back side
                    new Point(450, 500, 0),//house base: back side
                    new Point(450, 200, 200),//top corner
                    new Point(750, 200, 200),//top corner
                    new Point(750, 500, 200),//top corner
                    new Point(450, 500, 200),//top corner
                    new Point(600, 350, 350),//pinnacle of roof
                    new Point(690, 200, 0),//door
                    new Point(690, 200, 50),//door
                    new Point(710, 200, 50),//door
                    new Point(710, 200, 0),//door
                    new Point(685, 200, 0),//walkway from door
                    new Point(715, 200, 0),//walkway from door
                    new Point(685, 100, 0),//walkway from door
                    new Point(715, 100, 0),//walkway from door
                    new Point(520, 330, 270),//chimney base 17
                    new Point(530, 330, 280),//chimney base 18
                    new Point(530, 370, 280),//chimney base 19
                    new Point(520, 370, 270),//chimney base 20
                    new Point(520, 330, 340),//chimney top 21
                    new Point(530, 330, 340),//chimney top 22
                    new Point(530, 370, 340),//chimney top 23
                    new Point(520, 370, 340),//chimney top 24
            };

    private final LineSegment[] cubeStructure =
            {
                    new LineSegment(cubePoints[0], cubePoints[1]),
                    new LineSegment(cubePoints[1], cubePoints[2]),
                    new LineSegment(cubePoints[2], cubePoints[3]),
                    new LineSegment(cubePoints[3], cubePoints[0]),
                    new LineSegment(cubePoints[0], cubePoints[4]),
                    new LineSegment(cubePoints[1], cubePoints[5]),
                    new LineSegment(cubePoints[2], cubePoints[6]),
                    new LineSegment(cubePoints[3], cubePoints[7]),
                    new LineSegment(cubePoints[4], cubePoints[5]),
                    new LineSegment(cubePoints[5], cubePoints[6]),
                    new LineSegment(cubePoints[6], cubePoints[7]),
                    new LineSegment(cubePoints[7], cubePoints[4])
            };

    private LineSegment[] houseStructure = new LineSegment[]
            {
                    new LineSegment(housePoints[0], housePoints[1]),
                    new LineSegment(housePoints[1], housePoints[2]),
                    new LineSegment(housePoints[2], housePoints[3]),
                    new LineSegment(housePoints[3], housePoints[0]),
                    new LineSegment(housePoints[4], housePoints[5]),
                    new LineSegment(housePoints[5], housePoints[6]),
                    new LineSegment(housePoints[6], housePoints[7]),
                    new LineSegment(housePoints[7], housePoints[4]),
                    new LineSegment(housePoints[0], housePoints[4]),
                    new LineSegment(housePoints[1], housePoints[5]),
                    new LineSegment(housePoints[2], housePoints[6]),
                    new LineSegment(housePoints[3], housePoints[7]),
                    new LineSegment(housePoints[4], housePoints[8]),
                    new LineSegment(housePoints[5], housePoints[8]),
                    new LineSegment(housePoints[6], housePoints[8]),
                    new LineSegment(housePoints[7], housePoints[8]),
                    new LineSegment(housePoints[9], housePoints[10]),
                    new LineSegment(housePoints[10], housePoints[11]),
                    new LineSegment(housePoints[11], housePoints[12]),
                    new LineSegment(housePoints[12], housePoints[9]),
                    new LineSegment(housePoints[13], housePoints[15]),
                    new LineSegment(housePoints[15], housePoints[16]),
                    new LineSegment(housePoints[16], housePoints[14]),
                    new LineSegment(housePoints[17], housePoints[18]),//chimney
                    new LineSegment(housePoints[18], housePoints[19]),//chimney
                    new LineSegment(housePoints[19], housePoints[20]),//chimney
                    new LineSegment(housePoints[20], housePoints[17]),//chimney
                    new LineSegment(housePoints[17], housePoints[21]),//chimney
                    new LineSegment(housePoints[18], housePoints[22]),//chimney
                    new LineSegment(housePoints[19], housePoints[23]),//chimney
                    new LineSegment(housePoints[20], housePoints[24]),//chimney
                    new LineSegment(housePoints[21], housePoints[22]),//chimney
                    new LineSegment(housePoints[22], housePoints[23]),//chimney
                    new LineSegment(housePoints[23], housePoints[24]),//chimney
                    new LineSegment(housePoints[24], housePoints[21])//chimney
            };

    class RenderableObject
    {
        protected LineSegment[] structure;
        protected Color color = Color.GREEN;

        public void render(Graphics2D g2)
        {
            Color oldColor = g2.getColor();
            g2.setColor(color);
            for(int j = 0; j < structure.length; j++)
            {
                renderLineSegment(g2, structure[j]);
            }
            g2.setColor(oldColor);
        }
    }

    class Projectile extends RenderableObject
    {
        private Point velocity;
        private Point centerOfMass;
        private Point rotationAxis;//...is the line through the center of mass in this direction
        private double rotationAmount;
        private boolean active;

        public Projectile(Point initialPosition, Point initialVelocity, LineSegment[] components, Point axisOfRotation,
                          double rotationRate)
        {
            //construct a projectile
            this(initialPosition, initialVelocity, components);//call the other constructor first
            rotationAxis = axisOfRotation;
            rotationAmount = rotationRate;
        }

        public Projectile(Point initialPosition, Point initialVelocity, LineSegment[] components)
        {
            //construct a projectile
            velocity = initialVelocity;
            structure = new LineSegment[components.length];
            centerOfMass = new Point(0, 0, 0);
            for(int i = 0; i < structure.length; i++)
            {
                structure[i] = components[i].translate(initialPosition);
                centerOfMass = centerOfMass.add(structure[i].start).add(structure[i].end);
            }
            centerOfMass = centerOfMass.multiply(1.0 / (2.0 * structure.length));
            active = true;
        }

        public Point getPosition()
        {
            return centerOfMass.clone();
        }

        public boolean update(Point acceleration)
        {
            if(!active) return false;
            if(rotationAxis != null)
            {
                //rotate the structure (i.e., rotate each endpoint of each line segment in structure)
                //about the line through centerOfMass in the direction of rotationAxis

                //COMPLETE THIS FOR HW 6
                Point A = centerOfMass;
                Point B = A.add(rotationAxis);
                LineSegment rotateAbout = new LineSegment(A, B);
                for(LineSegment l : structure)
                {
                   l.start = rotate(l.start, rotationAmount, rotateAbout);
                   l.end = rotate(l.end, rotationAmount, rotateAbout);
                }
            }
            //update structure'S points and centerOfMass: they are moving at *velocity*
            //then update velocity by the given acceleration


            velocity = velocity.add(acceleration);
            centerOfMass = centerOfMass.add(velocity);
            for (LineSegment l : structure)
            {
                l.start = l.start.add(velocity);
                l.end = l.end.add(velocity);
            }

            //finally, check whether this projectile has hit the ground: if so, de-activate it
            for(int i = 0; i < structure.length; i++)
            {
                if(structure[i].start.z() <= 0 || structure[i].end.z() <= 0)
                {
                    active = false;
                    break;
                }
            }
            return true;//this projectile was active when update was called; update occurred
        }
    }

    class House extends RenderableObject
    {
        private Point[] houseP;
        boolean exploded;

        private final int[][] housePlaneIndexes =
                {
                        {0, 1, 4}, // front wall
                        {1, 2, 5}, // right wall
                        {2, 3, 6}, // back wall
                        {0, 3, 4}, // left wall
                        {0, 1, 2}, // floor
                        {4, 5, 8}, // front roof
                        {5, 6, 8}, // right roof
                        {6, 7, 8}, // back roof
                        {4, 7, 8}, // left roof
                };
        private final int[][] chimneyPlaneIndexes =
                {
                        {17, 18, 19}, // base
                        {17, 18, 21}, // front
                        {18, 19, 22}, // right
                        {19, 20, 23}, // back
                        {17, 20, 21}, // left
                        {21, 22, 23}, //top
                };

        private final Point interiorPoint;
        private final Plane[] houseBoundaryPlanes = new Plane[housePlaneIndexes.length];

        private final Plane[] chimneyBoundaryPlanes = new Plane[chimneyPlaneIndexes.length];

        public House(Point position)
        {
            houseP = new Point[housePoints.length];
            for(int i = 0; i < houseP.length; i++)
            {
                houseP[i] = housePoints[i].add(position);
            }

            structure = new LineSegment[houseStructure.length];
            for(int i = 0; i < structure.length; i++)
            {
                structure[i] = houseStructure[i].clone();
                structure[i] = structure[i].translate(position);
            }

            color = Color.GREEN;
            exploded = false;

            for (int i = 0; i < housePlaneIndexes.length; i++)
            {
                Point p1 = houseP[housePlaneIndexes[i][0]];
                Point p2 = houseP[housePlaneIndexes[i][1]];
                Point p3 = houseP[housePlaneIndexes[i][2]];
                houseBoundaryPlanes[i] = new Plane(p1, p2, p3);
            }

            for (int i = 0; i < chimneyPlaneIndexes.length; i++)
            {
                Point p1 = houseP[chimneyPlaneIndexes[i][0]];
                Point p2 = houseP[chimneyPlaneIndexes[i][1]];
                Point p3 = houseP[chimneyPlaneIndexes[i][2]];
                chimneyBoundaryPlanes[i] = new Plane(p1, p2, p3);
            }

            Point p1 = houseP[0].multiply(0.5);
            Point p2 = houseP[8].multiply(0.5);
            interiorPoint = p1.add(p2);
        }





        public boolean contains(Point P)
        {
            //Take advantage of the fact that our houses are convex (not counting the chimney!)
            return inHouse(P) || inChimney(P);
        }
        
        private boolean inHouse(Point P)
        {
            for (Plane plane : houseBoundaryPlanes)
            {
                if (!onSameSide(plane, P, interiorPoint)) return false;
            }
            return true;
        }
        private boolean inChimney(Point P)
        {
            for (Plane plane : chimneyBoundaryPlanes)
            {
                if (!onSameSide(plane, P, interiorPoint)) return false;
            }
            return true;
        }


        public void explode()
        {
            if(exploded) return;//already exploded
            Random rng = new Random(System.currentTimeMillis());
            for(int i = 0; i < structure.length; i++)
            {
                Point initialPos = structure[i].start;
                Point initialVel = new Point(8*rng.nextDouble()-4, 8*rng.nextDouble()-4, 8*rng.nextDouble()-4);
                Point rotationAxis = new Point(2*rng.nextDouble()-1, 2*rng.nextDouble()-1, 2*rng.nextDouble());
                double rotationAmount = rng.nextDouble();
                LineSegment[] piece = new LineSegment[1];
                piece[0] = new LineSegment(structure[i].start.subtract(initialPos), structure[i].end.subtract(initialPos));
                Projectile p = new Projectile(initialPos, initialVel, piece, rotationAxis, rotationAmount);
                flyingThings.add(p);
            }
            exploded = true;
        }
    }

    BufferedImage img;
    KeyListener myKeyListener;
    int width = 1920, height = 1024;
    Point origin = new Point(200, -200, height/2 + 50);
    Point iHat = new Point(1, 0, 0);
    Point jHat = new Point(0, 0, -1);
    Point eye = new Point(600,450,100);
    double eyeToViewportDistance = width / 2.0;
    double cutoffDistance = 30.0;
    double rotationAmount = -0.01;
    boolean seeBehindYou = true;
    long frameRate = 1000 / 60;//1000 / 30;//in milliseconds
    final Point gravitationalAcceleration = new Point(0, 0, -0.04);
    ArrayList<Projectile> flyingThings;
    ArrayList<House> houses;
    Animator animator;

    @Override public void paint(Graphics g)
    {
        Graphics2D g2 = img.createGraphics();
        g2.clearRect(0, 0, width, height);
        g2.setColor(Color.GREEN);

        g2.drawString("seeBehindYou=" + seeBehindYou + "; eye=" + eye
                + "; origin=" + origin + "; iHat =" + iHat
                + "; jHat=" + jHat, 0, 20);
        g2.setClip(0, 24, width, height);//so the scene rendering won't overwrite the eye information
        renderScene(g2);
        g.drawImage(img, 0, 0, null);
    }

    public void renderScene(Graphics2D g2)
    {
        for(int i = 0; i < houses.size(); i++)
        {
            House h = houses.get(i);
            h.render(g2);
        }

        for(int i = 0; i < flyingThings.size(); i++)
        {
            Projectile p = flyingThings.get(i);
            p.render(g2);
        }
    }

    private void renderLineSegment(Graphics2D g2, LineSegment seg)
    {
        LineSegment clipped = seg.clone();
        boolean visible = seeBehindYou ? true : clipLineSegment(clipped);
        if(!visible) return;
        Point A = clipped.start;
        Point B = clipped.end;
        PixelCoordinates p = getPixelCoordinates(A);
        PixelCoordinates q = getPixelCoordinates(B);
        g2.drawLine(p.x, p.y, q.x, q.y);
    }

    private PixelCoordinates getPixelCoordinates(Point P)
    {
        PixelCoordinates r = new PixelCoordinates();
        //Find the point of intersection of the graphics plane with the line from eye to P, in global coordinates
        Plane graphicsPlane = new Plane();
        graphicsPlane.representativePoint = origin;
        graphicsPlane.normalVector = CrossProduct(iHat, jHat);
        LineSegment lineOfSight = new LineSegment(eye, P);
        Point intersection = findIntersectionPoint(lineOfSight, graphicsPlane);
        //Convert from global coordinates to pixel coordinates with respect to origin, iHat, jHat
        Point intersectionRelativeToOrigin = intersection.subtract(origin);
        r.x = (int)DotProduct(intersectionRelativeToOrigin, iHat);
        r.y = (int)DotProduct(intersectionRelativeToOrigin, jHat);

        return r;
    }

    private Point findIntersectionPoint(LineSegment seg, Plane plane)
    {
        //seg: x = x0 + t*vx, y = y0 + t*vy, z = z0 + t*vz
        //plane: representative point P, normal vector n; equation is (Q - P)dot n = 0
        //(x0 + t*vx)*nx + (y0 + t*vy)*ny + (z0 + t*vz)*nz = P dot n
        //t*(v dot n) = (P - xyz) dot n
        Point P = plane.representativePoint;
        Point diff = P.subtract(seg.start);
        Point v = seg.end.subtract(seg.start);
        double t = DotProduct(diff, plane.normalVector) / DotProduct(v, plane.normalVector);
        Point intersection = new Point(seg.start.x() + t * v.x(), seg.start.y() + t * v.y(), seg.start.z() + t * v.z());
        return intersection;
    }

    private boolean onSameSide(Plane plane, Point A, Point B)//returns true iff A and B are on the same side of the plane
    {
        double dotA = DotProduct(plane.normalVector, A);
        double dotB = DotProduct(plane.normalVector, B);
        double threshold = DotProduct(plane.normalVector, plane.representativePoint);
        return Math.signum(dotA - threshold) == Math.signum(dotB - threshold);
    }

    private boolean clipLineSegment(LineSegment seg)
    {
        Plane cutoff = new Plane();
        Point kHat = CrossProduct(iHat, jHat);
        cutoff.normalVector = kHat;
        cutoff.representativePoint = eye.add(kHat.multiply(cutoffDistance));
        boolean startResult = onSameSide(cutoff, seg.start, eye);
        boolean endResult = onSameSide(cutoff, seg.end, eye);
        if(startResult && endResult) return false;
        if(!startResult && !endResult) return true;
        Point intersection = findIntersectionPoint(seg, cutoff);
        if(startResult) seg.start = intersection;
        if(endResult) seg.end = intersection;
        return true;
    }

    private double DotProduct(Point v, Point w)
    {
        return v.x() * w.x() + v.y() * w.y() + v.z() * w.z();
    }

    private Point CrossProduct(Point v, Point w)
    {
        Point r = new Point(v.y() * w.z() - v.z() * w.y(), - v.x() * w.z() + v.z() * w.x(), v.x() * w.y() - v.y() * w.x());
        return r;
    }

    private void setEye()
    {
        Point kHat = CrossProduct(iHat, jHat);
        eye = origin;

        eye = eye.add(iHat.multiply(width / 2.0));//center eye wrt viewport
        eye = eye.add(jHat.multiply(height / 2.0));

        eye = eye.subtract(kHat.multiply(eyeToViewportDistance));
    }

    private Point rotate(Point vector, double amount)
    {
        //rotate vector by the given amount of radians about the x-axis
        Point result = new Point
                (
                        vector.x(),
                        Math.cos(amount) * vector.y() + Math.sin(amount) * vector.z(),
                        -Math.sin(amount) * vector.y() + Math.cos(amount) * vector.z()
                );
        return result;
    }

    private Point rotate(Point vector, double amount, Point axisPoint)
    {
        //rotate vector by the given amount of radians about an axis passing through the origin
        //Here, the axis of rotation is the line from the origin to "axisPoint"
        double axisPointLength = axisPoint.length();
        if(axisPointLength == 0.0) return vector;
        axisPoint = axisPoint.multiply(1.0 / axisPointLength);
        Point result;

        //Construct a rotation which brings the given axis to the x-axis

        //First, complete the given axis to a right-handed orthonormal basis of R^3
        Point axisPerp = new Point(axisPoint.y(), -axisPoint.x(), 0);
        double lengthAxisPerp = axisPerp.length();
        if(lengthAxisPerp == 0.0)
        {
            axisPerp = new Point(0, axisPoint.z(), -axisPoint.y());
            lengthAxisPerp = axisPerp.length();
        }
        if(lengthAxisPerp == 0.0)
        {
            return vector;
        }
        axisPerp = axisPerp.multiply(1.0 / lengthAxisPerp);
        Point axisPerp2 = CrossProduct(axisPoint, axisPerp);

        //Now turn this orthonormal basis into a matrix
        RotationMatrix f = new RotationMatrix();
        f.setColumn(0, axisPoint);
        f.setColumn(1, axisPerp);
        f.setColumn(2, axisPerp2);

        //Compute the inverse of f: it is just the same as the transpose
        RotationMatrix fInverse = new RotationMatrix(f.transpose());

        //Do the work: compute result = f * rotate(result, amount) * fInverse, and return result
        result = fInverse.multiply(vector);
        result = rotate(result, amount);
        result = f.multiply(result);
        return result;
    }

    private Point rotate(Point vector, double amount, LineSegment axis)
    {
        //rotate vector by the given amount of radians about the given axis
        Point p = axis.start;
        LineSegment translatedAxis = axis.translate(p.multiply(-1));
        Point translatedVector = vector.subtract(p);
        Point rotatedVector = rotate(translatedVector, amount, translatedAxis.end);
        return rotatedVector.add(p);
    }

    public VectorGraphics()
    {
        setEye();
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        myKeyListener = new KeyListener()
        {
            @Override public void keyPressed(KeyEvent e)
            {
                Point kHat = CrossProduct(iHat, jHat);
                LineSegment eyeAlongiHat = new LineSegment(eye, eye.add(iHat));
                LineSegment eyeAlongjHat = new LineSegment(eye, eye.add(jHat));
                double multiplier = 3;
                int modifiers = e.getModifiers();
                if(modifiers > 0) multiplier *= 10.0;//turbo speed when SHIFT is pressed
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_UP://move forward
                        origin = origin.add(kHat.multiply(multiplier));
                        break;
                    case KeyEvent.VK_DOWN://move backward
                        origin = origin.subtract(kHat.multiply(multiplier));
                        break;
                    case KeyEvent.VK_LEFT://rotate left
                        origin = rotate(origin, -rotationAmount, eyeAlongjHat);
                        iHat = rotate(iHat, -rotationAmount, jHat);
                        break;
                    case KeyEvent.VK_RIGHT://rotate right
                        origin = rotate(origin, rotationAmount, eyeAlongjHat);
                        iHat = rotate(iHat, rotationAmount, jHat);
                        break;
                    case KeyEvent.VK_A://rotate up
                        origin = rotate(origin, rotationAmount, eyeAlongiHat);
                        jHat = rotate(jHat, rotationAmount, iHat);
                        break;
                    case KeyEvent.VK_Z://rotate down
                        origin = rotate(origin, -rotationAmount, eyeAlongiHat);
                        jHat = rotate(jHat, -rotationAmount, iHat);
                        break;
                    case KeyEvent.VK_B://toggle seeBehindYou
                        seeBehindYou = !seeBehindYou;
                        break;
                    case KeyEvent.VK_SPACE://launch a new projectile
                        Point projectileVelocity = kHat.multiply(3).multiply(multiplier);
                        double rotationRate = 0.135;
                        Projectile proj = new Projectile(eye, projectileVelocity, cubeStructure, jHat, rotationRate);
                        flyingThings.add(proj);
                        break;
                }
                //Adjust eye position to match new viewport
                setEye();
            }

            @Override public void keyTyped(KeyEvent e)
            {

            }

            @Override public void keyReleased(KeyEvent e)
            {

            }

        };
        addKeyListener(myKeyListener);
        flyingThings = new ArrayList<Projectile>();
        houses = new ArrayList<House>();
        double radius = 2000.0;
        double numHouses = 25;
        for(int i = 0; i < numHouses; i++)
        {
            Point basePoint = new Point(radius*Math.cos(2*i*Math.PI / numHouses),
                    radius*Math.sin(2*i*Math.PI / numHouses), 0);
            houses.add(new House(basePoint));
        }

        Timer timer = new Timer();
        animator = new Animator(this);
        timer.scheduleAtFixedRate(animator, 0, frameRate);
    }

    public static void main(String[] args)
    {
        JFrame f = new JFrame("CSCI 331 Lab 4");

        f.addWindowListener(new WindowAdapter()
        {

            @Override public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });

        VectorGraphics p = new VectorGraphics();
        f.add(p);
        f.pack();
        f.setVisible(true);
    }
}
