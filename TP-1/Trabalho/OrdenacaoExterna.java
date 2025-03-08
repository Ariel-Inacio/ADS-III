import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class OrdenacaoExterna {
    private static int numCaminhos;
    private static int maxRegistrosMem;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Digite o número de arquivos temporários: ");
        numCaminhos = scanner.nextInt();
        
        System.out.print("Digite o máximo de registros na memória antes da intercalação: ");
        maxRegistrosMem = scanner.nextInt();
        
        String nomeArquivo = "filmes.dat";
        ordenarArquivo(nomeArquivo);
    }

    public static void ordenarArquivo(String nomeArquivo) {
        dividirArquivo(nomeArquivo);
        intercalarArquivos(nomeArquivo);
    }

    private static void dividirArquivo(String nomeArquivo) {
        try (RandomAccessFile raf = new RandomAccessFile(nomeArquivo, "r")) {
            List<String> buffer = new ArrayList<>();
            int fileIndex = 0;
            
            while (raf.getFilePointer() < raf.length()) {
                String linha = raf.readLine();
                if (linha.trim().isEmpty()) continue; // Remove lápides
                
                buffer.add(linha);
                if (buffer.size() >= maxRegistrosMem) {
                    salvarOrdenado(buffer, "temp" + fileIndex + ".dat");
                    buffer.clear();
                    fileIndex = (fileIndex + 1) % numCaminhos;
                }
            }
            
            if (!buffer.isEmpty()) {
                salvarOrdenado(buffer, "temp" + fileIndex + ".dat");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void salvarOrdenado(List<String> registros, String nomeArquivo) {
        registros.sort(Comparator.comparingInt(l -> Integer.parseInt(l.split(",")[0].trim())));
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (String registro : registros) {
                bw.write(registro);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void intercalarArquivos(String nomeArquivo) {
        PriorityQueue<BufferedReader> heap = new PriorityQueue<>(numCaminhos, Comparator.comparing(l -> {
            try {
                return Integer.parseInt(l.readLine().split(",")[0].trim());
            } catch (IOException | NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }));
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeArquivo))) {
            List<BufferedReader> leitores = new ArrayList<>();
            for (int i = 0; i < numCaminhos; i++) {
                BufferedReader br = new BufferedReader(new FileReader("temp" + i + ".dat"));
                leitores.add(br);
                heap.add(br);
            }
            
            while (!heap.isEmpty()) {
                BufferedReader br = heap.poll();
                String linha = br.readLine();
                if (linha != null) {
                    bw.write(linha);
                    bw.newLine();
                    heap.add(br);
                } else {
                    br.close();
                }
            }
            
            for (BufferedReader br : leitores) {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}