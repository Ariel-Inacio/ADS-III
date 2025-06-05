package util;

import classes.Filmes;
import classes.RegistroID;
import indexacao.Arvore.ArvoreBMais;
import indexacao.Hash.*;
import indexacao.Lista.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Scanner;

public class atualizar {
    // Método para atualizar o arquivo binario
    public static void atualizarFilmeID(int IDDesejado, Filmes novoFilme, String binarioFile, ArvoreBMais<RegistroID> arvore, int index, ListaInvertida lista1, ListaInvertida lista2, List<Integer> Criterios, HashExtensivel<ParID> hash) {
        try (RandomAccessFile file = new RandomAccessFile(binarioFile, "rw")) {
            int Ultimo = file.readInt(); // Lê o último ID armazenado no arquivo
            
            long posicaoInicial;
            int tamanhoObjeto;
            boolean encontrado = false;
            boolean incrementar = false;
            
            // Percorre o arquivo em busca do filme com o ID desejado
            while (file.getFilePointer() < file.length()) {
                posicaoInicial = file.getFilePointer();
                
                try {
                    tamanhoObjeto = file.readInt(); // Lê o tamanho do objeto
                } catch (EOFException e) {
                    break; // Se atingir o fim do arquivo, encerra a busca
                }
                
                if (tamanhoObjeto <= 0 || tamanhoObjeto > 1000000) {
                    System.out.println("Tamanho de objeto inválido detectado: " + tamanhoObjeto);
                    break;
                }
                
                byte[] dadosFilmes = new byte[tamanhoObjeto];
                try {
                    file.readFully(dadosFilmes); // Lê os bytes do objeto
                } catch (EOFException e) {
                    System.out.println("Não foi possível ler todos os bytes esperados.");
                    break;
                }
                
                try (ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosFilmes);
                     ObjectInputStream objectIn = new ObjectInputStream(byteStream)) {
                    
                    Filmes filmes = (Filmes) objectIn.readObject();
                    
                    if (!filmes.getLAPIDE() && filmes.getID() == IDDesejado) {
                        encontrado = true;
                        
                        ByteArrayOutputStream newByteStream = new ByteArrayOutputStream();
                        try (ObjectOutputStream objectOut = new ObjectOutputStream(newByteStream)) {
                            objectOut.writeObject(novoFilme);
                        }
                        byte[] novoBytes = newByteStream.toByteArray();
                        int novoTamanho = novoBytes.length;
                        
                        if (novoTamanho <= tamanhoObjeto) {
                            file.seek(posicaoInicial + 4);
                            file.write(novoBytes);

                            //Remove um Filme/Serie da arvore B+ caso o mesmo tenha sido excluido
                            if(novoFilme.getLAPIDE() == true && index == 1){
                                try{
                                    arvore.delete(new RegistroID(IDDesejado, -1)); // Remove o ID da árvore B+
                                }catch(Exception e){
                                    System.out.println("Erro ao remover o ID da arvore B+");
                                    e.printStackTrace();
                                }
                            }

                            //Atualiza o Filme/Serie removendo e inserindo novamente
                            else{

                                if(index == 1){
                                    try {
                                        // Remove o registro antigo
                                        arvore.delete(new RegistroID(IDDesejado, -1));
                                        
                                        // Se o filme não estiver marcado como excluído, reinsere com o mesmo offset
                                        if (!novoFilme.getLAPIDE()) {
                                            arvore.create(new RegistroID(novoFilme.getID(), posicaoInicial));
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Erro ao atualizar árvore B+");
                                        e.printStackTrace();
                                    }
                                }

                                else if(index == 2){
                                    try {
                                        // Remove o registro antigo
                                        hash.delete(ParID.hash(IDDesejado));
                                        // Se o filme não estiver marcado como excluído, reinsere com o mesmo offset
                                        if (!novoFilme.getLAPIDE()) {
                                            hash.create(new ParID(posicaoInicial, novoFilme.getID()));
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Erro ao atualizar árvore hash");
                                        e.printStackTrace();
                                    }
                                }

                                else if(index == 3){

                                    try {
                                        // Remove o registro antigo
                                        lista1.delete(null, IDDesejado);
                                        lista2.delete(null, IDDesejado);

                                        // Se o filme não estiver marcado como excluído, reinsere com o mesmo offset
                                        if (!novoFilme.getLAPIDE()) {

                                            Escrever.AdicionarListaInvertida(lista1, posicaoInicial, novoFilme, Criterios.get(0));
                                            Escrever.AdicionarListaInvertida(lista2, posicaoInicial, novoFilme, Criterios.get(1));
                                        }

                                    } catch (Exception e) {
                                        System.out.println("Erro ao atualizar lista invertida");
                                        e.printStackTrace();
                                    }

                                }
                            }
                            
                            int diferenca = tamanhoObjeto - novoTamanho;
                            if (diferenca > 0) {
                                byte[] zeros = new byte[diferenca];
                                file.write(zeros); // Preenche o espaço restante com zeros
                            }
                        } 
                        
                        else {
                            file.seek(posicaoInicial + 4);
                            filmes.setLAPIDE(true); // Marca o registro como excluído
                            
                            ByteArrayOutputStream lapideStream = new ByteArrayOutputStream();
                            try (ObjectOutputStream lapideOut = new ObjectOutputStream(lapideStream)) {
                                lapideOut.writeObject(filmes);
                            }
                            byte[] lapideBytes = lapideStream.toByteArray();
                            
                            if (lapideBytes.length <= tamanhoObjeto) {
                                file.write(lapideBytes);
                            } 
                            else {
                                file.writeBoolean(true); // Marca como excluído de maneira simplificada
                            }
                            
                            file.seek(file.length()); // Adiciona o novo registro no final do arquivo
                            long novaPosicao = file.getFilePointer();
                            file.writeInt(novoTamanho);
                            file.write(novoBytes);

                            if(index == 1){
                                try {
                                    // Primeiro remove o registro antigo
                                    arvore.delete(new RegistroID(IDDesejado, -1));
                                    
                                    // Se não estiver marcado como excluído, adiciona o novo
                                    if (!novoFilme.getLAPIDE()) {
                                        arvore.create(new RegistroID(novoFilme.getID(), novaPosicao));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            else if(index == 2){
                                try {
                                    // Remove o registro antigo
                                    hash.delete(ParID.hash(IDDesejado));
                                    // Se o filme não estiver marcado como excluído, reinsere com o mesmo offset
                                    if (!novoFilme.getLAPIDE()) {
                                        hash.create(new ParID(posicaoInicial, novoFilme.getID()));
                                    }
                                } catch (Exception e) {
                                    System.out.println("Erro ao atualizar árvore hash");
                                    e.printStackTrace();
                                }
                            }

                            else if(index == 3){

                                try {
                                    // Remove o registro antigo
                                    lista1.delete(null, IDDesejado);
                                    lista2.delete(null, IDDesejado);

                                    // Se o filme não estiver marcado como excluído, reinsere com o mesmo offset
                                    if (!novoFilme.getLAPIDE()) {

                                        Escrever.AdicionarListaInvertida(lista1, posicaoInicial, novoFilme, Criterios.get(0));
                                        Escrever.AdicionarListaInvertida(lista2, posicaoInicial, novoFilme, Criterios.get(1));
                                    }

                                } catch (Exception e) {
                                    System.out.println("Erro ao atualizar lista invertida");
                                    e.printStackTrace();
                                }

                            }
                            
                            Ultimo = novoFilme.getID();
                            incrementar = true;

                            
                        }
                        break; // Interrompe a busca após a atualização
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao ler objeto na posição " + posicaoInicial + ": " + e.getMessage());
                }
            }
            
            if (!encontrado) {
                System.out.println("Registro com ID " + IDDesejado + " não encontrado.");
            } else if (incrementar) {
                file.seek(0);
                file.writeInt(Ultimo); // Atualiza o último ID no início do arquivo
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + binarioFile);
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para atulizar a duracao do objeto
    public static Filmes atualizarDuracao(Filmes novoFilme, Scanner sc) {

        //If usado para indicar se o objeto for um filme assim informando para o usuario digitar a duracao do filme
        if(novoFilme.getTIPO().equals("Movie")){
            System.out.println("Digite a nova duracao do Filme em minutos do filme...");
            String duracao = sc.nextLine();
            novoFilme.setDURACAO(duracao + " min");
            System.out.println("Duracao atualizada...");
        }

        //If usado para indicar se o objeto for uma serie assim informando para o usuario digitar a quantidades de temporadas
        else{
            System.out.println("Digite a nova quantidade de temporadas da Serie...");
            String duracao = sc.nextLine();
            novoFilme.setDURACAO(duracao + " Season");
            System.out.println("Duracao atualizada...");
        }

        return novoFilme;
        
    }
}
