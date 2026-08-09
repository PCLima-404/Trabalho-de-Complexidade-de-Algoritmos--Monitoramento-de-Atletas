# Monitoramento de Atletas — Versão 2 (Com Otimizações e Encriptação RSA)

**Disciplina:** Complexidade de Algoritmos  
**Professor:** Luis Paulo da Silva Carvalho  
**Instituição:** IFBA — Campus Vitória da Conquista

---

## O que é esta versão

A Versão 2 resolve todos os problemas de complexidade identificados na Versão 1 por meio de três otimizações e adiciona uma camada de **encriptação RSA com aleatoriedade real** extraída do vídeo de aquário de água-vivas.

### As três otimizações

| Otimização | Onde | Impacto |
|---|---|---|
| **(i) Limiar de envio** | `ClienteImpl.run()` | Só transmite leituras com variação > limiar — reduz drasticamente o tráfego |
| **(ii) O(N²) no cliente** | `ClienteImpl.detectarParesSimilares()` | Distribuído entre as 10 threads; servidor recebe apenas um inteiro |
| **(iii) Fila rotativa** | `OperacoesImpl.gravar()` | Servidor mantém no máximo 40 leituras/atleta — memória estável |

### A encriptação

- **Algoritmo:** RSA (criptografia assimétrica)
- **Fonte de aleatoriedade:** frames do vídeo do aquário de água-vivas (`https://youtu.be/Ega1KWkngt8`)
- **Intratabilidade:** sem a chave privada, recuperar os dados exige fatorar o módulo RSA de 1024 bits — computacionalmente intratável

---

## Estrutura de pastas

```
versao2/
├── encriptacao/       ← Módulo gerador de chaves RSA (executar PRIMEIRO)
│   ├── pom.xml
│   ├── video/
│   │   └── aquario.mp4             ← COLOQUE O VÍDEO AQUI (ver instruções)
│   └── src/main/java/br/edu/ifba/atletas/encriptacao/
│       ├── App.java                ← main() do gerador de chaves
│       ├── aleatoriedade/
│       │   └── GeradorDeAleatoriedadeReal.java  ← lê frames do vídeo
│       ├── chaves/
│       │   └── GeradorDeChaves.java             ← interface do gerador
│       ├── encriptador/
│       │   └── Encriptador.java                 ← classe abstrata base
│       ├── excecoes/
│       │   ├── FalhaEncriptacao.java
│       │   └── FalhaGeracaoDeChaves.java
│       └── impl/
│           ├── EncriptadorImpl.java             ← encripta/desencripta RSA
│           └── GeradorDeChavesImpl.java         ← gera par de chaves RSA
│
├── clientes/          ← Projeto Maven do CLIENTE
│   ├── pom.xml
│   ├── chave/
│   │   └── ch_publica.chv          ← gerada pelo módulo encriptacao
│   └── src/br/edu/ifba/atletas/clientes/
│       ├── App.java                ← main() do cliente
│       ├── comunicacao/
│       │   ├── Cliente.java
│       │   └── Resultado.java
│       ├── impl/
│       │   ├── Atleta.java
│       │   ├── ClienteImpl.java    ← com limiar + O(N²) no cliente + RSA
│       │   ├── Leitura.java
│       │   └── SensoriamentoImpl.java
│       └── sensoriamento/
│           └── Sensoriamento.java
│
└── servidor/          ← Projeto Maven do SERVIDOR
    ├── pom.xml
    ├── chave/
    │   └── ch_privada.chv          ← gerada pelo módulo encriptacao
    └── src/main/java/br/edu/ifba/atletas/servidor/
        ├── Servidor.java           ← main() do servidor
        ├── Rotas.java              ← endpoints HTTP com desencriptação RSA
        ├── impl/
        │   ├── Atleta.java
        │   ├── Leitura.java
        │   └── OperacoesImpl.java  ← fila rotativa + sem O(N²)
        └── operacoes/
            └── Operacoes.java
```

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Conexão com a internet (para download do JavaCV na primeira compilação)
- O vídeo do aquário em `versao2/encriptacao/video/aquario.mp4`

### Como baixar o vídeo

Use o `yt-dlp` (recomendado) ou qualquer conversor online:

```bash
yt-dlp -o "versao2/encriptacao/video/aquario.mp4" \
  "https://youtu.be/Ega1KWkngt8"
```

---

## Como executar (ordem obrigatória)

### Passo 1 — Gerar as chaves RSA (uma única vez)

```bash
cd versao2/encriptacao
mvn compile
mvn exec:java
```

Este passo:
- Lê frames do vídeo do aquário como fonte de entropia real
- Pula um número aleatório de frames (0 a 99) para garantir imprevisibilidade
- Gera o par de chaves RSA de 1024 bits
- Salva `ch_publica.chv` na pasta do cliente
- Salva `ch_privada.chv` na pasta do servidor

