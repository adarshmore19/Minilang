package minilang.plot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Matplotlib-style 2D Plotting and Charting Engine for MiniLang.
 * Supports Line plots, Scatter plots, Bar charts, auto-scaling, axes,
 * grid lines, legends, and saving to PNG.
 */
public class PlotEngine {

    public static class Series {
        public final String type; // "line", "scatter", "bar"
        public final List<Double> xData;
        public final List<Double> yData;
        public final List<String> categories;
        public final String label;
        public final Color color;

        public Series(String type, List<Double> xData, List<Double> yData, List<String> categories, String label, Color color) {
            this.type = type;
            this.xData = xData;
            this.yData = yData;
            this.categories = categories;
            this.label = label;
            this.color = color;
        }
    }

    private static PlotEngine instance;

    private final List<Series> seriesList = new ArrayList<>();
    private String title = "MiniLang Plot";
    private String xLabel = "X";
    private String yLabel = "Y";
    private boolean showGrid = true;
    private JFrame window;
    private final boolean headless;

    private final Color[] PALETTE = new Color[]{
            new Color(41, 128, 185),  // Blue
            new Color(231, 76, 60),   // Red
            new Color(39, 174, 96),   // Green
            new Color(142, 68, 173),  // Purple
            new Color(243, 156, 18),  // Orange
            new Color(22, 160, 133)   // Teal
    };
    private int colorIdx = 0;

    public static synchronized PlotEngine getInstance() {
        if (instance == null) {
            instance = new PlotEngine();
        }
        return instance;
    }

    private PlotEngine() {
        this.headless = GraphicsEnvironment.isHeadless();
    }

    public synchronized void setTitle(String title) {
        this.title = title;
    }

    public synchronized void setXLabel(String label) {
        this.xLabel = label;
    }

    public synchronized void setYLabel(String label) {
        this.yLabel = label;
    }

    public synchronized void setGrid(boolean grid) {
        this.showGrid = grid;
    }

    public synchronized void clear() {
        seriesList.clear();
        colorIdx = 0;
        title = "MiniLang Plot";
        xLabel = "X";
        yLabel = "Y";
        showGrid = true;
    }

    private Color nextColor() {
        Color c = PALETTE[colorIdx % PALETTE.length];
        colorIdx++;
        return c;
    }

    public synchronized void addLine(List<Double> xData, List<Double> yData, String label, Color color) {
        seriesList.add(new Series("line", xData, yData, null, label, color != null ? color : nextColor()));
    }

    public synchronized void addScatter(List<Double> xData, List<Double> yData, String label, Color color) {
        seriesList.add(new Series("scatter", xData, yData, null, label, color != null ? color : nextColor()));
    }

    public synchronized void addBar(List<String> categories, List<Double> values, String label, Color color) {
        seriesList.add(new Series("bar", null, values, categories, label, color != null ? color : nextColor()));
    }

    public BufferedImage renderImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setColor(new Color(248, 249, 250));
        g2.fillRect(0, 0, width, height);

        int padLeft = 70;
        int padRight = 40;
        int padTop = 60;
        int padBottom = 60;

        int plotW = width - padLeft - padRight;
        int plotH = height - padTop - padBottom;

        // Plot Area background
        g2.setColor(Color.WHITE);
        g2.fillRect(padLeft, padTop, plotW, plotH);
        g2.setColor(new Color(220, 224, 230));
        g2.drawRect(padLeft, padTop, plotW, plotH);

        // Determine Min and Max
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        boolean hasNumericX = false;
        boolean hasBar = false;
        int maxCategories = 0;

