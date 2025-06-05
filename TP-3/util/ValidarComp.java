package util;

import java.util.Arrays;

public class ValidarComp {

    public static void ValidadeComp(byte[] dados, byte[] dadosDecodificados) {
        
        // Comparar os arrays
        boolean dadosIdenticos = Arrays.equals(dados, dadosDecodificados);
        System.out.println("Dados preservados após compressão/descompressão: " + 
                        (dadosIdenticos ? "SIM" : "NÃO"));
        
        // Se não forem idênticos, encontrar onde diferem
        if (!dadosIdenticos) {
            int diferenças = 0;
            for (int j = 0; j < Math.min(dados.length, dadosDecodificados.length); j++) {
                if (dados[j] != dadosDecodificados[j]) {
                    System.out.println("Diferença no byte " + j + ": original=" + 
                                    dados[j] + ", descompactado=" + dadosDecodificados[j]);
                    if (++diferenças >= 10) break; // Limitar o número de diferenças mostradas
                }
            }
            if (dados.length != dadosDecodificados.length) {
                System.out.println("Tamanhos diferentes: original=" + 
                                dados.length + ", descompactado=" + dadosDecodificados.length);
            }
        }

    } 
    
}
