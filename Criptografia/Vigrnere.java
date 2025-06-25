package Criptografia;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Vigrnere {

    public static byte[] intToBytes(int value) {
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();

    }

    public static byte[] somaBytes(byte[] chave, byte[] mensagem, boolean decri) {

        byte[] resultado = new byte[mensagem.length];

        for(int i = 0; i < mensagem.length; i++){

            int m = mensagem[i] & 0xFF; // Converte byte para int
            int c = chave[i % chave.length] & 0xFF; // Converte byte da chave para int
            resultado [i] = (byte) ((m + (decri ? 256 - c : c)) % 256); // Soma e aplica módulo

        }

        return resultado;

    }

    public static void criptografia(String arquivoCripitografado, Scanner sc, String binarioFile, String arquivoDescriptografado){

        System.out.println("Digite a chave de criptografia: ");
        String chave = sc.nextLine();
        
        byte[] chaveBytes = chave.getBytes();

        try(RandomAccessFile raf1 = new RandomAccessFile(arquivoCripitografado, "wr")) {
        
            try(RandomAccessFile raf = new RandomAccessFile(binarioFile, "r")) {
                
                int ultimo = raf1.readInt();
                byte[] ultimoBytes = intToBytes(ultimo);
                ultimoBytes = somaBytes(chaveBytes, ultimoBytes, false); // Criptografa o último ID
                raf.write(ultimoBytes); // Escreve o último ID no arquivo criptografado

                while (raf.getFilePointer() < raf.length()) {
                    
                    // Lê o tamanho do próximo objeto Filme
                    int size = raf.readInt();
                    byte[] sizeBytes = intToBytes(size);
                    sizeBytes = somaBytes(chaveBytes, sizeBytes, false); // Criptografa o tamanho

                    // Lê os bytes do filme
                    byte[] filmeBytes = new byte[size];
                    raf.readFully(filmeBytes);
                    byte[] filmeCriptografado = somaBytes(chaveBytes, filmeBytes, false); // Criptografa os bytes do filme
                    
                    // Escreve o tamanho criptografado e os bytes criptografados no arquivo
                    raf1.write(sizeBytes);
                    raf1.write(filmeCriptografado);
                }


            } catch (Exception e) {
                System.out.println("Erro ao criptografar: " + e.getMessage());
            }

        }catch (Exception e) {
            System.out.println("Erro ao criptografar: " + e.getMessage());
        }

    }

    public
    
}
