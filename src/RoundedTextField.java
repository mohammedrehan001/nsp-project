import javax.swing.*;
import java.awt.*;

/**
 * Custom modern rounded text field for Swing UI.
 * College Project: Medical Laboratory Management System
 * Developed by: Mohammad Rehan
 */
public class RoundedTextField extends JTextField {
    private static final long serialVersionUID = 1L;
    private int radius;

    public RoundedTextField(int radius) {
        super();
        this.radius = radius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    public RoundedTextField(int columns, int radius) {
        super(columns);
        this.radius = radius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, radius, radius);
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(218, 225, 231));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
        g2.dispose();
    }
}
