// Asteroids . java Copyright (C) 2019 Ben Sanders
// TODO make levels that flow , one to the next . have a score and lives !

import org.w3c.dom.ls.LSOutput;

import java.util.Vector ;
import java.util.Random;
import java.time.LocalTime;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Race
{

    private static class AudioLooper implements Runnable{
        public void run(){
            while(!endgame){
                Long curTime = System.currentTimeMillis();
                if(curTime - lastAudioStart > audiolifeTime) {
                    playAudio();
                }
            }
        }

        public static void playAudio(){
            try{
                clip.stop();
            } catch(Exception e){
                //NOP
            }
            try{
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("music.wav").getAbsoluteFile());
                clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
                lastAudioStart = System.currentTimeMillis();
                audiolifeTime = (long)249000;
            } catch(Exception e){
                //NOP
            }
        }
    }
    public Race()
    {
        setup();
    }

    public static void setup()
    {
        appFrame = new JFrame("Lamborghini Countach Faceoff");
        XOFFSET = 0;
        YOFFSET = 40;
        WINWIDTH = 470;
        WINHEIGHT = 635;
        pi = 3.14159265358979;
        twoPi = 2.0 * 3.14159265358979;
        endgame = false;
        p1width = 25; // 18.5;
        p1height = 25; // 25;
        p2width = 25; // 18.5;
        p2height = 25; // 25;
        p1originalX = 85;
        p1originalY = 350;
        p2originalX = 105;
        p2originalY = 380;
        p1LapCount = 0;
        p2LapCount = 0;
        lastPassedStartTime = System.currentTimeMillis();
        p2lastPassedStartTime = System.currentTimeMillis();
        p1BestTime = (long) 100000000000.0;
        p2BestTime = (long) 100000000000.0;
        p1PassedFirst = false;
        p2PassedFirst = false;


        audiolifeTime = Long.valueOf(0);

        try
        {
            background = ImageIO.read(new File("race_track.png"));
            player = ImageIO.read(new File("countach blue.png"));
            player2 = ImageIO.read(new File("countach purple.png"));

        }
        catch (IOException ioe)
        {

        }
    }
    private static class Animate implements Runnable
    {
        public void run()
        {
            while (endgame == false)
            {
                backgroundDraw();
                playerDraw();
                player2Draw();

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {

                }
            }
        }
    }
    private static class PlayerMover implements Runnable
    {
        public PlayerMover()
        {
            velocitystep = 0.01;
            rotatestep = 0.03;
        }

        public void run()
        {
            while (endgame == false)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                }
                if (upPressed == true && p1velocity <= 2)
                {
                    p1velocity = p1velocity + velocitystep;
                }else{
                    p1velocity = p1velocity + (p1velocity > 0 ? (-velocitystep*0.75) :0);
                }
                if (downPressed == true)
                {
                    p1velocity = p1velocity - velocitystep;
                }else{
                    p1velocity = p1velocity - (p1velocity < 0 ? (-velocitystep*0.75) :0);
                }
                if (leftPressed == true)
                {
                    if (p1velocity < 0)
                    {
                        p1.rotate(rotatestep);
                    } else
                    {
                        p1.rotate(-rotatestep);
                    }
                }
                if (rightPressed == true)
                {
                    if (p1velocity < 0)
                    {
                        p1.rotate(-rotatestep);
                    }
                    else
                    {
                        p1.rotate(rotatestep);
                    }
                }
                // Get the RGBA color of the background image at the position of the player
                int x = (int) (p1.getX() + 0.5);
                int y = (int) (p1.getY() + 0.5);
                int color = background.getRGB(x, y);
                int alpha = (color >> 24) & 0xFF;
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                if (("Player 1 RGBA: " + red + ", " + green + ", " + blue + ", " + alpha).equals("Player 1 RGBA: 225, 180, 247, 255") || ("Player 1 RGBA: " + red + ", " + green + ", " + blue + ", " + alpha).equals("Player 1 RGBA: 180, 192, 247, 255")) {
                    p1velocity = 0.08;
                } else if (("Player 1 RGBA: " + red + ", " + green + ", " + blue + ", " + alpha).equals("Player 1 RGBA: 0, 128, 0, 255")) {
                    p1PassedFirst = true;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPassedStartTime > 5000) {
                        // Output the "Passed Start" event
                        System.out.println("Player 1 Passed Start");
                        System.out.println(currentTime - lastPassedStartTime);
                        // Update the last event time
                        System.out.println(p1BestTime + " > " + (currentTime - lastPassedStartTime) + " " +  (p1BestTime > (currentTime - lastPassedStartTime)));
                        if(p1BestTime > (currentTime - lastPassedStartTime) && p1PassedFirst){
                            p1BestTime = currentTime - lastPassedStartTime;
                        }
                        lastPassedStartTime = currentTime;
                        p1LapCount++;
                    }
                }

                p1.move(p1velocity * Math.cos(p1.getAngle() - pi / 2.0), p1velocity * Math.sin(p1.getAngle() - pi / 2.0));
                if(p1.screenWrap(XOFFSET, XOFFSET + WINWIDTH, YOFFSET, YOFFSET + WINHEIGHT)){
                    p1velocity = 0.0;
                }
