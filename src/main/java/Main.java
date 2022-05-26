import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Main {

    public static void main(String[] args) throws IOException {
        URL url = getUrl();
        if (url == null) return;

        nu.pattern.OpenCV.loadShared();
        Model m = new Model();
        BufferedImage orig = ImageIO.read(url);
        BufferedImage xyz = m.RGBtoXYZ(orig);
        m.XYZtoRGB(xyz);
        m.locus(orig);
        BufferedImage hsv = m.RGBtoHSV(orig);
        m.HSVtoRGB(hsv);
    }

    private static URL getUrl() {
        return Main.class.getResource("img.jpg");
    }
}
