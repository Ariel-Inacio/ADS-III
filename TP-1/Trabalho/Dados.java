import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;

public class Dados {

    public static void IniciarArquivoCSV (String file, String binarioFile, String binarioPais, String file2){

        System.out.println("Lendo arquivo...");

        int contador = 0;
        int contadorP = 0;

        try (BufferedReader leituraP = new BufferedReader(new FileReader(file2));
        DataOutputStream outP = new DataOutputStream(new FileOutputStream(binarioPais))){

            outP.writeInt(0);

            String lineP;
            String PLinha = leituraP.readLine();

            while((lineP = leituraP.readLine())!= null){

                List<String> dadosPais = extrairDadosLinha(lineP);

                contadorP++;
                escreverPaisBinario(outP, dadosPais);

            }

            RandomAccessFile Binario = new RandomAccessFile(binarioPais, "rw");
            Binario.seek(0);
            Binario.writeInt(contadorP);
            Binario.close();

        }catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader leitura = new BufferedReader(new FileReader(file));
        DataOutputStream out = new DataOutputStream(new FileOutputStream(binarioFile))){

            out.writeInt(0);

            String line;
            String PLinha = leitura.readLine();

            while((line = leitura.readLine()) != null){

                line = line.replaceAll(";", "").trim();
                line = line.replaceAll("^\"|\"$", "").trim();
                line = line.replaceAll("\"\"", "\"").trim();

                List<String> dadosFilme = extrairDadosLinha(line);

                String paisFilme = dadosFilme.get(4);
                paisFilme = PesquisarPaisAbre(binarioPais, paisFilme);
                contador++;
                escreverFilmeBinario(out, contador, dadosFilme, paisFilme);
                
            }

            RandomAccessFile Binario = new RandomAccessFile(binarioFile, "rw");
            Binario.seek(0);
            Binario.writeInt(contador);
            Binario.close();

            System.out.println("Arquivo binário salvo com sucesso com " + contador + " registros");

        }catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<String> extrairDadosLinha(String line){

        List<String> lista = new ArrayList<>();

        Matcher m = Pattern.compile("\"([^\"]*)\"|([^,]+)").matcher(line);
    
        while (m.find()) {
            String resultado = m.group(1) != null ? m.group(1) : m.group(2);
            if (resultado != null) {
                lista.add(resultado.trim());
            }
        }
        
        return lista;
    }

    private static void escreverPaisBinario(DataOutputStream out, List<String> lista) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(baos);

        byte[] nomeBytes = lista.get(0).getBytes("UTF-8");
        dataOut.writeShort(nomeBytes.length);
        dataOut.write(nomeBytes);

        byte[] abreBytes = lista.get(1).getBytes("UTF-8");
        dataOut.writeShort(abreBytes.length);
        dataOut.write(abreBytes);

