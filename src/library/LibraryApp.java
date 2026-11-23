package library;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import static java.awt.RenderingHints.*;

/**
 * Animated Swing front-end for the Library Book Management System.
 * All animation is time-based and driven by one 60 fps repaint timer.
 * @author OEL Group
 * Main window of the Athenaeum library management system.
 */

public class LibraryApp extends JFrame {
    static final Color BG1 = new Color(0x0B0F24), BG2 = new Color(0x1B1446), ACCENT = new Color(0x7C6CFF),
            TEAL = new Color(0x2DD4BF), TEXT = new Color(0xEEF0FF), MUTED = new Color(0x9AA0CC), RED = new Color(0xFB7185);
    static final Font BASE = new Font("Segoe UI", Font.PLAIN, 14);
    static long T0 = System.nanoTime();

    static double ease(double p) { p = Math.max(0, Math.min(1, p)); return 1 - Math.pow(1 - p, 3); }
    static float since(double delayMs, double durMs) { return (float) ease(((System.nanoTime() - T0) / 1e6 - delayMs) / durMs); }
    static float approach(float c, float t, float k) { float d = t - c; return Math.abs(d) < 0.002f ? t : c + d * k; }
    static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a))); }
    static Color mix(Color a, Color b, float t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t), (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }
        static Graphics2D g2(Graphics src) {
        Graphics2D g = (Graphics2D) src.create();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }
    static Color col(BookStatus s) {
        switch (s) { case AVAILABLE: return new Color(0x34D399); case ISSUED: return RED; default: return new Color(0xFBBF24); }
    }
    static int baseline(FontMetrics fm, int h) { return (h + fm.getAscent() - fm.getDescent()) / 2; }

    /** Animated gradient backdrop: drifting colour orbs and rising particles. */
    static class Backdrop extends JPanel {
        final float[][] dots = new float[16][];
        Backdrop() {
            Random r = new Random(7);
            for (int i = 0; i < dots.length; i++) dots[i] = new float[]{r.nextFloat(), r.nextFloat(), 2 + r.nextFloat() * 4, .02f + r.nextFloat() * .04f, r.nextFloat() * 6};
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = g2(g0); int w = getWidth(), h = getHeight(); double t = (System.nanoTime() - T0) / 1e9;
            g.setPaint(new GradientPaint(0, 0, BG1, w, h, BG2)); g.fillRect(0, 0, w, h);
            Color[] cs = {ACCENT, TEAL, new Color(0xF472B6)};
            for (int i = 0; i < 3; i++) {
                float cx = (float) (w * (0.5 + 0.4 * Math.sin(t * 0.25 + i * 2.1))), cy = (float) (h * (0.5 + 0.4 * Math.cos(t * 0.21 + i * 1.7)));
                g.setPaint(new RadialGradientPaint(cx, cy, Math.max(w, h) * 0.45f, new float[]{0, 1}, new Color[]{alpha(cs[i], 65), alpha(cs[i], 0)}));
                g.fillRect(0, 0, w, h);
            }
            for (float[] d : dots) {
                double y = ((d[1] - t * d[3]) % 1 + 1) % 1, x = d[0] + Math.sin(t * .5 + d[4]) * .02;
                g.setColor(new Color(255, 255, 255, (int) (50 * Math.sin(y * Math.PI))));
                g.fill(new Ellipse2D.Double(x * w, y * h, d[2], d[2]));
            }
            g.dispose();
        }
    }

    /** Glass card that slides and fades in after a delay. */
    static class Card extends JPanel {
        final double delay;
        Card(LayoutManager lm, double delay) { super(lm); this.delay = delay; setOpaque(false); }
        @Override public void paint(Graphics g0) {
            float p = since(delay, 700); Graphics2D g = (Graphics2D) g0.create();
            g.translate(0, (1 - p) * 30); g.setComposite(AlphaComposite.SrcOver.derive(p)); super.paint(g); g.dispose();
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = g2(g0); RoundRectangle2D r = new RoundRectangle2D.Float(.5f, .5f, getWidth() - 1, getHeight() - 1, 24, 24);
            g.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 28), 0, getHeight(), new Color(255, 255, 255, 8))); g.fill(r);
            g.setColor(new Color(255, 255, 255, 40)); g.draw(r); g.dispose();
        }
    }

    /** Statistic card with a count-up number. */
    static class Stat extends Card {
        final String label; final Color color; float shown; int target;
        Stat(String label, Color color, double delay) { super(null, delay); this.label = label; this.color = color; setPreferredSize(new Dimension(180, 88)); }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0); Graphics2D g = g2(g0); shown = approach(shown, target, .1f);
            g.setPaint(new GradientPaint(0, 20, color, 0, 68, alpha(color, 40))); g.fillRoundRect(18, 20, 4, 48, 4, 4);
            g.setFont(BASE.deriveFont(12f)); g.setColor(MUTED); g.drawString(label.toUpperCase(), 34, 34);
            g.setFont(BASE.deriveFont(Font.BOLD, 34f)); g.setColor(TEXT); g.drawString(String.valueOf(Math.round(shown)), 34, 70);
            g.dispose();
        }
    }

    /** Gradient button with hover glow, light sweep and press-squash. */
    static class Btn extends JButton {
        float hover, press; boolean over, down; final Color c1, c2;
        Btn(String t, Color c1, Color c2) {
            super(t); this.c1 = c1; this.c2 = c2;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setFont(BASE.deriveFont(Font.BOLD, 13f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setPreferredSize(new Dimension(120, 46));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { over = true; }
                public void mouseExited(MouseEvent e) { over = false; }
                public void mousePressed(MouseEvent e) { down = true; }
                public void mouseReleased(MouseEvent e) { down = false; }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            hover = approach(hover, over ? 1 : 0, .2f); press = approach(press, down ? 1 : 0, .35f);
            Graphics2D g = g2(g0); int w = getWidth(), h = getHeight(); float s = 1 - .05f * press + .03f * hover;
            g.translate(w / 2.0, h / 2.0); g.scale(s, s); g.translate(-w / 2.0, -h / 2.0);
            for (int i = 4; i >= 1; i--) { g.setColor(alpha(c1, (int) (hover * 14))); g.fill(new RoundRectangle2D.Float(6 - i * 1.5f, 6 - i * 1.5f, w - 12 + i * 3, h - 12 + i * 3, 18 + i * 2, 18 + i * 2)); }
            RoundRectangle2D r = new RoundRectangle2D.Float(6, 6, w - 12, h - 12, 16, 16);
            g.setPaint(new GradientPaint(0, 0, c1, w, h, c2)); g.fill(r);
            g.setClip(r); double x = (((System.nanoTime() - T0) / 1e9 * .9) % 1) * (w + 60) - 40;
            g.setColor(new Color(255, 255, 255, (int) (45 * hover))); g.fill(new Parallelogram(x, h)); g.setClip(null);
            g.setFont(getFont()); g.setColor(Color.WHITE); FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (w - fm.stringWidth(getText())) / 2, baseline(fm, h)); g.dispose();
        }
    }
    static class Parallelogram extends Path2D.Double {
        Parallelogram(double x, int h) { moveTo(x + 14, 0); lineTo(x + 34, 0); lineTo(x + 20, h); lineTo(x, h); closePath(); }
    }

    /** Text field with animated focus glow, placeholder and shake on error. */
    static class Field extends JTextField {
        float focus; long shakeAt = -1; final String hint;
        Field(String hint) {
            this.hint = hint; setOpaque(false); setForeground(TEXT); setCaretColor(TEAL); setFont(BASE.deriveFont(15f));
            setBorder(new EmptyBorder(0, 14, 0, 14)); setPreferredSize(new Dimension(100, 44)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }
        void shake() { shakeAt = System.nanoTime(); }
        double ms() { return shakeAt < 0 ? 1e9 : (System.nanoTime() - shakeAt) / 1e6; }
        @Override public void paint(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); double m = ms();
            if (m < 450) g.translate(Math.sin(m / 22) * 9 * (1 - m / 450), 0); super.paint(g); g.dispose();
        }
        @Override protected void paintComponent(Graphics g0) {
            focus = approach(focus, hasFocus() ? 1 : 0, .18f); float err = (float) Math.max(0, 1 - ms() / 1000);
            Graphics2D g = g2(g0); int w = getWidth(), h = getHeight();
            RoundRectangle2D r = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14);
            g.setColor(new Color(0, 0, 0, 70)); g.fill(r);
            Color edge = mix(mix(new Color(255, 255, 255, 40), TEAL, focus), RED, err);
            g.setStroke(new BasicStroke(1f + focus * .8f)); g.setColor(edge); g.draw(r);
            g.setColor(alpha(mix(TEAL, RED, err), (int) (focus * 90 + err * 90))); g.fillRoundRect(14, h - 3, (int) ((w - 28) * Math.max(focus, err)), 2, 2, 2);
            super.paintComponent(g);
            if (getText().isEmpty()) {
                g.setFont(getFont()); g.setColor(alpha(MUTED, (int) (170 - 90 * focus))); g.drawString(hint, 14, baseline(g.getFontMetrics(), h));
            }
            g.dispose();
        }
    }

    /** Sliding three-way status selector. */
    static class StatusPicker extends JComponent {
        BookStatus sel = BookStatus.AVAILABLE; float pos;
        StatusPicker() {
            setPreferredSize(new Dimension(100, 44)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { sel = BookStatus.values()[Math.min(2, e.getX() * 3 / Math.max(1, getWidth()))]; } });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = g2(g0); int w = getWidth(), h = getHeight(), cw = w / 3; pos = approach(pos, sel.ordinal(), .2f);
            g.setColor(new Color(0, 0, 0, 70)); g.fillRoundRect(1, 1, w - 2, h - 2, 14, 14);
            Color c = col(BookStatus.values()[Math.round(pos)]);
            g.setColor(alpha(c, 55)); g.fillRoundRect((int) (pos * cw) + 4, 4, cw - 8, h - 8, 11, 11);
            g.setColor(alpha(c, 200)); g.drawRoundRect((int) (pos * cw) + 4, 4, cw - 8, h - 8, 11, 11);
            g.setFont(BASE.deriveFont(Font.BOLD, 12f)); FontMetrics fm = g.getFontMetrics();
            for (BookStatus s : BookStatus.values()) {
                g.setColor(s == sel ? Color.WHITE : MUTED); String t = s.getLabel();
                g.drawString(t, s.ordinal() * cw + (cw - fm.stringWidth(t)) / 2, baseline(fm, h));
            }
            g.dispose();
        }
    }

    /** Custom-painted book list: staggered slide-in, hover glow, pulsing status pills. */
    static class BookList extends JComponent {
        static final int ROW = 66;
        List<Book> rows = new ArrayList<>(); float[] hov = new float[0]; int hoverIdx = -1; String selectedId, emptyMsg = "";
        final Map<String, Long> appear = new HashMap<>(); Consumer<Book> onSelect = b -> { };
        BookList() {
            addMouseMotionListener(new MouseMotionAdapter() { public void mouseMoved(MouseEvent e) { hoverIdx = idx(e.getY()); setCursor(Cursor.getPredefinedCursor(hoverIdx >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR)); } });
            addMouseListener(new MouseAdapter() {
                public void mouseExited(MouseEvent e) { hoverIdx = -1; }
                public void mousePressed(MouseEvent e) { int i = idx(e.getY()); if (i >= 0) { selectedId = rows.get(i).getId(); onSelect.accept(rows.get(i)); } }
            });
        }
        int idx(int y) { int i = y / ROW; return i >= 0 && i < rows.size() ? i : -1; }
        void setBooks(List<Book> b, String msg) {
            rows = b; emptyMsg = msg; hov = Arrays.copyOf(hov, b.size()); long now = System.nanoTime(); int n = 0;
            for (Book k : b) if (!appear.containsKey(k.getId())) appear.put(k.getId(), now + (n++) * 70_000_000L + 1_500_000_000L * (System.nanoTime() - T0 < 3e9 ? 1 : 0) / 1);
            setPreferredSize(new Dimension(100, Math.max(1, b.size()) * ROW)); revalidate();
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = g2(g0); int w = getWidth(); long now = System.nanoTime(); double t = (now - T0) / 1e9;
            if (rows.isEmpty()) {
                int cy = getParent() == null ? 120 : getParent().getHeight() / 2 + (int) (Math.sin(t * 2) * 6);
                g.setColor(alpha(ACCENT, 60)); g.fillRoundRect(w / 2 - 26, cy - 70, 52, 62, 10, 10);
                g.setColor(alpha(TEAL, 160)); g.fillRoundRect(w / 2 - 18, cy - 62, 6, 46, 3, 3);
                g.setFont(BASE.deriveFont(Font.BOLD, 17f)); g.setColor(TEXT); FontMetrics fm = g.getFontMetrics();
                g.drawString(emptyMsg, (w - fm.stringWidth(emptyMsg)) / 2, cy + 18); g.dispose(); return;
            }
            for (int i = 0; i < rows.size(); i++) {
                Book b = rows.get(i); hov[i] = approach(hov[i], i == hoverIdx ? 1 : 0, .2f);
                float p = (float) ease((now - appear.get(b.getId())) / 1e6 / 520.0);
                Graphics2D r = (Graphics2D) g.create(); r.translate((1 - p) * 70, i * ROW); r.setComposite(AlphaComposite.SrcOver.derive(p));
                boolean sel = b.getId().equals(selectedId);
                RoundRectangle2D rr = new RoundRectangle2D.Float(4, 4, w - 8, ROW - 8, 18, 18);
                r.setColor(sel ? alpha(ACCENT, 70) : new Color(255, 255, 255, (int) (6 + hov[i] * 22))); r.fill(rr);
                if (sel || hov[i] > .01f) { r.setColor(sel ? ACCENT : new Color(255, 255, 255, (int) (hov[i] * 50))); r.draw(rr); }
                r.setPaint(new GradientPaint(18, 14, ACCENT, 58, 54, TEAL)); r.fill(new Ellipse2D.Float(18, ROW / 2f - 20, 40, 40));
                r.setFont(BASE.deriveFont(Font.BOLD, 17f)); r.setColor(Color.WHITE); String ini = b.getTitle().substring(0, 1).toUpperCase();
                r.drawString(ini, 38 - r.getFontMetrics().stringWidth(ini) / 2f, ROW / 2f + 6);
                r.setFont(BASE.deriveFont(Font.BOLD, 15f)); r.setColor(TEXT); r.drawString(b.getTitle(), 74, 29);
                r.setFont(BASE.deriveFont(12.5f)); r.setColor(MUTED); r.drawString(b.getAuthor() + "   \u00B7   " + b.getId(), 74, 48);
                Color c = col(b.getStatus()); int px = w - 126, py = ROW / 2 - 13;
                r.setColor(alpha(c, 38)); r.fillRoundRect(px, py, 104, 26, 26, 26);
                double pulse = 1 + .5 * Math.sin(t * (2 + b.getStatus().ordinal()));
                r.setColor(alpha(c, 60)); r.fill(new Ellipse2D.Double(px + 14 - 3 * pulse, py + 13 - 3 * pulse, 6 * pulse, 6 * pulse));
                r.setColor(c); r.fillOval(px + 12, py + 11, 4, 4);
                r.setFont(BASE.deriveFont(Font.BOLD, 12f)); r.drawString(b.getStatus().getLabel(), px + 26, py + 17); r.dispose();
            }
            g.dispose();
        }
    }

    /** Glass-pane overlay: opening curtain and slide-up toast messages. */
    static class Overlay extends JComponent {
        String msg = ""; boolean ok; long at = -1;
        Overlay() { setOpaque(false); }
        @Override public boolean contains(int x, int y) { return false; }
        void toast(String m, boolean ok) { msg = m; this.ok = ok; at = System.nanoTime(); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = g2(g0); int w = getWidth(), h = getHeight(); double ms0 = (System.nanoTime() - T0) / 1e6;
            if (ms0 < 1900) {
                float a = 1 - (float) ease((ms0 - 600) / 1000); g.setPaint(new GradientPaint(0, 0, alpha(BG1, (int) (a * 255)), w, h, alpha(BG2, (int) (a * 255)))); g.fillRect(0, 0, w, h);
                g.setFont(BASE.deriveFont(Font.BOLD, (float) (44 + (1 - a) * 8))); FontMetrics fm = g.getFontMetrics(); String t = "ATHENAEUM";
                g.setPaint(new GradientPaint(w / 2f - 120, 0, alpha(ACCENT, (int) (a * 255)), w / 2f + 120, 0, alpha(TEAL, (int) (a * 255))));
                g.drawString(t, (w - fm.stringWidth(t)) / 2, h / 2);
                g.setColor(alpha(TEAL, (int) (a * 255))); g.fillRoundRect(w / 2 - 80, h / 2 + 24, (int) (160 * ease(ms0 / 900)), 3, 3, 3);
            }
            double ms = (System.nanoTime() - at) / 1e6, life = 3200; if (at < 0 || ms > life) { g.dispose(); return; }
            float in = (float) ease(ms / 350), out = ms > life - 400 ? (float) ((life - ms) / 400) : 1, al = Math.min(in, out);
            g.setFont(BASE.deriveFont(Font.BOLD, 14f)); FontMetrics fm = g.getFontMetrics(); int tw = fm.stringWidth(msg) + 84, th = 52;
            int x = (w - tw) / 2, y = (int) (h - 90 + (1 - in) * 40); Color c = ok ? col(BookStatus.AVAILABLE) : RED;
            g.setComposite(AlphaComposite.SrcOver.derive(al)); g.setColor(new Color(18, 20, 48, 245)); g.fillRoundRect(x, y, tw, th, 26, 26);
            g.setColor(alpha(c, 200)); g.drawRoundRect(x, y, tw, th, 26, 26); g.fillOval(x + 16, y + 14, 24, 24);
            g.setColor(BG1); g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (ok) { g.drawLine(x + 22, y + 26, x + 26, y + 30); g.drawLine(x + 26, y + 30, x + 34, y + 21); }
            else { g.drawLine(x + 23, y + 21, x + 33, y + 31); g.drawLine(x + 33, y + 21, x + 23, y + 31); }
            g.setColor(TEXT); g.drawString(msg, x + 54, y + baseline(fm, th));
            g.setColor(alpha(c, 160)); g.fillRoundRect(x + 20, y + th - 5, (int) ((tw - 40) * (1 - ms / life)), 2, 2, 2); g.dispose();
        }
    }

    /** Logo with a page-flipping book icon and gradient wordmark. */
    static class Header extends JComponent {
        Header() { setPreferredSize(new Dimension(380, 64)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = g2(g0); float p = since(900, 700); double t = (System.nanoTime() - T0) / 1e9;
            g.translate(-(1 - p) * 30, 0); g.setComposite(AlphaComposite.SrcOver.derive(p));
            g.setPaint(new GradientPaint(0, 8, ACCENT, 48, 56, TEAL)); g.fillRoundRect(0, 8, 48, 48, 16, 16);
            g.setColor(Color.WHITE); g.fillPolygon(new int[]{24, 24, 8, 8}, new int[]{20, 44, 46, 22}, 4);
            double f = Math.cos(t * 1.6); int fx = (int) (24 + 16 * Math.abs(f));
            g.setColor(new Color(255, 255, 255, 200)); g.fillPolygon(new int[]{24, fx, fx, 24}, new int[]{20, 22, 46, 44}, 4);
            g.setFont(BASE.deriveFont(Font.BOLD, 26f)); g.setPaint(new GradientPaint(66, 0, TEXT, 220, 0, new Color(0xC4B8FF))); g.drawString("Athenaeum", 66, 34);
            g.setFont(BASE.deriveFont(12.5f)); g.setColor(MUTED); g.drawString("Library Book Management System", 67, 54); g.dispose();
        }
    }

    // ---------------------------------------------------------------- application logic
    private final LibraryManager manager = new LibraryManager();
    private final BookList list = new BookList();
    private final Field idF = new Field("e.g. B-106"), titleF = new Field("Book title"), authorF = new Field("Author name"), search = new Field("Search books\u2026");
    private final StatusPicker picker = new StatusPicker();
    private final Stat[] stats = {new Stat("Total", ACCENT, 1000), new Stat("Available", col(BookStatus.AVAILABLE), 1100),
            new Stat("Issued", RED, 1200), new Stat("Reserved", col(BookStatus.RESERVED), 1300)};
    private final Overlay overlay = new Overlay();

    /** Builds the main window. */
    public LibraryApp() {
        super("Athenaeum \u2013 Library Book Management System");
        try { manager.addBook("B-101", "The Pragmatic Programmer", "Andrew Hunt", BookStatus.AVAILABLE);
            manager.addBook("B-102", "Clean Code", "Robert C. Martin", BookStatus.AVAILABLE);
            manager.addBook("B-103", "Effective Java", "Joshua Bloch", BookStatus.ISSUED);
            manager.addBook("B-104", "Design Patterns", "Erich Gamma", BookStatus.RESERVED);
            manager.addBook("B-105", "Refactoring", "Martin Fowler", BookStatus.AVAILABLE);
        } catch (LibraryException ignored) { }

        Backdrop root = new Backdrop(); root.setLayout(new BorderLayout(0, 0)); root.setBorder(new EmptyBorder(24, 28, 26, 28)); setContentPane(root);
        JPanel top = new JPanel(new BorderLayout(0, 18)); top.setOpaque(false); top.setBorder(new EmptyBorder(0, 0, 18, 0));
        JPanel headRow = new JPanel(new BorderLayout()); headRow.setOpaque(false); headRow.add(new Header(), BorderLayout.WEST);
        JPanel sw = new JPanel(new GridBagLayout()); sw.setOpaque(false); search.setPreferredSize(new Dimension(320, 44)); sw.add(search); headRow.add(sw, BorderLayout.EAST);
        JPanel statRow = new JPanel(new GridLayout(1, 4, 16, 0)); statRow.setOpaque(false); for (Stat s : stats) statRow.add(s);
        top.add(headRow, BorderLayout.NORTH); top.add(statRow, BorderLayout.CENTER); root.add(top, BorderLayout.NORTH);

        Card listCard = new Card(new BorderLayout(0, 10), 1350); listCard.setBorder(new EmptyBorder(18, 14, 14, 14));
        JLabel lt = new JLabel("  Collection"); lt.setFont(BASE.deriveFont(Font.BOLD, 18f)); lt.setForeground(TEXT); listCard.add(lt, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(list); sp.setOpaque(false); sp.getViewport().setOpaque(false); sp.setBorder(null);
        sp.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE); sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); sp.getVerticalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
            protected JButton createDecreaseButton(int o) { return zero(); }
            protected JButton createIncreaseButton(int o) { return zero(); }
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) { Graphics2D q = g2(g); q.setColor(new Color(255, 255, 255, 70)); q.fillRoundRect(r.x + 1, r.y, r.width - 2, r.height, 8, 8); q.dispose(); }
        });
        listCard.add(sp, BorderLayout.CENTER);

        Card form = new Card(null, 1500); form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS)); form.setBorder(new EmptyBorder(22, 22, 22, 22)); form.setPreferredSize(new Dimension(340, 0));
        JLabel ft = new JLabel("Book Details"); ft.setFont(BASE.deriveFont(Font.BOLD, 18f)); ft.setForeground(TEXT); add(form, ft, 14);
        add(form, tag("BOOK ID"), 4); add(form, idF, 12); add(form, tag("TITLE"), 4); add(form, titleF, 12); add(form, tag("AUTHOR"), 4); add(form, authorF, 12);
        add(form, tag("STATUS"), 4); add(form, picker, 18);
        JPanel btns = new JPanel(new GridLayout(2, 2, 4, 4)); btns.setOpaque(false); btns.setAlignmentX(LEFT_ALIGNMENT); btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        Color g1 = ACCENT, g2c = new Color(0x4F8BFF);
        Btn add = new Btn("Add Book", g1, g2c), upd = new Btn("Update", new Color(0x14B8A6), TEAL), stat = new Btn("Set Status", new Color(0xF59E0B), new Color(0xFBBF24)), del = new Btn("Delete", new Color(0xE11D48), RED);
        btns.add(add); btns.add(upd); btns.add(stat); btns.add(del); add(form, btns, 6);
        Btn clear = new Btn("Clear Form", new Color(255, 255, 255, 40), new Color(255, 255, 255, 25)); clear.setAlignmentX(LEFT_ALIGNMENT); clear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46)); form.add(clear);

        JPanel center = new JPanel(new BorderLayout(18, 0)); center.setOpaque(false); center.add(listCard, BorderLayout.CENTER); center.add(form, BorderLayout.EAST); root.add(center, BorderLayout.CENTER);
        setGlassPane(overlay); overlay.setVisible(true);

        list.onSelect = b -> { idF.setText(b.getId()); titleF.setText(b.getTitle()); authorF.setText(b.getAuthor()); picker.sel = b.getStatus(); };
        add.addActionListener(e -> run(() -> { manager.addBook(idF.getText(), titleF.getText(), authorF.getText(), picker.sel); list.selectedId = idF.getText().trim(); return "Book added  successfullyto the library."; }));
        upd.addActionListener(e -> run(() -> { manager.updateBook(idF.getText(), titleF.getText(), authorF.getText()); return "Book details updated."; }));
        stat.addActionListener(e -> run(() -> { manager.setStatus(idF.getText(), picker.sel); return "Status set to " + picker.sel.getLabel() + "."; }));
