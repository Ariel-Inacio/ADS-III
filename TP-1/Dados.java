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
    public static void IniciarArquivoCSV(String file, String binarioFile, String binarioPais, String file2) {

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
            DataOutputStream out = new DataOutputStream(new FileOutputStream(binarioFile))
        ) {
            leitura.readLine(); // Pula o cabeçalho
            out.writeInt(contador); // Escreve o número total de registros no início do arquivo
    
            String line;
            int registro = 0;
    
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
    
                // Escreve o tamanho do objeto serializado
                out.writeInt(tmpSize.length);
                
                // Escreve os bytes do objeto no arquivo binário
                out.write(tmpSize);
    
                // Liberação explícita de memória
                dadosFilme = null;
                tmp = null;
            }
    
            System.out.println("Arquivo binário salvo com sucesso com " + contador + " registros");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
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

    public static void main(String[] args) {
        int opcao;
        String file = "netflix1.csv";
        String file2 = "WorldCountriesList.csv";
        String binarioFile = "binario.bin";
        String binarioPais = "binarioPais.bin";
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

                    IniciarArquivoCSV(file, binarioFile, binarioPais, file2);
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