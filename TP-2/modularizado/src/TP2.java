
import classes.*;
import indexacao.Arvore.*;
import indexacao.Lista.*;
import java.io.*;
import java.nio.file.*;
import java.time.format.*;
import java.util.*;
import ordenacao.*;
import ui.Menus;
import ui.uiAtualizacao;
import util.*;

public class TP2 {
    public static void main(String[] args) {
        int opcao;
        String file = "E:\\Visual Studios\\Terceiro Semestre\\Trabalho\\TP2\\modularizado\\src\\ArquivosCSV\\netflix1.csv";
        String file2 = "E:\\Visual Studios\\Terceiro Semestre\\Trabalho\\TP2\\modularizado\\src\\ArquivosCSV\\WorldCountriesList.csv";
        String binarioFile = "binario.bin";
        String binarioPais = "binarioPais.bin";
        String dicinarioLista = null;
        String arquivoIndexacao = null;
        String arquivoIndexacao2 = null;
        int index = 0;
        List<Integer> tmp = new ArrayList<>();

        ArvoreBMais<RegistroID> arvore = null;

        ListaInvertida lista1 = null;
        ListaInvertida lista2 = null; 

        Scanner sc = new Scanner(System.in);

        do{

            //UI que mostrar as opçoes que o codigo pode realizar
            System.out.println("\t----Opcoes----");
            System.out.println("\t1: Ler arquivo CSV e escrever em binario (As demais opções podem da erro se não existir um arquivo binario)");
            System.out.println("\t2: Ler arquivo BINARO");
            System.out.println("\t3: Pesquisar Filme/Serie");
            System.out.println("\t4: Atualizar Filme/Serie");
            System.out.println("\t5: Remover Filme/Serie");
            System.out.println("\t6: Adicionar novo Filme/Serie");
            System.out.println("\t7: Ordenar");
            System.out.println("\t0: Sair");
            System.out.println("\t-------------------");

            opcao = sc.nextInt();

            switch(opcao){

                //Converte o arquivo CSV para binario
                case 1:{

                    //limpa buffer
                    sc.nextLine();

                    index = Menus.tiposDeIdexacao(sc);

                    if(index == 1){
                        try{    
                            sc.nextLine(); // Limpar o buffer
                            
                            System.out.println("\tDigite o nome do arquivo da ArvoreB:");
                            arquivoIndexacao = sc.nextLine();

                            arquivoIndexacao = arquivoIndexacao + ".bin";

                            System.out.println("\tQual o grau desejado da arvore:");
                            int grau = sc.nextInt();

                            arvore = new ArvoreBMais<>(RegistroID.class.getConstructor(), grau, arquivoIndexacao);

                        }catch(Exception e){
                            System.out.println("Erro ao criar a arvore B+");
                            e.printStackTrace();
                            arvore = null;
                        }
                    }

                    else if(index == 2){
                        arvore = null;
                    }

                    else{

                        try{

                            sc.nextLine(); // Limpar o buffer

                            List<Integer> criterios = Menus.MenuLista(sc);
                            tmp = criterios;
                            dicinarioLista = "dicionarioLista.bin";

                            arquivoIndexacao = FazerArquivo.Arquivo(criterios.get(0));
                            arquivoIndexacao2 = FazerArquivo.Arquivo(criterios.get(1));

                            arquivoIndexacao = arquivoIndexacao + ".bin";
                            arquivoIndexacao2 = arquivoIndexacao2 + ".bin";

                            System.out.println("\tQuantos dados por bloco?:");
                            int grau = sc.nextInt();

                            lista1 = new ListaInvertida(grau, dicinarioLista, arquivoIndexacao);
                            lista2 = new ListaInvertida(grau, dicinarioLista, arquivoIndexacao2);

                        }catch(Exception e){
                            System.out.println("Erro ao criar a lista invertida");
                            e.printStackTrace();
                            lista1 = null;
                            lista2 = null;
                        }
                        
                        arvore = null;
                    }

                    Escrever.IniciarArquivoCSV(file, binarioFile, binarioPais, file2, index, sc, arvore, lista1, lista2, tmp);

                    break;

                }

                //Le o arquivo binario
                case 2:{

                    Ler.lerBinario(binarioFile);
                    break;

                }

                //Pesquisa um filme/serie pelo ID ou nome
                case 3:{                                                                                                                                                                                                                                                                        

                    Filmes filmePesquisa = new Filmes();

                    if(index == 1){

                        System.out.println("\tDigite o ID do filme/serie:");
                        int IDDesejado = sc.nextInt();

                        filmePesquisa = Pesquisar.PesquisarIDArvore(binarioFile, IDDesejado, arvore);

                        if(filmePesquisa != null){
                            filmePesquisa.Ler();
                        }

                    }

                    else if(index == 2){

                        System.out.println("\tDigite o ID do filme/serie:");
                        int IDDesejado = sc.nextInt();

                        //Pesquisar.PesquisaHasing(sc);

                    }

                    else if(index == 3){

                       ArrayList<Long> posicoes = Menus.tipoPesquisaLista(sc,tmp, lista1, lista2);

                       for(int i = 0; i < posicoes.size(); i++){
                            filmePesquisa = Pesquisar.ListaFilmes(posicoes.get(i), binarioFile);
                            if(filmePesquisa != null){
                                filmePesquisa.Ler();
                            }
                        }
                        
                    }

                    break;

                }

                //Usando para atulizar um objeto
                case 4:{

                    System.out.println("Digite o ID do Filme desejado");
                    int IDDesejado = sc.nextInt();

                    //Pesquisa o objeto com base no ID 
                    Filmes novoFilme = Pesquisar.PesquisarIDArvore(binarioFile, IDDesejado, arvore);

                    //Se o objeto existir
                    if(novoFilme != null){
                        
                        uiAtualizacao.atualizarUI(IDDesejado, binarioFile, binarioPais, novoFilme, sc, arvore);

                        //Mostra um preview do objeto atualizado
                        novoFilme.Ler();

                    }

                    else{
                        System.out.println("Filme não encontrado!");
                    }
                    break;

                }

                case 5:{
                    // Remove um filme com base no ID informado pelo usuário
                    System.out.println("Digite o ID do Filme desejado para remover");
                    int IDDesejado = sc.nextInt();
                    Filmes novoFilme = Pesquisar.PesquisarIDArvore(binarioFile, IDDesejado, arvore);
                
                    if(novoFilme != null && arvore != null){
                        novoFilme.setLAPIDE(true); // Marca o filme como removido
                        
                        atualizar.atualizarFilmeID(IDDesejado, novoFilme, binarioFile, arvore);
                        System.out.println("Filme removido com sucesso!");
                    }
                    break;
                }
                
                case 6:{
                    // Adiciona um novo filme ao arquivo binário
                    try(RandomAccessFile in = new RandomAccessFile(binarioFile, "rw")){
                        //Encontra qual sera o ID do proximo Filme;
                        int ID = (Pesquisar.encontrarTamanho(binarioFile)) + 1;
                        int opcaoAdicinar;
                        List<String> lista = new ArrayList<>();
                
                        // Adiciona um ID temporario para a lista, devido a forma como a leitura de objeto e feita
                        lista.add("1");

                        // Adiciona o tipo do objeto (filme ou serie)
                        lista.add(Menus.tipo(sc));
                
                        // Adiciona o nome do objeto
                        sc.nextLine();
                        System.out.println("\tDigite o nome: ");
                        String nome = sc.nextLine();
                        lista.add(nome);
                
                        // Adicina o diretor do objeto
                        System.out.println("\tDigite o diretor: ");
                        String diretor = sc.nextLine();
                        lista.add(diretor);
                
                        // Adiciona o pais do objeto
                        boolean verificar = false;
                        boolean sairLoop = false;
                        while(!verificar && !sairLoop){

                            System.out.println("\tDigite o nome Pais, em ingles (se o pais for descoconhecido digite \"NOT\"):");
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
                                            lista.add("NOT");
                                            verificar = true;
                                            sairLoop = true;
                                            sc.nextLine();
                                            break;
                                        }
                                    }

                                }while(opcaoPais != 1 && opcaoPais != 2);

                            }
                            else{
                                lista.add(pais);
                                verificar = true;
                            }

                        }
                
                        // Adiciona a data de adição do objeto
                        String anoAdicao = null;
                        verificar = false;
                        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/M/d");
                        while (!verificar){
                            System.out.println("\tDigite a data de adição ex: 2001/5/28:");
                            anoAdicao = sc.nextLine();
                            //Verifica se o que o usuraio inseriu e uma data valida
                            verificar = ValidadeData.isDataValida(anoAdicao, format);
                        }
                        lista.add(anoAdicao);
                
                        // Adiciona a o ano de lançamento do objeto
                        verificar = false;
                        String anoLancamento = null;
                        //Verifica se o ano e valido
                        while (!verificar){
                            System.out.println("\tDigite o ano de lancamento ex: 2021:");
                            anoLancamento = sc.nextLine();
                            verificar = ValidadeData.isAnoValido(anoLancamento);
                        }
                        lista.add(anoLancamento);
                
                        // Adiciona a classificação indicativa do objeto
                        lista.add(Menus.classificacaoIndicativa(sc));
                
                        // Adiciona a duração do objeto
                        sc.nextLine();

                        if(lista.get(1).equals("Movie")){
                            System.out.println("\tDigite a durção do Filme em minutos do filme...");
                            String duracao = sc.nextLine();
                            lista.add(duracao + " min");
                        }
                        //If usado para indicar se o objeto for uma serie assim informando para o usuario digitar a quantidades de temporadas
                        else{
                            System.out.println("\tDigite a quantidade de temporadas da Serie...");
                            String duracao = sc.nextLine();
                            lista.add(duracao + " Season");
                        }
                
                        // Adiciona o genero do objeto
                        System.out.println("\tDigite o genero: ");
                        String genero = sc.nextLine();
                        lista.add(genero);
                
                        //Cria o objeto
                        Filmes novoFilme = new Filmes(lista, ID, true);
                        //Preview do objeto antes de adicionar
                        System.out.println("Preview");

                        //Escreve o objeto no arquivo binario
                        in.seek(0);
                        in.writeInt(novoFilme.getID());
                        in.seek(in.length()); 
                        long posicao = in.getFilePointer();         
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(novoFilme);
                        }
                        byte[] bytes = baos.toByteArray();
                        in.writeInt(bytes.length);
                        in.write(bytes);

