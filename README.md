# Monitoramento de Atletas — Trabalho Final de Complexidade de Algoritmos

**Disciplina:** Complexidade de Algoritmos
**Professor:** Luis Paulo da Silva Carvalho
**Instituição:** IFBA — Campus Vitória da Conquista

---

## 1. Visão geral do trabalho

O projeto simula um sistema **cliente-servidor de monitoramento de atletas em tempo real**: 10 atletas (10 threads independentes) geram leituras simuladas de sensores (batimentos cardíacos e passos por minuto) e as transmitem via HTTP para um servidor central, que deve identificar pares de atletas com desempenho similar.

O trabalho é entregue em **duas versões lado a lado**, propositalmente construídas para contar a mesma história duas vezes:

| | Versão 1 | Versão 2 |
|---|---|---|
| Pasta | `versao1/` | `versao2/` |
| Papel | Implementação **de referência**, sem otimizações — existe só para expor os gargalos | Implementação **otimizada**, resolve cada gargalo da V1 e adiciona RSA |
| Ideia central | "Aqui está o que acontece se você não pensar em complexidade" | "Aqui está como cada problema se resolve, com prova de complexidade" |

Ou seja, o objetivo pedagógico do trabalho não é só "fazer funcionar", e sim **demonstrar, com código executável e comparável, o impacto de decisões de complexidade algorítmica** em um sistema distribuído real (rede, threads, memória e segurança).

---

## 2. O problema simulado

- 10 atletas, cada um com sua própria **thread** (`ATL-01` a `ATL-10`).
- Cada atleta gera **1000 leituras** simuladas de sensor (batimentos cardíacos + passos/minuto), com pequeno atraso entre elas (`Thread.sleep(50)`), imitando um sensoriamento contínuo.
- As leituras são enviadas por HTTP para um servidor central (JAX-RS / Grizzly, em `localhost:8080`).
- O servidor precisa responder à pergunta: **quantos pares de atletas têm desempenho similar?** — o clássico problema de comparar todo mundo com todo mundo, que é O(N²) por natureza.

A diferença entre as duas versões está inteiramente em **onde e como** esse trabalho é feito.

---

## 3. Versão 1 — sem otimizações (`versao1/`)

Implementação ingênua, usada como baseline. Ela evidencia quatro problemas clássicos de complexidade/arquitetura:

| Problema | Onde | Complexidade | Efeito |
|---|---|---|---|
| Todas as leituras são enviadas, sem filtro | `ClienteImpl.run()` | O(L) envios por atleta (L=1000) | 10.000 requisições HTTP no total, mesmo as leituras com variação irrelevante |
| Dados trafegam em **texto puro** | `ClienteImpl.enviar()` | — | Qualquer interceptação de rede expõe os dados |
| Cálculo de pares similares roda **no servidor**, sobre tudo que chegou | `OperacoesImpl.detectarParesSimilares()` | **O(N²)** | Com 10.000 leituras acumuladas, ~50 milhões de comparações centralizadas |
| Servidor acumula leituras **sem limite** | `OperacoesImpl.gravar()` | O(1) amortizado, mas memória cresce sem teto | Memória do servidor cresce indefinidamente enquanto o sistema roda |

Arquitetura (dois projetos Maven independentes):

```
versao1/
├── clientes/   → simula os 10 atletas (thread por atleta)
└── servidor/   → recebe leituras em texto puro e calcula O(N²)
```

Endpoints do servidor (JAX-RS, prefixo `/atletas`):
- `GET /atletas/` — informações do serviço
- `POST /atletas/leituras/{dados}` — recebe uma leitura em JSON (texto puro) via path param
- `GET /atletas/pares` — dispara `detectarParesSimilares()` em O(N²) sobre todas as leituras acumuladas

---

## 4. Versão 2 — otimizada + encriptação RSA (`versao2/`)

Mesma simulação, mas resolvendo cada um dos quatro problemas da V1, mais uma camada de **encriptação RSA com aleatoriedade real**.

### 4.1 As três otimizações de complexidade

| # | Otimização | Onde | O que muda |
|---|---|---|---|
| i | **Limiar de envio** | `ClienteImpl.run()` via `ocorreuAltaOscilacao()` | Só é transmitida a leitura cujo desvio (batimentos ou passos) passa de um limiar. Reduz drasticamente E (enviadas) em relação a L (geradas), já que E ≪ L |
| ii | **O(N²) movido para o cliente** | `ClienteImpl.detectarParesSimilares()` | Cada uma das 10 threads calcula seus próprios pares localmente; o servidor deixa de receber leituras cruas e passa a receber só um inteiro por atleta — o trabalho O(N²) é **distribuído**, não eliminado, mas sai do caminho crítico do servidor |
| iii | **Fila rotativa no servidor** | `OperacoesImpl.gravar()` | O servidor mantém no máximo 40 leituras por atleta (memória com teto fixo, ~400 no total), em vez de crescer sem limite |

### 4.2 A camada de encriptação (RSA com entropia real)

- **Algoritmo:** RSA assimétrico, chaves de 1024 bits.
- **Fonte de aleatoriedade:** em vez de um `SecureRandom` comum, a semente vem de **frames de um vídeo real** (um aquário de água-vivas, `https://youtu.be/Ega1KWkngt8`), lidos via JavaCV/FFmpeg.
  - `GeradorDeAleatoriedadeReal` pula um número aleatório de frames (0–99) e extrai bytes de pixel como fonte de entropia visual.
  - `GeradorDeChavesImpl` usa essa entropia para inicializar o gerador de par de chaves RSA.
