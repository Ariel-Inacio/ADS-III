# TP-1 — CRUD Sequencial + Ordenação Externa

## Objetivo

Implementar a **base do projeto**: um sistema em **Java** capaz de importar uma base (ex.: CSV) e armazená-la em um **arquivo binário sequencial**, oferecendo operações completas de **CRUD**.

Além do CRUD, esta etapa também introduz a **Ordenação Externa**, permitindo reorganizar o arquivo e remover espaços de registros excluídos/atualizados.

## Requisitos do TP (checklist)

### Estrutura do arquivo binário

- Cabeçalho com `int` armazenando o **último ID utilizado**.
- Cada registro deve ter:
  - **Lápide** (`byte`) indicando válido/excluído;
  - **Tamanho do registro** (`int`);
  - **Vetor de bytes** representando o objeto.

### Entidade/dados

A base escolhida deve permitir representar os seguintes tipos (caso não exista, pode ser criado um campo adicional):

- String de tamanho fixo
- String de tamanho variável
- Data
- Lista de valores (com separador)
- Inteiro ou Float

### CRUD Sequencial (menu no terminal)

- **Carga da base** (importação CSV/API/outro) → grava em binário.
- **Ler registro por ID** (varredura sequencial do arquivo).
- **Atualizar registro**:
  - se mantiver o mesmo tamanho: sobrescreve no lugar;
  - se mudar de tamanho: marca como excluído (lápide) e escreve novo no final.
- **Deletar registro por ID**: marca lápide.

### Ordenação Externa

No menu deve existir uma opção que receba:

- Número de caminhos
- Número máximo de registros em memória primária por bloco

A ordenação externa deve:

- Compactar o arquivo logicamente (remover “buracos” de registros deletados/atualizados);
- Gerar um novo arquivo ordenado para continuar as operações de CRUD.

## Como esta etapa se conecta às próximas

- O arquivo binário gerado aqui é a base para o **TP-2 (indexação)**.
- A estrutura consistente de registro também será usada no **TP-3 (compressão/casamento)** e **TP-4 (criptografia)**.
