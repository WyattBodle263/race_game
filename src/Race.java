
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.awt.EventQueue;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.util.Collections;
class Wall extends PhysicalObject {

    public Wall(final int x, final int y, final int width, final int height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(final Graphics2D g2d, final ImageObserver imageObserver) {
        g2d.fill(getBounds());
    }
}

class TrackPaint extends Sprite {

    public TrackPaint(final int x, final int y, final int width, final int height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(final Graphics2D g2d, ImageObserver imageObserver) {
        g2d.setColor(Color.WHITE);
        g2d.fill(getBounds());
        try {
            // Load the PNG image
            BufferedImage backgroundImage = ImageIO.read(new File("assets/race_track.png"));

            // Draw the image onto the graphics context
            g2d.drawImage(backgroundImage, 0, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class TrackData {

    private final static int BOUNDING_WALL_THICKNESS = 30;
    private final static int CENTRE_WALL_THICKNESS = 300;
    private final static int CENTRE_WALL_LENGTH = 700;
    private final static int CENTRE_WALL_X_POSITION = 200;
    private final static int CENTRE_WALL_Y_POSITION = 200;

    private static final int FIRST_CAR_X = 200;
    private static final int FIRST_CAR_Y = 50;
    private static final int CAR_SPACING = 80;

    private static final int IBOX_X = 300;
    private static final int IBOX_Y = 520;


    public static int getCarX(final int carIndex) {
        return FIRST_CAR_X;
    }

    public static int getCarY(final int carIndex) {
        return FIRST_CAR_Y + CAR_SPACING * carIndex;
    }

    public static List<Wall> createWalls() {
        Wall northWall = new Wall(0, 0, Racer.TRACK_WIDTH, BOUNDING_WALL_THICKNESS);
        Wall southWall = new Wall(0, Racer.TRACK_HEIGHT - BOUNDING_WALL_THICKNESS, Racer.TRACK_WIDTH,
                BOUNDING_WALL_THICKNESS);
        Wall westWall = new Wall(0, 0, BOUNDING_WALL_THICKNESS, Racer.TRACK_HEIGHT);
        Wall eastWall = new Wall(Racer.TRACK_WIDTH - BOUNDING_WALL_THICKNESS, 0,
                BOUNDING_WALL_THICKNESS,
                Racer.TRACK_HEIGHT);
        List<Wall> walls = new ArrayList<>();
        Collections.addAll(walls, northWall, southWall, westWall, eastWall);
        return walls;
    }

    public static List<Car> createCars() {
        List<Car> cars = new ArrayList<>();
        cars.add(Car.fromIndex(0));
        cars.add(Car.fromIndex(1));
        return cars;
    }

    public static List<Box> createBoxes() {
        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box(IBOX_X, IBOX_Y));
        boxes.add(new Box(IBOX_X, IBOX_Y + 30));
        boxes.add(new Box(IBOX_X, IBOX_Y + 60));
        boxes.add(new Box(IBOX_X, IBOX_Y + 90));
        return boxes;
    }

    public static List<TrackPaint> createTrackPaints() {
        List<TrackPaint> paints = new ArrayList<>();
        paints.add(new TrackPaint(250, TrackData.BOUNDING_WALL_THICKNESS, 2,
                TrackData.CENTRE_WALL_Y_POSITION - TrackData.BOUNDING_WALL_THICKNESS));
        return paints;
    }

}

class Track extends JPanel implements Runnable {

    private static final int DELAY = 10;
    private final List<Car> cars;
    private final List<MovingObject> movingObjects;
    private final List<Wall> walls;
    private final List<TrackPaint> trackPaints;

    public Track() {
        cars = new ArrayList<>();
        movingObjects = new ArrayList<>();
        walls = new ArrayList<>();
        trackPaints = new ArrayList<>();
        initTrack();
    }

    private void initTrack() {
        addKeyListener(new TAdapter());
        setBackground(Color.GRAY);
        setFocusable(true);
        walls.addAll(TrackData.createWalls());
        trackPaints.addAll(TrackData.createTrackPaints());
        cars.addAll(TrackData.createCars());
        movingObjects.addAll(cars);
        movingObjects.addAll(TrackData.createBoxes());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        walls.forEach(wall -> wall.draw(g2d, this));
        trackPaints.forEach(paint -> paint.draw(g2d, this));
        movingObjects.forEach(obj -> obj.draw(g2d, this));
        drawStats(g2d);
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawStats(Graphics2D g2d) {
        g2d.drawString(cars.get(0).infoString(), 40, 50);
    }

    private void updateObjects(long timeDiff) {
        movingObjects.forEach(obj -> obj.update((float) timeDiff / 1000));
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            cars.forEach(car -> car.keyReleased(e));
        }

        @Override
        public void keyPressed(KeyEvent e) {
            cars.forEach(car -> car.keyPressed(e));
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        final Thread animatorThread = new Thread(this);
        animatorThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        while (true) {
            final long now = System.currentTimeMillis();
            final long timeDiff = now - lastTime;
            lastTime = now;
            CollisionManager.checkAndApplyCollisions(movingObjects, walls);
            updateObjects(timeDiff);
            repaint();
            long sleep = DELAY - timeDiff;
            if (sleep < 0) {
                sleep = 2;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                String msg = String.format("Thread interrupted: %s", e.getMessage());
                JOptionPane.showMessageDialog(this, msg, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

abstract class Sprite {
    protected double x;
    protected double y;
    protected int width;
    protected int height;
    protected boolean visible;
    protected Image image;

    public Sprite(int x, int y) {
        this.x = x;
        this.y = y;
        visible = true;
    }

    public Sprite(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        visible = true;
    }

    protected void loadImage(String imageName) {
        ImageIcon ii = new ImageIcon(imageName);
        image = ii.getImage();
        width = image.getWidth(null);
        height = image.getHeight(null);
    }

    public Image getImage() {
        return image;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public abstract void draw (Graphics2D g2d, ImageObserver imageObserver);
}

class Racer extends JFrame {

    public final static int TRACK_WIDTH = 495;
    public final static int TRACK_HEIGHT = 700;
    private final static int BOTTOM_MARGIN = 0;

    private void initUI() {
        add(new Track());
        setTitle("Lamborghini Countach Face-off");
        setSize(TRACK_WIDTH, TRACK_HEIGHT + BOTTOM_MARGIN);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public Racer() {
        initUI();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Racer ex = new Racer();
            ex.setVisible(true);
        });
    }

}

abstract class PhysicalObject extends Sprite {

    public PhysicalObject(final int x, final int y) {
        super(x, y);
    }

    public PhysicalObject(final int x, final int y, final int width, final int height) {
        super(x, y, width, height);
    }
}

abstract class MovingObject extends PhysicalObject {

    protected double mass; // must be non-zero
    public double xSpeed;
    public double ySpeed;
    protected double xForce;
    protected double yForce;

    public MovingObject(final int x, final int y) {
        super(x, y);
    }

    public MovingObject(final int x, final int y, final int width, final int height) {
        super(x, y, width, height);
    }

    public abstract void update(final double timeDiff);
}
class CollisionManager {

    private static final double COLLISION_ELASTICITY = 0.5;

    /**
     * Move objects apart to make sure they remain disjoint by moving the first object away.
     */
    private static void deconflict(MovingObject object1, MovingObject object2) {
        Rectangle intersection = object1.getBounds().intersection(object2.getBounds());
        if (intersection.width < intersection.height) {
            object1.x = object1.x - Math.signum(object2.x - object1.x) * intersection.width;
        } else {
            object1.y = object1.y - Math.signum(object2.y - object1.y) * intersection.height;
        }
    }

    private static void applyCollision(MovingObject object1, MovingObject object2) {
        final double massRatioObject1 = object1.mass / (object1.mass + object2.mass);
        final double massRatioObject2 = object2.mass / (object1.mass + object2.mass);

        final double xDistance = object2.x - object1.x;
        final double yDistance = object2.y - object1.y;
        final double distance = Math.sqrt(xDistance * xDistance + yDistance * yDistance);
        final double xDistanceUnit = distance == 0 ? 0 : xDistance / distance;
        final double yDistanceUnit = distance == 0 ? 0 : yDistance / distance;

        final double meanXSpeed = (object1.xSpeed + object2.xSpeed) / 2;
        final double meanYSpeed = (object1.ySpeed + object2.ySpeed) / 2;
        final double collisionXSpeed = object1.xSpeed - object2.xSpeed;
        final double collisionYSpeed = object1.ySpeed - object2.ySpeed;

        final double xResultant =
                (collisionXSpeed + xDistanceUnit * Math.abs(collisionYSpeed)) * COLLISION_ELASTICITY;
        object1.xSpeed = meanXSpeed - massRatioObject2 * xResultant;
        object2.xSpeed = meanXSpeed + massRatioObject1 * xResultant;

        final double yResultant =
                (collisionYSpeed + yDistanceUnit * Math.abs(collisionXSpeed)) * COLLISION_ELASTICITY;
        object1.ySpeed = meanYSpeed - massRatioObject2 * yResultant;
        object2.ySpeed = meanYSpeed + massRatioObject1 * yResultant;

        deconflict(object1, object2);
    }

    private static void applyCollision(MovingObject object1, Wall object2) {
        Rectangle carBounds = object1.getBounds();
        Rectangle car2Bounds = object2.getBounds();
        Rectangle intersection = carBounds.intersection(car2Bounds);
        if (intersection.width < intersection.height) {
            // horizontal collision
            object1.x = object1.x - Math.signum(object2.x - object1.x) * intersection.width;
            object1.xSpeed = -COLLISION_ELASTICITY * object1.xSpeed;
        } else {
            // vertical collision
            object1.y = object1.y - Math.signum(object2.y - object1.y) * intersection.height;
            object1.ySpeed = -COLLISION_ELASTICITY * object1.ySpeed;
        }
    }

    public static void checkAndApplyCollisions(List<MovingObject> movingObjects, List<Wall> walls) {
        // FIXME Too many collision checks (reflexivity)
        for (MovingObject collider : movingObjects) {
            for (MovingObject collidee : movingObjects) {
                if (collider.getBounds().intersects(collidee.getBounds())) {
                    if (!collider.equals(collidee)) {
                        CollisionManager.applyCollision(collider, collidee);
                    }
                }
            }
        }

        for (MovingObject collider : movingObjects) {
            for (Wall wall : walls) {
                if (collider.getBounds().intersects(wall.getBounds())) {
                    CollisionManager.applyCollision(collider, wall);
                }
            }
        }
    }

}

class Car extends MovingObject {

    // rad/s
    private static final double TURN_RATE = 4;

    private static final double AIR_DRAG_COEFFICIENT = 0.01;

    private static final double TYRE_TRACTION_COEFFICIENT = 800;
    private final static boolean DRAW_BOUNDING_BOXES = true;

    // px/s
    public double getIndicatedSpeed() {
        return indicatedSpeed;
    }

    public String infoString() {
        return String
                .format("Speed:%f, xH:%f, yH:%g, xS:%f, yS:%f\nxF:%f, yF:%f",
                        indicatedSpeed,
                        xHeading,
                        yHeading,
                        xSpeed,
                        ySpeed,
                        xForce,
                        yForce);
    }

    // radians
    public double getHeading() {
        return Math.atan2(xHeading, -yHeading);
    }

    private double indicatedSpeed;
    private double xHeading;
    private double yHeading;
    private boolean accelerating;
    private boolean braking;
    private boolean turningRight;
    private boolean turningLeft;

    private int forwardKeyCode = KeyEvent.VK_UP;
    private int backwardKeyCode = KeyEvent.VK_DOWN;
    private int leftKeyCode = KeyEvent.VK_LEFT;
    private int rightKeyCode = KeyEvent.VK_RIGHT;

    public Car(int x, int y) {
        super(x, y);
        initCar(0);
    }

    @Override
    public void draw(final Graphics2D g2d, ImageObserver imageObserver) {
        g2d.drawImage(getImage(), getAffineTransform(), imageObserver);

        if (DRAW_BOUNDING_BOXES) {
            g2d.draw(getBounds());
        }
    }

    public Car(int x, int y, int carIndex) {
        super(x, y);
        initCar(carIndex);
        if (carIndex == 1) {
            forwardKeyCode = KeyEvent.VK_W;
            backwardKeyCode = KeyEvent.VK_S;
            leftKeyCode = KeyEvent.VK_A;
            rightKeyCode = KeyEvent.VK_D;
        }
    }

    public static Car fromIndex(int carIndex) {
        return new Car(TrackData.getCarX(carIndex), TrackData.getCarY(carIndex), carIndex);
    }

    private void initCar(int carIndex) {
        if (carIndex == 1) {
            loadImage("assets/countach blue.png");
        } else {
            loadImage("assets/countach purple.png");
        }
        indicatedSpeed = 0;
        xSpeed = 0;
        ySpeed = 0;
        xForce = 0;
        yForce = 0;
        mass = 4;
        xHeading = 1;
        yHeading = 0;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == leftKeyCode) {
            turningLeft = true;
        }
        if (key == rightKeyCode) {
            turningRight = true;
        }
        if (key == forwardKeyCode) {
            accelerating = true;
        }
        if (key == backwardKeyCode) {
            braking = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == leftKeyCode) {
            turningLeft = false;
        }
        if (key == rightKeyCode) {
            turningRight = false;
        }
        if (key == forwardKeyCode) {
            accelerating = false;
        }
        if (key == backwardKeyCode) {
            braking = false;
        }
    }

    private void updateDynamicsFromInputs(final double timeDiff) {
        if (accelerating) {
            // FIXME: Decouple acceleration from frame rate
            // TODO: Extract magic number
            xSpeed += 3 * xHeading;
            ySpeed += 3 * yHeading;
        }
        if (braking) {
            xSpeed -= 2 * xHeading;
            ySpeed -= 2 * yHeading;
        }
        if (turningLeft) {
            // delta must be acute
            final double delta = -TURN_RATE * timeDiff;
            final double newXHeading = Math.cos(delta) * xHeading - Math.sin(delta) * yHeading;
            final double newYHeading = Math.sin(delta) * xHeading + Math.cos(delta) * yHeading;
            xHeading = newXHeading;
            yHeading = newYHeading;
        }
        if (turningRight) {
            final double delta = TURN_RATE * timeDiff;
            final double newXHeading = Math.cos(delta) * xHeading - Math.sin(delta) * yHeading;
            final double newYHeading = Math.sin(delta) * xHeading + Math.cos(delta) * yHeading;
            xHeading = newXHeading;
            yHeading = newYHeading;
        }
    }

    static double signedAngleBetweenVectors(double u1, double u2, double v1, double v2) {
        return Math.atan2(u2, u1) - Math.atan2(v2, v1);
    }

    static double lateralTyreForce(double xHeading, double yHeading, double xSpeed, double ySpeed) {
        if (xSpeed - xHeading == 0) {
            return 0;
        }
        return TYRE_TRACTION_COEFFICIENT * Math
                .sin(signedAngleBetweenVectors(xHeading, yHeading, xSpeed, ySpeed));
    }

    protected void updateForces() {
        final double xAirResistanceForceNewton = -xSpeed * Math.abs(xSpeed) * AIR_DRAG_COEFFICIENT;
        final double yAirResistanceForceNewton = -ySpeed * Math.abs(ySpeed) * AIR_DRAG_COEFFICIENT;
        final double lateralTyreForce = lateralTyreForce(xHeading, yHeading, xSpeed, ySpeed);
        final double xTyreResistanceForceNewton = -yHeading * lateralTyreForce;
        final double yTyreResistanceForceNewton = xHeading * lateralTyreForce;
        xForce = xAirResistanceForceNewton + xTyreResistanceForceNewton;
        yForce = yAirResistanceForceNewton + yTyreResistanceForceNewton;
    }

    //pre-condition: non-zero
    protected void updateSpeed(final double timeDiff) {
        final double dXSpeed = xForce / mass * timeDiff;
        xSpeed += dXSpeed;
        final double dYSpeed = yForce / mass * timeDiff;
        ySpeed += dYSpeed;
        indicatedSpeed = Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
    }

    // pre-condition: non-zero
    @Override
    public void update(final double timeDiff) {
        if (timeDiff == 0) {
            return;
        }

        // Original update logic
        updateDynamicsFromInputs(timeDiff);
        updateForces();
        updateSpeed(timeDiff);
        x += xSpeed * timeDiff;
        y += ySpeed * timeDiff;

        // New code for checking pixel color under the car
        try {
            BufferedImage backgroundImage = ImageIO.read(new File("assets/race_track.png"));
            int pixelX = (int) x + width / 2;  // Calculate center of the car
            int pixelY = (int) y + height / 2;
            if (pixelX >= 0 && pixelX < backgroundImage.getWidth() && pixelY >= 0 && pixelY < backgroundImage.getHeight()) {
                int pixelColor = backgroundImage.getRGB(pixelX, pixelY);
                // Check if pixel color matches rgba e1b4f7ff or b4c0f7ff
                if (pixelColor == 0xe1b4f7ff || pixelColor == 0xb4c0f7ff) {
                    // Do something if condition is met
                    System.out.println("Car is on track with specified pixel color!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public AffineTransform getAffineTransform() {
        AffineTransform affine = new AffineTransform();
        affine.translate(x, y);
        // center image
        affine.rotate(getHeading(), (double) width / 2, (double) height / 2);
        return affine;
    }

}

class Box extends MovingObject {

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;
    private static final double FRICTION_COEFFICIENT = 400;

    public Box(final int x, final int y) {
        super(x, y, WIDTH, HEIGHT);
        mass = 1;
    }

    @Override
    public void draw(final Graphics2D g2d, final ImageObserver imageObserver) {
        g2d.fill(getBounds());
    }

    protected void updateForces() {
        // Lower bound to both model static friction and to avoid division by 0.
        final double speed = Math.max(1, Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed));
        final double xTrackFrictionForce = - FRICTION_COEFFICIENT * xSpeed / speed;
        final double yTrackFrictionForce = - FRICTION_COEFFICIENT * ySpeed / speed;
        xForce = xTrackFrictionForce;
        yForce = yTrackFrictionForce;
    }

    // apply forces
    protected void updateSpeed(final double timeDiff) {
        final double dXSpeed = xForce / mass * timeDiff;
        xSpeed += dXSpeed;
        final double dYSpeed = yForce / mass * timeDiff;
        ySpeed += dYSpeed;

        // Check if the car is on top of the outline image and slow down if necessary
        if (isCarOnOutline()) {
            xSpeed *= 0.5; // Reduce speed by 50%
            ySpeed *= 0.5; // Reduce speed by 50%
        }
    }

    private boolean isCarOnOutline() {
        try {
            // Load the PNG image
            BufferedImage outlineImage = ImageIO.read(new File("assets/race_track_outline.png"));

            // Get the color of the pixel under the car's position
            int pixelColor = outlineImage.getRGB((int) x, (int) y);
            Color outlineColor = new Color(pixelColor, true);

            // Check if the color of the pixel is not transparent (indicating the car is on the outline)
            return outlineColor.getAlpha() != 0;
        } catch (IOException e) {
            e.printStackTrace();
            // Assume not on outline if image loading fails
            return false;
        }
    }


    @Override
    public void update(final double timeDiff) {
        if (timeDiff == 0) {
            return;
        }
        updateForces();
        updateSpeed(timeDiff);
        x += xSpeed * timeDiff;
        y += ySpeed * timeDiff;
    }

}
