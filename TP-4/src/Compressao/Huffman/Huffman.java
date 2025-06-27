package Compressao.Huffman;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

//Classe que representa um nó da árvore de Huffman.
class HuffmanNo implements Comparable<HuffmanNo> {
	byte n; // armazena o nó
	int frequencia; // Frequência de ocorrência do byte
	HuffmanNo left, right; // Referências para os nós filhos (esquerdo e direito)

	// Construtor para inicializar o nó com um byte e sua frequência
	public HuffmanNo(byte n, int f) {
		this.n = n;
		this.frequencia = f;
		left = right = null;
	}

	// Método de comparação para ordenar os nós na fila de prioridade com base na frequência
	@Override
	public int compareTo(HuffmanNo o) {
		return this.frequencia - o.frequencia;
	}
}

//Classe principal
public class Huffman {

	//Gera a tabela de códigos de Huffman
	public static HashMap<Byte, String> codeToBit(byte[] sequence) {
		HashMap<Byte, Integer> frequencyMap = new HashMap<>();
		for (byte c : sequence) {
			frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
		}

		PriorityQueue<HuffmanNo> pq = new PriorityQueue<>();
		for (Byte n : frequencyMap.keySet()) {
			pq.add(new HuffmanNo(n, frequencyMap.get(n)));
		}

		while (pq.size() > 1) {
			HuffmanNo left = pq.poll();
			HuffmanNo right = pq.poll();
			HuffmanNo father = new HuffmanNo((byte) 0, left.frequencia + right.frequencia);
			father.left = left;
			father.right = right;
			pq.add(father);
		}

		HuffmanNo root = pq.poll();
		HashMap<Byte, String> codes = new HashMap<>();
		generateCodes(root, "", codes);

		return codes;
	}

	//Método recursivo para gerar os códigos binários a partir da árvore da arvore bianria
	private static void generateCodes(HuffmanNo no, String code, HashMap<Byte, String> codes) {
		if (no == null) {
			return;
		}
		if (no.left == null && no.right == null) {
			codes.put(no.n, code);
		}
		generateCodes(no.left, code + "0", codes);
		generateCodes(no.right, code + "1", codes);
	}

	//Decodifica a sequência de bits 
	public static byte[] decode(String codedSequence, HashMap<Byte, String> codes) {
		ByteArrayOutputStream decodedSequence = new ByteArrayOutputStream();
		StringBuilder currentCode = new StringBuilder();
		HashMap<String, Byte> reverseCodes = new HashMap<>();

		for (HashMap.Entry<Byte, String> entry : codes.entrySet()) {
			reverseCodes.put(entry.getValue(), entry.getKey());
		}

		for (int i = 0; i < codedSequence.length(); i++) {
			currentCode.append(codedSequence.charAt(i));
			if (reverseCodes.containsKey(currentCode.toString())) {
				decodedSequence.write(reverseCodes.get(currentCode.toString()));
				currentCode.setLength(0);
			}
		}

		return decodedSequence.toByteArray();
	}
}
