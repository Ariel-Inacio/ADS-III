package CasamentoDePadrao;

import classes.Filmes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.Scanner;
import util.Pesquisar;

public class BoyerMoore{

    static int TOTAL_CARACTERES = 131072;

    // Função utilitária para retornar o maior entre dois inteiros
    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    // Função de pré-processamento para a heurística do caractere ruim
    public static void calcularUltimaOcorrencia(char[] padrao, int tamanho, int[] ultimaOcorrencia) {
        // Inicializa todas as ocorrências como -1
        for (int i = 0; i < TOTAL_CARACTERES; i++) {
            ultimaOcorrencia[i] = -1;
        }

        // Preenche a última posição em que cada caractere aparece no padrão
        for (int i = 0; i < tamanho; i++) {
            ultimaOcorrencia[(int) padrao[i]] = i;
        }
    }

    // Função que realiza a busca do padrão no texto usando a heurística do caractere ruim
    public static boolean buscarPadrao(char[] padrao, char[] texto) {
        int tamanhoPadrao = padrao.length;
        int tamanhoTexto = texto.length;

        int[] ultimaOcorrencia = new int[TOTAL_CARACTERES];

        // Pré-processamento do padrão
        calcularUltimaOcorrencia(padrao, tamanhoPadrao, ultimaOcorrencia);

        int deslocamento = 0; // deslocamento do padrão em relação ao texto

        // Enquanto houver possibilidade de alinhamento do padrão com o texto
        while (deslocamento <= (tamanhoTexto - tamanhoPadrao)) {
            int indicePadrao = tamanhoPadrao - 1;

            // Compara os caracteres do padrão com os do texto da direita para a esquerda
            while (indicePadrao >= 0 && padrao[indicePadrao] == texto[deslocamento + indicePadrao]) {
                indicePadrao--;
            }

            // Se todos os caracteres coincidem
            if (indicePadrao < 0) {
                return true; // Padrão encontrado

            } else {
                // Desloca o padrão de acordo com a última ocorrência do caractere ruim
                deslocamento += max(1, indicePadrao - ultimaOcorrencia[texto[deslocamento + indicePadrao]]);
            }
        }

        return false; // Padrão não encontrado
    }

    public static void inicio(String binarioFile, Scanner sc){

        try (RandomAccessFile dis = new RandomAccessFile(binarioFile, "r")) {
            int Ultimo = dis.readInt(); // Lê o último ID registrado

            System.err.println("\tDigite o que deseja pesquisar:");
            String padrao = sc.nextLine();

            int tmpBusca = 0; // Variável para contar o número de buscas realizadas
            
            while (dis.getFilePointer() < dis.length()) {
                int size = dis.readInt(); // Lê o tamanho do objeto
                byte[] FilmeBytes = new byte[size];
                dis.readFully(FilmeBytes); // Lê os dados do objeto
    
                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Filmes filme = (Filmes) ois.readObject(); // Converte os bytes para objeto Filmes
                    if (!filme.getLAPIDE()) {
                        
                        if(filme != null){
                                
                            for(int i = 1; i <= 7; i++){

                                String texto = Pesquisar.pesquisarTopico(i, filme);

                                Boolean encontrado = buscarPadrao(padrao.toCharArray(), texto.toCharArray());

                                if(encontrado){

                                    filme.Ler();
                                    break;

                                }

                            }

                            tmpBusca++;

                            System.out.println(tmpBusca);

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