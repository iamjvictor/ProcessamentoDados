import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PassaBaixa extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage filteredImage;
    
    // Filtros
    private static final int[][] MEDIA_3x3 = {
        {1, 1, 1},
        {1, 1, 1}, 
        {1, 1, 1}
    };
    
    private static final int[][] MEDIA_5x5 = {
        {1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1}
    };
    
    private static final int[][] GAUSS_3x3 = {
        {1, 2, 1},
        {2, 4, 2},
        {1, 2, 1}
    };
    
    private static final int[][] GAUSS_5x5 = {
        {1,  4,  6,  4, 1},
        {4, 16, 24, 16, 4},
        {6, 24, 36, 24, 6},
        {4, 16, 24, 16, 4},
        {1,  4,  6,  4, 1}
    };

    public PassaBaixa() {
        setTitle("Filtros Passa-Baixa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 3, 10, 10));
        
        try {
            originalImage = ImageIO.read(new File("Flamengo.jpg"));
            
            if (originalImage == null) {
                System.out.println("Erro: Não foi possível carregar a imagem");
                return;
            }

            // Aplicar os diferentes filtros
            BufferedImage media3x3 = applyFilter(originalImage, MEDIA_3x3, 9);
            BufferedImage media5x5 = applyFilter(originalImage, MEDIA_5x5, 25);
            BufferedImage gauss3x3 = applyFilter(originalImage, GAUSS_3x3, 16);
            BufferedImage gauss5x5 = applyFilter(originalImage, GAUSS_5x5, 256);

            // Adicionar painéis para exibir as imagens
            add(createImagePanel(originalImage, "Original"));
            add(createImagePanel(media3x3, "Média 3x3"));
            add(createImagePanel(media5x5, "Média 5x5"));
            add(createImagePanel(gauss3x3, "Gaussiano 3x3"));
            add(createImagePanel(gauss5x5, "Gaussiano 5x5"));

            pack();
            setLocationRelativeTo(null);
            setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel createImagePanel(BufferedImage image, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
        return panel;
    }

    private BufferedImage applyFilter(BufferedImage input, int[][] filter, int denominator) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage output = new BufferedImage(width, height, input.getType());
        int filterSize = filter.length;
        int margin = filterSize / 2;

        for (int y = margin; y < height - margin; y++) {
            for (int x = margin; x < width - margin; x++) {
                int r = 0, g = 0, b = 0;
                for (int fy = 0; fy < filterSize; fy++) {
                    for (int fx = 0; fx < filterSize; fx++) {
                        int rgb = input.getRGB(x + fx - margin, y + fy - margin);
                        int fr = (rgb >> 16) & 0xff;
                        int fg = (rgb >> 8) & 0xff;
                        int fb = rgb & 0xff;
                        int coeff = filter[fy][fx];
                        r += fr * coeff;
                        g += fg * coeff;
                        b += fb * coeff;
                    }
                }
                r = Math.min(255, Math.max(0, r / denominator));
                g = Math.min(255, Math.max(0, g / denominator));
                b = Math.min(255, Math.max(0, b / denominator));
                output.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return output;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PassaBaixa());
    }
}
