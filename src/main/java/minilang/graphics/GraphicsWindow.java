package minilang.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Built-in Hardware-Accelerated 2D and OpenGL-style Graphics Engine for MiniLang.
 * Safe for headless test runners (automatically no-ops if headless).
 */
public class GraphicsWindow {

    private static GraphicsWindow instance;

    public static synchronized GraphicsWindow getInstance() {
        if (instance == null) {
            instance = new GraphicsWindow();
        }
        return instance;
    }

    private JFrame frame;
    private BufferedImage buffer;
    private Graphics2D g2d;
    private int width = 800;
    private int height = 600;
    private boolean open = false;
    private final boolean headless;

    private Color currentColor = Color.WHITE;
    private String currentBeginMode = null;
    private final List<Point.Double> vertices = new ArrayList<>();

    private final Set<Integer> pressedKeys = new HashSet<>();
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean mouseDown = false;

    private long lastFrameTime = System.currentTimeMillis();

    private GraphicsWindow() {
        this.headless = GraphicsEnvironment.isHeadless();
    }

    public synchronized void openWindow(String title, int width, int height) {
        if (headless) {
            this.open = true;
            this.width = width;
            this.height = height;
            return;
        }

        if (frame != null) {
            frame.dispose();
        }

        this.width = width;
        this.height = height;
        this.buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.g2d = buffer.createGraphics();
        this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.g2d.setColor(Color.BLACK);
        this.g2d.fillRect(0, 0, width, height);

        frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (buffer != null) {
                    g.drawImage(buffer, 0, 0, null);
                }
            }
        };
        panel.setPreferredSize(new Dimension(width, height));
        frame.setContentPane(panel);
        frame.pack();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                open = false;
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDown = false;
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        frame.setVisible(true);
        this.open = true;
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized void update() {
        if (headless) {
            return;
        }

        if (frame != null && open) {
            frame.getContentPane().repaint();

            // Throttle to ~60 FPS
            long now = System.currentTimeMillis();
            long elapsed = now - lastFrameTime;
            if (elapsed < 16) {
                try {
                    Thread.sleep(16 - elapsed);
                } catch (InterruptedException ignored) {}
            }
            lastFrameTime = System.currentTimeMillis();
        }
    }

    public synchronized void clear(double r, double g, double b) {
        if (g2d == null) return;
        g2d.setColor(new Color(toComponent(r), toComponent(g), toComponent(b)));
        g2d.fillRect(0, 0, width, height);
    }

    public synchronized void color(double r, double g, double b) {
        this.currentColor = new Color(toComponent(r), toComponent(g), toComponent(b));
        if (g2d != null) {
            g2d.setColor(currentColor);
        }
    }

    public synchronized void rect(double x, double y, double w, double h) {
        if (g2d == null) return;
        g2d.setColor(currentColor);
        g2d.drawRect((int) x, (int) y, (int) w, (int) h);
    }

    public synchronized void fillRect(double x, double y, double w, double h) {
        if (g2d == null) return;
        g2d.setColor(currentColor);
        g2d.fillRect((int) x, (int) y, (int) w, (int) h);
    }

    public synchronized void circle(double x, double y, double r) {
        if (g2d == null) return;
        g2d.setColor(currentColor);
        g2d.drawOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
    }

    public synchronized void fillCircle(double x, double y, double r) {
        if (g2d == null) return;
        g2d.setColor(currentColor);
        g2d.fillOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
    }

    public synchronized void line(double x1, double y1, double x2, double y2) {
        if (g2d == null) return;
        g2d.setColor(currentColor);
        g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    public synchronized void text(String msg, double x, double y, int size) {
        if (g2d == null) return;
        g2d.setColor(currentColor);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        g2d.drawString(msg, (int) x, (int) y);
    }

    // --- OpenGL Immediate-Mode Emulation ---

    public synchronized void glBegin(String mode) {
        this.currentBeginMode = mode.toLowerCase();
        this.vertices.clear();
    }

    public synchronized void glVertex(double x, double y) {
        this.vertices.add(new Point.Double(x, y));
    }

    public synchronized void glEnd() {
        if (g2d == null || currentBeginMode == null || vertices.isEmpty()) return;

        g2d.setColor(currentColor);
        if (currentBeginMode.equals("lines")) {
            for (int i = 0; i < vertices.size() - 1; i += 2) {
                Point.Double p1 = vertices.get(i);
                Point.Double p2 = vertices.get(i + 1);
                g2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        } else if (currentBeginMode.equals("triangles") || currentBeginMode.equals("polygon") || currentBeginMode.equals("quads")) {
            int n = vertices.size();
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = (int) vertices.get(i).x;
                ys[i] = (int) vertices.get(i).y;
            }
            g2d.fillPolygon(xs, ys, n);
        } else if (currentBeginMode.equals("points")) {
            for (Point.Double p : vertices) {
                g2d.fillRect((int) p.x, (int) p.y, 2, 2);
            }
        }
        vertices.clear();
        currentBeginMode = null;
    }

    public synchronized boolean isKeyDown(String keyName) {
        int keyCode = parseKeyCode(keyName);
        return pressedKeys.contains(keyCode);
    }

    public synchronized int getMouseX() {
        return mouseX;
    }

    public synchronized int getMouseY() {
        return mouseY;
    }

    public synchronized boolean isMouseDown() {
        return mouseDown;
    }

    public synchronized void close() {
        open = false;
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
    }

    private int toComponent(double val) {
        if (val <= 1.0 && val > 0.0) {
            return (int) Math.round(val * 255.0);
        }
        return Math.max(0, Math.min(255, (int) Math.round(val)));
    }

    private int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    private int parseKeyCode(String key) {
        String k = key.toUpperCase().trim();
        if (k.length() == 1) {
            return (int) k.charAt(0);
        }
        return switch (k) {
            case "SPACE" -> KeyEvent.VK_SPACE;
            case "LEFT" -> KeyEvent.VK_LEFT;
            case "RIGHT" -> KeyEvent.VK_RIGHT;
            case "UP" -> KeyEvent.VK_UP;
            case "DOWN" -> KeyEvent.VK_DOWN;
            case "ENTER" -> KeyEvent.VK_ENTER;
            case "ESCAPE", "ESC" -> KeyEvent.VK_ESCAPE;
            default -> 0;
        };
    }
}