                        try {
                            arvore.create(new RegistroID(novoFilme.getID(), posicao));
                        } catch (Exception e) {
                            e.printStackTrace(); 
                        }
                
                        // Permite edição antes de confirmar a adição
                        uiAtualizacao.atualizarUI(ID, binarioFile, binarioPais, novoFilme, sc, arvore);

                    }catch (IOException e){
                        System.out.println("Arquivo nao encontrado");
                        e.printStackTrace();
                    }

                    break;

                }

                case 7:{

                    //Inicia a ordenação externa balanceada do arquivo
                    try{

                        //Pede para o usuario incira o numero de arquivos temporarios que serão utilizados
                        int numCaminhos;
                        System.out.println("Digite o numero de caminhos para a ordenação");
                        //Delimita a quantidade de arquivos
                        while((numCaminhos = sc.nextInt()) > 100){
                            System.out.println("Numero de caminhos muito grande (numero maximo 100)");
                        }

                        //Pede para o usuario incira o blocos que serão utilizados
                        int Blocos;
                        System.out.println("Digite o numero de registros máximo para cada ordenação em memória primária");
                        //Delimita a quantidade de blocos
                        while((Blocos = sc.nextInt()) > 1000){
                            System.out.println("Numero de registros maximo muito grande (numero maximo 1000)");
                        }

                        Ordenacao.ordenarExterna(binarioFile, numCaminhos, Blocos);

                        //Copia o conteudo do arquivo ordenado para o arquivo origonal
                        Files.copy(Paths.get("binario.bin.ordenado"), Paths.get(binarioFile), StandardCopyOption.REPLACE_EXISTING);

                        //Apago o arquivo ordenado e o arquivo TEMP de intercalação
                        Files.deleteIfExists(Paths.get(binarioFile + ".ordenado"));
                        Files.deleteIfExists(Paths.get(binarioFile + ".ordenado.intercalacao0"));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                }
                
                case 0:{

                    Path caminho = Paths.get(arquivoIndexacao);

                   
                    if (arvore != null) {
                        try {
                            arvore.close(); // Fechar o arquivo antes de excluir
                            System.out.println("Arquivo da árvore fechado com sucesso");
                        } catch (IOException e) {
                            System.out.println("Erro ao fechar o arquivo: " + e.getMessage());
                        }
                    } 
                    
                    if(arquivoIndexacao2 != null){
                        Path caminho2 = Paths.get(arquivoIndexacao2);
                    
                        // Verifica se o arquivo existe antes de tentar excluí-lo
                        try {
                            if (Files.exists(caminho)) {
                                boolean deletado = Files.deleteIfExists(Paths.get(arquivoIndexacao));
                                if (deletado) {
                                    System.out.println("Arquivo " + Paths.get(arquivoIndexacao).getFileName().toString() + " excluído com sucesso");
                                }
                            }
                            if (Files.exists(caminho2)) {
                                boolean deletado = Files.deleteIfExists(Paths.get(arquivoIndexacao2));
                                if (deletado) {
                                    System.out.println("Arquivo " + Paths.get(arquivoIndexacao2).getFileName().toString() + " excluído com sucesso");
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao excluir arquivo: " + e.getMessage());
                            e.printStackTrace();
                        }

                    }
                    
                    System.out.println("Saindo...");
                    break;

                }

                default:{

                    System.out.println("Opção inválida!");

                }

            }

        }while(opcao != 0);
        
        sc.close();
    }

}