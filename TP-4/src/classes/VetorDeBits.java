package classes;

import java.util.BitSet;

public class VetorDeBits {
    private BitSet vetor;
    
    // Construtor padrão - cria vetor vazio com bit sentinela
    public VetorDeBits() {
        vetor = new BitSet();
        vetor.set(0); // Define o bit 0 como sentinela
    }

    // Construtor com tamanho específico
    public VetorDeBits(int n) {
        vetor = new BitSet(n);
        vetor.set(n); // Define bit na posição n como sentinela
    }

    // Construtor a partir de array de bytes
    public VetorDeBits(byte[] v) {
        vetor = BitSet.valueOf(v); // Converte bytes para BitSet
    }

    public byte[] toByteArray() {
        return vetor.toByteArray(); // Converte o BitSet de volta para bytes
    }

   public void set(int i) {
        if(i >= vetor.length()-1) {
            vetor.clear(vetor.length()-1); // Remove sentinela atual
            vetor.set(i+1);                // Define nova sentinela
        }
        vetor.set(i); // Define o bit na posição i como 1
    }

    public void clear(int i) {
        if(i >= vetor.length()-1) {
            vetor.clear(vetor.length()-1); // Remove sentinela atual
            vetor.set(i+1);                // Define nova sentinela
        }
        vetor.clear(i); // Define o bit na posição i como 0
    }

    public boolean get(int i) {
        return vetor.get(i); // Retorna o valor do bit na posição i
    }

    public int length() {
        return vetor.length()-1; // Tamanho útil (excluindo sentinela)
    }

    public int size() {
        return vetor.size(); // Capacidade total
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<vetor.length()-1; i++) // Exclui a sentinela
            if(vetor.get(i))
                sb.append('1');
            else
                sb.append('0');
        return sb.toString();
    }
    
}
