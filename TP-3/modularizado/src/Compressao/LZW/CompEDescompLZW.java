package Compressao.LZW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CompEDescompLZW {

    public static float CodificaLZW (String binarioFile, String arquivoCompactado) {
        // Implementação da codificação LZW

        float eficiencia = 0.0f;

        try (RandomAccessFile raf = new RandomAccessFile(binarioFile, "r");
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivoCompactado))) {

            byte[] dados = new byte[(int) raf.length()];
            raf.seek(0);
            raf.readFully(dados);

            byte[] msgCodificada = LZW.codifica(dados); // Vetor de bits que contém os índices
            
            dos.writeInt(msgCodificada.length); // Escreve o tamanho do vetor de bytes
            dos.write(msgCodificada);//Escreve os dados compactados

            //Calcula a eficiência da compactação
            eficiencia = (100 * (1 - (float) msgCodificada.length / (float) dados.length));

            System.out.println("Tamanho original: " + dados.length + " bytes");
            System.out.println("Tamanho compactado: " + msgCodificada.length + " bytes");
            System.out.println("Eficiência: " + eficiencia + "%");
            System.out.println();

        }catch(IOException e){
            System.out.println("Erro ao compactar o arquivo: " + e.getMessage());
        }catch(Exception e){
            System.out.println("Erro ao compactar o arquivo: " + e.getMessage());
        }

        return eficiencia;

    }

    public static void DescodificaLZW (String arquivoCompactado, String arquivoDescompactado, String binariofile) {
        // Implementação da descodificação Huffman

        try(DataInputStream dis = new DataInputStream(new FileInputStream(arquivoCompactado))) {

            // Ler os dados compactados
            int tamanho = dis.readInt();
            byte[] dadosCompactados = new byte[tamanho];
            dis.readFully(dadosCompactados);

            // Descodificação
            byte[] dadosDescompactados = LZW.decodifica(dadosCompactados);

            // Salvar os dados descompactados no arquivo original
            try (FileOutputStream fos = new FileOutputStream(arquivoDescompactado)) {
                fos.write(dadosDescompactados);
                System.out.println("Descompactação LZW concluída!");
                System.out.println("Tamanho descompactado: " + dadosDescompactados.length + " bytes");
                System.out.println();
            }

        } catch (IOException e) {
            System.out.println("Erro ao descompactar o arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro ao descompactar o arquivo: " + e.getMessage());
        }

    }
    
}
