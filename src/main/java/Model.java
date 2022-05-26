import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Model {

    public BufferedImage HSVtoRGB(BufferedImage img) throws IOException {
        //opencv
        Mat rgbMat = new Mat();
        Imgproc.cvtColor(img2Mat(img), rgbMat, Imgproc.COLOR_HSV2BGR);
        BufferedImage resultL = (BufferedImage) HighGui.toBufferedImage(rgbMat);

        int h = img.getHeight();
        int w = img.getWidth();
        BufferedImage result = new BufferedImage(w, h, TYPE_INT_RGB);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = img.getRGB(j, i);
                int rgb = HSVtoRGB(Utils.ch3(color), Utils.ch2(color), Utils.ch1(color));
                result.setRGB(j, i, rgb);
            }
        }
        Utils.save(result, "result/HSVtoRGB", "result", "jpg");
        Utils.save(resultL, "result/HSVtoRGB", "resultLib", "jpg");
        Utils.save(diff(result, resultL), "result/HSVtoRGB", "diff", "jpg");
        return result;
    }

    public static int HSVtoRGB(float H, float S, float V) {
        float R, G, B;
        H /= 180f;
        S /= 255f;
        V /= 255f;
        if (S == 0) {
            R = V * 255;
            G = V * 255;
            B = V * 255;
        } else {
            float var_h = H * 6;
            if (var_h == 6) {
                var_h = 0;
            }
            int var_i = (int) Math.floor(var_h);
            float var_1 = V * (1 - S);
            float var_2 = V * (1 - S * (var_h - var_i));
            float var_3 = V * (1 - S * (1 - (var_h - var_i)));
            float var_r;
            float var_g;
            float var_b;
            if (var_i == 0) {
                var_r = V;
                var_g = var_3;
                var_b = var_1;
            } else if (var_i == 1) {
                var_r = var_2;
                var_g = V;
                var_b = var_1;
            } else if (var_i == 2) {
                var_r = var_1;
                var_g = V;
                var_b = var_3;
            } else if (var_i == 3) {
                var_r = var_1;
                var_g = var_2;
                var_b = V;
            } else if (var_i == 4) {
                var_r = var_3;
                var_g = var_1;
                var_b = V;
            } else {
                var_r = V;
                var_g = var_1;
                var_b = var_2;
            }
            R = var_r * 255;
            G = var_g * 255;
            B = var_b * 255;
        }
        return Utils.color(R, G, B);
    }

    public BufferedImage RGBtoHSV(BufferedImage img) throws IOException {
        //opencv
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(img2Mat(img), hsvMat, Imgproc.COLOR_BGR2HSV);
        BufferedImage resultL = (BufferedImage) HighGui.toBufferedImage(hsvMat);

        int h = img.getHeight();
        int w = img.getWidth();
        BufferedImage result = new BufferedImage(w, h, TYPE_INT_RGB);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = img.getRGB(j, i);
                double[] hsv = RGBtoHSV(Utils.ch1(color), Utils.ch2(color), Utils.ch3(color));
                result.setRGB(j, i, Utils.color(hsv[2], hsv[1], hsv[0]));
            }
        }
        Utils.save(result, "result/RGBtoHSV", "result", "jpg");
        Utils.save(resultL, "result/RGBtoHSV", "resultLib", "jpg");
        Utils.save(diff(result, resultL), "result/RGBtoHSV", "diff", "jpg");
        return result;
    }

    private static double[] RGBtoHSV(int r, int g, int b) {
        List<Integer> arr = Arrays.asList(r, g, b);
        double min = Collections.min(arr);
        double v = Collections.max(arr);
        double s;
        if (v == 0) {
            s = 0;
        } else {
            s = 1 - min / v;
        }
        double h = 0;
        if (v == r) {
            h = 60 * (g - b) / (v - min);
        } else if (v == g) {
            h = 60 * (b - r) / (v - min) + 120;
        } else if (v == b) {
            h = 60 * (r - g) / (v - min) + 240;
        }
        if (h < 0) h += 360;
        return new double[]{h / 2, s * 255, v * 255};
    }

    public void locus(BufferedImage img) throws IOException {
        int size = 1000;
        int xMove = (int) Math.round(0.312 * size) / 2;
        int yMove = (int) Math.round(0.329 * size) / 2;
        BufferedImage loscut = new BufferedImage(size, size, TYPE_INT_RGB);
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int rgb = img.getRGB(j, i);
                int[] xyz = RGBtoXYZ(Utils.ch1(rgb), Utils.ch2(rgb), Utils.ch3(rgb));
                double sum = xyz[0] + xyz[1] + xyz[2];
                if (sum > 0) {
                    double nx = xyz[0] / sum;
                    double ny = xyz[1] / sum;
                    double nz = xyz[2] / sum;
                    int x = (int) Math.round((1 - ny - nz) * size) + xMove;
                    int y = (int) Math.round((1 - nx - nz) * size * -1) + size - yMove;
                    try {
                        loscut.setRGB(x, y, rgb);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("x: " + x + "; y: " + (size - y));
                    }
                }
            }
        }
        Utils.save(loscut, "result/locus", "result", "jpg");
    }

    public BufferedImage XYZtoRGB(BufferedImage img) throws IOException {
        //opencv
        Mat rgbMat = new Mat();
        Imgproc.cvtColor(img2Mat(img), rgbMat, Imgproc.COLOR_XYZ2BGR);
        BufferedImage resultL = (BufferedImage) HighGui.toBufferedImage(rgbMat);

        int h = img.getHeight();
        int w = img.getWidth();
        BufferedImage result = new BufferedImage(w, h, TYPE_INT_RGB);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int xyz = img.getRGB(j, i);
                //int rgb = gammaCorrection(XYZtoRGB(ch3(xyz), ch2(xyz), ch1(xyz)), 2.4);
                int rgb = XYZtoRGB(Utils.ch3(xyz), Utils.ch2(xyz), Utils.ch1(xyz));
                result.setRGB(j, i, rgb);
            }
        }
        Utils.save(result, "result/XYZtoRGB", "result", "jpg");
        Utils.save(resultL, "result/XYZtoRGB", "resultLib", "jpg");
        Utils.save(diff(result, resultL), "result/XYZtoRGB", "diff", "jpg");
        return result;
    }

    private static int XYZtoRGB(int x, int y, int z) {
        double r = x * 3.240479 + y * -1.537150 + z * -0.498535;
        double g = x * -0.969256 + y * 1.875991 + z * 0.041556;
        double b = x * 0.055648 + y * -0.204043 + z * 1.057311;
        return Utils.color(r, g, b);
    }

    public BufferedImage RGBtoXYZ(BufferedImage img) throws IOException {
        //opencv
        Mat xyzMat = new Mat();
        Imgproc.cvtColor(img2Mat(img), xyzMat, Imgproc.COLOR_BGR2XYZ);
        BufferedImage resultL = (BufferedImage) HighGui.toBufferedImage(xyzMat);

        int h = img.getHeight();
        int w = img.getWidth();
        BufferedImage result = new BufferedImage(w, h, TYPE_INT_RGB);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int rgb = img.getRGB(j, i);
                int[] xyz = RGBtoXYZ(Utils.ch1(rgb), Utils.ch2(rgb), Utils.ch3(rgb));
                result.setRGB(j, i, Utils.color(xyz[2], xyz[1], xyz[0]));
            }
        }
        Utils.save(result, "result/RGBtoXYZ", "result", "jpg");
        Utils.save(resultL, "result/RGBtoXYZ", "resultLib", "jpg");
        Utils.save(diff(result, resultL), "result/RGBtoXYZ", "diff", "jpg");
        return result;
    }

    private static int[] RGBtoXYZ(double r, double g, double b) {
        return new int[]{
                (int) Math.round(r * 0.412453 + g * 0.357580 + b * 0.180423),
                (int) Math.round(r * 0.212671 + g * 0.715160 + b * 0.072169),
                (int) Math.round(r * 0.019334 + g * 0.119193 + b * 0.950227)
        };
    }

    private static BufferedImage diff(BufferedImage imgA, BufferedImage imgB) {
        int h = imgA.getHeight();
        int w = imgA.getWidth();
        BufferedImage result = new BufferedImage(w, h, TYPE_INT_RGB);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int colorA = imgA.getRGB(j, i);
                int colorB = imgB.getRGB(j, i);
                int r = Utils.ch1(colorA) - Utils.ch1(colorB);
                int g = Utils.ch2(colorA) - Utils.ch2(colorB);
                int b = Utils.ch3(colorA) - Utils.ch3(colorB);
                if (r < 0) r = 0;
                else if (r > 255) r = 255;
                if (g < 0) g = 0;
                else if (g > 255) g = 255;
                if (b < 0) b = 0;
                else if (b > 255) b = 255;
                result.setRGB(j, i, Utils.color(r, g, b));
            }
        }
        return result;
    }

    private static Mat img2Mat(BufferedImage image) {
        image = convertTo3ByteBGRType(image);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private static BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }
}
