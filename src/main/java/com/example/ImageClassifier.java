package com.example; // Pacote correto, correspondendo à pasta

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

// Nome da classe correto
public class ImageClassifier {

    public static void main(String[] args) {
        try {
            String imagePath = "binary.jpg";
            String promptText = "O que você vê nesta imagem? Descreva em detalhes.";
            File inputFile = new File("binary.jpg");
            BufferedImage originalImage = ImageIO.read(inputFile);
            System.out.println("Imagem lida com sucesso!");

            classifyImage(originalImage);

        } catch (Exception e) {
            System.err.println("Ocorreu um erro no método principal: ");
            e.printStackTrace();
        }
    }

    public static void classifyImage(BufferedImage originalImage) {
        // SUBSTITUA PELA SUA CHAVE DE API REAL
       Client client = Client.builder().apiKey("AIzaSyBBjHPfkrFs0a0Tcwqj2Nfn6cl_M_M6kDc").build(); 
        
        try {
            String vector = toVector(originalImage);
            
            
           

            GenerateContentResponse response =
                client.models.generateContent("gemini-2.0-flash-001", "i will send to you a vector of a binary image, you need to find out what is this image: " + vector, null);

        // Gets the text string from the response by the quick accessor method `text()`.
        System.out.println("Unary response: " + response.text());

            

        } catch (Exception e) {
            System.err.println("Ocorreu um erro ao chamar a API Gemini. Verifique sua CHAVE DE API e se o ARQUIVO DE IMAGEM existe.");
            e.printStackTrace();
        }
    }

    public static String toVector(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] histogram = new int[256];

        // Calcula o histograma de tons de cinza
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                // Conversão para tons de cinza
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                histogram[gray]++;
            }
        }

        // Normaliza o histograma e monta a string
        int totalPixels = width * height;
        StringBuilder sb = new StringBuilder();
        sb.append("Histograma de tons de cinza normalizado da imagem: [");
        for (int i = 0; i < histogram.length; i++) {
            double normalized = (double) histogram[i] / totalPixels;
            sb.append(String.format("%.4f", normalized));
            if (i < histogram.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    
    }
}