package tools.release;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/** Generates PNG and ICO counterparts of the checked-in SVG release icon. */
public final class IconGenerator {
    private IconGenerator() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected output directory");
        Path output = Path.of(args[0]);
        Files.createDirectories(output);

        int[] sizes = {16, 32, 48, 64, 128, 256, 512, 1024};
        List<byte[]> pngs = new ArrayList<>();
        for (int size : sizes) {
            byte[] png = renderPng(size);
            pngs.add(png);
            Files.write(output.resolve("tetris-" + size + ".png"), png);
            if (size == 512) Files.write(output.resolve("tetris.png"), png);
        }
        writeIco(output.resolve("tetris.ico"), sizes, pngs);
        writeIcns(output.resolve("tetris.icns"), sizes, pngs);
    }

    private static byte[] renderPng(int size) throws IOException {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double scale = size / 1024.0;
        graphics.scale(scale, scale);

        graphics.setColor(new Color(10, 17, 34));
        graphics.fillRoundRect(48, 48, 928, 928, 210, 210);
        graphics.setColor(new Color(38, 56, 88));
        graphics.setStroke(new BasicStroke(18));
        graphics.drawRoundRect(80, 80, 864, 864, 182, 182);

        graphics.setColor(new Color(69, 215, 239));
        Path2D t = new Path2D.Double();
        t.moveTo(300, 214); t.lineTo(724, 214); t.lineTo(724, 356); t.lineTo(583, 356);
        t.lineTo(583, 498); t.lineTo(441, 498); t.lineTo(441, 356); t.lineTo(300, 356); t.closePath();
        graphics.fill(t);

        drawBlock(graphics, new Color(255, 204, 63), 158, 640);
        drawBlock(graphics, new Color(255, 204, 63), 300, 640);
        drawBlock(graphics, new Color(255, 204, 63), 442, 640);
        drawBlock(graphics, new Color(255, 204, 63), 584, 640);
        drawBlock(graphics, new Color(250, 90, 120), 300, 782);
        drawBlock(graphics, new Color(250, 90, 120), 442, 782);
        drawBlock(graphics, new Color(118, 220, 114), 584, 782);
        drawBlock(graphics, new Color(118, 220, 114), 726, 782);

        graphics.setColor(new Color(134, 239, 255));
        graphics.setStroke(new BasicStroke(26, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine(512, 532, 512, 606);
        graphics.drawLine(474, 572, 512, 610);
        graphics.drawLine(550, 572, 512, 610);
        graphics.dispose();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        return bytes.toByteArray();
    }

    private static void drawBlock(Graphics2D graphics, Color color, int x, int y) {
        graphics.setColor(color);
        graphics.fillRect(x + 8, y + 8, 126, 126);
        graphics.setColor(new Color(7, 16, 31));
        graphics.setStroke(new BasicStroke(16));
        graphics.drawRect(x + 8, y + 8, 126, 126);
    }

    private static void writeIco(Path target, int[] sizes, List<byte[]> pngs) throws IOException {
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(target))) {
            writeShortLE(out, 0); writeShortLE(out, 1); writeShortLE(out, sizes.length);
            int offset = 6 + sizes.length * 16;
            for (int i = 0; i < sizes.length; i++) {
                int encoded = sizes[i] == 256 ? 0 : sizes[i];
                out.writeByte(encoded); out.writeByte(encoded); out.writeByte(0); out.writeByte(0);
                writeShortLE(out, 1); writeShortLE(out, 32);
                writeIntLE(out, pngs.get(i).length); writeIntLE(out, offset);
                offset += pngs.get(i).length;
            }
            for (byte[] png : pngs) out.write(png);
        }
    }

    private static void writeIcns(Path target, int[] sizes, List<byte[]> pngs) throws IOException {
        String[] types = {"icp4", "icp5", "ic07", "ic08", "ic09", "ic10"};
        int[] pngIndexes = {0, 1, 4, 5, 6, 7};
        int totalLength = 8;
        for (int index : pngIndexes) totalLength += 8 + pngs.get(index).length;
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(target))) {
            out.writeBytes("icns");
            out.writeInt(totalLength);
            for (int i = 0; i < types.length; i++) {
                byte[] png = pngs.get(pngIndexes[i]);
                out.writeBytes(types[i]);
                out.writeInt(8 + png.length);
                out.write(png);
            }
        }
    }

    private static void writeShortLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value); out.writeByte(value >>> 8);
    }

    private static void writeIntLE(DataOutputStream out, int value) throws IOException {
        out.writeByte(value); out.writeByte(value >>> 8); out.writeByte(value >>> 16); out.writeByte(value >>> 24);
    }
}