- **Fluxo de chaves:** o módulo `encriptacao/` gera o par uma única vez e distribui:
  - `ch_publica.chv` → vai para `clientes/chave/` (cliente encripta)
  - `ch_privada.chv` → vai para `servidor/chave/` (servidor desencripta)
- **Por que é intratável para um atacante:**
  1. Interceptar o tráfego só entrega bytes cifrados RSA — sem a chave privada, nada pode ser lido.
  2. Reproduzir a chave exigiria o arquivo exato do vídeo, o mesmo deslocamento de frames (escolhido aleatoriamente entre 0 e 99) e a mesma versão do decodificador — praticamente impossível de reconstruir.
  3. Fatorar diretamente o módulo RSA de 1024 bits é computacionalmente inviável com hardware atual.

### 4.3 Arquitetura (três projetos Maven)

```
versao2/
├── encriptacao/   → gera o par de chaves RSA a partir do vídeo (roda uma vez, antes de tudo)
├── clientes/       → 10 threads: geram leituras, filtram por limiar, calculam O(N²) local, encriptam e enviam
└── servidor/        → desencripta, aplica fila rotativa, soma os inteiros recebidos
```

Endpoints do servidor (mesmo formato da V1, mas com desencriptação):
- `GET /atletas/` — informações do serviço
- `POST /atletas/leituras/{dados}` — recebe leitura **encriptada com RSA**, desencripta com a chave privada e grava na fila rotativa
- `GET /atletas/pares` — soma os inteiros já calculados pelos clientes (O(N) sobre 10 valores, na prática O(1))

---

## 5. Comparativo direto V1 vs V2

| Aspecto | Versão 1 | Versão 2 |
|---|---|---|
| Leituras transmitidas | Todas (10.000) | Só as que passam do limiar (E ≪ 10.000) |
| Onde roda o O(N²) | No servidor (gargalo central) | No cliente, distribuído entre 10 threads |
| Memória do servidor | Cresce sem limite | Estável, com teto fixo (~400 leituras) |
| Segurança dos dados | Texto puro, sem proteção | RSA 1024 bits com entropia real do vídeo |
| Custo por consulta ao servidor | O(N²) a cada `GET /atletas/pares` | O(1)/O(N) trivial (soma de 10 inteiros) |

---

## 6. Threads e concorrência

Em ambas as versões, `App.java` cria **exatamente uma thread por atleta** (10 threads no total) — requisito explícito do enunciado (critério d.1). Cada thread roda `ClienteImpl.run()` de forma independente e paralela. Do lado do servidor, o container HTTP Grizzly gerencia seu próprio pool de threads para atender as requisições concorrentes vindas dos 10 clientes.

Na V2, cada thread de atleta faz mais trabalho localmente antes de falar com o servidor: gera as leituras, filtra pelo limiar, calcula seus próprios pares similares (O(N²) local) e só então encripta e envia — o paralelismo entre as 10 threads é o que torna esse O(N²) "distribuído" barato na prática, mesmo sem mudar a ordem de complexidade do algoritmo em si.

---

## 7. Pré-requisitos gerais

- Java 17+
- Maven 3.8+
- Para a Versão 2: conexão à internet (download do JavaCV na primeira compilação) e o vídeo do aquário salvo em `versao2/encriptacao/video/aquario.mp4` (já incluído neste pacote)

---

## 8. Como executar

### Versão 1 (dois terminais)

```bash
# Terminal 1 — servidor
cd versao1/servidor
mvn compile
mvn exec:java

# Terminal 2 — cliente (10 threads, envia tudo em texto puro)
cd versao1/clientes
mvn compile
mvn exec:java

# Consultar o resultado do O(N²) calculado no servidor
curl http://localhost:8080/atletas/pares
```

### Versão 2 (ordem obrigatória, três terminais)

```bash
# Passo 1 — gerar as chaves RSA a partir do vídeo (uma única vez)
cd versao2/encriptacao
mvn compile
mvn exec:java

# Passo 2 — servidor
cd versao2/servidor
mvn compile
mvn exec:java

# Passo 3 — cliente (filtra por limiar, calcula O(N²) local, encripta e envia)
cd versao2/clientes
mvn compile
mvn exec:java

# Consultar o total de pares (soma trivial dos inteiros recebidos)
curl http://localhost:8080/atletas/pares
```

Em ambas as versões, encerre o servidor pressionando `ENTER` no terminal correspondente.

> Cada versão também tem seu próprio `README.md` (`versao1/README.md` e `versao2/README.md`) com a estrutura de pastas completa, tabelas de complexidade por método e detalhes de execução — este README no nível raiz existe para dar a visão do trabalho como um todo e comparar as duas versões diretamente.

---

## 9. Stack técnica

- **Linguagem:** Java 17
- **Build:** Maven (projetos independentes por módulo, cada um com seu `pom.xml`)
- **Servidor HTTP:** Grizzly + Jersey (JAX-RS)
- **Serialização:** Jackson (`jackson-databind`)
- **Criptografia:** `javax.crypto` / RSA nativo do Java, com entropia extraída via JavaCV (FFmpeg bindings) na V2
- **Concorrência:** `java.lang.Thread` puro (uma thread por atleta) no cliente; pool de threads gerenciado pelo Grizzly no servidor
