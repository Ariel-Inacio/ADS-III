import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dados {

    //Função para ler o arquivo CSV e salva-lo como um arquivo Binario
    public static void IniciarArquivoCSV(String file, String binarioFile, String binarioPais, String file2, int index, Scanner sc, ArvoreBMais<RegistroID> arvore) {

        System.out.println("Lendo arquivo...");
        
        int contador = 0; // Contador de registros de filmes
        int contadorP = 0; // Contador de registros de países
    
        // Processamento do arquivo de países
        try (BufferedReader leituraP = new BufferedReader(new FileReader(file2));
             DataOutputStream outP = new DataOutputStream(new FileOutputStream(binarioPais))) {
    
            outP.writeInt(0); // Inicializa o arquivo binário com um contador zerado
    
            String lineP;
            leituraP.readLine(); // Pula o cabeçalho
    
            while ((lineP = leituraP.readLine()) != null) {
                List<String> dadosPais = extrairDadosLinha(lineP); // Extrai os dados de cada linha
                contadorP++;
                escreverPaisBinario(outP, dadosPais); // Escreve os dados do país no arquivo binário
            }
    
            // Atualiza o número de registros no início do arquivo binário
            RandomAccessFile Binario = new RandomAccessFile(binarioPais, "rw");
            Binario.seek(0);
            Binario.writeInt(contadorP);
            Binario.close();
    
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        // Contagem dos registros do arquivo de filmes
        try (BufferedReader contagem = new BufferedReader(new FileReader(file))) {
            contagem.readLine(); // Pula o cabeçalho
            while (contagem.readLine() != null) {
                contador++;
            }
        } catch (IOException e) {
            System.out.println("Erro ao contar registros: " + e.getMessage());
            return;
        }
    
        // Processamento do arquivo de filmes
        try (
            BufferedReader leitura = new BufferedReader(new FileReader(file));
            RandomAccessFile out = new RandomAccessFile(binarioFile, "rw");
        ) {
            leitura.readLine(); // Pula o cabeçalho
            out.writeInt(contador); // Escreve o número total de registros no início do arquivo
    
            String line;
            int registro = 0;

            int tmp1 = 1;

            while ((line = leitura.readLine()) != null) {
                // Limpeza da linha para remover caracteres indesejados
                line = line.replaceAll(";", "").trim();
                line = line.replaceAll("^\"|\"$", "").trim();
                line = line.replaceAll("\"\"", "\"").trim();
    
                List<String> dadosFilme = extrairDadosLinha(line);
    
                // Substitui o nome do país pelo código correspondente
                String paisFilme = dadosFilme.get(4);
                dadosFilme.set(4, PesquisarPaisAbre(binarioPais, paisFilme));
                registro++;
    
                // Criação do objeto Filme
                Filmes tmp = new Filmes(dadosFilme, registro, false);
    
                // Serializa o objeto Filme em bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(tmp);
                }
                byte[] tmpSize = baos.toByteArray();

                long posicaoAtual = out.getFilePointer();
    
                // Escreve o tamanho do objeto serializado
                out.writeInt(tmpSize.length);
                
                // Escreve os bytes do objeto no arquivo binário
                out.write(tmpSize);

                try {
                    arvore.create(new RegistroID(tmp1++, posicaoAtual));
                } catch (Exception e) {
                    e.printStackTrace(); 
                }
    
                // Liberação explícita de memória
                dadosFilme = null;
                tmp = null;
            }
    
            System.out.println("Arquivo binário salvo com sucesso com " + contador + " registros");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Função para tratar melhor as string presentes no arquivo CSV
    private static List<String> extrairDadosLinha(String line) {

        List<String> lista = new ArrayList<>(); // Lista para armazenar os dados extraídos da linha
    
        // Expressão regular para capturar campos entre aspas ou separados por vírgula
        Matcher m = Pattern.compile("\"([^\"]*)\"|([^,]+)").matcher(line);
    
        while (m.find()) {
            // Se o primeiro grupo da regex (dentro de aspas) for encontrado, usa ele; caso contrário, usa o segundo
            String resultado = m.group(1) != null ? m.group(1) : m.group(2);
            if (resultado != null) {
                lista.add(resultado.trim()); // Adiciona o valor à lista, removendo espaços extras
            }
        }
        
        return lista; // Retorna a lista de valores extraídos
    }

    // Métado para transormar o CSV dos paises em binario
    private static void escreverPaisBinario(DataOutputStream out, List<String> lista) throws IOException {

        // Criação de um fluxo de saída de bytes para armazenar os dados do país
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(baos);
    
        // Obtém o nome do país em bytes e escreve seu tamanho seguido pelos próprios bytes
        byte[] nomeBytes = lista.get(0).getBytes("UTF-8");
        dataOut.writeShort(nomeBytes.length);
        dataOut.write(nomeBytes);
    
        // Obtém a abreviação do país em bytes e escreve seu tamanho seguido pelos próprios bytes
        byte[] abreBytes = lista.get(1).getBytes("UTF-8");
        dataOut.writeShort(abreBytes.length);
        dataOut.write(abreBytes);
    
        // Converte os dados para um array de bytes e escreve no arquivo binário
        byte[] objectBytes = baos.toByteArray();
        out.writeInt(objectBytes.length);
        out.write(objectBytes);
    }

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
   
    // Método para atualizar o arquivo binario
    public static void atualizarFilmeID(int IDDesejado, Filmes novoFilme, String binarioFile) {
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
                            
                            int diferenca = tamanhoObjeto - novoTamanho;
                            if (diferenca > 0) {
                                byte[] zeros = new byte[diferenca];
                                file.write(zeros); // Preenche o espaço restante com zeros
                            }
                        } else {
                            file.seek(posicaoInicial + 4);
                            filmes.setLAPIDE(true); // Marca o registro como excluído
                            
                            ByteArrayOutputStream lapideStream = new ByteArrayOutputStream();
                            try (ObjectOutputStream lapideOut = new ObjectOutputStream(lapideStream)) {
                                lapideOut.writeObject(filmes);
                            }
                            byte[] lapideBytes = lapideStream.toByteArray();
                            
                            if (lapideBytes.length <= tamanhoObjeto) {
                                file.write(lapideBytes);
                            } else {
                                file.writeBoolean(true); // Marca como excluído de maneira simplificada
                            }
                            
                            file.seek(file.length()); // Adiciona o novo registro no final do arquivo
                            file.writeInt(novoTamanho);
                            file.write(novoBytes);
                            
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

    // Função que cria uma Interface de usuario para arulizao o filme
    public static void atualizarUI(int IDDesejado, String binarioFile, String binarioPais, Filmes novoFilme, Scanner sc){

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
                    atualizarFilmeID(IDDesejado, novoFilme, binarioFile);
                    System.out.println("Atualizando dados...");
                    break;
                }
            
                case 1:{

                    // Atualiza o tipo do filme (ex: filme, série, documentário)
                    novoFilme.setTIPO(tipo(sc));

                    sc.nextLine();
                    //Chama a função que atuliza a duração, com o objetivo de não tem confito entre serie e filmes, pois a duração de filmes e salvo em minutos e a de series em temporadas
                    novoFilme = atualizarDuracao(novoFilme, sc);

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
                        pais = PesquisarPaisAbre(binarioPais, pais);
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
                        verificar = isDataValida(anoAdicao, format);
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
                        verificar = isAnoValido(anoLancamento);
                    }

                    Year ano = Year.parse(anoLancamento);
                    novoFilme.setANO_LAN(ano);
                    System.out.println("Ano de lancamento atualizado...");
                    
                    break;
                }
            
                case 7:{
                    // Atualiza a classificação indicativa do filme
                    novoFilme.setCLASSIFICACAO(classificacaoIndicativa(sc));
                    System.out.println("Classificacao atualizada para...");
                    break;
                }
            
                case 8:{
                    // Atualiza a duração do filme ou série
                    sc.nextLine();

                    novoFilme = atualizarDuracao(novoFilme, sc);

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

    // Método para verificar se uma data e valida
    public static boolean isDataValida(String dataStr, DateTimeFormatter formato) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            LocalDate data = LocalDate.parse(dataStr, formato);
            return true;
        } catch (DateTimeParseException e) {
            System.out.println("Data invalida, tente novamente...");
            return false;
        }
    }

    // Método para verificar se um ano é valido
    public static boolean isAnoValido(String anoStr) {
        if (anoStr == null) {
            return false;
        }
        
        try {
            Year ano = Year.parse(anoStr);
            
            // Verifica se o ano está em um intervalo aceitável
            return (ano.getValue() >= 1900 && ano.getValue() <= 2100);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de ano inválido, tente novamente...");
            return false;
        }
    }
    
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
    
    //Função para pesquisar o nome de um filme no arquivo binario
    public static void PesquisarNome(String binarioFile, String NomeDesejado){
        try (RandomAccessFile dis = new RandomAccessFile(binarioFile, "r")){
            int Ultimo = dis.readInt(); // Lê o último ID registrado
            int encontrado = 0; // Variavel para contar quantos filmes tem o mesmo nome

            while (dis.getFilePointer() < dis.length()) {
                int size = dis.readInt(); // Lê o tamanho do objeto
                byte[] FilmeBytes = new byte[size];
                dis.readFully(FilmeBytes); // Lê os dados do objeto
    
                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Filmes filme = (Filmes) ois.readObject(); // Converte os bytes para objeto Filmes
                    if (!filme.getLAPIDE() && filme.getNOME().equals(NomeDesejado)) {
                        filme.Ler(); // Exibe os dados do filme encontrado
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }

            System.err.println("Foram encontrados " + encontrado + "objetos com esse nome");

        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //Função para pesquisar o ID de um filme no arquivo binario
    public static Filmes PesquisarID(String binarioFile, int IDDesejado) {
        try (RandomAccessFile dis = new RandomAccessFile(binarioFile, "r")) {
            int Ultimo = dis.readInt(); // Lê o último ID registrado
            
            while (dis.getFilePointer() < dis.length()) {
                int size = dis.readInt(); // Lê o tamanho do objeto
                byte[] FilmeBytes = new byte[size];
                dis.readFully(FilmeBytes); // Lê os dados do objeto
    
                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
                    Filmes filme = (Filmes) ois.readObject(); // Converte os bytes para objeto Filmes
                    if (!filme.getLAPIDE() && filme.getID() == IDDesejado) {
                        return filme; // Retorna o filme encontrado
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }

            System.err.println("ID não encontrado");

        } catch (IOException e) {
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

    // Método principal para realizar a ordenação externa balanceada
    public static void ordenarExterna(String arquivoBinario, int numCaminhos, int registrosPorBloco) {
        try {
            // Lê o ID do último objeto do cabeçalho
            int ultimoID = lerUltimoID(arquivoBinario);
            
            // Contagem total de registros no arquivo (agora feita percorrendo o arquivo)
            int totalRegistros = contarRegistros(arquivoBinario);
            
            if (totalRegistros <= registrosPorBloco) {
                // Se o total de registros é menor que o tamanho do bloco, ordenar tudo em memória
                ordenarArquivoCompleto(arquivoBinario, ultimoID);
                return;
            }
            
            // Fase de distribuição - divide o arquivo em blocos ordenados
            List<String> arquivosTemporarios = distribuicao(arquivoBinario, numCaminhos, registrosPorBloco, ultimoID);
            
            // Fase de intercalação - une os blocos ordenados
            String arquivoFinal = intercalacao(arquivosTemporarios, numCaminhos, arquivoBinario + ".ordenado", ultimoID);
            
            Files.copy(Paths.get(arquivoFinal), Paths.get(arquivoBinario + ".ordenado"), StandardCopyOption.REPLACE_EXISTING);
            
            // Limpar arquivos temporários
            for (String arquivo : arquivosTemporarios) {
                Files.deleteIfExists(Paths.get(arquivo));
            }
            
            System.out.println("Ordenação externa concluída com sucesso!");
            
        } catch (IOException e) {
            System.err.println("Erro durante a ordenação externa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Lê o ID do último objeto do cabeçalho
    private static int lerUltimoID(String arquivoBinario) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivoBinario, "r")) {
            // O primeiro inteiro é o ID do último objeto
            return raf.readInt();
        }
    }

    // Conta registros percorrendo o arquivo inteiro 
    private static int contarRegistros(String arquivoBinario) throws IOException {
        int contador = 0;
        
        try (RandomAccessFile raf = new RandomAccessFile(arquivoBinario, "r")) {
            // Pular o cabeçalho (ID do último objeto)
            raf.readInt();
            
            // Ler cada registro e contar os válidos (sem lápide)
            while (raf.getFilePointer() < raf.length()) {
                int size = raf.readInt();
                byte[] filmeBytes = new byte[size];
                raf.readFully(filmeBytes);
                
                try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                    
                    Filmes filme = (Filmes) ois.readObject();
                    if (!filme.getLAPIDE()) {
                        contador++;
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }
        }
        
        return contador;
    }

    // Ordena todo o arquivo em memória (quando é pequeno o suficiente)
    private static void ordenarArquivoCompleto(String arquivoBinario, int ultimoID) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(arquivoBinario, "r")) {
            // Pular o ID do último objeto
            in.readInt();
            
            // Lista para armazenar todos os filmes
            List<Filmes> filmes = new ArrayList<>();
            
            // Ler todos os filmes
            while (in.getFilePointer() < in.length()) {
                int size = in.readInt();
                byte[] filmeBytes = new byte[size];
                in.readFully(filmeBytes);
                
                try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                    
                    Filmes filme = (Filmes) ois.readObject();
                    if (!filme.getLAPIDE()) {
                        filmes.add(filme);
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }
            
            // Ordenar os filmes em memória
            Collections.sort(filmes);
            
            // Escrever de volta para o arquivo
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoBinario + ".ordenado"))) {
                // Escrever o ID do último objeto (preservar o mesmo do arquivo original)
                out.writeInt(ultimoID);
                
                // Escrever cada filme ordenado
                for (Filmes filme : filmes) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                        oos.writeObject(filme);
                    }
                    
                    byte[] filmeBytes = baos.toByteArray();
                    
                    // Escrever o tamanho
                    out.writeInt(filmeBytes.length);
                    
                    // Escrever os bytes do objeto
                    out.write(filmeBytes);
                }
            }
            
            System.out.println("Arquivo ordenado em memória com sucesso!");
        }
    }

    // Fase de distribuição - divide o arquivo em blocos ordenados
    private static List<String> distribuicao(String arquivoBinario, int numCaminhos, int registrosPorBloco, int ultimoID) throws IOException {
        List<String> arquivosTemporarios = new ArrayList<>();
        
        try (RandomAccessFile raf = new RandomAccessFile(arquivoBinario, "r")) {
            // Pular o ID do último objeto
            raf.readInt();
            
            // Criar arquivos temporários para cada caminho
            for (int i = 0; i < numCaminhos; i++) {
                String nomeArquivo = arquivoBinario + ".temp" + i;
                arquivosTemporarios.add(nomeArquivo);
                
                // Inicializar arquivo apenas com o ID do último objeto
                try (DataOutputStream out = new DataOutputStream(new FileOutputStream(nomeArquivo))) {
                    out.writeInt(ultimoID); // Preservar o ID do último objeto
                }
            }
            
            // Distribuir registros entre os caminhos
            int blocoAtual = 0;
            
            while (raf.getFilePointer() < raf.length()) {
                // Determinar qual arquivo temporário usar
                int caminhoAtual = blocoAtual % numCaminhos;
                String arquivoTemp = arquivosTemporarios.get(caminhoAtual);
                
                // Ler um bloco de registros para memória
                List<Filmes> bloco = new ArrayList<>();
                int registrosNoBloco = 0;
                
                while (raf.getFilePointer() < raf.length() && registrosNoBloco < registrosPorBloco) {
                    int size = raf.readInt();
                    byte[] filmeBytes = new byte[size];
                    raf.readFully(filmeBytes);
                    
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                        ObjectInputStream ois = new ObjectInputStream(bais)) {
                        
                        Filmes filme = (Filmes) ois.readObject();
                        if (!filme.getLAPIDE()) {
                            bloco.add(filme);
                            registrosNoBloco++;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                    }
                }
                
                // Ordenar o bloco em memória
                Collections.sort(bloco);
                
                // Escrever o bloco ordenado no arquivo temporário
                escreverBlocoOrdenado(arquivoTemp, bloco);
                
                blocoAtual++;
            }
            
            return arquivosTemporarios;
        }
    }

    // Escreve um bloco ordenado em um arquivo temporário
    private static void escreverBlocoOrdenado(String arquivoTemp, List<Filmes> bloco) throws IOException {
        // Ler todo o conteúdo atual do arquivo temporário
        byte[] conteudoAtual;
        int ultimoID;
        
        try (RandomAccessFile raf = new RandomAccessFile(arquivoTemp, "r")) {
            ultimoID = raf.readInt(); // Ler o ID do último objeto
            if (raf.getFilePointer() < raf.length()) {
                conteudoAtual = new byte[(int)(raf.length() - 4)]; // 4 bytes para o ID
                raf.readFully(conteudoAtual);
            } else {
                conteudoAtual = new byte[0];
            }
        } catch (EOFException e) {
            // Arquivo vazio ou com apenas o cabeçalho
            System.err.println("Aviso: Arquivo temporário pode estar vazio ou incompleto");
            return;
        }
        
        // Escrever o conteúdo atualizado no arquivo temporário
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoTemp))) {
            // Escrever o ID do último objeto
            out.writeInt(ultimoID);
            
            // Escrever os dados existentes
            if (conteudoAtual.length > 0) {
                out.write(conteudoAtual);
            }
            
            // Escrever o novo bloco
            for (Filmes filme : bloco) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(filme);
                }
                
                byte[] filmeBytes = baos.toByteArray();
                
                // Escrever o tamanho
                out.writeInt(filmeBytes.length);
                
                // Escrever os bytes do objeto
                out.write(filmeBytes);
            }
        }
    }

    // Fase de intercalação - une os blocos ordenados
    private static String intercalacao(List<String> arquivosTemporarios, int numCaminhos, String arquivoFinal, int ultimoID) throws IOException {
        // Se só há um arquivo temporário, ele já está ordenado
        if (arquivosTemporarios.size() == 1) {
            return arquivosTemporarios.get(0);
        }
        
        // Criar novos arquivos para a intercalação
        List<String> novosArquivos = new ArrayList<>();
        for (int i = 0; i < Math.max(1, arquivosTemporarios.size() / numCaminhos); i++) {
            String novoArquivo = arquivoFinal + ".intercalacao" + i;
            novosArquivos.add(novoArquivo);
            
            // Inicializar apenas com o ID do último objeto
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(novoArquivo))) {
                out.writeInt(ultimoID); // ID do último objeto
            }
        }
        
        // Processar arquivos em grupos de tamanho numCaminhos
        for (int i = 0; i < arquivosTemporarios.size(); i += numCaminhos) {
            // Selecionar os arquivos para este grupo
            List<String> grupoArquivos = new ArrayList<>();
            for (int j = 0; j < numCaminhos && i + j < arquivosTemporarios.size(); j++) {
                grupoArquivos.add(arquivosTemporarios.get(i + j));
            }
            
            // Intercalar este grupo para um arquivo de saída
            String arquivoSaida = novosArquivos.get(i / numCaminhos);
            intercalarGrupo(grupoArquivos, arquivoSaida, ultimoID);
            
            // Limpar os arquivos que já foram intercalados
            for (String arquivo : grupoArquivos) {
                Files.deleteIfExists(Paths.get(arquivo));
            }
        }
        
        // Recursivamente continuar a intercalação até que reste apenas um arquivo
        if (novosArquivos.size() > 1) {
            return intercalacao(novosArquivos, numCaminhos, arquivoFinal, ultimoID);
        } else {
            return novosArquivos.get(0);
        }
    }

    // Intercala um grupo de arquivos em um único arquivo de saída
    private static void intercalarGrupo(List<String> arquivos, String arquivoSaida, int ultimoID) throws IOException {
        // Preparar os leitores para cada arquivo
        List<RandomAccessFile> leitores = new ArrayList<>();
        List<Filmes> proximosFilmes = new ArrayList<>();
        
        // Heap para manter o próximo filme de cada arquivo
        PriorityQueue<FilmeComOrigem> heap = new PriorityQueue<>();
        
        try {
            // Abrir cada arquivo e ler o primeiro registro
            for (int i = 0; i < arquivos.size(); i++) {
                RandomAccessFile raf = new RandomAccessFile(arquivos.get(i), "r");
                leitores.add(raf);
                
                // Pular o ID do último objeto
                raf.readInt();
                
                // Tenta ler o primeiro filme
                if (raf.getFilePointer() < raf.length()) {
                    int size = raf.readInt();
                    byte[] filmeBytes = new byte[size];
                    raf.readFully(filmeBytes);
                    
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                        ObjectInputStream ois = new ObjectInputStream(bais)) {
                        
                        Filmes filme = (Filmes) ois.readObject();
                        proximosFilmes.add(filme);
                        
                        // Adiciona um filme ao heap se não houver lápide
                        if (!filme.getLAPIDE()) {
                            heap.add(new FilmeComOrigem(filme, i));
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                        proximosFilmes.add(null); // Placeholder para manter índices sincronizados
                    }
                } else {
                    proximosFilmes.add(null); // Não há mais filmes neste arquivo
                }
            }
            
            // Ler o conteúdo atual do arquivo de saída
            byte[] conteudoAtual;
            try (RandomAccessFile raf = new RandomAccessFile(arquivoSaida, "r")) {
                raf.readInt(); // Pular o ID do último objeto
                if (raf.getFilePointer() < raf.length()) {
                    conteudoAtual = new byte[(int)(raf.length() - 4)]; // 4 bytes para o ID
                    raf.readFully(conteudoAtual);
                } else {
                    conteudoAtual = new byte[0];
                }
            } catch (EOFException e) {
                // Arquivo vazio ou só com cabeçalho
                conteudoAtual = new byte[0];
            }
            
            // Preparar o arquivo de saída
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoSaida))) {
                // Escrever o ID do último objeto
                out.writeInt(ultimoID);
                
                // Escrever os dados existentes
                if (conteudoAtual.length > 0) {
                    out.write(conteudoAtual);
                }
                
                // Intercalar os registros
                while (!heap.isEmpty()) {
                    // Obter o próximo filme
                    FilmeComOrigem atual = heap.poll();
                    Filmes filme = atual.getFilme();
                    int origem = atual.getOrigem();
                    
                    // Escrever o filme no arquivo de saída
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                        oos.writeObject(filme);
                    }
                    
                    byte[] filmeBytes = baos.toByteArray();
                    
                    // Escrever o tamanho
                    out.writeInt(filmeBytes.length);
                    
                    // Escrever os bytes do objeto
                    out.write(filmeBytes);
                    
                    // Ler o próximo filme do mesmo arquivo
                    RandomAccessFile raf = leitores.get(origem);
                    if (raf.getFilePointer() < raf.length()) {
                        int size = raf.readInt();
                        filmeBytes = new byte[size];
                        raf.readFully(filmeBytes);
                        
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                            ObjectInputStream ois = new ObjectInputStream(bais)) {
                            
                            filme = (Filmes) ois.readObject();
                            
                            // Adiciona um filme ao heap se não houver lápide
                            if (!filme.getLAPIDE()) {
                                heap.add(new FilmeComOrigem(filme, origem));
                            }
                        } catch (ClassNotFoundException e) {
                            System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                        }
                    }
                }
            }
        } finally {
            // Fechar todos os leitores
            for (RandomAccessFile raf : leitores) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar arquivo: " + e.getMessage());
                }
            }
        }
    }

    // Classe auxiliar para manter o filme junto com sua origem
    private static class FilmeComOrigem implements Comparable<FilmeComOrigem> {
        private final Filmes filme;
        private final int origem;
        
        public FilmeComOrigem(Filmes filme, int origem) {
            this.filme = filme;
            this.origem = origem;
        }
        
        public Filmes getFilme() {
            return filme;
        }
        
        public int getOrigem() {
            return origem;
        }
        
        @Override
        public int compareTo(FilmeComOrigem outro) {
            return this.filme.compareTo(outro.filme);
        }
    }

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

    public static void main(String[] args) {
        int opcao;
        String file = "netflix1.csv";
        String file2 = "WorldCountriesList.csv";
        String binarioFile = "binario.bin";
        String binarioPais = "binarioPais.bin";
        int index;
        ArvoreBMais<RegistroID> arvore;
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
            System.out.println("\t8: Escolhoer metado de Indexação");
            System.out.println("\t0: Sair");
            System.out.println("\t-------------------");

            opcao = sc.nextInt();

            switch(opcao){

                //Converte o arquivo CSV para binario
                case 1:{

                    //limpa buffer
                    sc.nextLine();

                    index = tiposDeIdexacao(sc);

                    if(index == 1){
                        try{
                            System.out.println("Qual o grau desejado da arvore");
                            int grau = sc.nextInt();
                            arvore = new ArvoreBMais<>(RegistroID.class.getConstructor(), grau, "arvoreFile.bin");
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
                        arvore = null;
                    }

                    IniciarArquivoCSV(file, binarioFile, binarioPais, file2, index, sc, arvore);

                    int n1 = 1;
                    
                    while(n1 < 8791){
                        try{
                            //Lê o arquivo binario
                            ArrayList<RegistroID> lista = arvore.read(new RegistroID(n1, -1)); 
                            
                            System.out.print("Resposta: ");
                            for (int i = 0; i < lista.size(); i++)
                            System.out.print(lista.get(i) + "\n");

                            n1++;

                        } catch (Exception e) {
                            System.out.println("Erro ao ler o arquivo binário: " + e.getMessage());
                        }  
                    }

                    
                    
                    break;

                }

                //Le o arquivo binario
                case 2:{

                    lerBinario(binarioFile);
                    break;

                }

                //Pesquisa um filme/serie pelo ID ou nome
                case 3:{

                    Filmes filmePesquisa = new Filmes();
                    int opcaoPesquisar; 

                    do{

                        //UI para mostrar as opções de busca 
                        System.out.println("\t-----------------------------------------");
                        System.out.println("\t1: Pesquisar pelo ID");
                        System.out.println("\t2: Pesquisar pelo nome");
                        System.out.println("\t0: Sair");
                        System.out.println("\t-----------------------------------------");

                        opcaoPesquisar = sc.nextInt();

                        switch(opcaoPesquisar){

                            //Chama a função de busca pelo ID
                            case 1:{

                                System.out.println("\tDigite o ID do filme/serie:");

                                int IDDesejado = sc.nextInt();
                                filmePesquisa = PesquisarID(binarioFile, IDDesejado);

                                if(filmePesquisa != null){
                                    filmePesquisa.Ler();
                                }
                                
                                break;

                            }

                            //Chama a função de busca pelo ID
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

                //Usandao para atulizar um objeto
                case 4:{

                    System.out.println("Digite o ID do Filme desejado");
                    int IDDesejado = sc.nextInt();

                    //Pesquisa o objeto com base no ID 
                    Filmes novoFilme = PesquisarID(binarioFile, IDDesejado);

                    //Se o objeto existir
                    if(novoFilme != null){
                        
                        atualizarUI(IDDesejado, binarioFile, binarioPais, novoFilme, sc);

                        //Mostra um previw do objeto atualizado
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
                    Filmes novoFilme = PesquisarID(binarioFile, IDDesejado);
                
                    if(novoFilme != null){
                        novoFilme.setLAPIDE(true); // Marca o filme como removido
                        atualizarFilmeID(IDDesejado, novoFilme, binarioFile);
                        System.out.println("Filme removido com sucesso!");
                    }
                    break;
                }
                
                case 6:{
                    // Adiciona um novo filme ao arquivo binário
                    try(RandomAccessFile in = new RandomAccessFile(binarioFile, "rw")){
                        //Encontra qual sera o ID do proximo Filme;
                        int ID = (encontrarTamanho(binarioFile)) + 1;
                        int opcaoAdicinar;
                        List<String> lista = new ArrayList<>();
                
                        // Adiciona um ID temporario para a lista, devido a forma como a leitura de objeto e feita
                        lista.add("1");

                        // Adiciona o tipo do objeto (filme ou serie)
                        lista.add(tipo(sc));
                
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
                            pais = PesquisarPaisAbre(binarioPais, pais);
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
                            verificar = isDataValida(anoAdicao, format);
                        }
                        lista.add(anoAdicao);
                
                        // Adiciona a o ano de lançamento do objeto
                        verificar = false;
                        String anoLancamento = null;
                        //Verifica se o ano e valido
                        while (!verificar){
                            System.out.println("\tDigite o ano de lancamento ex: 2021:");
                            anoLancamento = sc.nextLine();
                            verificar = isAnoValido(anoLancamento);
                        }
                        lista.add(anoLancamento);
                
                        // Adiciona a classificação indicativa do objeto
                        lista.add(classificacaoIndicativa(sc));
                
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
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(novoFilme);
                        }
                        byte[] bytes = baos.toByteArray();
                        in.writeInt(bytes.length);
                        in.write(bytes);
                
                        // Permite edição antes de confirmar a adição
                        atualizarUI(ID, binarioFile, binarioPais, novoFilme, sc);

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

                        ordenarExterna(binarioFile, numCaminhos, Blocos);

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

interface RegistroArvoreBMais<T> {

    public short size(); // tamanho FIXO do registro
  
    public byte[] toByteArray() throws IOException; // representação do elemento em um vetor de bytes
  
    public void fromByteArray(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento
  
    public int compareTo(T obj); // compara dois elementos
  
    public T clone(); // clonagem de objetos
  
}

class ArvoreBMais<T extends RegistroArvoreBMais<T>> {

    private int ordem; // Número máximo de filhos que uma página pode conter
    private int maxElementos; // Variável igual a ordem - 1 para facilitar a clareza do código
    private int maxFilhos; // Variável igual a ordem para facilitar a clareza do código
    private RandomAccessFile arquivo; // Arquivo em que a árvore será armazenada
    private String nomeArquivo;
    private Constructor<T> construtor;

    // Variáveis usadas nas funções recursivas (já que não é possível passar valores
    // por referência)
    private T elemAux;
    private long paginaAux;
    private boolean cresceu;
    private boolean diminuiu;

    // Esta classe representa uma página da árvore (folha ou não folha).
    private class Pagina {

        protected int ordem; // Número máximo de filhos que uma página pode ter
        protected Constructor<T> construtor;
        protected int maxElementos; // Variável igual a ordem - 1 para facilitar a clareza do código
        protected int maxFilhos; // Variável igual a ordem para facilitar a clareza do código
        protected int TAMANHO_ELEMENTO; // Os elementos são de tamanho fixo
        protected int TAMANHO_PAGINA; // A página será de tamanho fixo, calculado a partir da ordem

        protected ArrayList<T> elementos; // Elementos da página
        protected ArrayList<Long> filhos; // Vetor de ponteiros para os filhos
        protected long proxima; // Próxima folha, quando a página for uma folha

        // Construtor da página
        public Pagina(Constructor<T> ct, int o) throws Exception {

            // Inicialização dos atributos
            this.construtor = ct;
            this.ordem = o;
            this.maxFilhos = this.ordem;
            this.maxElementos = this.ordem - 1;
            this.elementos = new ArrayList<>(this.maxElementos);
            this.filhos = new ArrayList<>(this.maxFilhos);
            this.proxima = -1;

            // Cálculo do tamanho (fixo) da página
            // cada elemento -> depende do objeto
            // cada ponteiro de filho -> 8 bytes
            // último filho -> 8 bytes
            // ponteiro próximo -> 8 bytes
            this.TAMANHO_ELEMENTO = this.construtor.newInstance().size();
            this.TAMANHO_PAGINA = 4 + this.maxElementos * this.TAMANHO_ELEMENTO + this.maxFilhos * 8 + 8;
        }

        // Retorna o vetor de bytes que representa a página para armazenamento em
        // arquivo
        protected byte[] toByteArray() throws IOException {

            // Um fluxo de bytes é usado para construção do vetor de bytes
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);

            // Quantidade de elementos presentes na página
            out.writeInt(this.elementos.size());

            // Escreve todos os elementos
            int i = 0;
            while (i < this.elementos.size()) {
                out.writeLong(this.filhos.get(i).longValue());
                out.write(this.elementos.get(i).toByteArray());
                i++;
            }
            if (this.filhos.size() > 0)
                out.writeLong(this.filhos.get(i).longValue());
            else
                out.writeLong(-1L);

            // Completa o restante da página com registros vazios
            byte[] registroVazio = new byte[TAMANHO_ELEMENTO];
            while (i < this.maxElementos) {
                out.write(registroVazio);
                out.writeLong(-1L);
                i++;
            }

            // Escreve o ponteiro para a próxima página
            out.writeLong(this.proxima);

            // Retorna o vetor de bytes que representa a página
            return ba.toByteArray();
        }

        // Reconstrói uma página a partir de um vetor de bytes lido no arquivo
        public void fromByteArray(byte[] buffer) throws Exception {

            // Usa um fluxo de bytes para leitura dos atributos
            ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
            DataInputStream in = new DataInputStream(ba);

            // Lê a quantidade de elementos da página
            int n = in.readInt();

            // Lê todos os elementos (reais ou vazios)
            int i = 0;
            this.elementos = new ArrayList<>(this.maxElementos);
            this.filhos = new ArrayList<>(this.maxFilhos);
            T elem;
            while (i < n) {
                this.filhos.add(in.readLong());
                byte[] registro = new byte[TAMANHO_ELEMENTO];
                in.read(registro);
                elem = this.construtor.newInstance();
                elem.fromByteArray(registro);
                this.elementos.add(elem);
                i++;
            }
            this.filhos.add(in.readLong());
            in.skipBytes((this.maxElementos - i) * (TAMANHO_ELEMENTO + 8));
            this.proxima = in.readLong();
        }
    }

    // ------------------------------------------------------------------------------

    public ArvoreBMais(Constructor<T> c, int o, String na) throws Exception {

        // Inicializa os atributos da árvore
        construtor = c;
        ordem = o;
        maxElementos = o - 1;
        maxFilhos = o;
        nomeArquivo = na;

        // Abre (ou cria) o arquivo, escrevendo uma raiz empty, se necessário.
        arquivo = new RandomAccessFile(nomeArquivo, "rw");
        if (arquivo.length() < 16) {
            arquivo.writeLong(-1); // raiz empty
            arquivo.writeLong(-1); // pointeiro lista excluídos
        }
    }

    // Testa se a árvore está empty. Uma árvore empty é identificada pela raiz == -1
    public boolean empty() throws IOException {
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        return raiz == -1;
    }

    // Busca recursiva por um elemento a partir da chave. Este metodo invoca
    // o método recursivo read1, passando a raiz como referência.
    // O método retorna a lista de elementos que possuem a chave (considerando
    // a possibilidade chaves repetidas)
    public ArrayList<T> read(T elem) throws Exception {

        // Recupera a raiz da árvore
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();

        // Executa a busca recursiva
        if (raiz != -1)
            return read1(elem, raiz);
        else {
            ArrayList<T> resposta = new ArrayList<>();
            return resposta;
        }
    }

    // Busca recursiva. Este método recebe a referência de uma página e busca
    // pela chave na mesma. A busca continua pelos filhos, se houverem.
    private ArrayList<T> read1(T elem, long pagina) throws Exception {

        // Como a busca é recursiva, a descida para um filho inexistente
        // (filho de uma página folha) retorna um vetor vazio.
        if (pagina == -1) {
            ArrayList<T> resposta = new ArrayList<>();
            return resposta;
        }

        // Reconstrói a página passada como referência a partir
        // do registro lido no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.fromByteArray(buffer);

        // Encontra o ponto em que a chave deve estar na página
        // Nesse primeiro passo, todas as chaves menores que a chave buscada
        // são ultrapassadas
        int i = 0;
        while (elem!=null && i < pa.elementos.size() && elem.compareTo(pa.elementos.get(i)) > 0) {
            i++;
        }

        // Chave encontrada (ou pelo menos o ponto onde ela deveria estar).
        // Segundo passo - testa se a chave é a chave buscada e se está em uma folha
        // Obs.: em uma árvore B+, todas as chaves válidas estão nas folhas
        if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && (elem==null || elem.compareTo(pa.elementos.get(i)) == 0)) {

            // Cria a lista de retorno e insere os elementos encontrados
            ArrayList<T> lista = new ArrayList<>();
            while (elem==null || elem.compareTo(pa.elementos.get(i)) <= 0) {

                if (elem==null || elem.compareTo(pa.elementos.get(i)) == 0)
                    lista.add(pa.elementos.get(i));
                i++;

                // Se chegar ao fim da folha, então avança para a folha seguinte
                if (i == pa.elementos.size()) {
                    if (pa.proxima == -1)
                        break;
                    arquivo.seek(pa.proxima);
                    arquivo.read(buffer);
                    pa.fromByteArray(buffer);
                    i = 0;
                }
            }
            return lista;
        }

        // Terceiro passo - se a chave não tiver sido encontrada nesta folha,
        // testa se ela está na próxima folha. Isso pode ocorrer devido ao
        // processo de ordenação.
        else if (i == pa.elementos.size() && pa.filhos.get(0) == -1) {

            // Testa se há uma próxima folha. Nesse caso, retorna um vetor vazio
            if (pa.proxima == -1) {
                ArrayList<T> resposta = new ArrayList<>();
                return resposta;
            }

            // Lê a próxima folha
            arquivo.seek(pa.proxima);
            arquivo.read(buffer);
            pa.fromByteArray(buffer);

            // Testa se a chave é a primeira da próxima folha
            i = 0;
            if (elem.compareTo(pa.elementos.get(i)) <= 0) {

                // Cria a lista de retorno
                ArrayList<T> lista = new ArrayList<>();

                // Testa se a chave foi encontrada, e adiciona todas as chaves
                // secundárias
                while (elem.compareTo(pa.elementos.get(i)) <= 0) {
                    if (elem.compareTo(pa.elementos.get(i)) == 0)
                        lista.add(pa.elementos.get(i));
                    i++;
                    if (i == pa.elementos.size()) {
                        if (pa.proxima == -1)
                            break;
                        arquivo.seek(pa.proxima);
                        arquivo.read(buffer);
                        pa.fromByteArray(buffer);
                        i = 0;
                    }
                }

                return lista;
            }

            // Se não houver uma próxima página, retorna um vetor vazio
            else {
                ArrayList<T> resposta = new ArrayList<>();
                return resposta;
            }
        }

        // Chave ainda não foi encontrada, continua a busca recursiva pela árvore
        if (elem==null || i == pa.elementos.size() || elem.compareTo(pa.elementos.get(i)) <= 0)
            return read1(elem, pa.filhos.get(i));
        else
            return read1(elem, pa.filhos.get(i + 1));
    }

    // Inclusão de novos elementos na árvore. A inclusão é recursiva. A primeira
    // função chama a segunda recursivamente, passando a raiz como referência.
    // Eventualmente, a árvore pode crescer para cima.
    public boolean create(T elem) throws Exception {

        // Carrega a raiz
        arquivo.seek(0);
        long pagina;
        pagina = arquivo.readLong();

        // O processo de inclusão permite que os valores passados como referência
        // sejam substituídos por outros valores, para permitir a divisão de páginas
        // e crescimento da árvore. Assim, são usados os valores globais elemAux
        // e chave2Aux. Quando há uma divisão, as chaves promovidas são armazenadas
        // nessas variáveis.
        elemAux = elem.clone();

        // Se houver crescimento, então será criada uma página extra e será mantido um
        // ponteiro para essa página. Os valores também são globais.
        paginaAux = -1;
        cresceu = false;

        // Chamada recursiva para a inserção do par de chaves
        boolean inserido = create1(pagina);

        // Testa a necessidade de criação de uma nova raiz.
        if (cresceu) {

            // Cria a nova página que será a raiz. O ponteiro esquerdo da raiz
            // será a raiz antiga e o seu ponteiro direito será para a nova página.
            Pagina novaPagina = new Pagina(construtor, ordem);
            novaPagina.elementos = new ArrayList<>(this.maxElementos);
            novaPagina.elementos.add(elemAux);
            novaPagina.filhos = new ArrayList<>(this.maxFilhos);
            novaPagina.filhos.add(pagina);
            novaPagina.filhos.add(paginaAux);

            // Acha o espaço em disco. Testa se há páginas excluídas.
            arquivo.seek(8);
            long end = arquivo.readLong();
            if(end==-1) {
                end = arquivo.length();
            } else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
                arquivo.seek(end);
                Pagina pa_excluida = new Pagina(construtor, ordem);
                byte[] buffer = new byte[pa_excluida.TAMANHO_PAGINA];
                arquivo.read(buffer);
                pa_excluida.fromByteArray(buffer);
                arquivo.seek(8);
                arquivo.writeLong(pa_excluida.proxima);
            }
            arquivo.seek(end);
            long raiz = arquivo.getFilePointer();
            arquivo.write(novaPagina.toByteArray());
            arquivo.seek(0);
            arquivo.writeLong(raiz);
            inserido = true;
        }

        return inserido;
    }

    // Função recursiva de inclusão. A função passa uma página de referência.
    // As inclusões são sempre feitas em uma folha.
    private boolean create1(long pagina) throws Exception {

        // Testa se passou para o filho de uma página folha. Nesse caso,
        // inicializa as variáveis globais de controle.
        if (pagina == -1) {
            cresceu = true;
            paginaAux = -1;
            return false;
        }

        // Lê a página passada como referência
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.fromByteArray(buffer);

        // Busca o próximo ponteiro de descida. Como pode haver repetição
        // da primeira chave, a segunda também é usada como referência.
        // Nesse primeiro passo, todos os pares menores são ultrapassados.
        int i = 0;
        while (i < pa.elementos.size() && (elemAux.compareTo(pa.elementos.get(i)) > 0)) {
            i++;
        }

        // Testa se o registro já existe em uma folha. Se isso acontecer, então
        // a inclusão é cancelada.
        if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && elemAux.compareTo(pa.elementos.get(i)) == 0) {
            cresceu = false;
            return false;
        }

        // Continua a busca recursiva por uma nova página. A busca continuará até o
        // filho inexistente de uma página folha ser alcançado.
        boolean inserido;
        if (i == pa.elementos.size() || elemAux.compareTo(pa.elementos.get(i)) < 0)
            inserido = create1(pa.filhos.get(i));
        else
            inserido = create1(pa.filhos.get(i + 1));

        // A partir deste ponto, as chamadas recursivas já foram encerradas.
        // Assim, o próximo código só é executado ao retornar das chamadas recursivas.

        // A inclusão já foi resolvida por meio de uma das chamadas recursivas. Nesse
        // caso, apenas retorna para encerrar a recursão.
        // A inclusão pode ter sido resolvida porque o par de chaves já existia
        // (inclusão inválida)
        // ou porque o novo elemento coube em uma página existente.
        if (!cresceu)
            return inserido;

        // Se tiver espaço na página, faz a inclusão nela mesmo
        if (pa.elementos.size() < maxElementos) {

            // Puxa todos elementos para a direita, começando do último
            // para gerar o espaço para o novo elemento e insere o novo elemento
            pa.elementos.add(i, elemAux);
            pa.filhos.add(i + 1, paginaAux);

            // Escreve a página atualizada no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.toByteArray());

            // Encerra o processo de crescimento e retorna
            cresceu = false;
            return true;
        }

        // O elemento não cabe na página. A página deve ser dividida e o elemento
        // do meio deve ser promovido (sem retirar a referência da folha).

        // Cria uma nova página
        Pagina np = new Pagina(construtor, ordem);

        // Move a metade superior dos elementos para a nova página,
        // considerando que maxElementos pode ser ímpar
        int meio = maxElementos / 2;
        np.filhos.add(pa.filhos.get(meio)); // COPIA o primeiro ponteiro
        for (int j = 0; j < (maxElementos - meio); j++) {
            np.elementos.add(pa.elementos.remove(meio)); // MOVE os elementos
            np.filhos.add(pa.filhos.remove(meio + 1)); // MOVE os demais ponteiros
        }

        // Testa o lado de inserção
        // Caso 1 - Novo registro deve ficar na página da esquerda
        if (i <= meio) {
            pa.elementos.add(i, elemAux);
            pa.filhos.add(i + 1, paginaAux);

            // Se a página for folha, seleciona o primeiro elemento da página
            // da direita para ser promovido, mantendo-o na folha
            if (pa.filhos.get(0) == -1)
                elemAux = np.elementos.get(0).clone();

            // caso contrário, promove o maior elemento da página esquerda
            // removendo-o da página
            else {
                elemAux = pa.elementos.remove(pa.elementos.size() - 1);
                pa.filhos.remove(pa.filhos.size() - 1);
            }
        }

        // Caso 2 - Novo registro deve ficar na página da direita
        else {

            int j = maxElementos - meio;
            while (elemAux.compareTo(np.elementos.get(j - 1)) < 0)
                j--;
            np.elementos.add(j, elemAux);
            np.filhos.add(j + 1, paginaAux);

            // Seleciona o primeiro elemento da página da direita para ser promovido
            elemAux = np.elementos.get(0).clone();

            // Se não for folha, remove o elemento promovido da página
            if (pa.filhos.get(0) != -1) {
                np.elementos.remove(0);
                np.filhos.remove(0);
            }

        }

        // Obtém um endereço para a nova página (página excluída ou fim do arquivo)
        arquivo.seek(8);
        long end = arquivo.readLong();
        if(end==-1) {
            end = arquivo.length();
        } else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
            arquivo.seek(end);
            Pagina pa_excluida = new Pagina(construtor, ordem);
            buffer = new byte[pa_excluida.TAMANHO_PAGINA];
            arquivo.read(buffer);
            pa_excluida.fromByteArray(buffer);
            arquivo.seek(8);
            arquivo.writeLong(pa_excluida.proxima);
        }

        // Se a página era uma folha e apontava para outra folha,
        // então atualiza os ponteiros dessa página e da página nova
        if (pa.filhos.get(0) == -1) {
            np.proxima = pa.proxima;
            pa.proxima = end;
        }

        // Grava as páginas no arquivo
        paginaAux = end;
        arquivo.seek(paginaAux);
        arquivo.write(np.toByteArray());

        arquivo.seek(pagina);
        arquivo.write(pa.toByteArray());

        return true;
    }

    // Remoção elementos na árvore. A remoção é recursiva. A primeira
    // função chama a segunda recursivamente, passando a raiz como referência.
    // Eventualmente, a árvore pode reduzir seu tamanho, por meio da exclusão da
    // raiz.
    public boolean delete(T elem) throws Exception {

        // Encontra a raiz da árvore
        arquivo.seek(0);
        long pagina;
        pagina = arquivo.readLong();

        // variável global de controle da redução do tamanho da árvore
        diminuiu = false;

        // Chama recursivamente a exclusão de registro (na elemAux e no
        // chave2Aux) passando uma página como referência
        boolean excluido = delete1(elem, pagina);

        // Se a exclusão tiver sido possível e a página tiver reduzido seu tamanho,
        // por meio da fusão das duas páginas filhas da raiz, elimina essa raiz
        if (excluido && diminuiu) {

            // Lê a raiz
            arquivo.seek(pagina);
            Pagina pa = new Pagina(construtor, ordem);
            byte[] buffer = new byte[pa.TAMANHO_PAGINA];
            arquivo.read(buffer);
            pa.fromByteArray(buffer);

            // Se a página tiver 0 elementos, apenas atualiza o ponteiro para a raiz,
            // no cabeçalho do arquivo, para o seu primeiro filho e insere a raiz velha
            // na lista de páginas excluídas
            if (pa.elementos.size() == 0) {
                arquivo.seek(0);
                arquivo.writeLong(pa.filhos.get(0));

                arquivo.seek(8);
                long end = arquivo.readLong();  // cabeça da lista de páginas excluídas
                pa.proxima = end;
                arquivo.seek(8);
                arquivo.writeLong(pagina);
                arquivo.seek(pagina);
                arquivo.write(pa.toByteArray());
            }
        }

        return excluido;
    }

    // Função recursiva de exclusão. A função passa uma página de referência.
    // As exclusões são sempre feitas em folhas e a fusão é propagada para cima.
    private boolean delete1(T elem, long pagina) throws Exception {

        // Declaração de variáveis
        boolean excluido = false;
        int diminuido;

        // Testa se o registro não foi encontrado na árvore, ao alcançar uma folha
        // inexistente (filho de uma folha real)
        if (pagina == -1) {
            diminuiu = false;
            return false;
        }

        // Lê o registro da página no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.fromByteArray(buffer);

        // Encontra a página em que o par de chaves está presente
        // Nesse primeiro passo, salta todas os pares de chaves menores
        int i = 0;
        while (i < pa.elementos.size() && elem.compareTo(pa.elementos.get(i)) > 0) {
            i++;
        }

        // Chaves encontradas em uma folha
        if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && elem.compareTo(pa.elementos.get(i)) == 0) {

            // Puxa todas os elementos seguintes para uma posição anterior, sobrescrevendo
            // o elemento a ser excluído
            pa.elementos.remove(i);
            pa.filhos.remove(i + 1);

            // Atualiza o registro da página no arquivo
            arquivo.seek(pagina);
            arquivo.write(pa.toByteArray());

            // Se a página contiver menos elementos do que o mínimo necessário,
            // indica a necessidade de fusão de páginas
            diminuiu = pa.elementos.size() < maxElementos / 2;
            return true;
        }

        // Se a chave não tiver sido encontrada (observar o return true logo acima),
        // continua a busca recursiva por uma nova página. A busca continuará até o
        // filho inexistente de uma página folha ser alcançado.
        // A variável diminuído mantem um registro de qual página eventualmente
        // pode ter ficado com menos elementos do que o mínimo necessário.
        // Essa página será filha da página atual
        if (i == pa.elementos.size() || elem.compareTo(pa.elementos.get(i)) < 0) {
            excluido = delete1(elem, pa.filhos.get(i));
            diminuido = i;
        } else {
            excluido = delete1(elem, pa.filhos.get(i + 1));
            diminuido = i + 1;
        }

        // A partir deste ponto, o código é executado após o retorno das chamadas
        // recursivas do método

        // Testa se há necessidade de fusão de páginas
        if (diminuiu) {

            // Carrega a página filho que ficou com menos elementos do
            // do que o mínimo necessário
            long paginaFilho = pa.filhos.get(diminuido);
            Pagina pFilho = new Pagina(construtor, ordem);
            arquivo.seek(paginaFilho);
            arquivo.read(buffer);
            pFilho.fromByteArray(buffer);

            // Cria uma página para o irmão (da direita ou esquerda)
            long paginaIrmaoEsq = -1, paginaIrmaoDir = -1;
            Pagina pIrmaoEsq = null, pIrmaoDir = null; // inicializados com null para controle de existência

            // Carrega os irmãos (que existirem)
            if (diminuido > 0) { // possui um irmão esquerdo, pois não é a primeira filho do pai
                paginaIrmaoEsq = pa.filhos.get(diminuido - 1);
                pIrmaoEsq = new Pagina(construtor, ordem);
                arquivo.seek(paginaIrmaoEsq);
                arquivo.read(buffer);
                pIrmaoEsq.fromByteArray(buffer);
            }
            if (diminuido < pa.elementos.size()) { // possui um irmão direito, pois não é o último filho do pai
                paginaIrmaoDir = pa.filhos.get(diminuido + 1);
                pIrmaoDir = new Pagina(construtor, ordem);
                arquivo.seek(paginaIrmaoDir);
                arquivo.read(buffer);
                pIrmaoDir.fromByteArray(buffer);
            }

            // Verifica se o irmão esquerdo existe e pode ceder algum elemento
            if (pIrmaoEsq != null && pIrmaoEsq.elementos.size() > maxElementos / 2) {

                // Se for folha, copia o elemento do irmão, já que o do pai será extinto ou
                // repetido
                if (pFilho.filhos.get(0) == -1)
                    pFilho.elementos.add(0, pIrmaoEsq.elementos.remove(pIrmaoEsq.elementos.size() - 1));

                // Se não for folha, desce o elemento do pai
                else
                    pFilho.elementos.add(0, pa.elementos.get(diminuido - 1));

                // Copia o elemento vindo do irmão para o pai (página atual)
                pa.elementos.set(diminuido - 1, pFilho.elementos.get(0));

                // Reduz o elemento no irmão
                pFilho.filhos.add(0, pIrmaoEsq.filhos.remove(pIrmaoEsq.filhos.size() - 1));

            }

            // Senão, verifica se o irmão direito existe e pode ceder algum elemento
            else if (pIrmaoDir != null && pIrmaoDir.elementos.size() > maxElementos / 2) {
                // Se for folha
                if (pFilho.filhos.get(0) == -1) {

                    // move o elemento do irmão
                    pFilho.elementos.add(pIrmaoDir.elementos.remove(0));
                    pFilho.filhos.add(pIrmaoDir.filhos.remove(0));

                    // sobe o próximo elemento do irmão
                    pa.elementos.set(diminuido, pIrmaoDir.elementos.get(0));
                }

                // Se não for folha, rotaciona os elementos
                else {
                    // Copia o elemento do pai, com o ponteiro esquerdo do irmão
                    pFilho.elementos.add(pa.elementos.get(diminuido));
                    pFilho.filhos.add(pIrmaoDir.filhos.remove(0));

                    // Sobe o elemento esquerdo do irmão para o pai
                    pa.elementos.set(diminuido, pIrmaoDir.elementos.remove(0));
                }
            }

            // Senão, faz a fusão com o irmão esquerdo, se ele existir
            else if (pIrmaoEsq != null) {
                // Se a página reduzida não for folha, então o elemento
                // do pai deve descer para o irmão
                if (pFilho.filhos.get(0) != -1) {
                    pIrmaoEsq.elementos.add(pa.elementos.remove(diminuido - 1));
                    pIrmaoEsq.filhos.add(pFilho.filhos.remove(0));
                }
                // Senão, apenas remove o elemento do pai
                else {
                    pa.elementos.remove(diminuido - 1);
                    pFilho.filhos.remove(0);
                }
                pa.filhos.remove(diminuido); // remove o ponteiro para a própria página

                // Copia todos os registros para o irmão da esquerda
                pIrmaoEsq.elementos.addAll(pFilho.elementos);
                pIrmaoEsq.filhos.addAll(pFilho.filhos);
                pFilho.elementos.clear(); 
                pFilho.filhos.clear();

                // Se as páginas forem folhas, copia o ponteiro para a folha seguinte
                if (pIrmaoEsq.filhos.get(0) == -1)
                    pIrmaoEsq.proxima = pFilho.proxima;

                // Insere o filho na lista de páginas excluídas
                arquivo.seek(8);
                pFilho.proxima = arquivo.readLong();
                arquivo.seek(8);
                arquivo.writeLong(paginaFilho);

            }

            // Senão, faz a fusão com o irmão direito, assumindo que ele existe
            else {
                // Se a página reduzida não for folha, então o elemento
                // do pai deve descer para o irmão
                if (pFilho.filhos.get(0) != -1) {
                    pFilho.elementos.add(pa.elementos.remove(diminuido));
                    pFilho.filhos.add(pIrmaoDir.filhos.remove(0));
                }
                // Senão, apenas remove o elemento do pai
                else {
                    pa.elementos.remove(diminuido);
                    pFilho.filhos.remove(0);
                }
                pa.filhos.remove(diminuido + 1); // remove o ponteiro para o irmão direito

                // Move todos os registros do irmão da direita
                pFilho.elementos.addAll(pIrmaoDir.elementos);
                pFilho.filhos.addAll(pIrmaoDir.filhos);
                pIrmaoDir.elementos.clear(); 
                pIrmaoDir.filhos.clear();

                // Se a página for folha, copia o ponteiro para a próxima página
                pFilho.proxima = pIrmaoDir.proxima;

                // Insere o irmão da direita na lista de páginas excluídas
                arquivo.seek(8);
                pIrmaoDir.proxima = arquivo.readLong();
                arquivo.seek(8);
                arquivo.writeLong(paginaIrmaoDir);

            }

            // testa se o pai também ficou sem o número mínimo de elementos
            diminuiu = pa.elementos.size() < maxElementos / 2;

            // Atualiza os demais registros
            arquivo.seek(pagina);
            arquivo.write(pa.toByteArray());
            arquivo.seek(paginaFilho);
            arquivo.write(pFilho.toByteArray());
            if (pIrmaoEsq != null) {
                arquivo.seek(paginaIrmaoEsq);
                arquivo.write(pIrmaoEsq.toByteArray());
            }
            if (pIrmaoDir != null) {
                arquivo.seek(paginaIrmaoDir);
                arquivo.write(pIrmaoDir.toByteArray());
            }
        }
        return excluido;
    }

    // Imprime a árvore, usando uma chamada recursiva.
    // A função recursiva é chamada com uma página de referência (raiz)
    public void print() throws Exception {
        long raiz;
        arquivo.seek(0);
        raiz = arquivo.readLong();
        System.out.println("Raiz: " + String.format("%04d", raiz));
        if (raiz != -1)
            print1(raiz);
        System.out.println();
    }

    // Impressão recursiva
    private void print1(long pagina) throws Exception {

        // Retorna das chamadas recursivas
        if (pagina == -1)
            return;
        int i;

        // Lê o registro da página passada como referência no arquivo
        arquivo.seek(pagina);
        Pagina pa = new Pagina(construtor, ordem);
        byte[] buffer = new byte[pa.TAMANHO_PAGINA];
        arquivo.read(buffer);
        pa.fromByteArray(buffer);

        // Imprime a página
        String endereco = String.format("%04d", pagina);
        System.out.print(endereco + "  " + pa.elementos.size() + ":"); // endereço e número de elementos
        for (i = 0; i < pa.elementos.size(); i++) {
            System.out.print("(" + String.format("%04d", pa.filhos.get(i)) + ") " + pa.elementos.get(i) + " ");
        }
        if (i > 0)
            System.out.print("(" + String.format("%04d", pa.filhos.get(i)) + ")");
        else
            System.out.print("(-001)");
        for (; i < maxElementos; i++) {
            System.out.print(" ------- (-001)");
        }
        if (pa.proxima == -1)
            System.out.println();
        else
            System.out.println(" --> (" + String.format("%04d", pa.proxima) + ")");

        // Chama recursivamente cada filho, se a página não for folha
        if (pa.filhos.get(0) != -1) {
            for (i = 0; i < pa.elementos.size(); i++)
                print1(pa.filhos.get(i));
            print1(pa.filhos.get(i));
        }
    }

}

class RegistroID implements RegistroArvoreBMais<RegistroID> {
    private int id; // ID do objeto
    private long offset; // Posição no arquivo de dados
    private static final short TAMANHO = 12; // 4 bytes para ID + 8 bytes para offset

    public RegistroID() {
        this(-1, -1);
    }

    public RegistroID(int id, long offset) {
        this.id = id;
        this.offset = offset;
    }

    public int getId() {
        return id;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public short size() {
        return TAMANHO;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeLong(offset);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        this.id = dis.readInt();
        this.offset = dis.readLong();
    }

    @Override
    public int compareTo(RegistroID obj) {
        return Integer.compare(this.id, obj.id);
    }

    @Override
    public RegistroID clone() {
        return new RegistroID(this.id, this.offset);
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Offset: " + offset;
    }
}

class Filmes implements Externalizable, Comparable<Filmes>{

    private boolean lapide;
    private int id;
    private String tipo;
    private String nome;
    private String diretor;
    private String pais;
    private LocalDate ano_adi;
    private Year ano_lan;
    private String classificacao;
    private String duracao;
    private String Genero;

    public Filmes(){}

    public Filmes(List<String> lista, int tmp, Boolean formasFormatacao) {
        // Define o formato da data com base na opção foramasFormatacao que e fornecia de acordo com a funcao que que instancia esta
        DateTimeFormatter format;
        if (formasFormatacao) {
            format = DateTimeFormatter.ofPattern("yyyy/M/d");
            LocalDate data = LocalDate.parse(lista.get(5), format);
            format = DateTimeFormatter.ofPattern("M/d/yyyy");
            lista.set(5, data.format(format));
        } else {
            format = DateTimeFormatter.ofPattern("M/d/yyyy");
        }
        
        // Inicializa os atributos do objeto Filmes
        this.lapide = false;
        this.id = tmp;
        this.tipo = lista.get(1);
        this.nome = lista.get(2);
        this.diretor = lista.get(3);
        this.pais = lista.get(4);
        
        // Converte a data para LocalDate
        LocalDate data = LocalDate.parse(lista.get(5), format);
        this.ano_adi = data;
        
        // Converte o ano de lançamento para Year
        Year anoLan = Year.parse(lista.get(6));
        this.ano_lan = anoLan;
        
        this.classificacao = lista.get(7);
        this.duracao = lista.get(8);
        this.Genero = lista.get(9);
    }
    
    @Override
    public void writeExternal(ObjectOutput Out) throws IOException {
        // Escreve os atributos no fluxo de saída
        Out.writeBoolean(lapide);
        Out.writeInt(id);
        
        byte[] tipoBytes = tipo.getBytes("UTF-8");
        Out.writeShort(tipoBytes.length);
        Out.write(tipoBytes);
        
        byte[] nomeBytes = nome.getBytes("UTF-32");
        Out.writeShort(nomeBytes.length);
        Out.write(nomeBytes);
        
        byte[] diretorBytes = diretor.getBytes("UTF-32");
        Out.writeShort(diretorBytes.length);
        Out.write(diretorBytes);
        
        byte[] paisBytes = pais.getBytes("UTF-8");
        Out.writeShort(paisBytes.length);
        Out.write(paisBytes);
        
        // Escreve a data de adição
        Out.writeByte(ano_adi.getMonthValue());
        Out.writeByte(ano_adi.getDayOfMonth());
        Out.writeShort(ano_adi.getYear());
        
        // Escreve o ano de lançamento
        Out.writeShort(ano_lan.getValue());
        
        byte[] classificacaoBytes = classificacao.getBytes("UTF-8");
        Out.writeShort(classificacaoBytes.length);
        Out.write(classificacaoBytes);
        
        byte[] duracaoBytes = duracao.getBytes("UTF-8");
        Out.writeShort(duracaoBytes.length);
        Out.write(duracaoBytes);
        
        byte[] GeneroBytes = Genero.getBytes("UTF-8");
        Out.writeShort(GeneroBytes.length);
        Out.write(GeneroBytes);
    }
    
    @Override
    public void readExternal(ObjectInput dataIn) throws IOException {
        try {
            // Lê os dados do fluxo de entrada
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
            
            int mes = dataIn.readByte();
            int dia = dataIn.readByte();
            int ano = dataIn.readShort();
            ano_adi = LocalDate.of(ano, mes, dia);
            
            int anoLan = dataIn.readShort();
            ano_lan = Year.of(anoLan);
            
            byte[] classificacaoBytes = new byte[dataIn.readShort()];
            dataIn.readFully(classificacaoBytes);
            classificacao = new String(classificacaoBytes, "UTF-8");
            
            byte[] duracaoBytes = new byte[dataIn.readShort()];
            dataIn.readFully(duracaoBytes);
            duracao = new String(duracaoBytes, "UTF-8");
            
            byte[] GeneroBytes = new byte[dataIn.readShort()];
            dataIn.readFully(GeneroBytes);
            Genero = new String(GeneroBytes, "UTF-8");
        } catch (IOException e) {
            throw new IOException("Erro ao ler objeto Filmes", e);
        }
    }
    
    @Override
    public int compareTo(Filmes f) {
        // Compara os filmes pelo ID
        return Integer.compare(this.id, f.id);
    }
    
    public void Ler(){
        // Exibe os dados do filme
        System.out.println("----------------------------------------");
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome.trim());
        System.out.println("Ano de Lancamento: " + ano_lan);
        System.out.println("Data de Adicao: " + ano_adi);
        System.out.println("Duração: " + duracao.trim());
        System.out.println("Diretor: " + diretor.trim());
        System.out.println("Pais: " + pais.trim());
        System.out.println("Gênero: " + Genero.trim());
        System.out.println("Tipo: " + tipo.trim());
        System.out.println("Faixa Etaria: " + classificacao.trim());
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

    public LocalDate getANO_ADI(){
        return ano_adi;
    }
    public void setANO_ADI (LocalDate ano_adi){
        this.ano_adi = ano_adi;
    }

    public Year getANO_LAN(){
        return ano_lan;
    }
    public void setANO_LAN (Year ano_lan){
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