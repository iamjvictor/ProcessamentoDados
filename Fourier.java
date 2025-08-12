import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Fourier {
    public static void main(String[] args) {
        try {
            // 1. Carrega a imagem em escala de cinza
            BufferedImage grayscaleImage = ImageIO.read(new File("grayscale.jpg"));
            if (grayscaleImage == null) {
                System.out.println("Erro: Não foi possível carregar a imagem grayscale.jpg");
                return;
            }

            int width = grayscaleImage.getWidth();
            int height = grayscaleImage.getHeight();

            // 2. Converte a imagem para uma matriz de doubles (valores de 0 a 255)
            double[][] imageData = new double[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    imageData[y][x] = grayscaleImage.getRGB(x, y) & 0xff;
                }
            }

            // 3. Prepara uma matriz para os dados complexos (parte real e imaginária)
            double[][] complexData = new double[height][2 * width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    complexData[y][2*x] = imageData[y][x];  // Parte real
                    complexData[y][2*x+1] = 0;              // Parte imaginária
                }
            }

            // 4. Aplica a FFT 2D (Transformada de Fourier Rápida)
            DoubleFFT_2D fft = new DoubleFFT_2D(height, width);
            fft.complexForward(complexData);

            // 5. Gera a imagem do espectro de frequência (visualização das frequências)
            BufferedImage spectrumImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            double maxSpectrum = Double.MIN_VALUE;

            // Primeiro encontra o valor máximo do espectro para normalização
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double re = complexData[y][2*x];
                    double im = complexData[y][2*x+1];
                    double magnitude = Math.log(1 + Math.sqrt(re*re + im*im));
                    maxSpectrum = Math.max(maxSpectrum, magnitude);
                }
            }

            // Centraliza o espectro e normaliza para exibição
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Shift para centralizar o espectro (frequências baixas no centro)
                    int shiftedY = (y + height/2) % height;
                    int shiftedX = (x + width/2) % width;
                    
                    double re = complexData[y][2*x];
                    double im = complexData[y][2*x+1];
                    double magnitude = Math.log(1 + Math.sqrt(re*re + im*im));
                    
                    int value = (int)(magnitude * 255 / maxSpectrum);
                    value = Math.min(255, Math.max(0, value));
                    spectrumImage.setRGB(shiftedX, shiftedY, (value << 16) | (value << 8) | value);
                }
            }

            // Salva a imagem do espectro de frequência
            ImageIO.write(spectrumImage, "jpg", new File("spectrum.jpg"));

            // 6. Aplica um filtro passa-alta no domínio da frequência
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Calcula a distância do ponto ao centro do espectro
                    double distanceFromCenter = Math.sqrt(
                        Math.pow((y - height/2.0), 2) + 
                        Math.pow((x - width/2.0), 2)
                    );
                    
                    // Filtro passa-alta suave: atenua baixas frequências, realça altas
                    double radius = Math.sqrt(Math.pow(height/2.0, 2) + Math.pow(width/2.0, 2));
                    double highPassFactor = 1.0 - Math.exp(-distanceFromCenter * 5.0 / radius);
                    
                    // Adiciona um boost extra para frequências altas
                    if (distanceFromCenter > radius * 0.1) {
                        highPassFactor *= 2.0;
                    }
                    
                    int realIndex = 2*x;
                    int imagIndex = 2*x+1;
                    complexData[y][realIndex] *= highPassFactor;
                    complexData[y][imagIndex] *= highPassFactor;
                }
            }

            // 7. Aplica a FFT inversa para voltar ao domínio espacial (imagem)
            fft.complexInverse(complexData, true);

            // 8. Constrói a imagem final a partir dos dados complexos (magnitude)
            BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            double maxVal = Double.MIN_VALUE;
            double minVal = Double.MAX_VALUE;

            // Encontra os valores máximo e mínimo para normalização
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double magnitude = Math.sqrt(
                        Math.pow(complexData[y][2*x], 2) + 
                        Math.pow(complexData[y][2*x+1], 2)
                    );
                    maxVal = Math.max(maxVal, magnitude);
                    minVal = Math.min(minVal, magnitude);
                }
            }

            // Normaliza e cria a imagem final
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double magnitude = Math.sqrt(
                        Math.pow(complexData[y][2*x], 2) + 
                        Math.pow(complexData[y][2*x+1], 2)
                    );
                    double normalized = (magnitude - minVal) / (maxVal - minVal);
                    int value = (int)(normalized * 255);
                    value = Math.min(255, Math.max(0, value));
                    resultImage.setRGB(x, y, (value << 16) | (value << 8) | value);
                }
            }

            // Salva a imagem final com bordas realçadas
            ImageIO.write(resultImage, "jpg", new File("fourier_enhanced.jpg"));
            System.out.println("Processamento FFT concluído. Resultados salvos em:");
            System.out.println("- spectrum.jpg (espectro de frequência)");
            System.out.println("- fourier_enhanced.jpg (imagem com bordas realçadas)");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe auxiliar para FFT 2D (implementação recursiva)
    private static class DoubleFFT_2D {
        private int height;
        private int width;

        public DoubleFFT_2D(int height, int width) {
            this.height = height;
            this.width = width;
        }

        // Aplica FFT nas linhas e colunas
        public void complexForward(double[][] data) {
            // FFT nas linhas
            for (int y = 0; y < height; y++) {
                fft1d(data[y], width, false);
            }

            // FFT nas colunas
            double[] temp = new double[2 * height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    temp[2*y] = data[y][2*x];
                    temp[2*y+1] = data[y][2*x+1];
                }
                fft1d(temp, height, false);
                for (int y = 0; y < height; y++) {
                    data[y][2*x] = temp[2*y];
                    data[y][2*x+1] = temp[2*y+1];
                }
            }
        }

        // Aplica FFT inversa nas linhas e colunas
        public void complexInverse(double[][] data, boolean scale) {
            // IFFT nas linhas
            for (int y = 0; y < height; y++) {
                fft1d(data[y], width, true);
            }

            // IFFT nas colunas
            double[] temp = new double[2 * height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    temp[2*y] = data[y][2*x];
                    temp[2*y+1] = data[y][2*x+1];
                }
                fft1d(temp, height, true);
                for (int y = 0; y < height; y++) {
                    data[y][2*x] = temp[2*y];
                    data[y][2*x+1] = temp[2*y+1];
                }
            }

            // Normaliza se necessário
            if (scale) {
                double factor = 1.0 / (width * height);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < 2*width; x++) {
                        data[y][x] *= factor;
                    }
                }
            }
        }

        // Implementação recursiva da FFT 1D (Cooley-Tukey)
        private void fft1d(double[] data, int n, boolean inverse) {
            if (n <= 1) return;

            // Divide em pares e ímpares
            double[] even = new double[n];
            double[] odd = new double[n];
            for (int i = 0; i < n/2; i++) {
                even[2*i] = data[2*i];
                even[2*i+1] = data[2*i+1];
                odd[2*i] = data[2*(i + n/2)];
                odd[2*i+1] = data[2*(i + n/2)+1];
            }

            // Recursão
            fft1d(even, n/2, inverse);
            fft1d(odd, n/2, inverse);

            // Combina os resultados
            double angle = 2 * Math.PI / n * (inverse ? 1 : -1);
            double wReal = 1;
            double wImag = 0;
            double wReal_step = Math.cos(angle);
            double wImag_step = Math.sin(angle);

            for (int i = 0; i < n/2; i++) {
                double oddReal = odd[2*i];
                double oddImag = odd[2*i+1];
                
                double temp = wReal * oddReal - wImag * oddImag;
                oddImag = wReal * oddImag + wImag * oddReal;
                oddReal = temp;

                data[2*i] = even[2*i] + oddReal;
                data[2*i+1] = even[2*i+1] + oddImag;
                data[2*(i + n/2)] = even[2*i] - oddReal;
                data[2*(i + n/2)+1] = even[2*i+1] - oddImag;

                temp = wReal * wReal_step - wImag * wImag_step;
                wImag = wReal * wImag_step + wImag * wReal_step;
                wReal = temp;
            }
        }
    }
}
