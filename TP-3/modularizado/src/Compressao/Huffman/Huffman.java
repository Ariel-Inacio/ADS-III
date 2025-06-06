package Compressao.Huffman;

import classes.VetorDeBits;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Classe que representa um nó da árvore de Huffman.
 * Cada nó armazena um byte, sua frequência e referências para os nós filhos.
 */
class HuffmanNode implements Comparable<HuffmanNode> {
	byte b; // Byte armazenado no nó
	int frequency; // Frequência de ocorrência do byte
	HuffmanNode left, right; // Referências para os nós filhos (esquerdo e direito)

	// Construtor para inicializar o nó com um byte e sua frequência
	public HuffmanNode(byte b, int f) {
		this.b = b;
		this.frequency = f;
		left = right = null;
	}

	// Método de comparação para ordenar os nós na fila de prioridade (com base na
	// frequência)
	@Override
	public int compareTo(HuffmanNode o) {
		return this.frequency - o.frequency;
	}
}

/**
 * Classe principal responsável pela compressão e descompressão usando o
 * algoritmo de Huffman.
 */
public class Huffman {

	/**
	 * Gera a tabela de códigos de Huffman para cada byte da sequência.
	 *
	 * @param sequence sequência de bytes a ser comprimida.
	 * @return um HashMap com o byte como chave e seu código binário como valor.
	 */
	public static HashMap<Byte, String> codeToBit(byte[] sequence) {
		HashMap<Byte, Integer> frequencyMap = new HashMap<>();
		for (byte c : sequence) {
			frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
		}

		PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
		for (Byte b : frequencyMap.keySet()) {
			pq.add(new HuffmanNode(b, frequencyMap.get(b)));
		}

		while (pq.size() > 1) {
			HuffmanNode left = pq.poll();
			HuffmanNode right = pq.poll();
			HuffmanNode father = new HuffmanNode((byte) 0, left.frequency + right.frequency);
			father.left = left;
			father.right = right;
			pq.add(father);
		}

		HuffmanNode root = pq.poll();
		HashMap<Byte, String> codes = new HashMap<>();
		generateCodes(root, "", codes);

		return codes;
	}

	/**
	 * Método recursivo para gerar os códigos binários a partir da árvore de
	 * Huffman.
	 */
	private static void generateCodes(HuffmanNode node, String code, HashMap<Byte, String> codes) {
		if (node == null) {
			return;
		}
		if (node.left == null && node.right == null) {
			codes.put(node.b, code);
		}
		generateCodes(node.left, code + "0", codes);
		generateCodes(node.right, code + "1", codes);
	}

	/**
	 * Decodifica uma sequência de bits para o texto original usando a tabela de
	 * códigos.
	 */
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
