package util;

import classes.Filmes;
import classes.RegistroID;
import indexacao.Arvore.ArvoreBMais;
import indexacao.Hash.*;
import indexacao.Lista.ElementoLista;
import indexacao.Lista.ListaInvertida;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Escrever {
    //Função para ler o arquivo CSV e salva-lo como um arquivo Binario
    public static void IniciarArquivoCSV(String file, String binarioFile, String binarioPais, String file2, int index, Scanner sc, ArvoreBMais<RegistroID> arvore, ListaInvertida lista1, ListaInvertida lista2, List<Integer> Criterios, HashExtensivel<ParID> hash) {

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

            File fileExistente = new File(binarioFile);
            if (fileExistente.exists()) {
                fileExistente.delete(); // Exclui o arquivo existente
            }
            
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
                dadosFilme.set(4, Pesquisar.PesquisarPaisAbre(binarioPais, paisFilme));
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

                if(index == 1){
                    try {
                        arvore.create(new RegistroID(registro, posicaoAtual));
                    } catch (Exception e) {
                        e.printStackTrace(); 
                    }
                }

                else if(index == 2){
                    
                    hash.create(new ParID(posicaoAtual, registro));
                    
                }

                else if(index == 3){

                    List<String> ListaCriterios = Arrays.stream(dadosFilme.get(Criterios.get(0)).split(",")).map(String::trim).collect(Collectors.toList());

                    for(int i = 0; i < ListaCriterios.size(); i++){
                        lista1.create(ListaCriterios.get(i), new ElementoLista(posicaoAtual, registro));
                    }

                    List<String> ListaCriterios2 = Arrays.stream(dadosFilme.get(Criterios.get(1)).split(",")).map(String::trim).collect(Collectors.toList());

                    for(int i = 0; i < ListaCriterios2.size(); i++){
                      lista2.create(ListaCriterios2.get(i), new ElementoLista(posicaoAtual, registro));
                    }

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

    //Método para adicinar um novo objeto a lista invertida
    public static void AdicionarListaInvertida(ListaInvertida lista, long posicao, Filmes novoFilme, int tmp) {

        try{

            List<String> ListaCriterios = Arrays.stream(novoFilme.CriterioLista(tmp).split(",")).map(String::trim).collect(Collectors.toList());

            for(int i = 0; i < ListaCriterios.size(); i++){
                lista.create(ListaCriterios.get(i), new ElementoLista(posicao, novoFilme.getID()));
            }
            
        }catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