        byte[] objectBytes = baos.toByteArray();
        out.writeInt(objectBytes.length);
        out.write(objectBytes);

    }

    private static void escreverFilmeBinario(DataOutput out, int id, List<String> lista, String pais) throws IOException {
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(baos);

        dataOut.writeBoolean(false);
        dataOut.writeInt(id);
        
        byte[] tipoBytes = lista.get(1).getBytes("UTF-8");
        dataOut.writeShort(tipoBytes.length);
        dataOut.write(tipoBytes);
        
        byte[] nomeBytes = lista.get(2).getBytes("UTF-32");
        dataOut.writeShort(nomeBytes.length);
        dataOut.write(nomeBytes);
        
        byte[] diretorBytes = lista.get(3).getBytes("UTF-32");
        dataOut.writeShort(diretorBytes.length);
        dataOut.write(diretorBytes);
        
        byte[] paisBytes = pais.getBytes("UTF-8");
        dataOut.writeShort(paisBytes.length);
        dataOut.write(paisBytes);
        
        byte[] anoAdiBytes = lista.get(5).getBytes("UTF-8");
        dataOut.writeShort(anoAdiBytes.length);
        dataOut.write(anoAdiBytes);
        
        byte[] anoLanBytes = lista.get(6).getBytes("UTF-8");
        dataOut.writeShort(anoLanBytes.length);
        dataOut.write(anoLanBytes);
        
        byte[] classificacaoBytes = lista.get(7).getBytes("UTF-8");
        dataOut.writeShort(classificacaoBytes.length);
        dataOut.write(classificacaoBytes);
        
        byte[] duracaoBytes = lista.get(8).getBytes("UTF-8");
        dataOut.writeShort(duracaoBytes.length);
        dataOut.write(duracaoBytes);
        
        byte[] generoBytes = lista.get(9).getBytes("UTF-8");
        dataOut.writeShort(generoBytes.length);
        dataOut.write(generoBytes);
        
        byte[] objectBytes = baos.toByteArray();
        out.writeInt(objectBytes.length);
        out.write(objectBytes);

    }

    public static void lerBinario(String binarioFile) {
        List<Filmes> filmes = new ArrayList<>();

        try (DataInputStream in = new DataInputStream(new FileInputStream(binarioFile))){

            int NTotal = in.readInt();
            
            for(int i = 0; i < NTotal; i++){

                byte[] objectBytes = new byte[in.readInt()];
                in.readFully(objectBytes);
        
                DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(objectBytes));
                
                Filmes filme = new Filmes();
                filme.readPersonalizado(dataIn);
                
                filmes.add(filme);
            }

            in.close();

        }catch (FileNotFoundException e) {
            System.out.println("Arquivo nao encontrado");
        }catch(IOException e){
            e.printStackTrace();
        }

        for(Filmes filme : filmes){

            if(filme.getLAPIDE() == false){
                filme.Ler();
            }

        }
    } 

    public static void atualizarFilmeID(int IDDesejado, Filmes novoFilme, String binarioFile) {

        try(RandomAccessFile file = new RandomAccessFile(binarioFile, "rw")){

            int Ntotal = file.readInt();

            long posicaoInicial;
            int tamanhoObjeto;
            boolean encontrado = false;
            boolean incrementar = false;

            for(int i = 0; i < Ntotal; i++){
                
                posicaoInicial = file.getFilePointer();

                tamanhoObjeto = file.readInt();
                byte[]dadosFilmes = new byte[tamanhoObjeto];
                file.readFully(dadosFilmes);

                ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosFilmes);
                DataInputStream dataIn = new DataInputStream(byteStream);

                boolean lapide = dataIn.readBoolean();
                int id = dataIn.readInt();

                if(!lapide && id == IDDesejado){

                    encontrado = true;

                    ByteArrayOutputStream newByteStrteam = new ByteArrayOutputStream();
                    DataOutputStream DataOut = new DataOutputStream(newByteStrteam);
                    novoFilme.writePersonalizado(DataOut, false);
                    byte[] novoBytes = newByteStrteam.toByteArray();
                    int novoTamanho = novoBytes.length;

                    if(novoTamanho <= tamanhoObjeto){

                        file.seek(posicaoInicial + 4);
                        file.write(novoBytes);

                        int diferenca = tamanhoObjeto - novoTamanho;
                        if(diferenca > 0){
                            byte[] zeros = new byte[diferenca];
                            file.write(zeros);
                        }

                    }

                    else{

                        file.seek(posicaoInicial + 4);
                        file.writeBoolean(true);

                        file.seek(file.length());
                        file.writeInt(novoTamanho);
                        file.write(novoBytes);

                        incrementar = true;
                    }

                    System.out.println("Filme atualizado com sucesso!");
                    break;
                }
            }

            if(!encontrado){
                System.out.println("Registro com ID " + IDDesejado + " nao encontrado.");
            }
            else if(incrementar){
                file.seek(0);
                file.writeInt(Ntotal + 1);
            }

        }catch (IOException e){
            System.out.println("Arquivo nao encontrado");
        }
    }

    public static String readUTF32(DataInputStream dataIn) throws IOException {

        int tamanho = dataIn.readShort();
        byte[] utf32Bytes = new byte[tamanho];
        dataIn.readFully(utf32Bytes);

        return new String(utf32Bytes, "UTF-32");
    }

    public static String PesquisarPaisAbre(String binarioFilePais, String NomePais){

        String resultado = "---";

        try(RandomAccessFile file = new RandomAccessFile(binarioFilePais, "r")){
            
            int NTotais = file.readInt();
            
            for(int i = 0; i < NTotais; i++){

                int tamanhoObjeto = file.readInt();
                byte[] dadosFilmes = new byte[tamanhoObjeto];
                file.readFully(dadosFilmes);

                ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosFilmes);
                DataInputStream dataIn = new DataInputStream(byteStream);

                String nome = dataIn.readUTF();

                if(nome.equals(NomePais)){

                    resultado = dataIn.readUTF();
                    break;

                }

            }

        }catch (IOException e){
            System.out.println("Arquivo nao encontrado");
            e.printStackTrace();
        }

        return resultado;

    }

    public static void PesquisarNome(String binarioFile, String NomeDesejado){

        try(RandomAccessFile file = new RandomAccessFile(binarioFile, "r")){

            int NTotais = file.readInt();

            for(int i = 0; i < NTotais; i++){
                
                int tamanhoObjeto = file.readInt();
                byte[]dadosFilmes = new byte[tamanhoObjeto];
                file.readFully(dadosFilmes);

                ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosFilmes);
                DataInputStream dataIn = new DataInputStream(byteStream);

                byteStream.mark(0);

                boolean lapide = dataIn.readBoolean();
                dataIn.readInt();
                dataIn.readUTF();
                String nome = readUTF32(dataIn);

                if(!lapide && NomeDesejado.equals(nome)){

                    byteStream.reset();
                    Filmes novoFilme = new Filmes();
                    novoFilme.readPersonalizado(dataIn);
                    novoFilme.Ler();

                }
                
            }

        }catch (IOException e){
            System.out.println("Arquivo nao encontrado");
            e.printStackTrace();
        }
        
    }

    public static Filmes PesquisarID(String binarioFile, int IDDesejado){
        
        try(RandomAccessFile file = new RandomAccessFile(binarioFile, "r")){

            int NTotais = file.readInt();

            for(int i = 0; i < NTotais; i++){
                
                int tamanhoObjeto = file.readInt();
                byte[]dadosFilmes = new byte[tamanhoObjeto];
                file.readFully(dadosFilmes);

                ByteArrayInputStream byteStream = new ByteArrayInputStream(dadosFilmes);
                DataInputStream dataIn = new DataInputStream(byteStream);

                byteStream.mark(0);

                boolean lapide = dataIn.readBoolean();
                int id = dataIn.readInt();

                if(!lapide && IDDesejado == id){

                    byteStream.reset();
                    Filmes novoFilme = new Filmes();
                    novoFilme.readPersonalizado(dataIn);

                    return novoFilme;

                }
                
            }

        }catch (IOException e){
            System.out.println("Arquivo nao encontrado");
            e.printStackTrace();
        }

        return null;

    }

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

                case 1:{

                    tipo = ("TV Show");
                    verificar1 = true;
                    break;

                }

                case 2:{

                    tipo = ("Movie");
                    verificar1 = true;
                    break;

                }

                default:{

                    System.out.println("\tOpcao invalida");

                }

            } 
            
        } while (!verificar1);

        return tipo;

    }

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
            System.out.println("\t6: TV-14 (Orientação Parental extremamente recomendada, Não recomendado para menores de 14 anos)");
            System.out.println("\t7: TV-MA (Audiência madura — não recomendado para menores de 17 anos)");
            System.out.println("\t-----------------------------------------");

            int opcao = sc.nextInt();

            switch(opcao){

                case 1:{

                    classificacao = ("TV-Y");
                    verificar2 = true;
                    break;

                }

                case 2:{

                    classificacao = ("TV-Y7");
                    verificar2 = true;
                    break;

                }

                case 3:{

                    classificacao = ("TV-Y7-FV");
                    verificar2 = true;
                    break;

                }

                case 4:{

                    classificacao = ("TV-G");
                    verificar2 = true;
                    break;

                }

                case 5:{

                    classificacao = ("TV-PG");
                    verificar2 = true;
                    break;

                }

                case 6:{

                    classificacao = ("TV-14");
                    verificar2 = true;
                    break;

                }

                case 7:{

                    classificacao = ("TV-MA");
                    verificar2 = true;
                    break;

                }

                default:{

                    System.out.println("\tOpcao invalida");

                }

            }

        }while(!verificar2);

        return classificacao;
    }

    public static void main(String[] args) {
        int opcao;
        String file = "netflix1.csv";
        String file2 = "WorldCountriesList.csv";
        String binarioFile = "binario.bin";
        String binarioPais = "binarioPais.bin";
        Scanner sc = new Scanner(System.in);

        do{

            System.out.println("\t----Opcoes----");
            System.out.println("\t1: Ler arquivo CSV");
            System.out.println("\t2: Ler arquivo BINARO");
            System.out.println("\t3: Pesquisar Filme/Serie");
            System.out.println("\t4: Atualizar Lista");
            System.out.println("\t5: Remover Filme/Serie");
            System.out.println("\t6: Adicionar novo Filme/Serie");
            System.out.println("\t0: Sair");
            System.out.println("\t-------------------");

            opcao = sc.nextInt();

            switch(opcao){

                case 1:{

                    IniciarArquivoCSV(file, binarioFile, binarioPais, file2);
                    break;

                }

                case 2:{

                    lerBinario(binarioFile);
                    break;

                }

                case 3:{

                    Filmes filmePesquisa = new Filmes();
                    int opcaoPesquisar; 

                    do{

                        System.out.println("\t-----------------------------------------");
                        System.out.println("\t1: Pesquisar pelo ID");
                        System.out.println("\t2: Pesquisar pelo nome");
                        System.out.println("\t0: Sair");
                        System.out.println("\t-----------------------------------------");

                        opcaoPesquisar = sc.nextInt();

                        switch(opcaoPesquisar){

                            case 1:{

                                System.out.println("\tDigite o ID do filme/serie:");

                                int IDDesejado = sc.nextInt();
                                filmePesquisa = PesquisarID(binarioFile, IDDesejado);

                                if(filmePesquisa != null){
                                    filmePesquisa.Ler();
                                }
                                
                                break;

                            }

                            case 2:{

                                sc.nextLine();

                                System.out.println("\tDigite o nome do filme/serie:");

                                String NomeDesejado = sc.nextLine();
                                PesquisarNome(binarioFile, NomeDesejado);

                                break;

                            }

                            case 0:{
                                System.out.println("Saindo...");
                                break;
                            }

                            default:{
                                System.out.println("opcao invailda");
                            }
                        } 

                    }while(opcaoPesquisar != 0);

                    break;

                }

                case 4:{

                    System.out.println("Digite o ID do Filme desejado");
                    int IDDesejado = sc.nextInt();

                    Filmes novoFilme = PesquisarID(binarioFile, IDDesejado);

                    if(novoFilme != null){

                        novoFilme.Ler();
                        int opcaoAtualizar;

                        do{

                            System.out.println("\t-----------------------------------------");
                            System.out.println("\t1: Atualizar Tipo (TV Show/Movie)");
                            System.out.println("\t2: Atualizar nome");
                            System.out.println("\t3: Atualizar diretor");
                            System.out.println("\t4: Atualizar Pais");
                            System.out.println("\t5: Atualizar ano de adicao");
                            System.out.println("\t6: Atualizar ano de lancamento");
                            System.out.println("\t7: Atualizar classificacao");
                            System.out.println("\t8: Atualizar duracao");
                            System.out.println("\t9: Atualizar genero");
                            System.out.println("\t0: Confirmar atualizacao");
                            System.out.println("\t-----------------------------------------");

                            opcaoAtualizar = sc.nextInt();

                            switch(opcaoAtualizar){
                        
                                case 0:{

                                    atualizarFilmeID(IDDesejado, novoFilme, binarioFile);
                                    System.out.println("Atualizando dados...");
                                    break;

                                }

                                case 1:{

                                    novoFilme.setTIPO(tipo(sc));    
                                    System.out.println("Tipo atualizado...");
                                    break;

                                }

                                case 2:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo nome:");
                                    String nome = sc.nextLine();
                                    novoFilme.setNOME(nome);
                                    System.out.println("Nome atualizado...");
                                    break;

                                }

                                case 3:{

                                    sc.nextLine();

                                    System.out.println("Digite o nome do novo diretor:");
                                    String diretor = sc.nextLine();
                                    novoFilme.setDIRETOR(diretor);
                                    System.out.println("Diretor atualizado...");
                                    break;

                                }

                                case 4:{

                                    sc.nextLine();

                                    System.out.println("Digite o nome do novo pais (Em ingles):");
                                    String pais = sc.nextLine();

                                    pais = pais.substring(0,1).toUpperCase() + pais.substring(1); 

                                    pais = PesquisarPaisAbre(binarioPais, pais);

                                    if(pais.equals("---")){
                                        System.out.println("Pais nao encontrado, tente novamente caso deseja adicinar um pais...");
                                    }

                                    else{
                                        novoFilme.setPAIS(pais);
                                        System.out.println("Pais atualizado...");
                                    }

                                    break;

                                }

                                case 5:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo ano de adicao:");
                                    String anoAdicao = sc.nextLine();
                                    novoFilme.setANO_ADI(anoAdicao);
                                    System.out.println("Ano de adicao atualizado...");
                                    break;

                                }

                                case 6:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo ano de lancamento:");
                                    String anoLancamento = sc.nextLine();
                                    novoFilme.setANO_LAN(anoLancamento);
                                    System.out.println("Ano de lancamento atualizado...");
                                    break;

                                }

                                case 7:{

                                    novoFilme.setCLASSIFICACAO(classificacaoIndicativa(sc));
                                    System.out.println("Classificacao atualizada para...");
                                    break;

                                }

                                case 8:{

                                    sc.nextLine();

                                    System.out.println("Digite a nova duracao do Filme/Serie (Para digite para cada situacao de acordo com nos exemplos: 120 min (para filmes)/ 2 Season (para series))");
                                    String duracao = sc.nextLine();

                                    novoFilme.setDURACAO(duracao);
                                    System.out.println("Duracao atualizada...");
                                    break;

                                }

                                case 9:{

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

                        novoFilme.Ler();
                        atualizarFilmeID(IDDesejado,novoFilme ,binarioFile);

                    }

                    else{
                        System.out.println("Filme não encontrado!");
                    }
                    break;

                }

                case 5:{

                    System.out.println("Digite o ID do Filme desejado para remover");
                    int IDDesejado = sc.nextInt();
                    Filmes novoFilme = PesquisarID(binarioFile, IDDesejado);

                    if(novoFilme!= null){

                        novoFilme.setLAPIDE(true);
                        atualizarFilmeID(IDDesejado, novoFilme, binarioFile);
                        System.out.println("Filme removido com sucesso!");

                    }

                    break;

                }

                case 6:{

                    try(RandomAccessFile BinarioFilmes = new RandomAccessFile(binarioFile, "rw")){

                        int posicao = BinarioFilmes.readInt();
                        posicao++;

                        int opcaoAdicinar;
                        List<String> lista = new ArrayList<>();

                        lista.add("1");

                        lista.add(tipo(sc));

                        sc.nextLine();
                        System.out.println("\tDigite o nome: ");
                        String nome = sc.nextLine();
                        lista.add(nome);

                        System.out.println("\tDigite o diretor: ");
                        String diretor = sc.nextLine();
                        lista.add(diretor);

                        System.out.println("\tDigite o pais (em ingles): ");
                        String pais = sc.nextLine();
                        pais = pais.substring(0,1).toUpperCase() + pais.substring(1); 
                        pais = PesquisarPaisAbre(binarioPais, pais);
                        lista.add(pais);

                        System.out.println("\tDigite o ano de adicao: ");
                        String anoAdicao = sc.nextLine();
                        lista.add(anoAdicao);

                        System.out.println("\tDigite o ano de lancamento: ");
                        String anoLancamento = sc.nextLine();
                        lista.add(anoLancamento);

                        lista.add(classificacaoIndicativa(sc));

                        sc.nextLine();
                        System.out.println("\tDigite a duracao do Filme/Serie (Para digite para cada situacao de acordo com nos exemplos: 120 min (para filmes)/ 2 Season (para series))");
                        String duracao = sc.nextLine();
                        lista.add(duracao);

                        System.out.println("\tDigite o genero: ");
                        String genero = sc.nextLine();
                        lista.add(genero);

                        Filmes novoFilme = new Filmes(lista, posicao);
                        System.out.println("Preview");
                        novoFilme.Ler();

                        do{

                            System.out.println("\t-----------------------------------------");
                            System.out.println("\t1: Mudar Tipo (TV Show/Movie)");
                            System.out.println("\t2: Mudar nome");
                            System.out.println("\t3: Mudar diretor");
                            System.out.println("\t4: Mudar Pais");
                            System.out.println("\t5: Mudar ano de adicao");
                            System.out.println("\t6: Mudar ano de lancamento");
                            System.out.println("\t7: Mudar classificacao");
                            System.out.println("\t8: Mudar duracao");
                            System.out.println("\t9: Mudar genero");
                            System.out.println("\t0: Confirmar");
                            System.out.println("\t-----------------------------------------");
                            
                            opcaoAdicinar = sc.nextInt();

                            switch(opcaoAdicinar){

                                case 0:{

                                    BinarioFilmes.seek(0);
                                    BinarioFilmes.writeInt(posicao);

                                    BinarioFilmes.seek(BinarioFilmes.length());
                                    escreverFilmeBinario(BinarioFilmes, posicao, lista, pais);    
                                    
                                    System.out.println("Atualizando dados...");

                                    break;

                                }

                                case 1:{

                                    System.out.println("\t-----------------------------------------");
                                    System.out.println("\t1: TV Show");
                                    System.out.println("\t2: Movie");
                                    System.out.println("\t-----------------------------------------");

                                    boolean verificar = false;
                                    
                                    do {

                                        int tipo = sc.nextInt();

                                        switch(tipo){

                                            case 1:{

                                                novoFilme.setTIPO("TV Show");
                                                System.out.println("Tipo atualizado para TV Show...");
                                                verificar = true;
                                                break;

                                            }

                                            case 2:{

                                                novoFilme.setTIPO("Movie");
                                                System.out.println("Tipo atualizado para Movie...");
                                                verificar = true;
                                                break;

                                            }

                                            default:{

                                                System.out.println("\tOpcao invalida");

                                            }

                                        } 
                                        
                                    } while (!verificar);
                                    
                                    break;

                                }

                                case 2:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo nome:");
                                    nome = sc.nextLine();
                                    novoFilme.setNOME(nome);
                                    System.out.println("Nome atualizado...");
                                    break;

                                }

                                case 3:{

                                    sc.nextLine();

                                    System.out.println("Digite o nome do novo diretor:");
                                    diretor = sc.nextLine();
                                    novoFilme.setDIRETOR(diretor);
                                    System.out.println("Diretor atualizado...");
                                    break;

                                }

                                case 4:{

                                    sc.nextLine();

                                    System.out.println("Digite o nome do novo pais (Em ingles):");
                                    pais = sc.nextLine();

                                    pais = pais.substring(0,1).toUpperCase() + pais.substring(1); 
                                    pais = PesquisarPaisAbre(binarioPais, pais);

                                    if(pais.equals("---")){
                                        System.out.println("Pais nao encontrado, tente novamente caso deseja adicinar um pais...");
                                    }

                                    else{
                                        novoFilme.setPAIS(pais);
                                        System.out.println("Pais atualizado...");
                                    }

                                    break;

                                }

                                case 5:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo ano de adicao:");
                                    anoAdicao = sc.nextLine();
                                    novoFilme.setANO_ADI(anoAdicao);
                                    System.out.println("Ano de adicao atualizado...");
                                    break;

                                }

                                case 6:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo ano de lancamento:");
                                    anoLancamento = sc.nextLine();
                                    novoFilme.setANO_LAN(anoLancamento);
                                    System.out.println("Ano de lancamento atualizado...");
                                    break;

                                }

                                case 7:{

                                    novoFilme.setCLASSIFICACAO(classificacaoIndicativa(sc));

                                }

                                case 8:{

                                    sc.nextLine();

                                    System.out.println("Digite a nova duracao do Filme/Serie (Para digite para cada situacao de acordo com nos exemplos: 120 min (para filmes)/ 2 Season (para series))");
                                    duracao = sc.nextLine();

                                    novoFilme.setDURACAO(duracao);
                                    System.out.println("Duracao atualizada...");
                                    break;

                                }

                                case 9:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo genero");
                                    genero = sc.nextLine();

                                    novoFilme.setGENERO(genero);
                                    System.out.println("Genero atualizado...");
                                    break;

                                }

                                default: {
                                    System.out.println("Opcao invalida");
                                }

                            }

                        }while(opcaoAdicinar != 0);

                    }catch (IOException e){
                        System.out.println("Arquivo nao encontrado");
                        e.printStackTrace();
                    }

                    break;

                }

                case 0:{
                    
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

class Filmes implements Externalizable{

    private boolean lapide;
    private int id;
    private String tipo;
    private String nome;
    private String diretor;
    private String pais;
    private String ano_adi;
    private String ano_lan;
    private String classificacao;
    private String duracao;
    private String Genero;

    public Filmes(){}

    public Filmes(List<String> lista, int tmp){

        this.lapide = false;
        this.id = tmp;
        this.tipo = lista.get(1);
        this.nome = lista.get(2);
        this.diretor = lista.get(3);
        this.pais = lista.get(4);
        this.ano_adi = lista.get(5);
        this.ano_lan = lista.get(6);
        this.classificacao = lista.get(7);
        this.duracao = lista.get(8);
        this.Genero = lista.get(9);
        
    }

    public void writePersonalizado(DataOutputStream out, Boolean escreverTamanho) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(baos);

        dataOut.writeBoolean(lapide);
        dataOut.writeInt(id);

        byte[] tipoBytes = tipo.getBytes("UTF-8");
        dataOut.writeShort(tipoBytes.length);
        dataOut.write(tipoBytes);

        byte[] nomeBytes = nome.getBytes("UTF-32");
        dataOut.writeShort(nomeBytes.length);
        dataOut.write(nomeBytes);

        byte[] diretorBytes = diretor.getBytes("UTF-32");
        dataOut.writeShort(diretorBytes.length);
        dataOut.write(diretorBytes);

        byte[] paisBytes = pais.getBytes("UTF-8");
        dataOut.writeShort(paisBytes.length);
        dataOut.write(paisBytes);

        byte[] ano_adiBytes = ano_adi.getBytes("UTF-8");
        dataOut.writeShort(ano_adiBytes.length);
        dataOut.write(ano_adiBytes);

        byte[] ano_lanBytes = ano_lan.getBytes("UTF-8");
        dataOut.writeShort(ano_lanBytes.length);
        dataOut.write(ano_lanBytes);

        byte[] classificacaoBytes = classificacao.getBytes("UTF-8");
        dataOut.writeShort(classificacaoBytes.length);
        dataOut.write(classificacaoBytes);

        byte[] duracaoBytes = duracao.getBytes("UTF-8");
        dataOut.writeShort(duracaoBytes.length);
        dataOut.write(duracaoBytes);

        byte[] GeneroBytes = Genero.getBytes("UTF-8");
        dataOut.writeShort(GeneroBytes.length);
        dataOut.write(GeneroBytes);

        byte[] objectBytes = baos.toByteArray();

        if(escreverTamanho){
            out.writeInt(objectBytes.length);
        }
        out.write(objectBytes);

    }

    public void readPersonalizado(DataInput dataIn) throws IOException{

        lapide = dataIn.readBoolean();
        id = dataIn.readInt();

        byte[] tipoBytes = new byte[dataIn.readShort()];
        dataIn.readFully(tipoBytes);
        tipo = new String(tipoBytes, "UTF-8");

        byte[] nomeBytes = new byte[dataIn.readShort()];
        dataIn.readFully(nomeBytes);
        nome = new String(nomeBytes, "UTF-32");

        byte[] diretorBytes = new byte[dataIn.readShort()];
        dataIn.readFully(diretorBytes);
        diretor = new String(diretorBytes, "UTF-32");

        byte[] paisBytes = new byte[dataIn.readShort()];
        dataIn.readFully(paisBytes);
        pais = new String(paisBytes, "UTF-8");

        byte[] ano_adiBytes = new byte[dataIn.readShort()];
        dataIn.readFully(ano_adiBytes);
        ano_adi = new String(ano_adiBytes, "UTF-8");

        byte[] ano_lanBytes = new byte[dataIn.readShort()];
        dataIn.readFully(ano_lanBytes);
        ano_lan = new String(ano_lanBytes, "UTF-8");

        byte[] classificacaoBytes = new byte[dataIn.readShort()];
        dataIn.readFully(classificacaoBytes);
        classificacao = new String(classificacaoBytes, "UTF-8");

        byte[] duracaoBytes = new byte[dataIn.readShort()];
        dataIn.readFully(duracaoBytes);
        duracao = new String(duracaoBytes, "UTF-8");

        byte[] GeneroBytes = new byte[dataIn.readShort()];
        dataIn.readFully(GeneroBytes);
        Genero = new String(GeneroBytes, "UTF-8");

    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {}

    @Override
    public void readExternal(ObjectInput in) throws IOException {}

    public void Ler(){

        System.out.println("----------------------------------------");
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Ano de Lancamento: " + ano_lan);
        System.out.println("Data de Adicao: " + ano_adi);
        System.out.println("Duração: " + duracao);
        System.out.println("Diretor: " + diretor);
        System.out.println("Pais: " + pais);
        System.out.println("Gênero: " + Genero);
        System.out.println("Tipo: " + tipo);
        System.out.println("Faixa Etaria: " + classificacao);
        System.out.println("----------------------------------------");

    }

    public boolean getLAPIDE(){
        return lapide;
    }
    public void setLAPIDE(boolean lapide){
        this.lapide = lapide;
    }

    public int getID(){
        return id;
    }
    public void setID (int id){
        this.id = id;
    }

    public String getTIPO(){
        return tipo;
    }
    public void setTIPO (String tipo){
        this.tipo = tipo;
    }

    public String getNOME(){
        return nome;
    }
    public void setNOME (String nome){
        this.nome = nome;
    }

    public String getDIRETOR(){
        return diretor;
    }
    public void setDIRETOR (String diretor){
        this.diretor = diretor;
    }

    public String getPAIS(){
        return pais;
    }
    public void setPAIS (String pais){
        this.pais = pais;
    }

    public String getANO_ADI(){
        return ano_adi;
    }
    public void setANO_ADI (String ano_adi){
        this.ano_adi = ano_adi;
    }

    public String getANO_LAN(){
        return ano_lan;
    }
    public void setANO_LAN (String ano_lan){
        this.ano_lan = ano_lan;
    }

    public String getCLSSIFICACAO(){
        return classificacao;
    }
    public void setCLASSIFICACAO (String classificacao){
        this.classificacao = classificacao;
    }

    public String getDURACAO(){
        return duracao;
    }
    public void setDURACAO (String duracao){
        this.duracao = duracao;
    }
    
    public String getGENERO(){
        return Genero;
    }
    public void setGENERO (String genero){
        this.Genero = genero;
    }
}