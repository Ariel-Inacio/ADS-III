# TP-2 — CRUD com Indexação (Árvore B+, Hash Estendido e Lista Invertida)

## Objetivo

Evoluir o sistema do TP-1 para permitir **acesso eficiente aos registros**, adicionando **estruturas de indexação** ao arquivo binário já existente.

O sistema deve continuar oferecendo operações completas de CRUD, mas agora com suporte a:

- **Indexação por ID** — para localizar rapidamente registros no arquivo de dados sem varredura sequencial.
- **Indexação por termos/atributos** — para buscas por campos textuais e similares, quando aplicável.

## Requisitos do TP (checklist)

### Arquivos e consistência

- Manter o **arquivo de dados** (binário) com a estrutura de registros definida no TP-1 (lápide + tamanho + vetor de bytes).
- Criar e manter **arquivos de índice separados** do arquivo de dados.
- Garantir que as estruturas de índice sejam **persistentes** (salvas em disco) e reutilizáveis entre execuções.

### Estruturas de indexação

No menu, permitir escolher e usar pelo menos uma (idealmente todas) das opções abaixo:

#### Árvore B+

- Índice que mapeia `ID → endereço/offset no arquivo de dados`.
- Deve suportar **inserção**, **remoção** e **atualização** da entrada no índice.
- Deve manter o arquivo de índice da árvore atualizado em disco após cada operação.

#### Hashing Estendido

- Índice por hash com **crescimento dinâmico** (diretório extensível).
- Deve mapear `ID → endereço/offset no arquivo de dados`.
- Deve tratar colisões por meio de divisão de buckets conforme necessário.

#### Lista Invertida

- Estrutura para busca por **termos ou atributos** (ex.: título, categoria, nome, etc.).
- Deve mapear `termo → lista de IDs` (ou referências diretas).
- Deve suportar **atualização** quando registros forem modificados ou removidos.

### CRUD (menu no terminal)

- **Carga da base** (importação CSV/API/outro) → grava registros no binário e constrói o(s) índice(s) escolhido(s).
- **Ler registro por ID**:
  - usar o índice (Árvore B+ ou Hash) para localizar o offset e acessar diretamente o registro — sem varredura sequencial.
- **Pesquisar por termos/atributos**:
  - usar a Lista Invertida para retornar os IDs correspondentes ao termo buscado.
- **Atualizar registro**:
  - atualizar o arquivo de dados seguindo a regra de tamanho do TP-1 (sobrescrever ou mover para o final);
  - refletir a alteração nas estruturas de índice (reapontar offsets, atualizar listas invertidas, etc.).
- **Deletar registro**:
  - marcar lápide no arquivo de dados;
  - remover ou atualizar entradas correspondentes em todas as estruturas de índice mantidas.

### Persistência

- Todos os arquivos de índice devem ser gravados em disco ao final de cada operação (ou ao encerrar o programa).
- Ao iniciar o programa, as estruturas de índice devem ser **carregadas do disco** automaticamente.

## Como esta etapa se conecta às próximas

- A indexação criada aqui será complementada pela **compressão de dados no TP-3**, que poderá ser aplicada aos arquivos de dados e/ou de índice.
- O fluxo completo do sistema (CRUD + índice + compressão + busca) converge no **TP-4**.

