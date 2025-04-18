package ui;

import indexacao.Lista.ListaInvertida;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import util.Pesquisar;

public class Menus {
    public static int tiposDeIdexacao(Scanner sc){
        int index = 0;
        boolean verificar = false;
        do{
            System.out.println("\tSelecione o tipo de indexacao desejada:");
            System.out.println("\t-----------------------------------------");
            System.out.println("\t1: Árvore B+");
            System.out.println("\t2: Hashing Estendido");
            System.err.println("\t3: Lista Invertida");
            System.out.println("\t-----------------------------------------");

            index = sc.nextInt();

            switch(index){
                case 1: index = 1; verificar = true; break;
                case 2: index = 2; verificar = true; break;
                case 3: index = 3; verificar = true; break;
                default: System.out.println("Opcao invalida"); break;
            }
        }while(!verificar);

        return index;
    }

    //Função para definir a classificação indicativa de acordo com o TV Parental Guidelines
    public static String classificacaoIndicativa(Scanner sc){
        String classificacao = null;
        boolean verificar2 = false;
        do{
            System.out.println("\tSeleciona a classificacao da media");
            System.out.println("\t-----------------------------------------");
            System.out.println("\t1: TV-Y (Conteúdo infantil)");
            System.out.println("\t2: TV-Y7 (Não recomendado para menores de 7 anos)");
            System.out.println("\t3: TV-Y7-FV (Não recomendado para menores de 7 anos com violência fantasiosa)");
            System.out.println("\t4: TV-G (Recomendado para todos os públicos)");
            System.out.println("\t5: TV-PG (Classificação destinada com a orientação parental)");
            System.out.println("\t6: TV-14 (Não recomendado para menores de 14 anos)");
            System.out.println("\t7: TV-MA (Não recomendado para menores de 17 anos)");
            System.out.println("\t-----------------------------------------");
    
            int opcao = sc.nextInt();
            switch(opcao){
                case 1: classificacao = "TV-Y"; verificar2 = true; break;
                case 2: classificacao = "TV-Y7"; verificar2 = true; break;
                case 3: classificacao = "TV-Y7-FV"; verificar2 = true; break;
                case 4: classificacao = "TV-G"; verificar2 = true; break;
                case 5: classificacao = "TV-PG"; verificar2 = true; break;
                case 6: classificacao = "TV-14"; verificar2 = true; break;
                case 7: classificacao = "TV-MA"; verificar2 = true; break;
                default: System.out.println("\tOpcao invalida");
            }
        } while (!verificar2);
        return classificacao;
    }

    //Função para definir a se o objeto e um Filme ou uma Serie
    public static String tipo(Scanner sc){
        boolean verificar1 = false;
        String tipo = null;
        do {
            System.out.println("\t Seleciona o tipo da media");
            System.out.println("\t-----------------------------------------");
            System.out.println("\t1: TV Show");
            System.out.println("\t2: Movie");
            System.out.println("\t-----------------------------------------");
    
            int opcaoAdicinar = sc.nextInt();
    
            switch(opcaoAdicinar){
                case 1: tipo = "TV Show"; verificar1 = true; break;
                case 2: tipo = "Movie"; verificar1 = true; break;
                default: System.out.println("\tOpcao invalida");
            }
        } while (!verificar1);
        return tipo;
    }

    public static List<Integer> MenuLista(Scanner sc) {

        List<Integer> lista = new ArrayList<>();
        boolean verificar = false;

        do{
    
            System.out.println("\t Selecione dois dos criterios para a craicao de duas lista invertida");
            System.out.println("\t-----------------------------------------");
            System.out.println("\t1: Movie/TV Show");
            System.out.println("\t2: Diretor");
            System.out.println("\t3: Pais");
            System.out.println("\t4: Data de Adicao");
            System.out.println("\t5: Ano de Lançamento");
            System.out.println("\t6: Classificacao Indicativa");
            System.out.println("\t7: Genero");
            System.out.println("\t-----------------------------------------");

            System.out.println("\tSelecione o primeiro criterio:");
            lista.add(sc.nextInt());
            System.err.println("\tSelecione o segundo criterio:");
            lista.add(sc.nextInt());

            if(lista.get(0) == lista.get(1)){
                System.out.println("\tOs criterios selecionados sao iguais, selecione novamente");
                lista.clear();
            }
            else if(lista.get(0) > 7 || lista.get(1) > 7 || lista.get(0) < 1 || lista.get(1) < 1){
                System.out.println("\tUm ou mais criterios selecionados sao invalidos, selecione novamente");
                lista.clear();
            }
            else{
                System.err.println("\tCriterios selecionados: " + lista.get(0) + " e " + lista.get(1));

                for(int i = 0; i < lista.size(); i++){

                    if(lista.get(i) <= 6 && lista.get(i) >= 2){
                        lista.set(i, lista.get(i) + 1); // Ajusta o índice para corresponder à lista de critérios
                    }

                    else if(lista.get(i) == 7){
                        lista.set(i, 9);// Ajusta o índice para corresponder à lista de critérios
                    }
                   
                }

                verificar = true;
            }

        }while(!verificar);

        return lista;

    }

    public static ArrayList<Long> tipoPesquisaLista(Scanner sc, List<Integer> tmp, ListaInvertida lista1, ListaInvertida lista2){

        boolean verificar = false;
        List<String> criterios = new ArrayList<>();
        ArrayList<Long> elementos = new ArrayList<>();

        for(int i = 0; i < tmp.size(); i++){
            criterios.add(Pesquisar.BuscarCriterio(tmp.get(i)));
        }
        do{
            System.out.println("\tSelecione o tipo de pesquisa desejada:");
            System.out.println("\t-----------------------------------------");
            System.out.println("\t1: Pesquisa na lista " + criterios.get(0));
            System.out.println("\t2: Pesquisa na lista " + criterios.get(1));
            System.out.println("\t3: Pesquisa por dados cruzados utilizando as duas listas");
            System.out.println("\t-----------------------------------------");

            int opcao = sc.nextInt();

            switch(opcao){
                case 1:{

                    sc.nextLine(); // Limpa o buffer do scanner

                    System.out.println("\tDigite o termo a ser pesquisado na lista " + criterios.get(0) + ": ");
                    String termo = sc.nextLine();

                    elementos = Pesquisar.PesquisarLista(lista1, termo);

                    verificar = true;
                    
                    break;

                }
                case 2:{

                    sc.nextLine(); // Limpa o buffer do scanner

                    System.out.println("\tDigite o termo a ser pesquisado na lista " + criterios.get(1) + ": ");
                    String termo = sc.nextLine();

                    elementos = Pesquisar.PesquisarLista(lista2, termo);

                    verificar = true;
                    
                    break;
                }
                case 3:{

                    sc.nextLine(); // Limpa o buffer do scanner

                    System.out.println("\tDigite o termo a ser pesquisado na lista " + criterios.get(0) + ": ");
                    String termo1 = sc.nextLine();

                    System.out.println("\tDigite o termo a ser pesquisado na lista " + criterios.get(1) + ": ");
                    String termo2 = sc.nextLine();

                    ArrayList<Long> elementos1 = Pesquisar.PesquisarLista(lista1, termo1);
                    ArrayList<Long> elementos2 = Pesquisar.PesquisarLista(lista2, termo2);

                    for(Long elemento : elementos1){
                        if(elementos2.contains(elemento)){
                            elementos.add(elemento);
                        }
                    }

                    verificar = true;
                    
                    break;
                }
                default: System.out.println("Opcao invalida"); break;
            }
        }while(!verificar);

        return elementos;

    }
}
