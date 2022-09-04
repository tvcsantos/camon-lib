package pt.unl.fct.di.tsantos.util;

import net.sourceforge.tuned.FileUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;

public class ImageUtilities {

    public static byte[] toByteArray(String image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File file = new File(image);
        BufferedImage bi = ImageIO.read(file);
        String ext = FileUtilities.getExtension(file);
        ImageIO.write(bi, ext, baos);
        return baos.toByteArray();
    }

    public static byte[] toByteArray(BufferedImage image,
            String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        return baos.toByteArray();
    }

    public static BufferedImage toBufferedImage(byte[] bytes)
            throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bi = ImageIO.read(bais);
        return bi;
    }

    public static BufferedImage scale(String src, int width, int height,
            int type) throws IOException {
        File srcFile = new File(src);
        BufferedImage bsrc = ImageIO.read(srcFile);
        BufferedImage bdest = new BufferedImage(width, height, type);
        Graphics2D g = bdest.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance((double) width
                / bsrc.getWidth(), (double) height / bsrc.getHeight());
        g.drawRenderedImage(bsrc, at);
        return bdest;
    }

    public static BufferedImage convert(String src, int type)
            throws IOException {
        BufferedImage bi = ImageIO.read(new File(src));
        BufferedImage image = new BufferedImage(bi.getWidth(),
                bi.getHeight(), type);
        Graphics g = image.getGraphics();
        g.drawImage(bi, 0, 0, null);
        g.dispose();
        return image;
    }

    public static BufferedImage scale(BufferedImage bsrc,
            int width, int height, int type) {
        BufferedImage bdest = new BufferedImage(width, height,
                type);
        Graphics2D g = bdest.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance((double) width
                / bsrc.getWidth(), (double) height / bsrc.getHeight());
        g.drawRenderedImage(bsrc, at);
        return bdest;
    }

    public static BufferedImage scale(BufferedImage bsrc, double factor) {
        int width = (int) (bsrc.getWidth() * factor);
        int height = (int) (bsrc.getHeight() * factor);
        int type = bsrc.getType();
        return scale(bsrc, width, height, type);
    }

    public static BufferedImage convert(BufferedImage bsrc, int type) {
        BufferedImage image = new BufferedImage(bsrc.getWidth(),
                bsrc.getHeight(), type);
        Graphics g = image.getGraphics();
        g.drawImage(bsrc, 0, 0, null);
        g.dispose();
        return image;
    }

    public static BufferedImage rotate(BufferedImage image, double angle) {
        int width = image.getWidth(); int height = image.getHeight();
        int type = image.getType();
        BufferedImage result = new BufferedImage(width, height, type);
        Graphics2D g = result.createGraphics();
        AffineTransform at =
                AffineTransform.getRotateInstance(Math.toRadians(angle),
                width/2, height/2);
        g.drawRenderedImage(image, at);
        return result;

    }

    public static BufferedImage convert(Image image) {
        if (image instanceof BufferedImage) return (BufferedImage) image;
        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_BGR);
        bufferedImage.createGraphics().drawImage(image, 0, 0, null);
        return bufferedImage;
    }

    public static BufferedImage renew(RenderedImage image, String imageFormat)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, imageFormat, bos);
        BufferedImage bufferedImage =
                ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        return bufferedImage;
    }

    public static BufferedImage watermark(BufferedImage image,
            String watermarkText, Font font, Color from, Color to) {
        // create graphics context and enable anti-aliasing
        BufferedImage result = new BufferedImage(image.getWidth(),
                image.getHeight(), image.getType());

        //Graphics2D g2d = original.createGraphics();
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(image, null, 0, 0);
        g2d.scale(1, 1);
        g2d.addRenderingHints(
                new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // create watermark text shape for rendering
        //Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        GlyphVector fontGV = font.createGlyphVector(
                g2d.getFontRenderContext(), watermarkText);
        Rectangle size = fontGV.getPixelBounds(
                g2d.getFontRenderContext(), 0, 0);
        Shape textShape = fontGV.getOutline();
        double textWidth = size.getWidth();
        double textHeight = size.getHeight();
        AffineTransform rotate45 =
                AffineTransform.getRotateInstance(Math.PI / 4d);
        Shape rotatedText = rotate45.createTransformedShape(textShape);

        // use a gradient that repeats 4 times
        g2d.setPaint(new GradientPaint(0, 0,
                            from,
                            image.getWidth() / 2, image.getHeight() / 2,
                            to));
        /*g2d.setPaint(new GradientPaint(0, 0,
                            new Color(0f, 0f, 0f, 0.1f),
                            original.getWidth() / 2, original.getHeight() / 2,
                            new Color(1f, 1f, 1f, 0.1f)));*/
        g2d.setStroke(new BasicStroke(0.5f));

        // step in y direction is calc'ed using pythagoras + 5 pixel padding
        double yStep = Math.sqrt(textWidth * textWidth / 2) + 5;

        // step over image rendering watermark text
        for (double x = -textHeight * 3; x < image.getWidth();
                                                x += (textHeight * 3)) {
            double y = -yStep;
            for (; y < image.getHeight(); y += yStep) {
                g2d.draw(rotatedText);
                g2d.fill(rotatedText);
                g2d.translate(0, yStep);
            }
            g2d.translate(textHeight * 3, -(y + yStep));
        }

        return result;
    }

    public static BufferedImage getImage(Class<?> aClass, String path) 
            throws IOException {
        InputStream is = aClass.getResourceAsStream(path);
        return ImageIO.read(is);
    }

    public static BufferedImage getImageSafe(Class<?> aClass, String path) {
        InputStream is = aClass.getResourceAsStream(path);
        try {
            if (is == null) return null;
            return ImageIO.read(is);
        } catch (IOException ex) {
            return null;
        }
    }

    public static ImageIcon getImageIconSafe(Class<?> aClass, String path) {
        BufferedImage imageSafe = getImageSafe(aClass, path);
        if (imageSafe == null) return null;
        return new ImageIcon(imageSafe);
    }
}
