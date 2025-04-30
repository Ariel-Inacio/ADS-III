package ordenacao;

import classes.Filmes;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
public class Ordenacao {

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

    
}
