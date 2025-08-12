import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Otzu {

    
    
    public static void main(String[] args) {
        try {
            File inputFile = new File("Flamengo.jpg");
            System.out.println("Tentando ler arquivo: " + inputFile.getAbsolutePath());
            System.out.println("Arquivo existe? " + inputFile.exists());
            
            if (!inputFile.exists()) {
                System.out.println("Erro: Arquivo Flamengo.jpg não encontrado na pasta atual!");
                return;
            }
            BufferedImage originalImage = ImageIO.read(inputFile);
            if (originalImage == null) {
                System.out.println("Erro: Não foi possível carregar a imagem. Verifique se o arquivo é uma imagem válida.");
                return;
            }
            System.out.println("Imagem carregada com sucesso! Dimensões: " + originalImage.getWidth() + "x" + originalImage.getHeight());
            
            // Converter para escala de cinza
            BufferedImage grayImage = convertToGrayscale(originalImage);
            
            // Aplicar limiarização de Otsu
            BufferedImage binaryImage = convertToBinary(grayImage);
            
            // Salvar as imagens resultantes
            ImageIO.write(grayImage, "jpg", new File("grayscale.jpg"));
            ImageIO.write(binaryImage, "jpg", new File("binary.jpg"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static BufferedImage convertToGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = original.getRGB(i, j);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                // Conversão para tons de cinza usando média ponderada
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                grayscale.setRGB(i, j, (gray << 16) | (gray << 8) | gray);
            }
        }
        return grayscale;
    }
    
    private static BufferedImage convertToBinary(BufferedImage grayscale) {
        int width = grayscale.getWidth();
        int height = grayscale.getHeight();
        
        // Calcular histograma
        int[] histogram = new int[256];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int gray = grayscale.getRGB(i, j) & 0xff;
                histogram[gray]++;
            }
        }
        int total = width * height;
        
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }
        
        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float maxVariance = 0;
        int threshold = 0;
        
        // Calcular limiar ótimo usando método de Otsu
        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            
            wF = total - wB;
            if (wF == 0) break;
            
            sumB += i * histogram[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            
            float variance = wB * wF * (mB - mF) * (mB - mF);
            
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }
        
        // Aplicar limiarização
        BufferedImage binary = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int gray = grayscale.getRGB(i, j) & 0xff;
                binary.setRGB(i, j, gray > threshold ? 0xFFFFFF : 0);
            }
        }
        
        return binary;
    }
}
