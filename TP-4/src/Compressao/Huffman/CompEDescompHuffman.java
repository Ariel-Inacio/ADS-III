package Compressao.Huffman;

import classes.VetorDeBits;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class CompEDescompHuffman {

    // Função que implementação da codificação Huffman
    public static float CodificaHuffman(String binarioFile, String arquivoCompactado) {

        float eficiencia = 0.0f;

        try (RandomAccessFile raf = new RandomAccessFile(binarioFile, "r");
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivoCompactado))) {
            
            byte[] dados = new byte[(int) raf.length()];
            raf.seek(0);
            raf.readFully(dados);

            HashMap<Byte, String> codigos = Huffman.codeToBit(dados);

            //Codificação
            VetorDeBits sequenciaCodificada = new VetorDeBits();

            int i = 0;
            for (byte b : dados) {
                String codigo = codigos.get(b);
                if (codigo == null)
                    continue;
                for (char c : codigo.toCharArray()) {
                    if (c == '0')
                        sequenciaCodificada.clear(i++);
                    else
                        sequenciaCodificada.set(i++);
                }
            }

            byte[] vb = sequenciaCodificada.toByteArray();

            // Salvar a tabela de códigos (necessária para descompressão)
            ObjectOutputStream oos = new ObjectOutputStream(dos);
            oos.writeObject(codigos);

            // IMPORTANTE: Salvar o número de bits válidos
            dos.writeInt(i); // Número de bits realmente usados
            
            // Salvar os dados compactados
            dos.writeInt(vb.length);
            dos.write(vb);

            eficiencia = (100 * (1 - (float) vb.length / (float) dados.length));

            System.out.println("Tamanho original: "+dados.length+" bytes");
            System.out.println("Tamanho compactado: "+vb.length+" bytes");
            System.out.println("Eficiência: " + eficiencia + "%");
            System.out.println();

        }catch(IOException e){
            System.out.println("Erro ao compactar o arquivo: " + e.getMessage());
        }

        return eficiencia;

    }

    //Função que implementação da descodificação Huffman
    public static void DescodificaHuffman (String arquivoCompactado, String arquivoDescompactado, String binariofile) {

        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivoCompactado))) {
            
            // Ler a tabela de códigos
            ObjectInputStream ois = new ObjectInputStream(dis);
            HashMap<Byte, String> codigos = (HashMap<Byte, String>) ois.readObject();

            // Ler o número de bits válidos
            int numBitsValidos = dis.readInt();

            // Ler os dados compactados
            int tamanho = dis.readInt();
            byte[] dadosCompactados = new byte[tamanho];
            dis.readFully(dadosCompactados);

            // Converter para VetorDeBits e extrair apenas os bits válidos
            VetorDeBits vetorBits = new VetorDeBits(dadosCompactados);
            StringBuilder sequenciaCodificada = new StringBuilder();
            
            for (int i = 0; i < numBitsValidos; i++) {
                sequenciaCodificada.append(vetorBits.get(i) ? '1' : '0');
            }

            byte[] dadosDescompactados = Huffman.decode(sequenciaCodificada.toString(), codigos);

            // Salvar no arquivo descompactado
            try (FileOutputStream fos = new FileOutputStream(arquivoDescompactado)) {
                fos.write(dadosDescompactados);
                System.out.println("Descompactação Huffman concluída!");
                System.out.println("Tamanho descompactado: " + dadosDescompactados.length + " bytes");
                System.out.println();
                
                // Verificar se os dados foram restaurados corretamente
                try (RandomAccessFile rafVerif = new RandomAccessFile(arquivoDescompactado, "r")) {
                    if (rafVerif.length() > 4) {
                        System.out.println("Arquivo descompactado com estrutura correta!");
                    } else {
                        System.out.println("ERRO: Arquivo descompactado muito pequeno.");
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro ao descompactar o arquivo: " + e.getMessage());
        }

    }
    
}
