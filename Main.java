import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Main {
    public static void main(String[] args) {
        try {
            BufferedImage binaryImage = ImageIO.read(new File("binary.jpg"));
            
          
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de imagem 'binary.jpg'.");
            e.printStackTrace();
        }
    }
}
