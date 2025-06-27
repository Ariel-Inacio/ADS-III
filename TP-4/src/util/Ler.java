package util;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;

import classes.Filmes;

public class Ler {
    // Le o arquivo binario
    public static void lerBinario(String binarioFile) {
        try (RandomAccessFile in = new RandomAccessFile(binarioFile, "r")) {
            // Lendo o último ID dos registros
            int ultimo = in.readInt();
            System.out.println(ultimo);
            
            // Lendo os filmes armazenados no arquivo binário
            while (in.getFilePointer() < in.length()) {
                
                // Lê o tamanho do próximo objeto Filme
                int size = in.readInt();
                byte[] FilmeBytes = new byte[size];
                in.readFully(FilmeBytes);
    
                // Converte os bytes de volta para um objeto Filme
                try (ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {
    
                    Filmes filme = (Filmes) ois.readObject();
                    
                    // Verifica se o registro não está marcado como excluído antes de exibir
                    if (!filme.getLAPIDE()) {
                        filme.Ler();
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }
    
        } catch (EOFException e) {
            System.out.println("Fim do arquivo atingido.");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado.");
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
