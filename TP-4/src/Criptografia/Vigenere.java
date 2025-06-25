package Criptografia;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Vigenere {

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

    public static void criptografia(String arquivoCripitografado, Scanner sc, String binarioFile){

        System.out.println("Digite a chave de criptografia: ");
        String chave = sc.nextLine();
        
        byte[] chaveBytes = chave.getBytes();

        try(RandomAccessFile raf1 = new RandomAccessFile(arquivoCripitografado, "rw")) {
        
            try(RandomAccessFile raf = new RandomAccessFile(binarioFile, "r")) {
                
                raf.seek(0); // Garante que estamos no início do arquivo
                
                // Lê o último ID dos registros
                int ultimo = raf.readInt();
                byte[] ultimoBytes = intToBytes(ultimo);
                ultimoBytes = somaBytes(chaveBytes, ultimoBytes, false); // Criptografa o último ID
                raf1.write(ultimoBytes); // Escreve o último ID no arquivo criptografado

                int tmp = 1;

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

                    System.out.println(tmp);

                    tmp++;

                }


            } catch (Exception e) {
                System.out.println("Erro ao criptografar: " + e.getMessage());
                e.printStackTrace();
            }

        }catch (Exception e) {
            System.out.println("Erro ao criptografar: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void descriptografia(String arquivoCripitografado, Scanner sc, String arquivoDescriptografado){

        System.out.println("Digite a chave de descriptografia: ");
        String chave = sc.nextLine();
        
        byte[] chaveBytes = chave.getBytes();

        try(RandomAccessFile raf1 = new RandomAccessFile(arquivoCripitografado, "r")) {
        
            try(RandomAccessFile raf = new RandomAccessFile(arquivoDescriptografado, "rw")) {
                
                byte[] ultimoBytes = new byte[4];
                raf1.readFully(ultimoBytes); // Lê os bytes do último ID
                ultimoBytes = somaBytes(chaveBytes, ultimoBytes, true); // Descriptografa o último ID
                int ultimo = ByteBuffer.wrap(ultimoBytes).getInt(); // Converte de volta para int
                raf.writeInt(ultimo); // Escreve o último ID no arquivo descriptografado

                while (raf1.getFilePointer() < raf1.length()) {
                    
                    // Lê o tamanho do próximo objeto Filme
                    byte[] sizeBytes = new byte[4];
                    raf1.readFully(sizeBytes);
                    sizeBytes = somaBytes(chaveBytes, sizeBytes, true); // Descriptografa o tamanho
                    int tamanho = ByteBuffer.wrap(sizeBytes).getInt(); // Converte de volta para int

                    // Lê os bytes do filme
                    byte[] filmeBytes = new byte[tamanho];
                    raf1.readFully(filmeBytes);
                    byte[] filmeDescriptografado = somaBytes(chaveBytes, filmeBytes, true); // Descriptografa os bytes do filme
                    
                    // Escreve o tamanho descriptografado e os bytes descriptografados no arquivo
                    raf.writeInt(tamanho);
                    raf.write(filmeDescriptografado);
                }

            } catch (Exception e) {
                System.out.println("Erro ao descriptografar: " + e.getMessage());
            }

        }catch (Exception e) {
            System.out.println("Erro ao descriptografar: " + e.getMessage());
        }

    }
    
}
