import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        try (BufferedReader contagem = new BufferedReader(new FileReader(file))) {
            String linha;
            // Pular o cabeçalho
            contagem.readLine();
            
            while (contagem.readLine() != null) {
                contador++;
            }
        } catch (IOException e) {
            System.out.println("Erro ao contar registros: " + e.getMessage());
            return;
        }
        
        // Agora escrevemos os registros
        try (
            BufferedReader leitura = new BufferedReader(new FileReader(file));
            DataOutputStream out = new DataOutputStream(new FileOutputStream(binarioFile))
        ) {
            // Pular o cabeçalho
            String PLinha = leitura.readLine();
            
            // Primeiro escrevemos o contador
            out.writeInt(contador);
            
            // Agora processamos linha por linha e escrevemos cada filme
            String line;
            int registro = 0;
            
            while ((line = leitura.readLine()) != null) {
                line = line.replaceAll(";", "").trim();
                line = line.replaceAll("^\"|\"$", "").trim();
                line = line.replaceAll("\"\"", "\"").trim();
                
                List<String> dadosFilme = extrairDadosLinha(line);
                
                String paisFilme = dadosFilme.get(4);
                dadosFilme.set(4, PesquisarPaisAbre(binarioPais, paisFilme));
                registro++;
                
                Filmes tmp = new Filmes(dadosFilme, registro, false);
               
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(tmp);
                }
                
                byte[] tmpSize = baos.toByteArray();
                
                // Escreve o tamanho em bytes do objeto serializado
                out.writeInt(tmpSize.length);
                
                // Escreve os bytes do objeto
                out.write(tmpSize);
                
                // Opcional: liberar memória explicitamente
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

    public static void lerBinario(String binarioFile) {
        try (RandomAccessFile in = new RandomAccessFile(binarioFile, "r")) {
            // Lendo o ultimo ID do registros
            int ultimo = in.readInt();

            System.out.println(ultimo);
            
            // Lendo os filmes
            while (in.getFilePointer() < in.length()) {
                
                int size = in.readInt();

                byte[] FilmeBytes = new byte[size];
                in.readFully(FilmeBytes);

                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {

                    Filmes filme = (Filmes) ois.readObject();
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

    public static void atualizarFilmeID(int IDDesejado, Filmes novoFilme, String binarioFile) {
        try (RandomAccessFile file = new RandomAccessFile(binarioFile, "rw")) {
            int Ultimo = file.readInt();
            
            long posicaoInicial;
            int tamanhoObjeto;
            boolean encontrado = false;
            boolean incrementar = false;
            
            // Verifique se chegou ao final do arquivo antes de tentar ler
            while (file.getFilePointer() < file.length()) {
                posicaoInicial = file.getFilePointer();
                
                // Tente ler o tamanho do objeto
                try {
                    tamanhoObjeto = file.readInt();
                } catch (EOFException e) {
                    // Fim do arquivo atingido
                    break;
                }
                
                // Verifique se o tamanho é válido para evitar problemas de memória
                if (tamanhoObjeto <= 0 || tamanhoObjeto > 1000000) {
                    System.out.println("Tamanho de objeto inválido detectado: " + tamanhoObjeto);
                    break;
                }
                
                byte[] dadosFilmes = new byte[tamanhoObjeto];
                try {
                    file.readFully(dadosFilmes);
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
                                file.write(zeros);
                            }
                        } else {
                            file.seek(posicaoInicial + 4);
                            Filmes filmeComLapide = filmes;
                            filmeComLapide.setLAPIDE(true);
                            
                            ByteArrayOutputStream lapideStream = new ByteArrayOutputStream();
                            try (ObjectOutputStream lapideOut = new ObjectOutputStream(lapideStream)) {
                                lapideOut.writeObject(filmeComLapide);
                            }
                            byte[] lapideBytes = lapideStream.toByteArray();
                            
                            // Garantir que não estamos escrevendo mais bytes do que o tamanho original
                            if (lapideBytes.length <= tamanhoObjeto) {
                                file.write(lapideBytes);
                            } else {
                                // Apenas marque como excluído usando um método mais simples se necessário
                                file.writeBoolean(true);
                            }
                            
                            // Adicionar o novo registro no final do arquivo
                            file.seek(file.length());
                            file.writeInt(novoTamanho);
                            file.write(novoBytes);
                            
                            Ultimo = novoFilme.getID();
                            incrementar = true;
                        }
                        break; // Saia do loop depois de encontrar e atualizar
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao ler objeto na posição " + posicaoInicial + ": " + e.getMessage());
                    // Avance para o próximo registro em vez de parar completamente
                    // continue;
                }
            }
            
            if (!encontrado) {
                System.out.println("Registro com ID " + IDDesejado + " não encontrado.");
            } else if (incrementar) {
                file.seek(0);
                file.writeInt(Ultimo);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + binarioFile);
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static String PesquisarPaisAbre(String binarioFilePais, String NomePais){

        String resultado = "NOT";

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

        try (RandomAccessFile dis = new RandomAccessFile(binarioFile, "r")){
            // Ler o ultimo ID do registros
            int Ultimo = dis.readInt();
            
            while (dis.getFilePointer() < dis.length()) {
                
                int size = dis.readInt();

                byte[] FilmeBytes = new byte[size];
                dis.readFully(FilmeBytes);

                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {

                    Filmes filme = (Filmes) ois.readObject();
                    if (!filme.getLAPIDE() && filme.getNOME().equals(NomeDesejado)) {
                        filme.Ler();
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }

        } catch (EOFException e) {
            System.out.println("Fim do arquivo atingido antes do esperado.");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + binarioFile);
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    public static Filmes PesquisarID(String binarioFile, int IDDesejado) {
        try (RandomAccessFile dis = new RandomAccessFile(binarioFile, "r")) {
            // Ler o ultimo ID do registros
            int Ultimo = dis.readInt();
            
            while (dis.getFilePointer() < dis.length()) {
                
                int size = dis.readInt();

                byte[] FilmeBytes = new byte[size];
                dis.readFully(FilmeBytes);

                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); 
                    ObjectInputStream ois = new ObjectInputStream(bais)) {

                    Filmes filme = (Filmes) ois.readObject();
                    if (!filme.getLAPIDE() && filme.getID() == IDDesejado) {
                        return filme;
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                }
            }

        } catch (EOFException e) {
            System.out.println("Fim do arquivo atingido antes do esperado.");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado: " + binarioFile);
        } catch (IOException e) {
            System.out.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Não encontrou o filme com o ID desejado
        return null;
    }

    public static int encontrarTamanho(String arquivo) {
        int quantidade = 0;
    
        try (RandomAccessFile in = new RandomAccessFile(arquivo, "r")) {
            // Lendo o ultimo ID do registros
            int ultimo = in.readInt();
            
            // Lendo os filmes
            while (in.getFilePointer() < in.length()) {
                
                int size = in.readInt();

                byte[] FilmeBytes = new byte[size];
                in.readFully(FilmeBytes);

                try(ByteArrayInputStream bais = new ByteArrayInputStream(FilmeBytes); ObjectInputStream ois = new ObjectInputStream(bais)) {

                    Filmes filme = (Filmes) ois.readObject();
                    if (filme.getID() > quantidade){
                        quantidade = filme.getID();
                    }
                } catch (ClassNotFoundException e) {
                    e.getMessage();
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
        
        return quantidade;
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

    public static int lerNumeroTotalFilmes(String arquivoEntrada) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivoEntrada, "r")) {
            int total = raf.readInt();
            System.out.println("Número total de filmes lido: " + total);
            return total;
        }
    }

    // Método principal para realizar a ordenação externa balanceada
    public static void ordenarExterna(String arquivoBinario, int numCaminhos, int registrosPorBloco) {
        try {
            // Contagem total de registros no arquivo
            int totalRegistros = contarRegistros(arquivoBinario);
            System.out.println("Total de registros: " + totalRegistros);
            
            if (totalRegistros <= registrosPorBloco) {
                // Se o total de registros é menor que o tamanho do bloco, ordenar tudo em memória
                ordenarArquivoCompleto(arquivoBinario);
                return;
            }
            
            // Fase de distribuição - divide o arquivo em blocos ordenados
            List<String> arquivosTemporarios = distribuicao(arquivoBinario, numCaminhos, registrosPorBloco);
            
            // Fase de intercalação - une os blocos ordenados
            String arquivoFinal = intercalacao(arquivosTemporarios, numCaminhos, arquivoBinario + ".ordenado");
            
            // Renomear o arquivo final ou copiar para substituir o original, se desejado
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
    
    // Conta o número total de registros no arquivo binário
    private static int contarRegistros(String arquivoBinario) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivoBinario, "r")) {
            // O primeiro inteiro é o contador de registros
            return raf.readInt();
        }
    }
    
    // Ordena todo o arquivo em memória (quando é pequeno o suficiente)
    private static void ordenarArquivoCompleto(String arquivoBinario) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(arquivoBinario, "r")) {
            // Ler o contador
            int totalRegistros = in.readInt();
            
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
                // Escrever o contador
                out.writeInt(filmes.size());
                
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
    private static List<String> distribuicao(String arquivoBinario, int numCaminhos, int registrosPorBloco) throws IOException {
        List<String> arquivosTemporarios = new ArrayList<>();
        
        try (RandomAccessFile raf = new RandomAccessFile(arquivoBinario, "r")) {
            // Ler o contador de registros
            int totalRegistros = raf.readInt();
            
            // Calcular número de blocos necessários
            int numBlocos = (int) Math.ceil((double) totalRegistros / registrosPorBloco);
            
            // Criar arquivos temporários para cada caminho
            for (int i = 0; i < numCaminhos; i++) {
                String nomeArquivo = arquivoBinario + ".temp" + i;
                arquivosTemporarios.add(nomeArquivo);
                
                // Inicializar arquivo com um contador zerado
                try (DataOutputStream out = new DataOutputStream(new FileOutputStream(nomeArquivo))) {
                    out.writeInt(0); // Contador inicializado com 0
                }
            }
            
            // Distribuir registros entre os caminhos
            int blocoAtual = 0;
            int registrosLidos = 0;
            
            while (raf.getFilePointer() < raf.length() && registrosLidos < totalRegistros) {
                // Determinar qual arquivo temporário usar
                int caminhoAtual = blocoAtual % numCaminhos;
                String arquivoTemp = arquivosTemporarios.get(caminhoAtual);
                
                // Ler um bloco de registros para memória
                List<Filmes> bloco = new ArrayList<>();
                int registrosNoBloco = 0;
                
                while (raf.getFilePointer() < raf.length() && registrosNoBloco < registrosPorBloco && registrosLidos < totalRegistros) {
                    
                    int size = raf.readInt();
                    byte[] filmeBytes = new byte[size];
                    raf.readFully(filmeBytes);
                    
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                         ObjectInputStream ois = new ObjectInputStream(bais)) {
                        
                        Filmes filme = (Filmes) ois.readObject();
                        if (!filme.getLAPIDE()) {
                            bloco.add(filme);
                            registrosNoBloco++;
                            registrosLidos++;
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
        // Ler o arquivo temporário para obter o contador atual
        int contadorAtual = 0;
        try (RandomAccessFile raf = new RandomAccessFile(arquivoTemp, "r")) {
            contadorAtual = raf.readInt();
        } catch (EOFException e) {
            // Arquivo vazio, contador será 0
        }
        
        // Criar uma cópia do arquivo com os dados existentes
        byte[] dadosExistentes = new byte[0];
        if (contadorAtual > 0) {
            try (RandomAccessFile raf = new RandomAccessFile(arquivoTemp, "r")) {
                raf.readInt(); // Pular o contador
                dadosExistentes = new byte[(int)(raf.length() - 4)];
                raf.readFully(dadosExistentes);
            }
        }
        
        // Escrever os dados de volta junto com o novo bloco
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoTemp))) {
            // Atualizar o contador
            out.writeInt(contadorAtual + bloco.size());
            
            // Escrever os dados existentes
            if (dadosExistentes.length > 0) {
                out.write(dadosExistentes);
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
    private static String intercalacao(List<String> arquivosTemporarios, int numCaminhos, String arquivoFinal) throws IOException {
        // Se só há um arquivo temporário, ele já está ordenado
        if (arquivosTemporarios.size() == 1) {
            return arquivosTemporarios.get(0);
        }
        
        // Criar novos arquivos para a intercalação
        List<String> novosArquivos = new ArrayList<>();
        for (int i = 0; i < Math.max(1, arquivosTemporarios.size() / numCaminhos); i++) {
            String novoArquivo = arquivoFinal + ".intercalacao" + i;
            novosArquivos.add(novoArquivo);
            
            // Inicializar com contador zerado
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(novoArquivo))) {
                out.writeInt(0); // Contador inicializado com 0
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
            intercalarGrupo(grupoArquivos, arquivoSaida);
            
            // Limpar os arquivos que já foram intercalados
            for (String arquivo : grupoArquivos) {
                Files.deleteIfExists(Paths.get(arquivo));
            }
        }
        
        // Recursivamente continuar a intercalação até que reste apenas um arquivo
        if (novosArquivos.size() > 1) {
            return intercalacao(novosArquivos, numCaminhos, arquivoFinal);
        } else {
            return novosArquivos.get(0);
        }
    }
    
    // Intercala um grupo de arquivos em um único arquivo de saída
    private static void intercalarGrupo(List<String> arquivos, String arquivoSaida) throws IOException {
        // Preparar os leitores para cada arquivo
        List<RandomAccessFile> leitores = new ArrayList<>();
        List<Integer> contadores = new ArrayList<>();
        
        // Heap para manter o próximo filme de cada arquivo
        PriorityQueue<FilmeComOrigem> heap = new PriorityQueue<>();
        
        try {
            // Abrir cada arquivo e ler o primeiro registro
            for (int i = 0; i < arquivos.size(); i++) {
                RandomAccessFile raf = new RandomAccessFile(arquivos.get(i), "r");
                leitores.add(raf);
                
                // Ler o contador
                int contador = raf.readInt();
                contadores.add(contador);
                
                // Se houver registros, adicionar o primeiro ao heap
                if (contador > 0 && raf.getFilePointer() < raf.length()) {
                    int size = raf.readInt();
                    byte[] filmeBytes = new byte[size];
                    raf.readFully(filmeBytes);
                    
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(filmeBytes);
                         ObjectInputStream ois = new ObjectInputStream(bais)) {
                        
                        Filmes filme = (Filmes) ois.readObject();
                        if (!filme.getLAPIDE()) {
                            heap.add(new FilmeComOrigem(filme, i));
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Erro ao converter para classe Filmes: " + e.getMessage());
                    }
                }
            }
            
            // Calcular o total de registros
            int totalRegistros = contadores.stream().mapToInt(Integer::intValue).sum();
            
            // Ler o contador atual do arquivo de saída
            int contadorSaida = 0;
            try (RandomAccessFile raf = new RandomAccessFile(arquivoSaida, "r")) {
                contadorSaida = raf.readInt();
            } catch (EOFException e) {
                // Arquivo vazio, contador será 0
            }
            
            // Preparar o arquivo de saída
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoSaida))) {
                // Escrever o contador atualizado
                out.writeInt(contadorSaida + totalRegistros);
                
                // Copiar os dados existentes (se houver)
                if (contadorSaida > 0) {
                    try (RandomAccessFile raf = new RandomAccessFile(arquivoSaida, "r")) {
                        raf.readInt(); // Pular o contador
                        byte[] dadosExistentes = new byte[(int)(raf.length() - 4)];
                        raf.readFully(dadosExistentes);
                        out.write(dadosExistentes);
                    }
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

            System.out.println("\t----Opcoes----");
            System.out.println("\t1: Ler arquivo CSV");
            System.out.println("\t2: Ler arquivo BINARO");
            System.out.println("\t3: Pesquisar Filme/Serie");
            System.out.println("\t4: Atualizar Lista");
            System.out.println("\t5: Remover Filme/Serie");
            System.out.println("\t6: Adicionar novo Filme/Serie");
            System.out.println("\t7: Ordenar");
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

                                    if(pais.equals("NOT")){
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

                                    System.out.println("Digite o novo ano de adicao ex: 2001/5/28:");
                                    String anoAdicao = sc.nextLine();
                                    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/M/d");
                                    LocalDate data = LocalDate.parse(anoAdicao, format);
                                    novoFilme.setANO_ADI(data);
                                    System.out.println("Ano de adicao atualizado...");
                                    break;

                                }

                                case 6:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo ano de lancamento ex: 2021:");
                                    String anoLancamento = sc.nextLine();
                                    Year ano = Year.parse(anoLancamento);
                                    novoFilme.setANO_LAN(ano);
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

                    try(RandomAccessFile in = new RandomAccessFile(binarioFile, "rw")){

                        int ultimo = in.readInt();
                        int ID = (encontrarTamanho(binarioFile)) + 1;

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

                        System.out.println("\tDigite o novo ano de adicao ex: 2001/5/28:");
                        String anoAdicao = sc.nextLine();
                        lista.add(anoAdicao);

                        System.out.println("\tDigite o ano de lancamento ex: 2021: ");
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

                        Filmes novoFilme = new Filmes(lista, ID, true);
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

                                    if(pais.equals("NOT")){
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

                                    System.out.println("Digite o novo ano de adicao ex: 12/31/2001:");
                                    anoAdicao = sc.nextLine();
                                    DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
                                    LocalDate data = LocalDate.parse(anoAdicao, format);
                                    novoFilme.setANO_ADI(data);
                                    System.out.println("Ano de adicao atualizado...");
                                    break;

                                }

                                case 6:{

                                    sc.nextLine();

                                    System.out.println("Digite o novo ano de lancamento ex: 2021:");
                                    anoLancamento = sc.nextLine();
                                    Year ano = Year.parse(anoLancamento);
                                    novoFilme.setANO_LAN(ano);
                                    System.out.println("Ano de lancamento atualizado...");
                                    break;

                                }

                                case 7:{

                                    novoFilme.setCLASSIFICACAO(classificacaoIndicativa(sc));
                                    break;

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

                case 7:{

                    try{


                        int numCaminhos;

                        System.out.println("Digite o numero de caminhos para a ordenação");

                        while((numCaminhos = sc.nextInt()) > 100){
                            System.out.println("Numero de caminhos muito grande (numero maximo 100)");
                        }
 
                        int Blocos;

                        System.out.println("Digite o numero de registros máximo para cada ordenação em memória primária");


                        while((Blocos = sc.nextInt()) > 1000){
                            System.out.println("Numero de registros maximo muito grande (numero maximo 1000)");
                        }

                        ordenarExterna(binarioFile, numCaminhos, Blocos);

                        Files.copy(Paths.get("binario.bin.ordenado"), Paths.get(binarioFile), StandardCopyOption.REPLACE_EXISTING);

                        Files.deleteIfExists(Paths.get("binario.bin.ordenado"));
                        Files.deleteIfExists(Paths.get("binario.bin.ordenado.intercalacao0"));
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

        DateTimeFormatter format;
    
        if (formasFormatacao) {
            format = DateTimeFormatter.ofPattern("yyyy/M/d");
            LocalDate data = LocalDate.parse(lista.get(5), format);
    
            format = DateTimeFormatter.ofPattern("M/d/yyyy");
            lista.set(5, data.format(format));
        } else {
            format = DateTimeFormatter.ofPattern("M/d/yyyy");
        }
    
        this.lapide = false;
        this.id = tmp;
        this.tipo = lista.get(1);
        this.nome = lista.get(2);
        this.diretor = lista.get(3);
        this.pais = lista.get(4);
    
        LocalDate data = LocalDate.parse(lista.get(5), format);
        this.ano_adi = data;
    
        Year anoLan = Year.parse(lista.get(6));
        this.ano_lan = anoLan;
    
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

        dataOut.writeByte(ano_adi.getMonthValue());
        dataOut.writeByte(ano_adi.getDayOfMonth());
        dataOut.writeShort(ano_adi.getYear());

        dataOut.writeShort(ano_lan.getValue());

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

    }

    @Override
    public void writeExternal(ObjectOutput Out) throws IOException {
    
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
    
        Out.writeByte(ano_adi.getMonthValue());
        Out.writeByte(ano_adi.getDayOfMonth());
        Out.writeShort(ano_adi.getYear());
    
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
            
            // Lê os dados usando a lógica do readPersonalizado
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
        return Integer.compare(this.id, f.id);
    }

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