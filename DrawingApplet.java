import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DrawingApplet extends JApplet {
    private int currentShape = 0;  // 0 - Line, 1 - Rectangle, 2 - Oval, 3 - Circle, 4 - Brush, 5 - Obtuse Triangle
    private Color currentColor = Color.BLACK;  // Initial color
    private int lineThickness = 1;  // Initial line thickness
    private boolean eraseMode = false;  // Initially, erase mode is off
    private boolean fillShapes = false;  // Initially, fill mode is off

    private ArrayList<ArrayList<Shape>> drawings = new ArrayList<>(); // Store all drawings
    private ArrayList<Shape> currentDrawing = new ArrayList<>(); // Current drawing

    public void init() {
        setLayout(new BorderLayout());

        // Canvas
        Canvas canvas = new Canvas() {
            Point startDrag, endDrag;
            Shape currentShapeObj;

            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(lineThickness));

                for (ArrayList<Shape> drawing : drawings) {
                    for (Shape shape : drawing) {
                        g2.setColor(shape.getColor());
                        if (shape.isFill())
                            g2.fill(shape.getShape());
                        g2.draw(shape.getShape());
                    }
                }

                for (Shape shape : currentDrawing) {
                    g2.setColor(shape.getColor());
                    if (shape.isFill())
                        g2.fill(shape.getShape());
                    g2.draw(shape.getShape());
                }
            }

            {
                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        startDrag = new Point(e.getX(), e.getY());
                        endDrag = startDrag;
                        if (currentShape == 4) { // Brush
                            currentShapeObj = new Shape(new java.awt.geom.Line2D.Float(startDrag, endDrag), currentColor, false);
                            currentDrawing.add(currentShapeObj);
                        }
                        repaint();
                    }

                    public void mouseReleased(MouseEvent e) {
                        currentDrawing.add(currentShapeObj);
                        currentShapeObj = null;
                        repaint();
                    }
                });

                addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        endDrag = new Point(e.getX(), e.getY());

                        switch (currentShape) {
                            case 0: // Line
                                currentShapeObj = new Shape(new java.awt.geom.Line2D.Float(startDrag, endDrag), currentColor, false);
                                break;
                            case 1: // Rectangle
                                currentShapeObj = new Shape(new Rectangle(Math.min(startDrag.x, endDrag.x), Math.min(startDrag.y, endDrag.y),
                                        Math.abs(startDrag.x - endDrag.x), Math.abs(startDrag.y - endDrag.y)), currentColor, fillShapes);
                                break;
                            case 2: // Oval
                                currentShapeObj = new Shape(new java.awt.geom.Ellipse2D.Float(Math.min(startDrag.x, endDrag.x), Math.min(startDrag.y, endDrag.y),
                                        Math.abs(startDrag.x - endDrag.x), Math.abs(startDrag.y - endDrag.y)), currentColor, fillShapes);
                                break;
                            case 3: // Circle
                                int diameter = Math.max(Math.abs(startDrag.x - endDrag.x), Math.abs(startDrag.y - endDrag.y));
                                currentShapeObj = new Shape(new java.awt.geom.Ellipse2D.Float(Math.min(startDrag.x, endDrag.x), Math.min(startDrag.y, endDrag.y),
                                        diameter, diameter), currentColor, fillShapes);
                                break;
                            case 4: // Brush
                                currentShapeObj = new Shape(new java.awt.geom.Line2D.Float(startDrag, endDrag), currentColor, false);
                                currentDrawing.add(currentShapeObj);
                                startDrag = endDrag;
                                break;
                            case 5: // Obtuse Triangle
                                Path2D.Float trianglePath = new Path2D.Float();
                                trianglePath.moveTo(startDrag.x, startDrag.y);
                                trianglePath.lineTo(endDrag.x, endDrag.y);
                                trianglePath.lineTo(startDrag.x, endDrag.y);
                                trianglePath.closePath();
                                currentShapeObj = new Shape(trianglePath, currentColor, fillShapes);
                                break;
                        }
                        repaint();
                    }
                });
            }
        };
        add(canvas, BorderLayout.CENTER);

        // ToolBar
        JToolBar toolBar = new JToolBar();
        JButton lineButton = new JButton("Line");
        lineButton.addActionListener(e -> currentShape = 0);
        toolBar.add(lineButton);
        JButton rectButton = new JButton("Rect");
        rectButton.addActionListener(e -> currentShape = 1);
        toolBar.add(rectButton);
        JButton ovalButton = new JButton("Oval");
        ovalButton.addActionListener(e -> currentShape = 2);
        toolBar.add(ovalButton);
        JButton circleButton = new JButton("Circle");
        circleButton.addActionListener(e -> currentShape = 3);
        toolBar.add(circleButton);
        JButton brushButton = new JButton("Brush");
        brushButton.addActionListener(e -> currentShape = 4);
        toolBar.add(brushButton);
        JButton triangleButton = new JButton("Triangle");
        triangleButton.addActionListener(e -> currentShape = 5);
        toolBar.add(triangleButton);

        JButton fillButton = new JButton("Fill");
        fillButton.addActionListener(e -> fillShapes = !fillShapes);
        toolBar.add(fillButton);

        JButton blackButton = new JButton("Black");
        blackButton.addActionListener(e -> currentColor = Color.BLACK);
        toolBar.add(blackButton);
        JButton blueButton = new JButton("Blue");
        blueButton.addActionListener(e -> currentColor = Color.BLUE);
        toolBar.add(blueButton);
        JButton redButton = new JButton("Red");
        redButton.addActionListener(e -> currentColor = Color.RED);
        toolBar.add(redButton);

        JButton eraseButton = new JButton("Erase");
        eraseButton.addActionListener(e -> {
            currentDrawing.clear();  // Remove current drawing
            canvas.repaint();  // Clear the canvas
        });
        toolBar.add(eraseButton);

        JButton saveButton = new JButton("Save Image");
        saveButton.addActionListener(e -> saveImage(canvas));
        toolBar.add(saveButton);

        JButton newDrawingButton = new JButton("New Drawing");
        newDrawingButton.addActionListener(e -> {
            if (!currentDrawing.isEmpty()) {
                drawings.add(currentDrawing);
                currentDrawing = new ArrayList<>();
            }
        });
        toolBar.add(newDrawingButton);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
        slider.addChangeListener(e -> lineThickness = slider.getValue());
        toolBar.add(slider);

        add(toolBar, BorderLayout.NORTH);
    }

    private void saveImage(Component component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        component.paint(g2d);
        g2d.dispose();

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(image, "png", file);
                JOptionPane.showMessageDialog(this, "Image saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + e.getMessage());
            }
        }
    }
}

class Shape {
    private final java.awt.Shape shape;
    private final Color color;
    private final boolean fill;

    public Shape(java.awt.Shape shape, Color color, boolean fill) {
        this.shape = shape;
        this.color = color;
        this.fill = fill;
    }

    public java.awt.Shape getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public boolean isFill() {
        return fill;
    }
}