package minilang.turtle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Kid-friendly interactive Turtle Graphics engine for visual learning.
 */
public class TurtleEngine {

    private static TurtleEngine instance;

    private int width = 600;
    private int height = 600;
    private double x;
    private double y;
    private double angle = -90.0; // Pointing upwards (standard turtle orientation)
    private boolean penDown = true;
    private Color penColor = new Color(52, 152, 219); // Nice blue
    private int penSize = 2;
    private int delayMs = 5;

    private BufferedImage canvas;
    private Graphics2D g2;
    private JFrame window;
    private JPanel panel;
    private final boolean headless;

    public static synchronized TurtleEngine getInstance() {
        if (instance == null) {
            instance = new TurtleEngine();
        }
        return instance;
    }

    private TurtleEngine() {
        this.headless = GraphicsEnvironment.isHeadless();
        initCanvas(600, 600, "MiniLang Turtle Graphics");
    }

    public synchronized void initCanvas(int w, int h, String title) {
        this.width = w;
        this.height = h;
        this.x = w / 2.0;
        this.y = h / 2.0;
        this.angle = -90.0;
        this.penDown = true;

        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2 = canvas.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        if (!headless) {
            if (window == null) {
                window = new JFrame(title);
                window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        if (canvas != null) {
                            g.drawImage(canvas, 0, 0, null);
                        }
                        // Draw turtle indicator
                        Graphics2D g2p = (Graphics2D) g;
                        g2p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2p.setColor(new Color(46, 204, 113));
                        g2p.fillOval((int) x - 6, (int) y - 6, 12, 12);
                    }
                };
                panel.setPreferredSize(new Dimension(width, height));
                window.getContentPane().add(panel);
                window.pack();
                window.setLocationRelativeTo(null);
                window.setVisible(true);
            } else {
                window.setTitle(title);
                panel.setPreferredSize(new Dimension(width, height));
                window.pack();
            }
        }
    }

    public synchronized void forward(double dist) {
        double rad = Math.toRadians(angle);
        double newX = x + dist * Math.cos(rad);
        double newY = y + dist * Math.sin(rad);

        if (penDown && g2 != null) {
            g2.setColor(penColor);
            g2.setStroke(new BasicStroke(penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(x, y, newX, newY));
        }

        this.x = newX;
        this.y = newY;
        update();
    }

    public synchronized void backward(double dist) {
        forward(-dist);
    }

    public synchronized void turnRight(double deg) {
        this.angle = (angle + deg) % 360.0;
    }

    public synchronized void turnLeft(double deg) {
        this.angle = (angle - deg) % 360.0;
    }

    public synchronized void penUp() {
        this.penDown = false;
    }

    public synchronized void penDown() {
        this.penDown = true;
    }

    public synchronized void setPenColor(int r, int g, int b) {
        this.penColor = new Color(clamp(r), clamp(g), clamp(b));
    }

    public synchronized void setPenSize(int size) {
        this.penSize = Math.max(1, size);
    }

    public synchronized void circle(double radius) {
        // Approximate a circle with 36 small steps
        double step = (2.0 * Math.PI * radius) / 36.0;
        double angleStep = 360.0 / 36.0;
        for (int i = 0; i < 36; i++) {
            forward(step);
            turnRight(angleStep);
        }
    }

    public synchronized void clear() {
        if (g2 != null) {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);
        }
        this.x = width / 2.0;
        this.y = height / 2.0;
        this.angle = -90.0;
        update();
    }

    public synchronized void setSpeed(int delayMs) {
        this.delayMs = Math.max(0, delayMs);
    }

    public synchronized void update() {
        if (!headless && panel != null) {
            panel.repaint();
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public synchronized boolean save(String filePath) {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            ImageIO.write(canvas, "png", file);
            return true;
        } catch (IOException e) {
            System.err.println("[Turtle Error] Failed to save canvas: " + e.getMessage());
            return false;
        }
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
