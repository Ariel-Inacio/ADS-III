package Compressao.LZW;

import classes.VetorDeBits;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 12; // 12 bits por índice, ao seja ao todo podem ter 4090 itens no dicinario

    // Funcao para Codificação
    public static byte[] codifica(byte[] msnBytes) throws Exception {

        // Cria o dicionário e o preenche com os 256 primeiros valores de bytes correspondentes a tabela ASCII
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
        ArrayList<Byte> vetorBytes; 
        int i, j;
        byte b;
        
        for (j = -128; j < 128; j++) {
            b = (byte) j;
            vetorBytes = new ArrayList<>(); // Cada byte será encaixado no dicionário como um vetor de um único elemento
            vetorBytes.add(b); 
            dicionario.add(vetorBytes);
        }

        // Vetor de inteiros para resposta
        ArrayList<Integer> saida = new ArrayList<>();

        // Agora inicia a fase de codificação

        i = 0;
        int indice; //posição do vetor de bytes no dicionário
        int ultimoIndice; //Último índice encontrado no dicionário
        while (i < msnBytes.length) {

            // Cria um novo vetor de bytes para acumular os bytes
            vetorBytes = new ArrayList<>();

            // Adiciona o próximo byte da mensagem ao vetor de bytes, para busca no dicionário
            b = msnBytes[i];
            vetorBytes.add(b);
            indice = dicionario.indexOf(vetorBytes);
            ultimoIndice = indice;

            // Tenta acrescentar mais bytes ao vetor de bytes
            while (indice != -1 && i < msnBytes.length - 1) {

                i++;
                b = msnBytes[i];
                vetorBytes.add(b);
                indice = dicionario.indexOf(vetorBytes); // Faz nova busca

                if (indice != -1)
                    ultimoIndice = indice;
            }

            // Acrescenta o último indice encontrado ao vetor de índices
            saida.add(ultimoIndice);

            //Adiocina o novo vetor de bytes, com o último caráter que provocou a falha na busca, ao dicionário
            if (dicionario.size() < (Math.pow(2, BITS_POR_INDICE) - 1))
                dicionario.add(vetorBytes);

            // Testa se os bytes acabaram sem provocar a codificação anterior
            if (indice != -1 && i == msnBytes.length - 1)
                break;
        }

        // Transforma o vetor de índices como uma sequência de bits
        VetorDeBits bits = new VetorDeBits(saida.size()*BITS_POR_INDICE);
        int l = saida.size()*BITS_POR_INDICE-1;
        for (i=saida.size()-1; i>=0; i--) {
            int n = saida.get(i);

            // apenas um contador de bits
            for(int m=0; m<BITS_POR_INDICE; m++) {  
                if(n%2==0)
                    bits.clear(l);
                else
                    bits.set(l);
                l--;
                n /= 2;
            }
        }

        // Retorna o vetor de bits
        return bits.toByteArray();
    }

     // Funcao para Decodificação
    public static byte[] decodifica(byte[] msnCodificada) throws Exception {

        // Cria o vetor de bits a partir do vetor de bytes
        VetorDeBits bits = new VetorDeBits(msnCodificada);

        // Transforma a sequência de bits em um vetor de índices inteiros
        int i, j, k;
        ArrayList<Integer> indices = new ArrayList<>();
        k=0;
        for (i=0; i < bits.length()/BITS_POR_INDICE; i++) {
            int n = 0;
            for(j=0; j<BITS_POR_INDICE; j++) {
                n = n*2 + (bits.get(k++)?1:0);
            }
            indices.add(n);
        }
        // Cria o vetor de bytes para decodificação de cada índice
        ArrayList<Byte> vetorBytes;

        // Cria um vetor de bytes que representa a mensagem original
        ArrayList<Byte> msnBytes = new ArrayList<>();

        // Cria um novo dicionário, inicializado com os 256 primeiros valores de bytes correspondentes a tabela ASCII
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
        byte b;
        for (j = -128, i = 0; j < 128; j++, i++) {
            b = (byte) j;
            vetorBytes = new ArrayList<>(); // Cada byte será encaixado no dicionário como um vetor de um único elemento
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        // Agora inicia a fase de Decodificação

        ArrayList<Byte> proximoVetorBytes;

        // Decodifica todos os índices
        i = 0;
        while (i < indices.size()) {

            // Decoficia o índice. 
            vetorBytes = (ArrayList<Byte>) (dicionario.get(indices.get(i))).clone();

            // Acrescenta cada byte do vetor retornado à sequência de bytes da mensagem original
            for (j = 0; j < vetorBytes.size(); j++)
                msnBytes.add(vetorBytes.get(j));

            // Adiciona o clone do vetor de bytes ao dicionário, se couber
            if (dicionario.size() < (Math.pow(2, BITS_POR_INDICE) - 1))
                dicionario.add(vetorBytes);

            // Recupera a sequência de bytes do próximo índice (se houver) e acrescenta o seu primeiro byte à sequência do último índice decodificado
            i++;
            if (i < indices.size()) {
                proximoVetorBytes = (ArrayList<Byte>) dicionario.get(indices.get(i));
                vetorBytes.add(proximoVetorBytes.get(0));
            }
        }

        // Cria um vetor de Byte, a partir do ArrayList
        byte[] msnVetorBytes = new byte[msnBytes.size()];
        for (i = 0; i < msnBytes.size(); i++)
            msnVetorBytes[i] = msnBytes.get(i);

        return msnVetorBytes;
    }

}
