package CasamentoDePadrao;

import classes.Filmes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.Scanner;
import util.Pesquisar;

public class KMP{
    
    // Constrói o vetor LPS (prefixo mais longo que também é sufixo)
    public static void construirLps(String padrao, int[] lps) {
        
        //armazena o tamanho do maior prefixo que também é sufixo
        int comprimento = 0;

        // Define o primeiro valor de LPS é sempre 0
        lps[0] = 0;

        int i = 1;
        while (i < padrao.length()) {
            
            // Se os caracteres coincidirem, incrementa o comprimento do LPS
            if (padrao.charAt(i) == padrao.charAt(comprimento)) {
                comprimento++;
                lps[i] = comprimento;
                i++;
            } else {
                if (comprimento != 0) {
                    // Volta para o valor anterior do LPS, evitando comparações desnecessárias
                    comprimento = lps[comprimento - 1];
                } else {
                    // Nenhum prefixo correspondente encontrado
                    lps[i] = 0;
                    i++;
                }
            }
        }
    }

    // Função que realiza a busca do padrão no texto
    public static Boolean buscar(String padrao, String texto) {
        int tamanhoTexto = texto.length();
        int tamanhoPadrao = padrao.length();

        int[] lps = new int[tamanhoPadrao]; // Vetor LPS
        construirLps(padrao, lps); // Pré-processamento do padrão

        // Ponteiros para texto (i) e padrão (j)
        int i = 0;
        int j = 0;

        while (i < tamanhoTexto) {
            // Se os caracteres coincidirem, avança ambos ponteiros
            if (texto.charAt(i) == padrao.charAt(j)) {
                i++;
                j++;

                // Padrão completo encontrado
                if (j == tamanhoPadrao) {
                    
                    return true;

                }
            } else {
                // Se houver uma falha na correspondência
                if (j != 0) {
                    // Volta para a posição anterior do LPS
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return false; // Padrão não encontrado;
    }

    //Função que inicia a busca do padrão no arquivo binário
    public static void inicio(String binarioFile, Scanner sc , String padrao){

        try (RandomAccessFile dis = new RandomAccessFile(binarioFile, "r")) {
            int Ultimo = dis.readInt(); // Lê o último ID registrado

            int tmpBusca = 0; // Variável para contar o número de buscas realizadas
            
            while (dis.getFilePointer() < dis.length()) {
                int size = dis.readInt(); // Lê o tamanho do objeto
                byte[] FilmeBytes = new byte[size];
                dis.readFully(FilmeBytes); // Lê os dados do objeto
    
                //Verifica filme por filme para encontrar o padrão
                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Filmes filme = (Filmes) ois.readObject(); // Converte os bytes para objeto Filmes
                    //Se o filme não tiver lapide, verifica se o padrão está presente
                    if (!filme.getLAPIDE()) {
                        
                        if(filme != null){
                                
                            for(int i = 1; i <= 7; i++){

                                // Pesquisa o tópico no filme
                                String texto = Pesquisar.pesquisarTopico(i, filme);

                                //Busca o padrão no filme
                                Boolean encontrado = KMP.buscar(padrao, texto);

                                //Se for encontrado printa o filme
                                if(encontrado){

                                    filme.Ler();
                                    break;

                                }

                            }
                        }

                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
