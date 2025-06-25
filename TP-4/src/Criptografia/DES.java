package Criptografia;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DES {

    public static byte[] XOR(byte[] L, byte[] R){

        byte[] resultado = new byte[L.length];
        for (int i = 0; i < L.length; i++) {
            resultado[i] = (byte) (L[i] ^ R[i]);
        }
        return resultado;

    }

    public static byte[] intToBytes(int value) {
        
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();

    }

    public static void criptografia(String arquivoCripitografado, Scanner sc, String binarioFile, String arquivoDescriptografado){

        System.out.println("Digite a chave de criptografia: ");
        String chave = sc.nextLine();
        
        try(RandomAccessFile raf1 = new RandomAccessFile(arquivoCripitografado, "rw")) {
        
            try(RandomAccessFile raf = new RandomAccessFile(binarioFile, "r")) {
                
                raf.seek(0); // Garante que estamos no início do arquivo
                
                // Lê o último ID dos registros
                int ultimo = raf.readInt();
                byte[] ultimoBytes = intToBytes(ultimo);

                
            } catch (Exception e) {
                System.out.println("Erro ao criptografar: " + e.getMessage());
                e.printStackTrace();
            }

        }catch (Exception e) {
            System.out.println("Erro ao criptografar: " + e.getMessage());
            e.printStackTrace();
        }

    }

    

    public static void teste(String arquivoCripitografado, Scanner sc, String binarioFile) {

        try{

            // Palavra escolhida como senha
            System.out.println("Digite a chave para criptografia: ");
            String senha = sc.nextLine();

            // Gera hash da senha (por exemplo com MD5) e pega os 8 primeiros bytes
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(senha.getBytes("UTF-8"));
            byte[] chaveDESBytes = Arrays.copyOf(hash, 8); // DES = 8 bytes (64 bits)

            // Cria a chave DES
            SecretKey chaveDES = new SecretKeySpec(chaveDESBytes, "DES");

            Cipher cifraDES;

            System.out.println("Chave gerada para criptografia: " + Arrays.toString(chaveDESBytes));

            // Cria a cifra
            cifraDES = Cipher.getInstance("DES/ECB/PKCS5Padding");

            // Inicializa a cifra para o processo de encriptação
            cifraDES.init(Cipher.ENCRYPT_MODE, chaveDES);

            try(RandomAccessFile raf = new RandomAccessFile(binarioFile, "r");
            RandomAccessFile raf1 = new RandomAccessFile(arquivoCripitografado, "rw")) {

            raf.seek(0); // Garante que estamos no início do arquivo
            
            // Lê o último ID dos registros
            int ultimo = raf.readInt();
            byte[] ultimoBytes = intToBytes(ultimo);
            ultimoBytes = cifraDES.doFinal(ultimoBytes); // Encripta o último ID
            raf1.write(ultimoBytes); // Escreve o último ID no arquivo criptografado

            int tmp = 1;

            while (raf.getFilePointer() < raf.length()) {
                
                // Lê o tamanho do próximo objeto Filme
                int size = raf.readInt();
                byte[] sizeBytes = intToBytes(size);
                sizeBytes = cifraDES.doFinal(sizeBytes); // Encripta o tamanho

                // Lê os bytes do filme
                byte[] filmeBytes = new byte[size];
                raf.readFully(filmeBytes);
                byte[] filmeCriptografado = cifraDES.doFinal(filmeBytes); // Encripta os bytes do filme
                
                // CORREÇÃO: Salva também o tamanho dos dados criptografados
                byte[] tamanhoCriptografadoBytes = intToBytes(filmeCriptografado.length);
                tamanhoCriptografadoBytes = cifraDES.doFinal(tamanhoCriptografadoBytes);
                
                // Escreve: tamanho original, tamanho criptografado, dados criptografados
                raf1.write(sizeBytes);                    // 8 bytes - tamanho original criptografado
                raf1.write(tamanhoCriptografadoBytes);    // 8 bytes - tamanho criptografado criptografado
                raf1.write(filmeCriptografado);           // X bytes - dados criptografados

                System.out.println("Registro " + tmp + " - Tamanho original: " + size +  ", Tamanho criptografado: " + filmeCriptografado.length);
                
            }

            } catch (Exception e) {
                System.out.println("Erro ao ler o arquivo binário: " + e.getMessage());
                e.printStackTrace();
            }

        }catch(NoSuchAlgorithmException e){
                e.printStackTrace();
        }catch(NoSuchPaddingException e){
                e.printStackTrace();
        }catch(InvalidKeyException e){
                e.printStackTrace();
        }catch(UnsupportedEncodingException e){
                e.printStackTrace();
        }

    }

    public static void descriptografia(String arquivoCripitografado, Scanner sc, String arquivoDescriptografado){

        try{

            // Palavra escolhida como chave
            System.out.println("Digite a chave para descriptografia: ");
            String senha = sc.nextLine();

            // Gera hash da senha (por exemplo com MD5) e pega os 8 primeiros bytes
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(senha.getBytes("UTF-8"));
            byte[] chaveDESBytes = Arrays.copyOf(hash, 8); // DES = 8 bytes (64 bits)

            // DEBUG: Mostrar a chave gerada
            System.out.println("Chave gerada: " + Arrays.toString(chaveDESBytes));

            // Cria a chave DES
            SecretKey chaveDES = new SecretKeySpec(chaveDESBytes, "DES");

            Cipher cifraDES;

            // Cria a cifra
            cifraDES = Cipher.getInstance("DES/ECB/PKCS5Padding");

            // Inicializa a cifra também para o processo de decriptação
            cifraDES.init(Cipher.DECRYPT_MODE, chaveDES);

            try(RandomAccessFile raf = new RandomAccessFile(arquivoCripitografado, "r");
            RandomAccessFile raf1 = new RandomAccessFile(arquivoDescriptografado, "rw")) {

                // Lê o último ID (sempre 8 bytes quando criptografado)
                byte[] ultimoBytes = new byte[8];
                raf.readFully(ultimoBytes);
                ultimoBytes = cifraDES.doFinal(ultimoBytes);
                int ultimo = ByteBuffer.wrap(ultimoBytes).getInt();
                raf1.writeInt(ultimo);
                
                System.out.println("Último ID descriptografado: " + ultimo);

                int contador = 0;
                while (raf.getFilePointer() < raf.length()) {
                    contador++;
                    System.out.println("Processando registro " + contador);
                    System.out.println("Posição atual no arquivo: " + raf.getFilePointer());

                    // Lê o tamanho original (sempre 8 bytes quando criptografado)
                    byte[] sizeBytes = new byte[8];
                    raf.readFully(sizeBytes);
                    sizeBytes = cifraDES.doFinal(sizeBytes);
                    int tamanhoOriginal = ByteBuffer.wrap(sizeBytes).getInt();
                    
                    // CORREÇÃO: Lê o tamanho criptografado (sempre 8 bytes quando criptografado)
                    byte[] tamanhoCriptografadoBytes = new byte[8];
                    raf.readFully(tamanhoCriptografadoBytes);
                    tamanhoCriptografadoBytes = cifraDES.doFinal(tamanhoCriptografadoBytes);
                    int tamanhoCriptografado = ByteBuffer.wrap(tamanhoCriptografadoBytes).getInt();
                    
                    System.out.println("Tamanho original: " + tamanhoOriginal + 
                                     ", Tamanho criptografado: " + tamanhoCriptografado);

                    // Lê exatamente o número de bytes criptografados
                    byte[] filmeBytes = new byte[tamanhoCriptografado];
                    raf.readFully(filmeBytes);
                    byte[] filmeDescriptografado = cifraDES.doFinal(filmeBytes);
                    
                    // Remove padding extra, mantendo apenas o tamanho original
                    byte[] filmeFinal = Arrays.copyOf(filmeDescriptografado, tamanhoOriginal);
                    
                    raf1.writeInt(tamanhoOriginal);
                    raf1.write(filmeFinal);
                    
                    System.out.println("Registro " + contador + " processado com sucesso");
                }

            }catch (Exception e) {
                System.out.println("Erro ao ler o arquivo binário: " + e.getMessage());
                e.printStackTrace();
            }

        }catch(NoSuchAlgorithmException e){
                e.printStackTrace();
        }catch(NoSuchPaddingException e){
                e.printStackTrace();
        }catch(InvalidKeyException e){
                e.printStackTrace();
        }catch(UnsupportedEncodingException e){
                e.printStackTrace();
        }

    }

}
