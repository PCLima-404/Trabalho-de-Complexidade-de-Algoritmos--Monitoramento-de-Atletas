# Monitoramento de Atletas — Versão 1 (Sem Otimizações)

**Disciplina:** Complexidade de Algoritmos  
**Professor:** Luis Paulo da Silva Carvalho  
**Instituição:** IFBA — Campus Vitória da Conquista

---

## O que é esta versão

A Versão 1 é a implementação **de referência sem otimizações**, criada para evidenciar os problemas de complexidade que a Versão 2 resolve. Ela simula o monitoramento de 10 atletas via sistema cliente-servidor.

### Problemas intencionais desta versão

| Problema | Onde ocorre | Complexidade |
|---|---|---|
| Todas as leituras enviadas sem filtro | `ClienteImpl.run()` | O(L) envios totais |
| Dados em texto puro (sem encriptação) | `ClienteImpl.enviar()` | Interceptável |
| O(N²) executado no servidor | `OperacoesImpl.detectarParesSimilares()` | O(N²) |
| Memória cresce indefinidamente | `OperacoesImpl.gravar()` | O(L × tempo) |

---

## Estrutura de pastas

```
versao1/
├── clientes/          ← Projeto Maven do CLIENTE
│   ├── pom.xml
│   └── src/br/edu/ifba/atletas/clientes/
│       ├── App.java                        ← main() do cliente
│       ├── comunicacao/
│       │   ├── Cliente.java                ← interface de comunicação
│       │   └── Resultado.java              ← enum SUCESSO/FALHA
│       ├── impl/
│       │   ├── Atleta.java                 ← modelo do atleta
│       │   ├── ClienteImpl.java            ← lógica do cliente (sem otimizações)
│       │   ├── Leitura.java                ← modelo de uma leitura de sensor
│       │   └── SensoriamentoImpl.java      ← geração aleatória de leituras O(N)
│       └── sensoriamento/
│           └── Sensoriamento.java          ← interface de sensoriamento
│
└── servidor/          ← Projeto Maven do SERVIDOR
    ├── pom.xml
    └── src/main/java/br/edu/ifba/atletas/servidor/
        ├── Servidor.java                   ← main() do servidor
        ├── Rotas.java                      ← endpoints HTTP JAX-RS
        ├── impl/
        │   ├── Atleta.java
        │   ├── Leitura.java
        │   └── OperacoesImpl.java          ← O(N²) executado aqui
        └── operacoes/
            └── Operacoes.java              ← interface de operações
```

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Servidor rodando em `localhost:8080` antes de iniciar o cliente

---

## Como executar

### 1. Compilar e iniciar o SERVIDOR

```bash
cd versao1/servidor
mvn compile
mvn exec:java
```

O servidor ficará aguardando requisições em `http://localhost:8080`. Mantenha este terminal aberto.

### 2. Compilar e iniciar o CLIENTE (em outro terminal)

```bash
cd versao1/clientes
mvn compile
mvn exec:java
```

O cliente criará **10 threads** (uma por atleta) e enviará **1000 leituras por atleta** ao servidor.

### 3. Consultar o resultado do O(N²) no servidor

```bash
curl http://localhost:8080/atletas/pares
```

Retorna o total de pares de atletas com desempenho similar, calculado no servidor.

### 4. Encerrar o servidor

Pressione `ENTER` no terminal do servidor.

---

## Funcionalidades implementadas

### Cliente (`ClienteImpl.java`)

| Método | Complexidade | Descrição |
|---|---|---|
| `configurar()` | O(1) | Associa atleta e sensoriamento |
| `ocorreuAltaOscilacao()` | O(1) | Verifica variação entre leituras |
| `enviar(Leitura)` | O(1) | Envia leitura em texto puro |
| `enviar(int)` | O(1) | Envia total de pares |
| `run()` | O(L) | Gera e envia todas as leituras sem filtro |

### Sensoriamento (`SensoriamentoImpl.java`)

| Método | Complexidade | Descrição |
|---|---|---|
| `gerar(N)` | O(N) | Gera N leituras aleatórias de batimentos e passos |

### Servidor (`OperacoesImpl.java`)

| Método | Complexidade | Descrição |
|---|---|---|
| `gravar(Atleta, Leitura)` | O(1) amortizado | Acumula leituras sem limite |
| `gravar(Atleta, int)` | O(1) | Registra total de pares |
| `detectarParesSimilares()` | **O(N²)** | Gargalo principal — dois laços aninhados |

---

## Threads

O `App.java` cria **uma thread por atleta** (10 threads no total), conforme exigido pelo critério d.1 do enunciado. Cada thread executa `ClienteImpl.run()` de forma paralela e independente.

O servidor Grizzly gerencia internamente um pool de threads para atender as 10 threads do cliente em paralelo.

---

## Por que esta versão tem problemas

1. **Tráfego excessivo:** todas as 10.000 leituras (10 atletas × 1000) são transmitidas, incluindo as com variação mínima.
2. **O(N²) centralizado:** com 10.000 leituras, `detectarParesSimilares()` realiza ~50 milhões de comparações no servidor.
3. **Memória ilimitada:** sem fila rotativa, o servidor acumula leituras indefinidamente.
4. **Sem proteção:** dados em texto puro — qualquer interceptação expõe os dados completos.

Todos esses problemas são resolvidos na **Versão 2**.
