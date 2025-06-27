package Criptografia;

import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

public class DES {

    // Tabela de permutação inicial
    private static final int[] IP = {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };

    // Tabela de permutação final (inversa da inicial)
    private static final int[] FP = {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
    };

    // Tabela de expansão (32 bits -> 48 bits)
    private static final int[] E = {
        32, 1, 2, 3, 4, 5,
        4, 5, 6, 7, 8, 9,
        8, 9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32, 1
    };

    // Permutação P
    private static final int[] P = {
        16, 7, 20, 21, 29, 12, 28, 17,
        1, 15, 23, 26, 5, 18, 31, 10,
        2, 8, 24, 14, 32, 27, 3, 9,
        19, 13, 30, 6, 22, 11, 4, 25
    };

    // S-boxes
    private static final int[][][] S = {
        {{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
         {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
         {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
         {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}},
        
        {{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
         {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
         {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
         {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}},
        
        {{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
         {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
         {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
         {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}},
        
        {{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
         {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
         {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
         {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}},
        
        {{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
         {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
         {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
         {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}},
        
        {{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
         {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
         {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
         {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}},
        
        {{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
         {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
         {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
         {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}},
        
        {{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
         {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
         {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
         {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}}
    };

    // Permutação de escolha 1 para chave
    private static final int[] PC1 = {
        57, 49, 41, 33, 25, 17, 9,
        1, 58, 50, 42, 34, 26, 18,
        10, 2, 59, 51, 43, 35, 27,
        19, 11, 3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
        7, 62, 54, 46, 38, 30, 22,
        14, 6, 61, 53, 45, 37, 29,
        21, 13, 5, 28, 20, 12, 4
    };

    // Permutação de escolha 2 para chave
    private static final int[] PC2 = {
        14, 17, 11, 24, 1, 5,
        3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8,
        16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55,
        30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53,
        46, 42, 50, 36, 29, 32
    };

    // Número de deslocamentos para cada rodada
    private static final int[] SHIFTS = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

    public static byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    // Aplicar permutação
    private static long permute(long input, int[] table, int inputBits) {
        long output = 0;
        for (int i = 0; i < table.length; i++) {
            if ((input & (1L << (inputBits - table[i]))) != 0) {
                output |= (1L << (table.length - 1 - i));
            }
        }
        return output;
    }

    // Rotação à esquerda
    private static int rotateLeft(int value, int positions, int bits) {
        return ((value << positions) | (value >>> (bits - positions))) & ((1 << bits) - 1);
    }

    // Gerar chaves para todas as rodadas
    private static long[] generateKeys(long key) {
        long[] keys = new long[16];
        
        // Aplicar PC1
        long keyPC1 = permute(key, PC1, 64);
        
        // Dividir em duas metades
        int c = (int) (keyPC1 >>> 28);
        int d = (int) (keyPC1 & 0x0FFFFFFF);
        
        for (int i = 0; i < 16; i++) {
            // Aplicar rotação
            c = rotateLeft(c, SHIFTS[i], 28);
            d = rotateLeft(d, SHIFTS[i], 28);
            
            // Combinar e aplicar PC2
            long combined = ((long) c << 28) | d;
            keys[i] = permute(combined, PC2, 56);
        }
        
        return keys;
    }

    // Função F do DES
    private static int f(int r, long k) {
        // Expansão E
        long expanded = permute(r, E, 32);
        
        // XOR com a chave
        expanded ^= k;
        
        // Aplicar S-boxes
        int result = 0;
        for (int i = 0; i < 8; i++) {
            int sixBits = (int) ((expanded >>> (42 - i * 6)) & 0x3F);
            int row = ((sixBits & 0x20) >>> 4) | (sixBits & 0x01);
            int col = (sixBits >>> 1) & 0x0F;
            result = (result << 4) | S[i][row][col];
        }
        
        // Aplicar permutação P
        return (int) permute(result, P, 32);
    }

    // Criptografar um bloco de 8 bytes
    private static long encryptBlock(long block, long key) {
        long[] keys = generateKeys(key);
        
        // Permutação inicial
        long ip = permute(block, IP, 64);
        
        // Dividir em duas metades
        int l = (int) (ip >>> 32);
        int r = (int) (ip & 0xFFFFFFFFL);
        
        // 16 rodadas
        for (int i = 0; i < 16; i++) {
            int temp = r;
            r = l ^ f(r, keys[i]);
            l = temp;
        }
        
        // Combinar (R16, L16)
        long combined = ((long) r << 32) | (l & 0xFFFFFFFFL);
        
        // Permutação final
        return permute(combined, FP, 64);
    }

    // Descriptografar um bloco de 8 bytes
    private static long decryptBlock(long block, long key) {
        long[] keys = generateKeys(key);
        
        // Permutação inicial
        long ip = permute(block, IP, 64);
        
        // Dividir em duas metades
        int l = (int) (ip >>> 32);
        int r = (int) (ip & 0xFFFFFFFFL);
        
        // 16 rodadas (chaves em ordem reversa)
        for (int i = 15; i >= 0; i--) {
            int temp = r;
            r = l ^ f(r, keys[i]);
            l = temp;
        }
        
        // Combinar (R16, L16)
        long combined = ((long) r << 32) | (l & 0xFFFFFFFFL);
        
        // Permutação final
        return permute(combined, FP, 64);
    }

    // Converter byte array para long
    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        return result;
    }

    // Converter long para byte array
    private static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        return bytes;
    }

    // Aplicar padding PKCS5
    private static byte[] addPadding(byte[] data) {
        int blockSize = 8;
        int paddingSize = blockSize - (data.length % blockSize);
        if (paddingSize == 0) paddingSize = blockSize;
        
        byte[] padded = new byte[data.length + paddingSize];
        System.arraycopy(data, 0, padded, 0, data.length);
        
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) paddingSize;
        }
        
        return padded;
    }

    // Remover padding PKCS5
    private static byte[] removePadding(byte[] data) {
        int paddingSize = data[data.length - 1] & 0xFF;
        return Arrays.copyOf(data, data.length - paddingSize);
    }

    public static void criptografia(String arquivoCripitografado, Scanner sc, String binarioFile) {
        try {
            // Palavra escolhida como senha
            System.out.println("Digite a chave para criptografia: ");
            String senha = sc.nextLine();

            // Gera hash da senha (por exemplo com MD5) e pega os 8 primeiros bytes
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(senha.getBytes("UTF-8"));
            byte[] chaveDESBytes = Arrays.copyOf(hash, 8);
            long chaveDES = bytesToLong(chaveDESBytes);

            try (RandomAccessFile raf = new RandomAccessFile(binarioFile, "r");
                 RandomAccessFile raf1 = new RandomAccessFile(arquivoCripitografado, "rw")) {

                raf.seek(0);
                
                // Lê o último ID dos registros
                int ultimo = raf.readInt();
                byte[] ultimoBytes = intToBytes(ultimo);
                ultimoBytes = addPadding(ultimoBytes);
                
                // Criptografa o último ID
                for (int i = 0; i < ultimoBytes.length; i += 8) {
                    byte[] block = Arrays.copyOfRange(ultimoBytes, i, i + 8);
                    long blockLong = bytesToLong(block);
                    long encrypted = encryptBlock(blockLong, chaveDES);
                    raf1.write(longToBytes(encrypted));
                }

                int tmp = 1;
                while (raf.getFilePointer() < raf.length()) {
                    // Lê o tamanho do próximo objeto Filme
                    int size = raf.readInt();
                    byte[] sizeBytes = intToBytes(size);
                    sizeBytes = addPadding(sizeBytes);
                    
                    // Criptografa o tamanho
                    for (int i = 0; i < sizeBytes.length; i += 8) {
                        byte[] block = Arrays.copyOfRange(sizeBytes, i, i + 8);
                        long blockLong = bytesToLong(block);
                        long encrypted = encryptBlock(blockLong, chaveDES);
                        raf1.write(longToBytes(encrypted));
                    }

                    // Lê os bytes do filme
                    byte[] filmeBytes = new byte[size];
                    raf.readFully(filmeBytes);
                    filmeBytes = addPadding(filmeBytes);
                    
                    // Escreve o tamanho criptografado dos dados do filme
                    byte[] tamanhoCriptBytes = intToBytes(filmeBytes.length);
                    tamanhoCriptBytes = addPadding(tamanhoCriptBytes);
                    for (int i = 0; i < tamanhoCriptBytes.length; i += 8) {
                        byte[] block = Arrays.copyOfRange(tamanhoCriptBytes, i, i + 8);
                        long blockLong = bytesToLong(block);
                        long encrypted = encryptBlock(blockLong, chaveDES);
                        raf1.write(longToBytes(encrypted));
                    }
                    
                    // Criptografa os dados do filme
                    for (int i = 0; i < filmeBytes.length; i += 8) {
                        byte[] block = Arrays.copyOfRange(filmeBytes, i, i + 8);
                        long blockLong = bytesToLong(block);
                        long encrypted = encryptBlock(blockLong, chaveDES);
                        raf1.write(longToBytes(encrypted));
                    }

                    System.out.println("Registro " + tmp + " - Tamanho original: " + size + 
                                     ", Tamanho criptografado: " + filmeBytes.length);
                    tmp++;
                }

                System.out.println("Criptografia concluída com sucesso!");

            } catch (Exception e) {
                System.out.println("Erro ao ler o arquivo binário: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void descriptografia(String arquivoCripitografado, Scanner sc, String arquivoDescriptografado) {
        try {
            // Palavra escolhida como chave
            System.out.println("Digite a chave para descriptografia: ");
            String senha = sc.nextLine();

            // Gera hash da senha (por exemplo com MD5) e pega os 8 primeiros bytes
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(senha.getBytes("UTF-8"));
            byte[] chaveDESBytes = Arrays.copyOf(hash, 8);
            long chaveDES = bytesToLong(chaveDESBytes);

            System.out.println("Chave gerada: " + Arrays.toString(chaveDESBytes));

            try (RandomAccessFile raf = new RandomAccessFile(arquivoCripitografado, "r");
                 RandomAccessFile raf1 = new RandomAccessFile(arquivoDescriptografado, "rw")) {

                // Lê o último ID (8 bytes criptografados)
                byte[] ultimoBytes = new byte[8];
                raf.readFully(ultimoBytes);
                long ultimoLong = bytesToLong(ultimoBytes);
                long decryptedUltimo = decryptBlock(ultimoLong, chaveDES);
                byte[] ultimoDecrypted = removePadding(longToBytes(decryptedUltimo));
                int ultimo = ByteBuffer.wrap(ultimoDecrypted).getInt();
                raf1.writeInt(ultimo);
                
                System.out.println("Último ID descriptografado: " + ultimo);

                int contador = 0;
                while (raf.getFilePointer() < raf.length()) {
                    contador++;
                    System.out.println("Processando registro " + contador);

                    // Lê o tamanho original (8 bytes criptografados)
                    byte[] sizeBytes = new byte[8];
                    raf.readFully(sizeBytes);
                    long sizeLong = bytesToLong(sizeBytes);
                    long decryptedSize = decryptBlock(sizeLong, chaveDES);
                    byte[] sizeDecrypted = removePadding(longToBytes(decryptedSize));
                    int tamanhoOriginal = ByteBuffer.wrap(sizeDecrypted).getInt();
                    
                    // Lê o tamanho criptografado (8 bytes criptografados)
                    byte[] tamanhoCriptBytes = new byte[8];
                    raf.readFully(tamanhoCriptBytes);
                    long tamanhoCriptLong = bytesToLong(tamanhoCriptBytes);
                    long decryptedTamanhoCript = decryptBlock(tamanhoCriptLong, chaveDES);
                    byte[] tamanhoCriptDecrypted = removePadding(longToBytes(decryptedTamanhoCript));
                    int tamanhoCriptografado = ByteBuffer.wrap(tamanhoCriptDecrypted).getInt();
                    
                    System.out.println("Tamanho original: " + tamanhoOriginal + 
                                     ", Tamanho criptografado: " + tamanhoCriptografado);

                    // Lê os dados criptografados
                    byte[] filmeBytes = new byte[tamanhoCriptografado];
                    raf.readFully(filmeBytes);
                    
                    // Descriptografa os dados
                    byte[] filmeDecrypted = new byte[tamanhoCriptografado];
                    for (int i = 0; i < tamanhoCriptografado; i += 8) {
                        byte[] block = Arrays.copyOfRange(filmeBytes, i, i + 8);
                        long blockLong = bytesToLong(block);
                        long decrypted = decryptBlock(blockLong, chaveDES);
                        byte[] decryptedBlock = longToBytes(decrypted);
                        System.arraycopy(decryptedBlock, 0, filmeDecrypted, i, 8);
                    }
                    
                    // Remove padding e ajusta para o tamanho original
                    byte[] filmeFinal = removePadding(filmeDecrypted);
                    filmeFinal = Arrays.copyOf(filmeFinal, tamanhoOriginal);
                    
                    raf1.writeInt(tamanhoOriginal);
                    raf1.write(filmeFinal);
                }

                System.out.println("Descriptografia concluída com sucesso!");

            } catch (Exception e) {
                System.out.println("Erro ao ler o arquivo binário: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}