del.addActionListener(e -> {
    int choice = JOptionPane.showConfirmDialog(this,
            "Delete book '" + idF.getText().trim() + "'? This cannot be undone.",
            "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (choice == JOptionPane.YES_OPTION)
        run(() -> { manager.deleteBook(idF.getText()); clearForm(); return "Book deleted."; });
});        clear.addActionListener(e -> clearForm());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); } public void removeUpdate(DocumentEvent e) { refresh(); } public void changedUpdate(DocumentEvent e) { refresh(); } });
        refresh();
        new javax.swing.Timer(16, e -> getContentPane().repaint()).start();
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(1180, 760); setMinimumSize(new Dimension(1000, 640)); setLocationRelativeTo(null);
    }

    private interface Action { String go() throws LibraryException; }

    private void run(Action a) {
        try { String m = a.go(); overlay.toast(m, true); refresh(); }
        catch (LibraryException ex) {
            overlay.toast(ex.getMessage(), false);
            ("title".equals(ex.getField()) ? titleF : "author".equals(ex.getField()) ? authorF : idF).shake();
        }
    }

    private void clearForm() { idF.setText(""); titleF.setText(""); authorF.setText(""); picker.sel = BookStatus.AVAILABLE; list.selectedId = null; }

    private void refresh() {
        String q = search.getText().trim().toLowerCase(); List<Book> shown = new ArrayList<>();
        for (Book b : manager.getAllBooks())
            if ((b.getId() + b.getTitle() + b.getAuthor() + b.getStatus().getLabel()).toLowerCase().contains(q)) shown.add(b);
        list.setBooks(shown, manager.getAllBooks().isEmpty() ? "The library is empty \u2013 add your first book" : "No books match your search");
        stats[0].target = manager.getAllBooks().size(); stats[1].target = manager.count(BookStatus.AVAILABLE);
        stats[2].target = manager.count(BookStatus.ISSUED); stats[3].target = manager.count(BookStatus.RESERVED);
    }

    private static JLabel tag(String t) { JLabel l = new JLabel(t); l.setFont(BASE.deriveFont(Font.BOLD, 11f)); l.setForeground(MUTED); return l; }
    private static void add(JPanel p, JComponent c, int gap) { c.setAlignmentX(LEFT_ALIGNMENT); p.add(c); p.add(Box.createVerticalStrut(gap)); }

    /**
     * Application entry point.
     * @param args unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { LibraryApp f = new LibraryApp(); T0 = System.nanoTime(); f.setVisible(true); });
    }
}