        for (Series s : seriesList) {
            if ("bar".equals(s.type)) {
                hasBar = true;
                if (s.categories != null) {
                    maxCategories = Math.max(maxCategories, s.categories.size());
                }
                for (double y : s.yData) {
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            } else {
                hasNumericX = true;
                for (int i = 0; i < s.xData.size(); i++) {
                    double x = s.xData.get(i);
                    double y = s.yData.get(i);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (minY == Double.MAX_VALUE) { minY = 0; maxY = 10; }
        if (minX == Double.MAX_VALUE) { minX = 0; maxX = 10; }
        if (minY == maxY) { minY -= 1; maxY += 1; }
        if (minX == maxX) { minX -= 1; maxX += 1; }
        if (hasBar && minY > 0) minY = 0; // Bar charts typically start at 0

        // Draw Title
        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (width - fm.stringWidth(title)) / 2, padTop - 25);

        // Draw Axis Labels
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fmAxis = g2.getFontMetrics();
        g2.drawString(xLabel, padLeft + (plotW - fmAxis.stringWidth(xLabel)) / 2, height - 15);

        // Draw Grid and Y-Ticks
        int yTicks = 6;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i <= yTicks; i++) {
            double val = minY + (maxY - minY) * (i / (double) yTicks);
            int py = padTop + plotH - (int) ((val - minY) / (maxY - minY) * plotH);

            if (showGrid) {
                g2.setColor(new Color(238, 238, 238));
                g2.drawLine(padLeft, py, padLeft + plotW, py);
            }

            g2.setColor(new Color(127, 140, 141));
            g2.drawLine(padLeft - 5, py, padLeft, py);
            String strVal = String.format(Math.abs(val) < 10 ? "%.2f" : "%.0f", val);
            g2.drawString(strVal, padLeft - g2.getFontMetrics().stringWidth(strVal) - 8, py + 4);
        }

        // Draw Series Data
        if (hasBar) {
            for (Series s : seriesList) {
                if ("bar".equals(s.type)) {
                    int n = s.yData.size();
                    int barWidth = Math.max(4, (plotW / Math.max(1, n)) - 10);
                    for (int i = 0; i < n; i++) {
                        double yVal = s.yData.get(i);
                        int bx = padLeft + (i * plotW / n) + (plotW / n - barWidth) / 2;
                        int by = padTop + plotH - (int) ((yVal - minY) / (maxY - minY) * plotH);
                        int bh = (padTop + plotH) - by;

                        g2.setColor(s.color);
                        g2.fillRect(bx, by, barWidth, bh);
                        g2.setColor(s.color.darker());
                        g2.drawRect(bx, by, barWidth, bh);

                        // Category label
                        if (s.categories != null && i < s.categories.size()) {
                            g2.setColor(new Color(80, 80, 80));
                            String cat = s.categories.get(i);
                            int catW = g2.getFontMetrics().stringWidth(cat);
                            g2.drawString(cat, bx + (barWidth - catW) / 2, padTop + plotH + 18);
                        }
                    }
                }
            }
        }

        if (hasNumericX) {
            // Draw X-Ticks
            int xTicks = 6;
            for (int i = 0; i <= xTicks; i++) {
                double val = minX + (maxX - minX) * (i / (double) xTicks);
                int px = padLeft + (int) ((val - minX) / (maxX - minX) * plotW);

                if (showGrid) {
                    g2.setColor(new Color(238, 238, 238));
                    g2.drawLine(px, padTop, px, padTop + plotH);
                }

                g2.setColor(new Color(127, 140, 141));
                g2.drawLine(px, padTop + plotH, px, padTop + plotH + 5);
                String strVal = String.format(Math.abs(val) < 10 ? "%.2f" : "%.0f", val);
                g2.drawString(strVal, px - g2.getFontMetrics().stringWidth(strVal) / 2, padTop + plotH + 18);
            }

            for (Series s : seriesList) {
                if ("line".equals(s.type)) {
                    g2.setColor(s.color);
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D path = new Path2D.Double();
                    for (int i = 0; i < s.xData.size(); i++) {
                        double x = s.xData.get(i);
                        double y = s.yData.get(i);
                        double px = padLeft + ((x - minX) / (maxX - minX)) * plotW;
                        double py = padTop + plotH - ((y - minY) / (maxY - minY)) * plotH;
                        if (i == 0) path.moveTo(px, py);
                        else path.lineTo(px, py);
                    }
                    g2.draw(path);
                } else if ("scatter".equals(s.type)) {
                    g2.setColor(s.color);
                    for (int i = 0; i < s.xData.size(); i++) {
                        double x = s.xData.get(i);
                        double y = s.yData.get(i);
                        int px = padLeft + (int) (((x - minX) / (maxX - minX)) * plotW);
                        int py = padTop + plotH - (int) (((y - minY) / (maxY - minY)) * plotH);
                        g2.fillOval(px - 4, py - 4, 8, 8);
                    }
                }
            }
        }

        // Draw Legends
        int legX = padLeft + 15;
        int legY = padTop + 20;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Series s : seriesList) {
            if (s.label != null && !s.label.isEmpty()) {
                g2.setColor(s.color);
                g2.fillRect(legX, legY - 8, 12, 10);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(s.label, legX + 18, legY);
                legX += g2.getFontMetrics().stringWidth(s.label) + 30;
            }
        }

        g2.dispose();
        return image;
    }

    public synchronized void show(int width, int height) {
        if (headless) {
            System.out.println("[Plot] Display skipped in headless mode. Plot contains " + seriesList.size() + " series.");
            return;
        }

        BufferedImage img = renderImage(width, height);
        if (window != null) {
            window.dispose();
        }

        window = new JFrame(title);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel label = new JLabel(new ImageIcon(img));
        window.getContentPane().add(label);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public synchronized boolean save(String filePath, int width, int height) {
        try {
            BufferedImage img = renderImage(width, height);
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            ImageIO.write(img, "png", file);
            return true;
        } catch (IOException e) {
            System.err.println("[Plot Error] Failed to save plot: " + e.getMessage());
            return false;
        }
    }
}
