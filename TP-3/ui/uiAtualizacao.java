package ui;

import classes.Filmes;
import classes.RegistroID;
import indexacao.Arvore.ArvoreBMais;
import indexacao.Hash.HashExtensivel;
import indexacao.Hash.ParID;
import indexacao.Lista.ListaInvertida;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import util.*;

public class uiAtualizacao {
    // Função que cria uma Interface de usuario para arulizao o filme
    public static void atualizarUI(int IDDesejado, String binarioFile, String binarioPais, Filmes novoFilme, Scanner sc, ArvoreBMais<RegistroID> arvore, int index, ListaInvertida lista1, ListaInvertida lista2, List<Integer> Criterios, HashExtensivel<ParID> hash) {

        int opcaoAtualizar;

        do{

            //previw do objeto
            novoFilme.Ler();

            //UI para mostrar as opções de atualizacao
            System.out.println("\t-----------------------------------------");
            System.out.println("\t1: Atualizar Tipo (TV Show/Movie)");
            System.out.println("\t2: Atualizar nome");
            System.out.println("\t3: Atualizar diretor");
            System.out.println("\t4: Atualizar Pais");
            System.out.println("\t5: Atualizar data de adição");
            System.out.println("\t6: Atualizar ano de lancamento");
            System.out.println("\t7: Atualizar classificacao");
            System.out.println("\t8: Atualizar duracao");
            System.out.println("\t9: Atualizar genero");
            System.out.println("\t0: Confirmar");
            System.out.println("\t-----------------------------------------");

            opcaoAtualizar = sc.nextInt();

            switch(opcaoAtualizar){
                case 0:{
                    // Atualiza todas as informações do filme com base no ID
                    atualizar.atualizarFilmeID(IDDesejado, novoFilme, binarioFile, arvore, index, lista1, lista2, Criterios, hash);
                    System.out.println("Atualizando dados...");
                    break;
                }
            
                case 1:{

                    // Atualiza o tipo do filme (ex: filme, série, documentário)
                    novoFilme.setTIPO(Menus.tipo(sc));

                    sc.nextLine();
                    //Chama a função que atuliza a duração, com o objetivo de não tem confito entre serie e filmes, pois a duração de filmes e salvo em minutos e a de series em temporadas
                    novoFilme = atualizar.atualizarDuracao(novoFilme, sc);

                    System.out.println("Tipo atualizado...");
                    break;
                }
            
                case 2:{
                    // Atualiza o nome do filme
                    sc.nextLine();
                    System.out.println("Digite o novo nome:");
                    String nome = sc.nextLine();
                    novoFilme.setNOME(nome);
                    System.out.println("Nome atualizado...");
                    break;
                }
            
                case 3:{
                    // Atualiza o diretor do filme
                    sc.nextLine();
                    System.out.println("Digite o nome do novo diretor:");
                    String diretor = sc.nextLine();
                    novoFilme.setDIRETOR(diretor);
                    System.out.println("Diretor atualizado...");
                    break;
                }
            
                case 4:{
                    // Atualiza o país de origem do filme, convertendo a primeira letra para maiúscula e o resto para minuscula   
                    boolean verificar = false;
                    boolean sairLoop = false;
                    sc.nextLine();
                    while(!verificar && !sairLoop){

                        System.out.println("\tDigite o nome do novo Pais, em ingles (se o pais for descoconhecido digite \"NOT\"):");
                        String pais = sc.nextLine();
                        pais = pais.substring(0,1).toUpperCase() + pais.substring(1).toLowerCase();
                            
                        // Pesquisa se o país existe na base de dados binária
                        pais = Pesquisar.PesquisarPaisAbre(binarioPais, pais);
                        if(pais.equals("NOT")){
                            System.out.println("\tPais nao encontrado...\n\t1: Tentar novamente\n\t2: Manter \"NOT\"");
                            int opcaoPais;

                            do{
                                opcaoPais = sc.nextInt();
                                
                                switch(opcaoPais){
                                    case 1:{
                                        sc.nextLine(); // Limpar o buffer
                                        break;
                                    }
                                    case 2:{
                                        novoFilme.setPAIS("NOT");
                                        verificar = true;
                                        sairLoop = true;
                                        sc.nextLine();
                                        break;
                                    }
                                }

                            }while(opcaoPais != 1 && opcaoPais != 2);

                        }
                        else{
                            novoFilme.setPAIS(pais);
                            verificar = true;
                        }

                    }
                    
                    break;
                }
            
                case 5:{
                    //Atualiza o data de adição
                    sc.nextLine();

                    String anoAdicao = null;
                    boolean verificar = false;
                    DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");

                    while (!verificar){
                        System.out.println("Digite o novo data de adicao ex: 12/31/2001:");
                        anoAdicao = sc.nextLine();
                        //Verifica se o que o usuraio inseriu e uma data valida
                        verificar = ValidadeData.isDataValida(anoAdicao, format);
                    }

                    LocalDate data = LocalDate.parse(anoAdicao, format);
                    novoFilme.setANO_ADI(data);
                    System.out.println("Data de adição atualizado...");
                    break;
                   
                }
            
                case 6:{
                    // Atualiza o ano de lançamento do filme
                    sc.nextLine();
                    boolean verificar = false;
                    String anoLancamento = null;

                    //Verifica se o ano e valido
                    while (!verificar){
                        System.out.println("Digite o novo ano de lancamento ex: 2021:");
                        anoLancamento = sc.nextLine();
                        verificar = ValidadeData.isAnoValido(anoLancamento);
                    }

                    Year ano = Year.parse(anoLancamento);
                    novoFilme.setANO_LAN(ano);
                    System.out.println("Ano de lancamento atualizado...");
                    
                    break;
                }
            
                case 7:{
                    // Atualiza a classificação indicativa do filme
                    novoFilme.setCLASSIFICACAO(Menus.classificacaoIndicativa(sc));
                    System.out.println("Classificacao atualizada para...");
                    break;
                }
            
                case 8:{
                    // Atualiza a duração do filme ou série
                    sc.nextLine();

                    novoFilme = atualizar.atualizarDuracao(novoFilme, sc);

                    break;
                    
                }
            
                case 9:{
                    // Atualiza o gênero do filme
                    sc.nextLine();
                    System.out.println("Digite o novo genero");
                    String genero = sc.nextLine();
                    novoFilme.setGENERO(genero);
                    System.out.println("Genero atualizado...");
                    break;
                }
            

                default: {
                    System.out.println("Opcao invalida");
                }

            }

        }while(opcaoAtualizar != 0);

    }
}
