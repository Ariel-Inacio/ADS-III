# TP-4 — Casamento de Padrões e Criptografia

## Objetivo

Finalizar e expandir o projeto com duas funcionalidades avançadas:

- **Casamento de padrões** — busca eficiente de ocorrências de um padrão em textos/campos/arquivos.
- **Criptografia** — proteção dos dados armazenados, com suporte a criptografar e descriptografar.

Esta etapa integra e consolida todas as funcionalidades implementadas nos TPs anteriores (CRUD, indexação e compressão).

## Requisitos do TP (checklist)

### Casamento de padrões (menu no terminal)

- Permitir que o usuário informe um **padrão** (string) e execute a busca:
  - em campos textuais dos registros (ex.: título, descrição, nome, etc.) e/ou
  - diretamente nos dados armazenados no arquivo binário.
- Implementar pelo menos dois algoritmos clássicos:

#### KMP (Knuth-Morris-Pratt)

- Pré-processar o padrão para construir a **tabela de falha** (_failure function_).
- Realizar a busca no texto com complexidade linear.

#### Boyer-Moore

- Pré-processar o padrão para construir as heurísticas de **mau caractere** e/ou **sufixo bom**.
- Realizar a busca aproveitando saltos para acelerar o processo.

- Exibir os resultados de forma clara:
  - encontrou / não encontrou;
  - posições ou registros onde o padrão ocorre;
  - comparações realizadas ou tempo de execução (opcional).

### Criptografia (menu no terminal)

- Opção para **criptografar** os dados ou arquivo de dados.
- Opção para **descriptografar** e recuperar o conteúdo original.
- Implementar os algoritmos abaixo:

#### DES (Data Encryption Standard)

- Cifra simétrica de bloco.
- Deve permitir criptografar e descriptografar utilizando uma chave fornecida pelo usuário.

#### Vigenère

- Cifra clássica de substituição polialfabética.
- Deve permitir criptografar e descriptografar utilizando uma palavra-chave fornecida pelo usuário.

### Integração com TPs anteriores

- Manter compatibilidade com:
  - **TP-1**: arquivo binário e operações de CRUD;
  - **TP-2**: estruturas de indexação (Árvore B+, Hash Estendido, Lista Invertida);
  - **TP-3**: compressão e descompressão (Huffman e LZW).
- Definir e documentar a **ordem de operações** quando funcionalidades são combinadas, por exemplo:
  - comprimir → criptografar (ao armazenar);
  - descriptografar → descomprimir (ao recuperar).

### Fluxo recomendado de operações combinadas

1. Importar e gravar os dados no arquivo binário (TP-1).
2. Construir as estruturas de índice (TP-2).
3. Compactar o arquivo com o algoritmo escolhido (TP-3).
4. Criptografar o arquivo compactado com o algoritmo escolhido (TP-4).
5. Para recuperar: descriptografar → descompactar → consultar via índice ou CRUD.

