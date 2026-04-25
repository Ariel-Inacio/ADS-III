# TP-3 — Compressão de Dados (Huffman e LZW)

## Objetivo

Adicionar ao projeto recursos de **compressão e descompressão de arquivos**, aplicando algoritmos clássicos e integrando o processo ao arquivo binário utilizado no CRUD e na indexação das etapas anteriores.

## Requisitos do TP (checklist)

### Funcionalidades (menu no terminal)

- Opção para **compactar** o arquivo de dados (e/ou outros arquivos relevantes do projeto).
- Opção para **descompactar** e recuperar o arquivo original (ou equivalente funcional).
- Exibir, após cada operação, informações básicas como **tamanho original**, **tamanho comprimido** e **taxa de compressão**.

### Algoritmos obrigatórios

#### Huffman

- Construir a **árvore de Huffman** com base na frequência dos bytes/caracteres do arquivo.
- Gerar a **tabela de códigos** (codebook) e armazená-la junto ao arquivo comprimido.
- Compactar o arquivo de entrada e gravar o resultado em disco.
- Descompactar o arquivo, reconstituindo os dados originais a partir da tabela de códigos.

#### LZW

- Inicializar o **dicionário** com o alfabeto padrão e expandi-lo dinamicamente durante a compressão.
- Compactar o arquivo de entrada e gravar o resultado em disco.
- Descompactar o arquivo reconstruindo o dicionário durante a leitura dos códigos gerados.

### Regras e validações

- A descompressão deve produzir um resultado **idêntico ao arquivo original** (integridade dos dados).
- Os dois algoritmos devem ser implementados de forma **independente**, cada um com sua própria opção no menu.
- Registrar (ao menos em tela) a **taxa de compressão** obtida por cada algoritmo para comparação.

### Integração com o projeto

- A compressão deve ser aplicável ao arquivo binário de dados gerado no TP-1/TP-2.
- Após a descompressão, o sistema deve ser capaz de retomar normalmente as operações de leitura, busca e CRUD sobre o arquivo recuperado.

## Como esta etapa se conecta às próximas

- A compressão implementada aqui pode ser usada em conjunto com a **criptografia e o casamento de padrões do TP-4**.
- Definir (e documentar) a ordem de operações quando combinadas (ex.: compactar → criptografar, e como reverter corretamente).