//                System.out.println("p1 x: " + p1.getX());
//                System.out.println("p1 y: " + p1.getY());
            }
        }
        private double velocitystep;
        private double rotatestep;
    }
    private static class Player2Mover implements Runnable
    {
        public Player2Mover()
        {
            velocitystep = 0.01;
            rotatestep = 0.05;
        }

        public void run()
        {
            while (endgame == false)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                }
                if (wPressed == true && p2velocity <= 2)
                {
                    p2velocity = p2velocity + velocitystep;
                }else{
                    p2velocity = p2velocity + (p2velocity > 0 ? (-velocitystep*0.75) :0);
                }
                if (sPressed == true)
                {
                    p2velocity = p2velocity - velocitystep;
                }else{
                    p2velocity = p2velocity - (p1velocity < 0 ? (-velocitystep*0.75) :0);
                }
                if (aPressed == true)
                {
                    if (p2velocity < 0)
                    {
                        p2.rotate(rotatestep);
                    } else
                    {
                        p2.rotate(-rotatestep);
                    }
                }
                if (dPressed == true)
                {
                    if (p2velocity < 0)
                    {
                        p2.rotate(-rotatestep);
                    }
                    else
                    {
                        p2.rotate(rotatestep);
                    }
                }
                // Get the RGBA color of the background image at the position of the player
                int x = (int) (p2.getX() + 0.5);
                int y = (int) (p2.getY() + 0.5);
                int color = background.getRGB(x, y);
                int alpha = (color >> 24) & 0xFF;
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                if (("Player 2 RGBA: " + red + ", " + green + ", " + blue + ", " + alpha).equals("Player 2 RGBA: 225, 180, 247, 255") || ("Player 2 RGBA: " + red + ", " + green + ", " + blue + ", " + alpha).equals("Player 2 RGBA: 180, 192, 247, 255")) {
                    p2velocity = 0.08;
                } else if (("Player 2 RGBA: " + red + ", " + green + ", " + blue + ", " + alpha).equals("Player 2 RGBA: 0, 128, 0, 255")) {
                    p2PassedFirst = true;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - p2lastPassedStartTime > 5000) {
                        // Output the "Passed Start" event
                        System.out.println("Player 2 Passed Start");
                        System.out.println(currentTime - p2lastPassedStartTime);
                        // Update the last event time
                        System.out.println(p2BestTime + " > " + (currentTime - p2lastPassedStartTime) + " " +  (p2BestTime > (currentTime - p2lastPassedStartTime)));
                        if(p2BestTime > (currentTime - p2lastPassedStartTime) && p2PassedFirst){
                            p2BestTime = currentTime - p2lastPassedStartTime;
                        }
                        p2lastPassedStartTime = currentTime;
                        p2LapCount++;
                    }
                }

                p2.move(p2velocity * Math.cos(p2.getAngle() - pi / 2.0), p2velocity * Math.sin(p2.getAngle() - pi / 2.0));
                if(p2.screenWrap(XOFFSET, XOFFSET + WINWIDTH, YOFFSET, YOFFSET + WINHEIGHT)){
                    p2velocity = 0.0;
                }
            }
        }
        private double velocitystep;
        private double rotatestep;
    }

    private static class CollisionChecker implements Runnable
    {
        public void run()
        {
            System.out.println("RUNNING COLLISION CHECK");
            Random randomNumbers = new Random(LocalTime.now().getNano());
            while (endgame == false)
            {
                try
                {
                    // TODO compare all asteroids to all player bullets
                    // compare all asteroids to player
                    try {
                        // compare p1 to p2
                        if (collisionOccurs(p2, p1)) {
                            double p1X = p1.x;
                            double p1Y = p1.y;
                            double p2X = p2.x;
                            double p2Y = p2.y;

                            System.out.println("YOU HIT Another Player");
                            player = ImageIO.read(new File("explosions.png"));
                            player2 = ImageIO.read(new File("explosions.png"));
                            // Pause for 2 seconds
                            // Pause all threads for 2 seconds
                            synchronized (this) {
                                wait(2000);
                                p1velocity = 0;
                                p2velocity = 0;
                                p1.x = p1X - 25;
                                p1.y = p1Y;
                                p2.x = p1X + 25;
                                p2.y = p1Y;
                            }
                            player = ImageIO.read(new File("countach blue.png"));
                            player2 = ImageIO.read(new File("countach purple.png"));

                        }
                        // compare p1 to p2
                        if (collisionOccurs(dirt, p1)) {
                            System.out.println("YOU HIT Dirt");
                        }
                    }
                    catch (java.lang.NullPointerException jlnpe)
                    {
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (java.lang.ArrayIndexOutOfBoundsException jlaioob)
                {
                }
            }
        }
    }
    private static class WinChecker implements Runnable
    {
        public void run()
        {
            while (endgame == false)
            {
//                if (asteroids.size() == 0)
//                {
//                    endgame = true;
//                    System.out.println("Game Over You Win");
//                }
            }
        }
    }

    // TODO make one lock rotate function which takes as input objInner,
    // objOuter, and point relative to objInner’s x, y that objOuter must
    // rotate around.
    // dist is a distance between the two objects at the bottom of objInner.

    private static AffineTransformOp rotateImageObject(ImageObject obj) {
        AffineTransform at = AffineTransform.getRotateInstance(obj.getAngle(), obj.getWidth() / 2.0,
                obj.getHeight() / 2.0);
        AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        return atop;
    }

    private static AffineTransformOp spinImageObject(ImageObject obj) {
        AffineTransform at = AffineTransform.getRotateInstance(obj.getInternalAngle(), obj.getWidth() / 2.0,
                obj.getHeight() / 2.0);
        AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return atop;
    }

    private static void backgroundDraw() {
        Graphics g = appFrame.getGraphics();
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(background, XOFFSET, YOFFSET, null);
        int speed = (int) Math.round(p1velocity * 55); //TODO: Add this
        int speed2 = (int) Math.round(p2velocity * 55); //TODO: Add this

        g2D.drawString("Player 1 Speed: " + speed + " mph", XOFFSET + 10, YOFFSET + 20); //TODO: Add this
        g2D.drawString("Player 2 Speed: " + speed2 + " mph", XOFFSET + 355, YOFFSET + 20); //TODO: Add this
        g2D.drawString("Player 1 Lap: " + p1LapCount, XOFFSET + 10, YOFFSET + 35); //TODO: Add this
        g2D.drawString("Player 2 Lap: " + p2LapCount, XOFFSET + 355, YOFFSET + 35); //TODO: Add this
        if(p1BestTime == 100000000000.0){
            g2D.drawString("Player 1 Best Time: " + "0.0", XOFFSET + 10, YOFFSET + 50); //TODO: Add this
        }else {
            g2D.drawString("Player 1 Best Time: " + p1BestTime / 1000.0, XOFFSET + 10, YOFFSET + 50); //TODO: Add this
        }
        if(p2BestTime == 100000000000.0){
            g2D.drawString("Player 2 Best Time: " + "0.0", XOFFSET + 355, YOFFSET + 50); //TODO: Add this
        }else {
            g2D.drawString("Player 2 Best Time: " + p2BestTime / 1000.0, XOFFSET + 355, YOFFSET + 50); //TODO: Add this
        }

    }
    private static void playerDraw() {
        Graphics g = appFrame.getGraphics();
        Graphics2D g2D = (Graphics2D) g;

        // Get the RGBA color of the background image at the position of the player
        int x = (int) (p1.getX() + 0.5);
        int y = (int) (p1.getY() + 0.5);
        g2D.drawImage(rotateImageObject(p1).filter(player, null), x, y, null);
    }

    private static void player2Draw() {
        Graphics g = appFrame.getGraphics();
        Graphics2D g2D = (Graphics2D) g;

        // Get the RGBA color of the background image at the position of the player
        int x = (int) (p2.getX() + 0.5);
        int y = (int) (p2.getY() + 0.5);

        g2D.drawImage(rotateImageObject(p2).filter(player2, null), x, y, null);
    }
    private static class KeyPressed extends AbstractAction {
        public KeyPressed() {
            action = "";
        }

        public KeyPressed(String input) {
            action = input;
        }

        public void actionPerformed(ActionEvent e) {
            if (action.equals("UP")) {
                upPressed = true;
            }
            if (action.equals("DOWN")) {
                downPressed = true;
            }
            if (action.equals("LEFT")) {
                leftPressed = true;
            }
            if (action.equals("RIGHT")) {
                rightPressed = true;
            }
            if (action.equals("W")) {
                wPressed = true;
            }
            if (action.equals("S")) {
                sPressed = true;
            }
            if (action.equals("A")) {
                aPressed = true;
            }
            if (action.equals("D")) {
                dPressed = true;
            }

        }
        private String action;
    }

    private static class KeyReleased extends AbstractAction {
        public KeyReleased() {
            action = "";
        }

        public KeyReleased(String input) {
            action = input;
        }

        public void actionPerformed(ActionEvent e) {
            if (action.equals("UP")) {
                upPressed = false;
            }
            if (action.equals("DOWN")) {
                downPressed = false;
            }
            if (action.equals("LEFT")) {
                leftPressed = false;
            }
            if (action.equals("RIGHT")) {
                rightPressed = false;
            }
            if (action.equals("W")) {
                wPressed = false;
            }
            if (action.equals("S")) {
                sPressed = false;
            }
            if (action.equals("A")) {
                aPressed = false;
            }
            if (action.equals("D")) {
                dPressed = false;
            }
        }

        private String action;
    }
    private static class QuitGame implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            endgame = true;
        }
    }

    private static class StartGame implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            endgame = true;
            upPressed = false;
            downPressed = false;
            leftPressed = false;
            rightPressed = false;
            wPressed = false;
            sPressed = false;
            aPressed = false;
            dPressed = false;

            p1 = new ImageObject(p1originalX, p1originalY, p1width, p1height, 0.0);
            p2 = new ImageObject(p2originalX, p2originalY, p2width, p2height, 0.0);

            p1velocity = 0.0;
            p2velocity = 0.0;

            lastAudioStart = System.currentTimeMillis();

            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
            endgame = false;
            Thread t1 = new Thread(new Animate());
            Thread t2 = new Thread(new PlayerMover());
            Thread t5 = new Thread(new Player2Mover());
            Thread t3 = new Thread(new CollisionChecker());
            Thread t4 = new Thread(new WinChecker());
            Thread t6 = new Thread(new AudioLooper());


            t1.start();
            t2.start();
            t3.start();
            t4.start();
            t5.start();
            t6.start();
        }
    }

    private static Boolean isInside(double p1x, double p1y, double p2x1, double p2y1, double p2x2, double p2y2) {
        Boolean ret = false;
        if (p1x > p2x1 && p1x < p2x2) {
            if (p1y > p2y1 && p1y < p2y2) {
                ret = true;
            } else if (p1y > p2y2 && p1y < p2y1) {
                ret = true;
            }
        }
        if (p1x > p2x2 && p1x < p2x1) {
            if (p1y > p2y1 && p1y < p2y2) {
                ret = true;
            } else if (p1y > p2y2 && p1y < p2y1) {
                ret = true;
            }
        }
        return ret;
    }

    private static Boolean collisionOccursCoordinates(double p1x1, double p1y1, double p1x2, double p1y2,
                                                      double p2x1, double p2y1, double p2x2, double p2y2) {
        Boolean ret = false;
        if (isInside(p1x1, p1y1, p2x1, p2y1, p2x2, p2y2)) {
            ret = true;
        }
        if (isInside(p1x1, p1y2, p2x1, p2y1, p2x2, p2y2)) {
            ret = true;
        }
        if (isInside(p1x2, p1y1, p2x1, p2y1, p2x2, p2y2)) {
            ret = true;
        }
        if (isInside(p1x2, p1y2, p2x1, p2y1, p2x2, p2y2)) {
            ret = true;
        }
        if (isInside(p2x1, p2y1, p1x1, p1y1, p1x2, p1y2)) {
            ret = true;
        }
        if (isInside(p2x1, p2y2, p1x1, p1y1, p1x2, p1y2)) {
            ret = true;
        }
        if (isInside(p2x2, p2y1, p1x1, p1y1, p1x2, p1y2)) {
            ret = true;
        }
        if (isInside(p2x2, p2y2, p1x1, p1y1, p1x2, p1y2)) {
            ret = true;
        }
        return ret;
    }

    private static Boolean collisionOccurs(ImageObject obj1, ImageObject obj2) {
        Boolean ret = false;
        if (collisionOccursCoordinates(
                obj1.getX(), obj1.getY(),
                obj1.getX() + obj1.getWidth(), obj1.getY() + obj1.getHeight(),
                obj2.getX(), obj2.getY(),
                obj2.getX() + obj2.getWidth(), obj2.getY() + obj2.getHeight()
        )) {
            ret = true;
        }
//        System.out.println();
        return ret;
    }


    private static class ImageObject {
        public ImageObject() {
        }

        public ImageObject(double xinput, double yinput, double xwidthinput, double yheightinput, double angleinput) {
            x = xinput;
            y = yinput;
            xwidth = xwidthinput;
            yheight = yheightinput;
            angle = angleinput;
            internalangle = 0.0;
            coords = new Vector<Double>();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return xwidth;
        }

        public double getHeight() {
            return yheight;
        }

        public double getAngle() {
            return angle;
        }

        public double getInternalAngle() {
            return internalangle;
        }

        public void setAngle(double angleinput) {
            angle = angleinput;
        }

        public void setInternalAngle(double internalangleinput) {
            internalangle = internalangleinput;
        }

        public Vector<Double> getCoords() {
            return coords;
        }

        public void setCoords(Vector<Double> coordsinput) {
            coords = coordsinput;
            generateTriangles();
            // printTriangles();
        }

        public void generateTriangles() {
            triangles = new Vector<Double>();
            // format: (0, 1), (2, 3), (4, 5) is the (x, y) coords of a triangle.
            // get center point of all coordinates.
            comX = getComX();
            comY = getComY();

            for (int i = 0; i < coords.size(); i = i + 2) {
                triangles.addElement(coords.elementAt(i));
                triangles.addElement(coords.elementAt(i + 1));
                triangles.addElement(coords.elementAt((i + 2) % coords.size()));
                triangles.addElement(coords.elementAt((i + 3) % coords.size()));
                triangles.addElement(comX);
                triangles.addElement(comY);
            }
        }

        public double getComX() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 0; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public double getComY() {
            double ret = 0;
            if (coords.size() > 0) {
                for (int i = 1; i < coords.size(); i = i + 2) {
                    ret = ret + coords.elementAt(i);
                }
                ret = ret / (coords.size() / 2.0);
            }
            return ret;
        }

        public void move(double xinput, double yinput) {
            x = x + xinput;
            y = y + yinput;
        }

        public void moveto(double xinput, double yinput) {
            x = xinput;
            y = yinput;
        }

        public boolean screenWrap(double leftEdge, double rightEdge, double topEdge, double bottomEdge) {
            boolean wrapped = false;
            if (x > rightEdge) {
                x = rightEdge -5;
                wrapped = true;
            }
            if (x < leftEdge) {
                x = leftEdge + 5;
                wrapped = true;
            }
            if (y > bottomEdge) {
                y = bottomEdge -5;
                wrapped = true;
            }
            if (y < topEdge) {
                y = topEdge +5;
                wrapped = true;
            }
            return wrapped;
        }

        public void rotate(double angleinput) {
            angle = angle + angleinput;
            while (angle > twoPi) {
                angle = angle % twoPi;
            }
            while (angle < 0) {
                angle = angle + twoPi;
            }
        }

        public void spin(double internalangleinput) {
            internalangle = internalangle + internalangleinput;
            while (internalangle > twoPi) {
                internalangle = internalangle % twoPi;
            }
            while (internalangle < 0) {
                internalangle = internalangle + twoPi;
            }
        }

        private double x;
        private double y;
        private double xwidth;
        private double yheight;
        private double angle; // in Radians
        private double internalangle; // in Radians
        private Vector<Double> coords;
        private Vector<Double> triangles;
        private double comX;
        private double comY;
    }

    private static void bindKey(JPanel myPanel, String input) {
        myPanel.getInputMap(IFW).put(KeyStroke.getKeyStroke("pressed " + input), input + " pressed");
        myPanel.getActionMap().put(input + " pressed", new KeyPressed(input));
        myPanel.getInputMap(IFW).put(KeyStroke.getKeyStroke("released " + input), input + " released");
        myPanel.getActionMap().put(input + " released", new KeyReleased(input));
    }

    private static class LapNumbers implements ActionListener {
        public int decodeLevel(String input) {
            int ret = 3;
            if (input.equals("One")) {
                ret = 1;
            } else if (input.equals("Two")) {
                ret = 2;
            } else if (input.equals("Three")) {
                ret = 3;
            } else if (input.equals("Four")) {
                ret = 4;
            } else if (input.equals("Five")) {
                ret = 5;
            } else if (input.equals("Six")) {
                ret = 6;
            } else if (input.equals("Seven")) {
                ret = 7;
            } else if (input.equals("Eight")) {
                ret = 8;
            } else if (input.equals("Nine")) {
                ret = 9;
            } else if (input.equals("Ten")) {
                ret = 10;
            }
            return ret;
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String textLevel = (String) cb.getSelectedItem();
            numLaps = decodeLevel(textLevel);
        }
    }



    public static void main(String[] args) {
        setup();
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setSize(495, 800);

        JPanel myPanel = new JPanel();

        String[] numLaps = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"};

        JComboBox<String> levelMenu = new JComboBox<String>(numLaps);
        levelMenu.setSelectedIndex(2);
        levelMenu.addActionListener(new LapNumbers());
        myPanel.add(levelMenu);

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(new StartGame());
        myPanel.add(newGameButton);
        JButton quitButton = new JButton("Quit Game");
        quitButton.addActionListener(new QuitGame());
        myPanel.add(quitButton);
        bindKey(myPanel, "UP");
        bindKey(myPanel, "DOWN");
        bindKey(myPanel, "LEFT");
        bindKey(myPanel, "RIGHT");
        bindKey(myPanel, "W");
        bindKey(myPanel, "S");
        bindKey(myPanel, "A");
        bindKey(myPanel, "D");

        appFrame.getContentPane().add(myPanel, "South");
        appFrame.setVisible(true);
    }

    private static Boolean endgame;
    private static BufferedImage background;

    private static BufferedImage player;
    private static BufferedImage player2;
    private static Boolean upPressed;
    private static Boolean downPressed;
    private static Boolean leftPressed;
    private static Boolean rightPressed;
    private static Boolean wPressed;
    private static Boolean sPressed;
    private static Boolean aPressed;
    private static Boolean dPressed;
    private static ImageObject p1;
    private static ImageObject p2;
    private static ImageObject dirt;
    private static int numLaps;
    private static double p1width;
    private static double p1height;
    private static double p1originalX;
    private static double p1originalY;
    private static double p1velocity;
    private static double p2width;
    private static double p2height;
    private static double p2originalX;
    private static double p2originalY;
    private static double p2velocity;
    private static int XOFFSET;
    private static int YOFFSET;
    private static int WINWIDTH;
    private static int WINHEIGHT;
    private static int p1LapCount;
    private static int p2LapCount;
    private static long lastPassedStartTime;
    private static long p1BestTime;

    private static double pi;
    private static double twoPi;
    private static JFrame appFrame;
    private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
    private static Long audiolifeTime;
    private static Long lastAudioStart;
    private static Clip clip;
    private static boolean p1PassedFirst;
    private static boolean p2PassedFirst;
    private static long p2lastPassedStartTime;
    private static long p2BestTime;


}


