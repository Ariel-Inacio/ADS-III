import CasamentoDePadrao.BoyerMoore;
import CasamentoDePadrao.KMP;
import Compressao.Huffman.CompEDescompHuffman;
import Compressao.LZW.CompEDescompLZW;
import Criptografia.DES;
import Criptografia.Vigenere;
import classes.Cronometro;
import classes.Filmes;
import classes.RegistroID;
import indexacao.Arvore.ArvoreBMais;
import indexacao.Hash.HashExtensivel;
import indexacao.Hash.ParID;
import indexacao.Lista.FazerArquivo;
import indexacao.Lista.ListaInvertida;
import java.io.*;
import java.nio.file.*;
import java.time.format.*;
import java.util.*;
import ordenacao.Ordenacao;
import ui.*;
import util.*;

public class TP4 {
    public static void main(String[] args) {
        int opcao;
        String file = "src/ArquivosCSV/netflix1.csv";
        String file2 = "src/ArquivosCSV/WorldCountriesList.csv";
        String binarioFile = "src/Binarios/binario.bin";
        String binarioPais = "src/Binarios/binarioPais.bin";
        String dicinarioLista = null;
        String arquivoIndexacao = null;
        String arquivoIndexacao2 = null;
        String arquivoCompactado = "src/Binarios/binarioCompressao";
        String arquivoDescompactado = "src/Binarios/descompactado";
        String arquivoCriptografado = null;
        String arquivoDescriptografado = "src/Binarios/Descriptografado.bin";
        int index = 0;
        List<Integer> tmp = new ArrayList<>();

        ArvoreBMais<RegistroID> arvore = null;

        HashExtensivel<ParID> hash = null;

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
            System.out.println("\t8: Compactar");
            System.out.println("\t9: Descompactar");
            System.out.println("\t10: Busca por casamento de padrão");
            System.out.println("\t11: Criptografar");
            System.out.println("\t12: Descriptografar");
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

                            arquivoIndexacao = "src/Binarios/" + arquivoIndexacao + ".bin";

                            int grau = 0;

                            while(grau < 2 || grau > 100){
                                System.out.println("\tQual o grau desejado da arvore (minimo 2 e maximo 100):");
                                grau = sc.nextInt();
                            }
                            arvore = new ArvoreBMais<>(RegistroID.class.getConstructor(), grau, arquivoIndexacao);

                        }catch(Exception e){
                            System.out.println("Erro ao criar a arvore B+");
                            e.printStackTrace();
                            arvore = null;
                        }
                    }

                    else if(index == 2){
                        try{

                            sc.nextLine(); // Limpar o buffer

                            System.out.println("\tDigite o nome do arquivo da Hash:");
                            arquivoIndexacao = sc.nextLine();

                            int tamanho = 0;

                            while(tamanho < 2 || tamanho > 100){
                                System.out.println("\tQual a quantidade de blocos desejado no hash (minimo 2 e maximo 100):");
                                tamanho = sc.nextInt();
                            }

                            hash = new HashExtensivel<>(ParID.class.getConstructor(), tamanho, "src/Binarios/" + arquivoIndexacao + ".bin", "src/Binarios/" + arquivoIndexacao + "Bucket.bin");

                        }catch(Exception e){
                            System.out.println("Erro ao criar a arvore B+");
                            e.printStackTrace();
                        }
                    }

                    else{

                        try{

                            sc.nextLine(); // Limpar o buffer

                            List<Integer> criterios = Menus.MenuLista(sc);
                            tmp = criterios;
                            dicinarioLista = "src/Binarios/dicionarioLista.bin";

                            arquivoIndexacao = FazerArquivo.Arquivo(criterios.get(0));//Cria o arquivo de indexação
                            arquivoIndexacao2 = FazerArquivo.Arquivo(criterios.get(1));//Cria o arquivo de indexação2

                            arquivoIndexacao = "src/Binarios/" + arquivoIndexacao + ".bin";
                            arquivoIndexacao2 = "src/Binarios/" + arquivoIndexacao2 + ".bin";

                            int grau = 0;

                            while(grau < 2 || grau > 100){
                                System.out.println("\tQuantos dados por bloco deseja por na lista invertida (minimo 2 e maximo 100, sendo recomendado entre 40 e 50, por ser mais rapido rapido):");
                                grau = sc.nextInt();
                            }

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

                    Escrever.IniciarArquivoCSV(file, binarioFile, binarioPais, file2, index, sc, arvore, lista1, lista2, tmp, hash);

                    break;

                }

                //Le o arquivo binario
                case 2:{

                    Ler.lerBinario(arquivoDescriptografado);
                    break;

                }

                //Pesquisa um filme/serie
                case 3:{                                                                                                                                                                                                                                                                        

                    Filmes filmePesquisa = new Filmes();

                    if(index == 1){

                        System.out.println("\tDigite o ID do Filme/Serie:");
                        int IDDesejado = sc.nextInt();

                        filmePesquisa = Pesquisar.PesquisarIDArvore(binarioFile, IDDesejado, arvore);

                        if(filmePesquisa != null){
                            filmePesquisa.Ler();
                        }

                    }

                    else if(index == 2){

                        System.out.println("\tDigite o ID do Filme/Serie:");
                        int IDDesejado = sc.nextInt();

                        try{

                            long endereco = hash.read(ParID.hash(IDDesejado)).getEndereco();
                            filmePesquisa = Pesquisar.PesquisarID(binarioFile, IDDesejado);

                            if(filmePesquisa != null){
                                filmePesquisa.Ler();
                            }


                        }catch(Exception e){
                            System.out.println("ID não encontrado!");
                        }

                    }

                    else if(index == 3){

                       ArrayList<Long> posicoes = Menus.tipoMenuLista(sc,tmp, lista1, lista2, binarioPais);

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

                    Filmes novoFilme = null;

                    System.out.println("Digite o ID do Filme desejado");
                    int IDDesejado = sc.nextInt();

                    if(index == 1){    

                        //Pesquisa o objeto com base no ID 
                        novoFilme = Pesquisar.PesquisarIDArvore(binarioFile, IDDesejado, arvore);

                        //Se o objeto existir
                        if(novoFilme != null){
                            
                            uiAtualizacao.atualizarUI(IDDesejado, binarioFile, binarioPais, novoFilme, sc, arvore, index, lista1, lista2, tmp, hash);

                            //Mostra um preview do objeto atualizado
                            novoFilme.Ler();

                        }

                        else{
                            System.out.println("Filme/Serie não encontrado!");
                        }
                        break;

                    }

                    else if(index == 2){

                        try{

                            long endereco = hash.read(ParID.hash(IDDesejado)).getEndereco();

                            //Pesquisa o objeto com base no ID 
                            novoFilme = Pesquisar.PesquisarID(binarioFile, IDDesejado);

                            //Se o objeto existir
                            if(novoFilme != null){

                                uiAtualizacao.atualizarUI(IDDesejado, binarioFile, binarioPais, novoFilme, sc, arvore, index, lista1, lista2, tmp, hash);

                                //Mostra um preview do objeto atualizado
                                novoFilme.Ler();

                            }

                            else{
                                System.out.println("Filme/Serie não encontrado!");
                            }
                            break;

                        }catch(Exception e){
                            System.out.println("ID não encontrado!");
                        }

                    }

                    else if(index == 3){

                        novoFilme = Pesquisar.PesquisarID(binarioFile, IDDesejado);

                        //Se o objeto existir
                        if(novoFilme != null){

                            //Atualiza o objeto com base no ID
                            uiAtualizacao.atualizarUI(IDDesejado, binarioFile, binarioPais, novoFilme, sc, arvore, index, lista1, lista2, tmp , hash);

                            //Mostra um preview do objeto atualizado
                            novoFilme.Ler();

                        }

                        else{
                            System.out.println("Filme/Serie não encontrado!");
                        }
                        break;
                        
                    }

                }

                // Remove um filme/serie com base no ID informado pelo usuário
                case 5:{
                    
                    int IDDesejado;
                    Filmes novoFilme;

                    if(index == 1){

                        System.out.println("Digite o ID do Filme/Serie desejado para remover");
                        IDDesejado = sc.nextInt();
                        novoFilme = Pesquisar.PesquisarIDArvore(binarioFile, IDDesejado, arvore);
                    
                        if(novoFilme != null && arvore != null){
                            novoFilme.setLAPIDE(true); // Marca o filme como removido
                            
                            atualizar.atualizarFilmeID(IDDesejado, novoFilme, binarioFile, arvore, index, lista1, lista2, tmp, hash);
                            System.out.println("Filme/Serie removido com sucesso!");
                        }

                    }

                    else if(index == 2){

                        System.out.println("Digite o ID do Filme/Serie desejado para remover");
                        IDDesejado = sc.nextInt();

                        try{

                            long endereco = hash.read(ParID.hash(IDDesejado)).getEndereco();
                            novoFilme = Pesquisar.PesquisarID(binarioFile, IDDesejado);
                        
                            if(novoFilme != null && hash != null){
                                
                                hash.delete(ParID.hash(IDDesejado));
                                novoFilme.setLAPIDE(true); // Marca o filme como removido
                                
                                atualizar.atualizarFilmeID(IDDesejado, novoFilme, binarioFile, arvore, index, lista1, lista2, tmp, hash);
                                System.out.println("Filme/Serie removido com sucesso!");
                            }
                        }catch(Exception e){
                            System.out.println("ID não encontrado!");
                        }

                    }

                    else if(index == 3){

                        try{

                            int apagar = Menus.MenuApagarLista(sc);
                            Long endereco = null;
                            

                            //Apaga um Filme/Serie de todos os criterios
                            if(apagar == 1){

                                System.out.println("Digite o ID do Filme/Serie desejado para remover");
                                IDDesejado = sc.nextInt();

                                endereco = lista1.encontrarEndereco(IDDesejado);
                                novoFilme = Pesquisar.ListaFilmes(endereco, binarioFile);

                                novoFilme.setLAPIDE(true); // Marca o filme como removido
                                atualizar.atualizarFilmeID(IDDesejado, novoFilme, binarioFile, arvore, index, lista1, lista2, tmp, hash);

                                lista1.delete(null, IDDesejado);
                                lista2.delete(null, IDDesejado);
                                
                            }

                            // Apaga um Filme/Serie de um unico criterio
                            else if(apagar == 2){

                                int tipoLista = Menus.SelecinarCriterio(sc, tmp);
                                List<ListaInvertida> tmpLista = new ArrayList<>();
                                int tmpcriterio = 0;

                                System.out.println("Digite o ID do Filme/Serie desejado para remover");
                                IDDesejado = sc.nextInt();

                                sc.nextLine(); // Limpar o buffer

                                System.out.println("Digite o criterio desejado para remover da lista de " + Pesquisar.BuscarCriterio(tmp.get(tipoLista)));
                                String criterio = sc.nextLine();
                                
                                if(tipoLista == 0){
                                    lista1.delete(criterio, IDDesejado);
                                    endereco = lista1.encontrarEndereco(IDDesejado);
                                    tmpLista.add(lista1);
                                    tmpLista.add(lista2);
                                }
                                
                                else if(tipoLista == 1){
                                    lista2.delete(criterio, IDDesejado);
                                    endereco = lista2.encontrarEndereco(IDDesejado);
                                    tmpLista.add(lista2);
                                    tmpLista.add(lista1);
                                    tmpcriterio = 1;
                                }

                                if((tmpLista.get(0).encontrarID(IDDesejado))){

                                    System.out.println("Filme/Serie removendo...");

                                }

                                else if((tmpLista.get(1).encontrarID(IDDesejado))){

                                    System.out.println("Filme/Serie removendo...");
                                    System.out.println("Filme/Serie removendo, e não existe mais na lista invertida " + Pesquisar.BuscarCriterio(tmp.get(tmpcriterio)) + "!");
                                    

                                }

                                else{

                                    System.out.println("Filme/Serie removido, e não existe mais nas listas invertidas!");

                                    novoFilme = Pesquisar.ListaFilmes(endereco, binarioFile);

                                    if(novoFilme != null){
                                        novoFilme.setLAPIDE(true); // Marca o filme como removido
                                        atualizar.atualizarFilmeID(IDDesejado, novoFilme, binarioFile, arvore, index, lista1, lista2, tmp, hash);
                                    }                                    
                            
                                }

                            }

                            //Apaga um Criterio inteiramente da lista invertida
                            else if(apagar == 3){

                                int tipoLista = Menus.SelecinarCriterio(sc, tmp);

                                System.out.println("Digite o criterio desejado para remover da lista de " + Pesquisar.BuscarCriterio(tmp.get(tipoLista)));
                                sc.nextLine(); // Limpar o buffer
                                String criterio = sc.nextLine();
                                
                                if(tipoLista == 0){
                                    lista1.delete(criterio, -1);
                                }
                                
                                else if(tipoLista == 1){
                                    lista2.delete(criterio, -1);
                                }
                                
                            }

                        }catch(Exception e){
                            System.out.println("Erro ao remover o filme/serie");
                            e.printStackTrace();
                        }

                    }
                    
                    break;
                }

                // Adiciona um novo filme ao arquivo binário
                case 6:{
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

                        if(index == 1){

                            try {
                                arvore.create(new RegistroID(novoFilme.getID(), posicao));
                            } catch (Exception e) {
                                e.printStackTrace(); 
                            }

                        }

                        else if(index == 2){

                            try{
                                hash.create(new ParID(posicao, novoFilme.getID()));
                            }catch(Exception e){
                                e.printStackTrace();
                            }

                        }

                        else if(index == 3){

                            Escrever.AdicionarListaInvertida(lista1, posicao, novoFilme, tmp.get(0));

                            Escrever.AdicionarListaInvertida(lista2, posicao, novoFilme, tmp.get(1));

                        }
                
                        // Permite edição antes de confirmar a adição
                        uiAtualizacao.atualizarUI(ID, binarioFile, binarioPais, novoFilme, sc, arvore, index, lista1, lista2, tmp, hash);

                    }catch (IOException e){
                        System.out.println("Arquivo nao encontrado");
                        e.printStackTrace();
                    }

                    break;

                }

                //Inicia a ordenação externa balanceada do arquivo
                case 7:{

                    try {
                        // Pede para o usuario inserir o numero de arquivos temporarios
                        int numCaminhos;
                        System.out.println("Digite o numero de caminhos para a ordenação");
                        // Delimita a quantidade de arquivos
                        while((numCaminhos = sc.nextInt()) > 100){
                            System.out.println("Numero de caminhos muito grande (numero maximo 100)");
                        }
                    
                        // Pede para o usuario inserir o blocos que serão utilizados
                        int Blocos;
                        System.out.println("Digite o numero de registros máximo para cada ordenação em memória primária");
                        // Delimita a quantidade de blocos
                        while((Blocos = sc.nextInt()) > 1000){
                            System.out.println("Numero de registros maximo muito grande (numero maximo 1000)");
                        }
                    
                        // Extrair o diretório e o nome do arquivo base
                        File fileT = new File(binarioFile);
                        String dir = fileT.getParent();
                        String nomeArquivo = fileT.getName();
                        String arquivoOrdenado = dir + File.separator + nomeArquivo + ".ordenado";
                        
                        // Chamar o método de ordenação externa
                        Ordenacao.ordenarExterna(binarioFile, numCaminhos, Blocos);
                        
                        // Copiar o arquivo ordenado para o arquivo original
                        Path origemPath = Paths.get(arquivoOrdenado);
                        Path destinoPath = Paths.get(binarioFile);
                        
                        if (Files.exists(origemPath)) {
                            Files.copy(origemPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
                            
                            // Apagar arquivos temporários
                            Files.deleteIfExists(origemPath);
                            Files.deleteIfExists(Paths.get(arquivoOrdenado + ".intercalacao0"));
                            
                            System.out.println("Ordenação concluída com sucesso!");
                        } else {
                            System.out.println("Arquivo ordenado não foi encontrado: " + arquivoOrdenado);
                        }
                    } catch (Exception e) {
                        System.out.println("Erro durante a ordenação: " + e.getMessage());
                        e.printStackTrace();
                    }

                    break;

                }

                //Funcao que compacta o arquivo binario
                case 8:{

                    //Chama um menu de seleção de compactação
                    int compactar = Menus.MenuCompactar(sc);
                    float[] eficiencia = new float[2]; //Variavel para armazenar a eficiencia de compressão para Huffman e LZW
                    double[] tempo = new double[2]; //Variavel para armazenar o tempo de compressão para Huffman e LZW
                    arquivoCompactado = "src/Binarios/binarioCompressao";

                    //Cria o arquivo binario que sera compactado em Huffman
                    if(compactar == 1){

                        //Cria um string como final para ser compativel com o lambda
                        final String arquivoHuffman = arquivoCompactado + "Huffman.bin";

                        //Função lambda que cronometrar e a eficiencia da compressão Huffman
                        Cronometro.cronometrar("Compressão Huffman", () -> {
                            CompEDescompHuffman.CodificaHuffman(binarioFile, arquivoHuffman);
                        });

                    }

                    //Cria o arquivo binario que sera compactado em LZW
                    else if(compactar == 2){

                        //Cria um string como final para ser compativel com o lambda
                        final String arquivoLZW = arquivoCompactado + "LZW.bin";

                        //Função lambda que cronometrar e a eficiencia da compressão LZW
                        Cronometro.cronometrar("Compressão LZW", () -> {
                            CompEDescompLZW.CodificaLZW(binarioFile, arquivoLZW);
                        });

                    }

                    //Compacta os arquivos binarios em Huffman e LZW e compara sua eficiencia e sua velocidade
                    else if(compactar == 3){

                        //Cria um string como final para ser compativel com o lambda
                        final String arquivoHuffman = arquivoCompactado + "Huffman.bin";
                        final String arquivoLZW = arquivoCompactado + "LZW.bin";

                       //Função lambda que cronometrar e a eficiencia da compressão Huffman
                        tempo[0] = Cronometro.cronometrar("Compressão Huffman", () -> {
                            eficiencia[0] = CompEDescompHuffman.CodificaHuffman(binarioFile, arquivoHuffman);
                        });

                        //Função lambda que cronometrar e a eficiencia da compressão LZW
                        tempo[1] = Cronometro.cronometrar("Compressão LZW", () -> {
                            eficiencia[1] = CompEDescompLZW.CodificaLZW(binarioFile, arquivoLZW);
                        });

                        System.out.println("----------------------------------------------------------------------------------");

                        //Compara a eficiencia de compressão entre Huffman e LZW
                        if(eficiencia[0] > eficiencia[1]){
                            System.out.println("A compactação Huffman foi mais eficiente com " + eficiencia[0] + "% de eficiência.");
                        } else if(eficiencia[1] > eficiencia[0]){
                            System.out.println("A compactação LZW foi mais eficiente com " + eficiencia[1] + "% de eficiência.");
                        } else {
                            System.out.println("Ambas as compactações tiveram a mesma eficiência: " + eficiencia[0] + "%.");
                        }

                        //Compara o tempo de compressão entre Huffman e LZW
                        if(tempo[0] < tempo[1]){
                            System.out.println("A compactação Huffman foi mais rapido com " + tempo[0] + "segundos.");
                        } else if(tempo[1] < tempo[0]){
                            System.out.println("A compactação LZW foi mais rapido com " + tempo[1] + "segundos.");
                        } else {
                            System.out.println("Ambas as compactações tiveram o mesmo tempo: " + tempo[0] + "segundos.");
                        }

                        System.out.println("----------------------------------------------------------------------------------");

                    }

                    break;

                }

                //Descompacta o arquivo binario
                case 9:{

                    //Chama um menu de seleção de descompactação
                    int descompactar = Menus.MenuDescompactar(sc);
                    arquivoCompactado = "src/Binarios/binarioCompressao";
                    double[] tempo = new double[2];//Variavel para armazenar o tempo de compressão para Huffman e LZW

                    //Descompacta o arquivo binario compactado em Huffman
                    if(descompactar == 1){

                        //Cria um string como final para ser compativel com o lambda
                        final String arquivoHuffman = arquivoCompactado + "Huffman.bin";

                        //Função lambda que cronometrar a decompressão Huffman
                        Cronometro.cronometrar("Compressão Huffman", () -> {
                            CompEDescompHuffman.DescodificaHuffman(arquivoHuffman, arquivoDescompactado, binarioFile);
                        });

                        atualizar.ReescreverArquivo(binarioFile, arquivoDescompactado);

                    }

                    //Descompacta o arquivo binario compactado em LZW
                    else if(descompactar == 2){

                        //Cria um string como final para ser compativel com o lambda
                        final String arquivoLZW = arquivoCompactado + "LZW.bin";

                        //Função lambda que cronometrar a decompressão LZW
                        Cronometro.cronometrar("Compressão LZW", () -> {
                            CompEDescompLZW.DescodificaLZW(arquivoLZW, arquivoDescompactado, binarioFile);
                        });

                        atualizar.ReescreverArquivo(binarioFile, arquivoDescompactado);

                    }

                    //Descompacta os arquivos binarios em Huffman e LZW e compara sua eficiencia e sua velocidade
                    else if(descompactar == 3){

                        //Cria um string como final para ser compativel com o lambda
                        final String arquivoHuffman = arquivoCompactado + "Huffman.bin";
                        final String arquivoLZW = arquivoCompactado + "LZW.bin";

                        //Função lambda que cronometrar a decompressão Huffman
                        tempo[0] = Cronometro.cronometrar("Compressão Huffman", () -> {
                            CompEDescompHuffman.DescodificaHuffman(arquivoHuffman, arquivoDescompactado, binarioFile);
                        });

                        //Função lambda que cronometrar a decompressão LZW
                        tempo[1] = Cronometro.cronometrar("Compressão LZW", () -> {
                            CompEDescompLZW.DescodificaLZW(arquivoLZW, arquivoDescompactado, binarioFile);
                        });

                        atualizar.ReescreverArquivo(binarioFile, arquivoDescompactado);

                        System.out.println("----------------------------------------------------------------------------------");

                        //Compara o tempo de descompressão entre Huffman e LZW
                        if(tempo[0] < tempo[1]){
                            System.out.println("A descompactação Huffman foi mais rapido com " + tempo[0] + "segundos.");
                        } else if(tempo[1] < tempo[0]){
                            System.out.println("A descompactação LZW foi mais rapido com " + tempo[1] + "segundos.");
                        } else {
                            System.out.println("Ambas as descompactações tiveram o mesmo tempo: " + tempo[0] + "segundos.");
                        }

                        System.out.println("----------------------------------------------------------------------------------");

                    }
                    
                    break;

                }

                case 10:{

                    //Menu para selecionar o tipo de casamento de padrão
                    int casamneto = Menus.MenuCasamento(sc);
                    double[] tempo = new double[2];//Variavel para armazenar o tempo de busca de padrão para KMP e Boyer-Moore

                    //Inicia a busca de padrão KMP
                    if(casamneto == 1){

                        sc.nextLine(); // Limpar o buffer

                        System.err.println("\tDigite o que deseja pesquisar:");
                        String padrao = sc.nextLine();

                        KMP.inicio(binarioFile, sc, padrao);

                    }

                    //Inicia a busca de padrão Boyer-Moore
                    else if(casamneto == 2){

                        sc.nextLine(); // Limpar o buffer

                        System.err.println("\tDigite o que deseja pesquisar:");
                        String padrao = sc.nextLine();

                        BoyerMoore.inicio(binarioFile, sc, padrao);

                    }

                    //Faz uma comparação entre os dois métodos de casamento de padrão
                    else if(casamneto == 3){

                        sc.nextLine(); // Limpar o buffer

                        System.err.println("\tDigite o que deseja pesquisar:");
                        String padrao = sc.nextLine();

                        tempo[0] = Cronometro.cronometrar("Casamento de padrão KMP:", () -> {
                            KMP.inicio(binarioFile, sc , padrao);
                        });

                        tempo[1] = Cronometro.cronometrar("Casamento de padrão Boyer-Moore:", () -> {
                            BoyerMoore.inicio(binarioFile, sc ,padrao);
                        });

                        System.out.println();

                        System.out.println("----------------------------------------------------------------------------------");

                        //Compara o tempo de busca de padrão entre KMP e Boyer-Moore
                        if(tempo[0] < tempo[1]){
                            System.out.println("O metado KMP foi mais rapido com " + tempo[0] + "segundos.");
                        } else if(tempo[1] < tempo[0]){
                            System.out.println("O metado Boyer-Moore foi mais rapido com " + tempo[1] + "segundos.");
                        } else {
                            System.out.println("Ambas os metados tiveram o mesmo tempo: " + tempo[0] + "segundos.");
                        }

                        System.out.println("----------------------------------------------------------------------------------");

                    }

                    break;

                }

                case 11:{

                    int Criptografia = Menus.MenuCriptografar(sc);

                    if(Criptografia == 1){

                        arquivoCriptografado = "src/Binarios/criptografadoVigenere.bin";

                        sc.nextLine(); // Limpar o buffer

                        Vigenere.criptografia(arquivoCriptografado, sc, binarioFile);

                    }

                    else if(Criptografia == 2){

                        arquivoCriptografado = "src/Binarios/criptografadoDES.bin";

                        sc.nextLine(); // Limpar o buffer

                        DES.criptografia(arquivoCriptografado, sc, binarioFile);

                    }

                    break;

                }

                case 12:{

                    int Descriptografia = Menus.MenuDescriptografar(sc);

                    if(Descriptografia == 1){

                        arquivoCriptografado = "src/Binarios/criptografadoVigenere.bin";

                        sc.nextLine(); // Limpar o buffer

                        Vigenere.descriptografia(arquivoCriptografado, sc, arquivoDescriptografado);

                        atualizar.ReescreverArquivo(binarioFile, arquivoDescriptografado);

                    }

                    else if(Descriptografia == 2){

                        arquivoCriptografado = "src/Binarios/criptografadoDES.bin";

                        sc.nextLine(); // Limpar o buffer

                        DES.descriptografia(arquivoCriptografado, sc, arquivoDescriptografado);

                        atualizar.ReescreverArquivo(binarioFile, arquivoDescriptografado);

                    }

                    break;

                }
                
                //Exclui o arquivo binário e os arquivos de indexação temporarios
                case 0:{

                    try{

                        Path binarioFilePath = Paths.get(binarioFile);
                        Path binarioPaisPath = Paths.get(binarioPais);
                        Path caminhoDescomp = Paths.get(arquivoDescompactado);
                        Path arquivoDescriptografadoPath = Paths.get(arquivoDescriptografado);

                        boolean deletado;

                        // Exclui o arquivo se ele existir
                        deletado = Files.deleteIfExists(binarioPaisPath);
                        if (deletado) {
                            System.out.println("Arquivo " + binarioPaisPath.getFileName().toString() + " excluído com sucesso");
                        }

                        // Exclui o arquivo se ele existir
                        deletado = Files.deleteIfExists(binarioFilePath);
                        if (deletado) {
                            System.out.println("Arquivo " + binarioFilePath.getFileName().toString() + " excluído com sucesso");
                        }

                        deletado = Files.deleteIfExists(caminhoDescomp);
                        if (deletado) {
                            System.out.println("Arquivo " + caminhoDescomp.getFileName().toString() + " excluído com sucesso");
                        }

                        deletado = Files.deleteIfExists(arquivoDescriptografadoPath);
                        if (deletado) {
                            System.out.println("Arquivo " + arquivoDescriptografadoPath.getFileName().toString() + " excluído com sucesso");
                        }

                        if(arquivoIndexacao != null){
                        
                            //Obtem o caminho do arquivo de indexação
                            Path caminho = Paths.get(arquivoIndexacao);

                            if (arvore != null) {
                                try {
                                    arvore.close(); // Fechar o arquivo antes de excluir
                                } catch (IOException e) {
                                    System.out.println("Erro ao fechar o arquivo: " + e.getMessage());
                                }
                            }

                            if(lista1 != null) {
                                try {
                                    lista1.close(); // Fechar o arquivo antes de excluir
                                } catch (IOException e) {
                                    System.out.println("Erro ao fechar arquivos da primeira lista: " + e.getMessage());
                                }
                            }
                            
                            if(lista2 != null) {
                                try {
                                    lista2.close(); // Fechar o arquivo antes de excluir
                                } catch (IOException e) {
                                    System.out.println("Erro ao fechar arquivos da segunda lista: " + e.getMessage());
                                }
                            }

                            if(hash != null) {
                                try {
                                    hash.close(); // Fechar o arquivo e exclui
                                } catch (IOException e) {
                                    System.out.println("Erro ao fechar arquivos da hash: " + e.getMessage());
                                }
                            }

                            
                            // Exclui o arquivo se ele existir
                            deletado = Files.deleteIfExists(caminho);
                            if (deletado) {
                                System.out.println("Arquivo " + caminho.getFileName().toString() + " excluído com sucesso");
                            }
                            
                            // Verifica se o arquivo existe antes de tentar excluí-lo
                            if(arquivoIndexacao2 != null){
                                Path caminho2 = Paths.get(arquivoIndexacao2);
                                Path dicinario = Paths.get(dicinarioLista);
                            
                                
                                deletado = Files.deleteIfExists(caminho2);
                                if (deletado) {
                                    System.out.println("Arquivo " + caminho2.getFileName().toString() + " excluído com sucesso");
                                }
                                
                                
                                deletado = Files.deleteIfExists(dicinario);
                                if (deletado) {
                                    System.out.println("Arquivo " + dicinario.getFileName().toString() + " excluído com sucesso");
                                }
                                
                            }

                        }

                    } catch (IOException e) {
                        System.out.println("Erro ao excluir arquivo: " + e.getMessage());
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