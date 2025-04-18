package util;

import classes.Filmes;
import classes.RegistroID;
import indexacao.Arvore.ArvoreBMais;
import indexacao.Lista.ElementoLista;
import indexacao.Lista.ListaInvertida;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Pesquisar {
    //Função para pesquisar a abreviação do pais
    public static String PesquisarPaisAbre(String binarioFilePais, String NomePais){
        String resultado = "NOT";
        try(RandomAccessFile file = new RandomAccessFile(binarioFilePais, "r")){
            int NTotais = file.readInt(); // Lê o número total de registros
            
            for(int i = 0; i < NTotais; i++){
                int tamanhoObjeto = file.readInt(); // Lê o tamanho do objeto
                byte[] dadosFilmes = new byte[tamanhoObjeto];
                file.readFully(dadosFilmes); // Lê os dados do objeto
    
                ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosFilmes);
                DataInputStream dataIn = new DataInputStream(byteStream);
    
                String nome = dataIn.readUTF(); // Lê o nome do país
                if(nome.equals(NomePais)){
                    resultado = dataIn.readUTF(); // Retorna a abreviação do país
                    break;
                }
            }
        } catch (IOException e){
            System.out.println("Arquivo nao encontrado");
            e.printStackTrace();
        }
        return resultado;
    }

    //Função para pesquisar o ID de um filme na Arvore B+
    public static Filmes PesquisarIDArvore(String binarioFile, int IDDesejado, ArvoreBMais<RegistroID> arvore) {
        try (RandomAccessFile raf = new RandomAccessFile(binarioFile, "r")) {
            //Lê o arquivo binario
            
            ArrayList<RegistroID> lista = arvore.read(new RegistroID(IDDesejado, -1)); 

            if (lista.isEmpty()) {
                System.out.println("ID não encontrado.");
                return null; // Retorna null se o ID não for encontrado
            }

            else{
                raf.seek(lista.get(0).getOffset()); // Move o ponteiro para o offset do filme

                int size = raf.readInt(); // Lê o tamanho do objeto
                byte[] FilmeBytes = new byte[size];
                raf.readFully(FilmeBytes); // Lê os dados do objeto
                
                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Filmes filme = (Filmes) ois.readObject(); // Converte os bytes para objeto Filmes
                    if (!filme.getLAPIDE()) {
                        return filme; // Retorna o filme se não estiver marcado como excluído
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
        }catch (Exception e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Retorna null se o filme não for encontrado
    }

    //Métado para encontrar o maior ID entre os objetos presentes no arquivo binario
    public static int encontrarTamanho(String arquivo) {
        int quantidade = 0;
        try (RandomAccessFile in = new RandomAccessFile(arquivo, "r")) {
            int ultimo = in.readInt(); // Lê o último ID registrado
            
            while (in.getFilePointer() < in.length()) {
                int size = in.readInt(); // Lê o tamanho do objeto
                byte[] FilmeBytes = new byte[size];
                in.readFully(FilmeBytes); // Lê os dados do objeto
    
                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Filmes filme = (Filmes) ois.readObject();
                    if (filme.getID() > quantidade){
                        quantidade = filme.getID(); // Atualiza o maior ID encontrado
                    }
                } catch (ClassNotFoundException e) {
                    e.getMessage();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
        return quantidade;
    }

    public static String BuscarCriterio(int criterio) {
        String index = null;
        switch(criterio){
            case 1: index = "Movie/TV Show"; break; // Movie/TV Show
            case 3: index = "Diretor"; break; // Diretor
            case 4: index = "Pais"; break; // País
            case 5: index = "Data de Adição"; break; // Data de Adição
            case 6: index = "Ano de Lançamento"; break; // Ano de Lançamento
            case 7: index = "Classificação Indicativa"; break; // Classificação Indicativa
            case 9: index = "Gênero"; break; // Gênero
            default: System.out.println("Opcao invalida"); break;
        }
        return index;
    }

    public static ArrayList<Long> PesquisarLista(ListaInvertida lista, String chave) {
        ElementoLista[] resultado = null;
        ArrayList<Long> listaPosicoes = new ArrayList<>(); // Lista para armazenar as posições encontradas
        try {

            resultado = lista.read(chave); // Lê os elementos da lista invertida

            for(int i = 0; i < resultado.length; i++){
                if(resultado[i] != null){
                    listaPosicoes.add(resultado[i].getLocalizacao()); // Adiciona a posição à lista de resultados
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao pesquisar na lista invertida: " + e.getMessage());
        }

        return listaPosicoes; // Retorna os resultados encontrados

    }

    public static Filmes ListaFilmes(Long posicao, String binarioFile) {
        Filmes filme = null;
        try (RandomAccessFile raf = new RandomAccessFile(binarioFile, "r")) {
            raf.seek(posicao); // Move o ponteiro para a posição do filme

            int size = raf.readInt(); // Lê o tamanho do objeto
            byte[] FilmeBytes = new byte[size];
            raf.readFully(FilmeBytes); // Lê os dados do objeto
    
            try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                filme = (Filmes) ois.readObject(); // Converte os bytes para objeto Filmes

                if (!filme.getLAPIDE()) {
                    return filme; // Retorna o filme se não estiver marcado como excluído
                }

            } catch (ClassNotFoundException e) {
                System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }catch (Exception e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Retorna null se o filme não for encontrado
    }

}
