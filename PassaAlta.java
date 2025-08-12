import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PassaAlta extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage sobelImage;
    private BufferedImage laplacianImage;
    private BufferedImage gradientImage;
    private BufferedImage grayImage;
    private BufferedImage graySobelImage;
    private BufferedImage grayLaplacianImage;
    private BufferedImage grayGradientImage;
    
    // Kernels para os filtros
    private static final int[][] SOBEL_X = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static final int[][] SOBEL_Y = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
    private static final int[][] LAPLACIAN = {{0, 1, 0}, {1, -4, 1}, {0, 1, 0}};
    private static final int TOLERANCE = 80;
    
    public PassaAlta() {
        setTitle("Filtros de Detecção de Bordas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 4, 10, 10));
        
        try {
            // Carregar imagem original
            originalImage = ImageIO.read(new File("Flamengo.jpg"));

            if (originalImage == null) {
                System.out.println("Erro: Não foi possível carregar a imagem");
                return;
            }

            // Converter para escala de cinza
            grayImage = convertToGrayscale(originalImage);

            // Aplicar filtros na imagem colorida
            sobelImage = applySobelFilter(originalImage);
            laplacianImage = applyLaplacianFilter(originalImage);
            gradientImage = applyGradientFilter(originalImage);

            // Aplicar filtros na imagem em escala de cinza
            graySobelImage = applySobelFilter(grayImage);
            grayLaplacianImage = applyLaplacianFilter(grayImage);
            grayGradientImage = applyGradientFilter(grayImage);

            // Criar painéis para exibir as imagens
            add(createImagePanel(originalImage, "Original RGB"));
            add(createImagePanel(sobelImage, "Sobel RGB"));
            add(createImagePanel(laplacianImage, "Laplaciano RGB"));
            add(createImagePanel(gradientImage, "Gradiente RGB"));
            
            add(createImagePanel(grayImage, "Original Gray"));
            add(createImagePanel(graySobelImage, "Sobel Gray"));
            add(createImagePanel(grayLaplacianImage, "Laplaciano Gray"));
            add(createImagePanel(grayGradientImage, "Gradiente Gray"));

            // Salvar imagens
            ImageIO.write(sobelImage, "png", new File("sobel_rgb.png"));
            ImageIO.write(laplacianImage, "png", new File("laplacian_rgb.png"));
            ImageIO.write(gradientImage, "png", new File("gradient_rgb.png"));
            ImageIO.write(graySobelImage, "png", new File("sobel_gray.png"));
            ImageIO.write(grayLaplacianImage, "png", new File("laplacian_gray.png"));
            ImageIO.write(grayGradientImage, "png", new File("gradient_gray.png"));

            pack();
            setLocationRelativeTo(null);
            setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage convertToGrayscale(BufferedImage input) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = output.createGraphics();
        g.drawImage(input, 0, 0, null);
        g.dispose();
        return output;
    }

    private JPanel createImagePanel(BufferedImage image, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JLabel label = new JLabel(new ImageIcon(image));
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }

    private BufferedImage applySobelFilter(BufferedImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, input.getType());
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double[] vhw = {0.0, 0.0};
                int channels = input.getType() == BufferedImage.TYPE_BYTE_GRAY ? 1 : 3;
                
                for (int k = 0; k < channels; k++) {
                    double vh = 0.0;
                    double vw = 0.0;
                    
                    for (int s = 0; s < 3; s++) {
                        for (int t = 0; t < 3; t++) {
                            int pixel = input.getRGB(x + t - 1, y + s - 1);
                            int value = (pixel >> (k * 8)) & 0xff;
                            vh += SOBEL_Y[s][t] * value;
                            vw += SOBEL_X[s][t] * value;
                        }
                    }
                    
                    vhw[0] += vh;
                    vhw[1] += vw;
                }

                double magnitude = Math.sqrt(vhw[0] * vhw[0] + vhw[1] * vhw[1]);
                int value = (int) Math.min(255, Math.max(0, magnitude));
                
                if (magnitude > TOLERANCE) {
                    value = 0;
                } else {
                    value = 255;
                }
                
                if (channels == 1) {
                    output.setRGB(x, y, (value << 16) | (value << 8) | value);
                } else {
                    output.setRGB(x, y, (value << 16) | (value << 8) | value);
                }
            }
        }
        return output;
    }

    private BufferedImage applyLaplacianFilter(BufferedImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, input.getType());
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sum = 0;
                int channels = input.getType() == BufferedImage.TYPE_BYTE_GRAY ? 1 : 3;
                
                for (int k = 0; k < channels; k++) {
                    for (int s = 0; s < 3; s++) {
                        for (int t = 0; t < 3; t++) {
                            int pixel = input.getRGB(x + t - 1, y + s - 1);
                            int value = (pixel >> (k * 8)) & 0xff;
                            sum += LAPLACIAN[s][t] * value;
                        }
                    }
                }

                int value = (int) Math.min(255, Math.max(0, Math.abs(sum)));
                if (value > TOLERANCE) {
                    value = 0;
                } else {
                    value = 255;
                }
                
                if (channels == 1) {
                    output.setRGB(x, y, (value << 16) | (value << 8) | value);
                } else {
                    output.setRGB(x, y, (value << 16) | (value << 8) | value);
                }
            }
        }
        return output;
    }

    private BufferedImage applyGradientFilter(BufferedImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, input.getType());
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double[] vhw = {0.0, 0.0};
                int channels = input.getType() == BufferedImage.TYPE_BYTE_GRAY ? 1 : 3;
                
                for (int k = 0; k < channels; k++) {
                    // Gradiente horizontal
                    int gx = Math.abs(((input.getRGB(x+1, y) >> (k * 8)) & 0xff) - 
                                    ((input.getRGB(x-1, y) >> (k * 8)) & 0xff));
                    
                    // Gradiente vertical
                    int gy = Math.abs(((input.getRGB(x, y+1) >> (k * 8)) & 0xff) - 
                                    ((input.getRGB(x, y-1) >> (k * 8)) & 0xff));
                    
                    vhw[0] += gx;
                    vhw[1] += gy;
                }

                double magnitude = Math.sqrt(vhw[0] * vhw[0] + vhw[1] * vhw[1]);
                int value = (int) Math.min(255, Math.max(0, magnitude));
                
                if (magnitude > TOLERANCE) {
                    value = 0;
                } else {
                    value = 255;
                }
                
                if (channels == 1) {
                    output.setRGB(x, y, (value << 16) | (value << 8) | value);
                } else {
                    output.setRGB(x, y, (value << 16) | (value << 8) | value);
                }
            }
        }
        return output;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PassaAlta());
    }
}