### Passo 2 — Iniciar o SERVIDOR

```bash
cd versao2/servidor
mvn compile
mvn exec:java
```

O servidor ficará aguardando em `http://localhost:8080`. Mantenha este terminal aberto.

### Passo 3 — Executar o CLIENTE (outro terminal)

```bash
cd versao2/clientes
mvn compile
mvn exec:java
```

### Passo 4 — Consultar o total de pares

```bash
curl http://localhost:8080/atletas/pares
```

### Passo 5 — Encerrar o servidor

Pressione `ENTER` no terminal do servidor.

---

## Funcionalidades implementadas

### Cliente (`ClienteImpl.java`)

| Método | Complexidade | Descrição |
|---|---|---|
| `configurar()` | O(1) | Associa atleta, sensoriamento e carrega chave pública |
| `ocorreuAltaOscilacao()` | O(1) | Verifica variação entre leituras |
| `encriptar()` | O(M) → O(1) | Encripta com RSA; M fixo por leitura |
| `enviar(Leitura)` | O(1) efetivo | Encripta e transmite ao servidor |
| `enviar(int)` | O(1) | Encripta e envia resultado do O(N²) |
| `detectarParesSimilares()` | **O(N²)** | Roda no CLIENTE — não sobrecarrega o servidor |
| `run()` | O(L) + O(E²) | L geradas, E enviadas após limiar (E ≪ L), pares calculados localmente |

### Sensoriamento (`SensoriamentoImpl.java`)

| Método | Complexidade | Descrição |
|---|---|---|
| `gerar(N)` | O(N) | Gera N leituras aleatórias |

### Servidor (`OperacoesImpl.java`)

| Método | Complexidade | Descrição |
|---|---|---|
| `gravar(Atleta, Leitura)` | O(1) amortizado | Fila rotativa com máx. 40 leituras/atleta |
| `gravar(Atleta, int)` | O(1) | Registra inteiro calculado pelo cliente |
| `detectarAltasOscilacoes()` | O(N) → O(1) | Soma 10 inteiros — sem O(N²) |

### Módulo de Encriptação

| Classe | Complexidade | Descrição |
|---|---|---|
| `GeradorDeAleatoriedadeReal` | O(P) por frame | Extrai bytes de frames do vídeo do aquário |
| `GeradorDeChavesImpl.gerarChaves()` | O(K) → O(1) | Gera par RSA com entropia real |
| `EncriptadorImpl.encriptar()` | O(M) → O(1) | Encripta com chave pública RSA |
| `EncriptadorImpl.desencriptar()` | O(M) → O(1) | Desencripta com chave privada RSA |

---

## Como funciona a encriptação com aleatoriedade real

```
Vídeo aquário de água-vivas (MP4)
        ↓
GeradorDeAleatoriedadeReal
  → lê frames via FFmpeg/JavaCV
  → deslocamento aleatório (0-99 frames)
  → bytes dos pixels = entropia visual real
        ↓
GeradorDeChavesImpl
  → inicializa RSA KeyPairGenerator com entropia real
  → gera par de chaves RSA 1024 bits
        ↓
ch_publica.chv  →  CLIENTE encripta os dados
ch_privada.chv  →  SERVIDOR desencripta os dados
```

### Por que é intratável para um atacante

1. **Intercepta os dados:** recebe apenas bytes cifrados RSA — sem a chave privada não consegue decifrar.
2. **Tenta reproduzir a chave:** precisaria do arquivo exato do vídeo, do mesmo deslocamento de frames (0-99, escolhido aleatoriamente) e da mesma versão do JavaCV. Combinações impossíveis de enumerar na prática.
3. **Tenta fatorar a chave pública:** fatorar um módulo RSA de 1024 bits com hardware atual leva décadas — problema computacionalmente intratável.

---

## Threads

O `App.java` cria **uma thread por atleta** (10 threads no total), conforme exigido pelo critério d.1 do enunciado. Cada thread:
- Gera 1000 leituras
- Filtra pelo limiar (envia apenas as com variação significativa)
- Calcula seus próprios pares similares em O(N²) localmente
- Envia ao servidor apenas os dados filtrados (encriptados) e o resultado inteiro

O servidor Grizzly gerencia um pool de threads interno para atender as requisições paralelas.

---

## Comparativo com a Versão 1

| Aspecto | Versão 1 | Versão 2 |
|---|---|---|
| Leituras transmitidas | Todas (10.000) | Apenas as com variação > limiar |
| O(N²) | No servidor (gargalo) | No cliente (distribuído entre 10 threads) |
| Memória do servidor | Ilimitada (cresce sempre) | Estável (máx. 400 leituras totais) |
| Segurança dos dados | Texto puro — vulnerável | RSA com entropia real — intratável |
| Complexidade no servidor | O(N²) a cada consulta | O(1) por recebimento |
