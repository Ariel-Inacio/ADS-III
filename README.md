# ADS-III (Algoritmos e Estruturas de Dados III)

Repositório da disciplina **Algoritmos e Estruturas de Dados III (PUC Minas)**.

Este projeto implementa uma base completa de manipulação de **arquivos binários** e **estruturas/algoritmos** típicos da disciplina, organizada em **Tps (Trabalhos Práticos)**, cada um com um objetivo claro e uma evolução incremental do código.

## Objetivo do trabalho

Construir, em **Java**, uma aplicação capaz de:

- Persistir dados em **arquivo binário** (com **CRUD**);
- Executar operações avançadas sobre esse arquivo, incluindo:
  - **Ordenação externa**;
  - **Indexação** (Árvore B+, Hash Extensível, Lista Invertida);
  - **Compressão** (Huffman, LZW);
  - **Casamento de padrões** (Boyer–Moore, KMP);
  - **Criptografia** (Vigenère e DES).

> A ideia central é evoluir o mesmo projeto por etapas, adicionando módulos e técnicas a cada TP.

## Estrutura do repositório (visão geral)

- `TP-1/` — Primeira etapa: leitura/escrita e ordenação em arquivo binário.
- `TP-2/` — Segunda etapa: arquivo indexado (B+, Hash, Lista Invertida).
- (Demais TPs/etapas podem ser adicionados no mesmo padrão.)

Cada pasta de TP deve conter:

- Código Java do respectivo trabalho prático;
- Documentação específica (README do TP);
- Arquivos auxiliares (ex.: CSV de entrada), quando aplicável.

## Etapas (o que foi feito em cada fase)

### TP-1 — Arquivo binário + ordenação

Objetivo: criar a base do projeto com persistência em arquivo.

Entregas típicas desta etapa:

- Estrutura de registro em arquivo binário;
- **CRUD completo** no arquivo;
- Rotinas de leitura/escrita;
- **Ordenação externa** (quando aplicável ao escopo do TP);
- Organização inicial do projeto.

Leia mais em: `TP-1/README.md`.

### TP-2 — Indexação (B+, Hash e Lista Invertida)

Objetivo: acelerar buscas e operações usando estruturas de índice.

Entregas típicas desta etapa:

- Implementação e manutenção de índices:
  - **Árvore B+**;
  - **Hash Extensível**;
  - **Lista Invertida**;
- Integração do índice com o CRUD do arquivo binário;
- Ajustes de consistência (inserção/remoção/atualização refletindo nos índices).

Leia mais em: `TP-2/README.md`.

## Funcionalidades implementadas (consolidado)

- CRUD completo em arquivos binários
- Ordenação externa
- Indexação com:
  - Árvore B+
  - Hash Extensível
  - Lista Invertida
- Compressão com:
  - Huffman
  - LZW
- Casamento de padrões com:
  - Boyer–Moore
  - Knuth–Morris–Pratt (KMP)
- Criptografia com:
  - Vigenère (simétrica)
  - DES (conforme especificação do trabalho)

## Autores

- Amanda Canizela Guimarães
- Ariel Inácio Jordão Coelho

## Como executar (ajuste conforme o TP)

1. Clone o repositório:
   ```bash
   git clone https://github.com/Ariel-Inacio/ADS-III.git
   ```
2. Entre na pasta do TP desejado (ex.: `TP-1` ou `TP-2`).
3. Abra em uma IDE Java (IntelliJ/Eclipse/VS Code).
4. Compile e execute a classe principal (ex.: `Main.java`), conforme instruções do README do TP.
