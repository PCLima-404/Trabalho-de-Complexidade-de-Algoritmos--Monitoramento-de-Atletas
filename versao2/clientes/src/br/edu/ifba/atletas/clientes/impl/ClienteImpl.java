package br.edu.ifba.atletas.clientes.impl;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifba.atletas.clientes.comunicacao.Cliente;
import br.edu.ifba.atletas.clientes.comunicacao.Resultado;
import br.edu.ifba.atletas.clientes.sensoriamento.Sensoriamento;

/*
 * Versão 2: com três otimizações em relação à V1:
 *   (i)  Limiar de envio: só envia leituras com variação significativa.
 *   (ii) O(N²) deslocado para o cliente: detectarParesSimilares() roda aqui;
 *        o servidor recebe apenas o inteiro resultante.
 *   (iii)Encriptação RSA: dados enviados com chave pública; servidor desencripta
 *        com chave privada gerada a partir do vídeo do aquário de água-vivas.
 */
public class ClienteImpl implements Cliente<Atleta, Leitura>, Runnable {

    private static final int TOTAL_DE_LEITURAS = 1000;

    private static final String URL_SERVIDOR = "http://localhost:8080";
    private static final String URL_ATLETAS  = URL_SERVIDOR + "/atletas/";

    private static final String ALGORITMO_ENCRIPTACAO = "RSA";

    // Ajuste este caminho para onde o módulo encriptacao gravou a chave pública
   private static final String CAMINHO_CHAVE_PUBLICA =
    "chave/ch_publica.chv";

    // (i) Limiares de envio
    private static final int LIMIAR_ENVIO_BATIMENTOS = 5;
    private static final int LIMIAR_ENVIO_PASSOS     = 5;

    // (ii) Limiares para detecção de alta oscilação
    private static final int LIMIAR_OSCILACOES_BATIMENTOS = 10;
    private static final int LIMIAR_OSCILACOES_PASSOS     = 10;

    private Atleta atleta                        = null;
    private Sensoriamento<Leitura> sensoriamento = null;
    private Leitura ultimaLeitura                = new Leitura(0, 0);
    private PublicKey chave                      = null;

    /*
     * Complexidade: O(B) onde B = bytes da chave pública (fixo ~160 bytes) → O(1).
     * Razão: lê o arquivo da chave e reconstrói o objeto PublicKey.
     */
    @Override
    public void configurar(Atleta atleta, Sensoriamento<Leitura> sensoriamento)
            throws Exception {
        this.atleta        = atleta;
        this.sensoriamento = sensoriamento;
        this.chave         = getChave();
    }

    /*
     * Lê a chave pública RSA do arquivo gerado pelo módulo encriptacao.
     *
     * Complexidade: O(B) onde B = tamanho do arquivo de chave. B fixo → O(1).
     */
    private PublicKey getChave() throws Exception {
        File arquivo = new File(CAMINHO_CHAVE_PUBLICA);
        FileInputStream stream = new FileInputStream(arquivo);
        byte[] bytes = stream.readAllBytes();
        stream.close();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(ALGORITMO_ENCRIPTACAO);
        return kf.generatePublic(spec);
    }

    /*
     * Encripta os dados com a chave pública RSA.
     *
     * Complexidade: O(M) onde M = tamanho dos dados. M fixo por leitura → O(1).
     * Consequências: intratabilidade — sem a chave privada o texto cifrado
     * não pode ser recuperado em tempo viável.
     */
    private byte[] encriptar(String dados) throws Exception {
        Cipher cifrador = Cipher.getInstance(ALGORITMO_ENCRIPTACAO);
        cifrador.init(Cipher.ENCRYPT_MODE, chave);
        return cifrador.doFinal(dados.getBytes());
    }

    /*
     * Verifica se a variação entre duas leituras ultrapassa os limiares.
     *
     * Complexidade: O(1) — apenas operações aritméticas.
     */
    @Override
    public boolean ocorreuAltaOscilacao(Leitura leituraAtual, Leitura ultimaLeitura,
            int limiarOscilacaoBatimentos, int limiarOscilacaoPassos) {
        int oscilacaoBatimentos = Math.abs(
            leituraAtual.getBatimentosCardiacos() - ultimaLeitura.getBatimentosCardiacos());
        int oscilacaoPassos = Math.abs(
            leituraAtual.getPassosPorMinuto() - ultimaLeitura.getPassosPorMinuto());

        return (oscilacaoBatimentos > limiarOscilacaoBatimentos
             || oscilacaoPassos    > limiarOscilacaoPassos);
    }

    /*
     * Envia uma leitura encriptada com RSA ao servidor.
     *
     * Complexidade: O(M) encriptação + O(1) HTTP = O(1) efetivo (M fixo por leitura).
     * Consequências: só chamado quando a leitura passou pelo limiar — reduz tráfego.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Resultado enviar(Leitura leitura) throws Exception {
        Map<String, String> json = new HashMap<>();
        json.put("id",        atleta.getIdentificacao());
        json.put("batimentos", leitura.getBatimentosCardiacos() + "");
        json.put("passos",     leitura.getPassosPorMinuto() + "");

        ObjectMapper mapeador = new ObjectMapper();
        String dadosBrutos = mapeador.writeValueAsString(json);
        System.out.println("leitura antes da encriptação: " + dadosBrutos);

        String dadosEncriptados = new String(
            Base64.getUrlEncoder().encode(encriptar(dadosBrutos)));
        System.out.println("leitura depois da encriptação: " + dadosEncriptados);

        URL urlEnvio = new URL(URL_ATLETAS + "leituras/" + dadosEncriptados);
        HttpURLConnection conexao = (HttpURLConnection) urlEnvio.openConnection();
        conexao.setRequestMethod("POST");
        if (conexao.getResponseCode() != 200) {
            throw new Exception("erro de conexão com o servidor");
        }
        conexao.disconnect();

        return Resultado.SUCESSO;
    }

    /*
     * Envia o total de pares similares calculado no cliente ao servidor.
     * O servidor recebe apenas o inteiro — não executa O(N²).
     *
     * Complexidade: O(1) — uma requisição com um inteiro encriptado.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Resultado enviar(int pares) throws Exception {
        Map<String, String> json = new HashMap<>();
        json.put("id",    atleta.getIdentificacao());
        json.put("pares", pares + "");

        ObjectMapper mapeador = new ObjectMapper();
        String dadosBrutos = mapeador.writeValueAsString(json);
        String dadosEncriptados = new String(
            Base64.getUrlEncoder().encode(encriptar(dadosBrutos)));

        URL urlEnvio = new URL(URL_ATLETAS + "pares/" + dadosEncriptados);
        HttpURLConnection conexao = (HttpURLConnection) urlEnvio.openConnection();
        conexao.setRequestMethod("POST");
        if (conexao.getResponseCode() != 200) {
            throw new Exception("erro de conexão com o servidor");
        }
        conexao.disconnect();

        return Resultado.SUCESSO;
    }

    /*
     * (ii) Detecta pares de leituras com batimentos similares — executado NO CLIENTE.
     *
     * Complexidade: O(N²) onde N = leituras que passaram pelo limiar.
     * Razão: dois laços aninhados comparam cada par (i, j) com j > i.
     * Otimização vs V1: roda distribuído em cada thread (cliente), não no servidor
     * central. O servidor recebe apenas o inteiro resultante.
     * Consequências: cada thread processa sua própria lista local de N leituras,
     * dividindo a carga total entre os 10 atletas/threads em paralelo.
     */
    private int detectarParesSimilares(List<Leitura> leituras) {
        int pares = 0;
        int n = leituras.size();

        // O(N²): dois laços aninhados
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                double dif = Math.abs(
                    leituras.get(i).getBatimentosCardiacos() -
                    leituras.get(j).getBatimentosCardiacos()
                );
                if (dif <= 10.0) {
                    pares++;
                }
            }
        }

        return pares;
    }

    /*
     * Método executado pela Thread do atleta.
     * Aplica as três otimizações: limiar, O(N²) no cliente e encriptação RSA.
     *
     * Complexidade: O(L) geração + O(L) iteração com filtro + O(E²) pares
     * onde L = total gerado e E = leituras que passaram pelo limiar (E ≤ L).
     * Na prática E << L, reduzindo significativamente o tráfego para o servidor.
     */
    @Override
    public void run() {
        List<Leitura> leituras    = sensoriamento.gerar(TOTAL_DE_LEITURAS);
        List<Leitura> enviadas    = new java.util.ArrayList<>();

        for (Leitura leitura : leituras) {
            int difBatimentos = Math.abs(
                leitura.getBatimentosCardiacos() - ultimaLeitura.getBatimentosCardiacos());
            int difPassos = Math.abs(
                leitura.getPassosPorMinuto() - ultimaLeitura.getPassosPorMinuto());

            // (i) Limiar: só envia se a variação for significativa
            if (difBatimentos > LIMIAR_ENVIO_BATIMENTOS
                    || difPassos > LIMIAR_ENVIO_PASSOS) {

                System.out.println("leitura e alta oscilação sendo enviadas (encriptadas)...");

                try {
                    enviar(leitura);

                    boolean temAltaOscilacao = ocorreuAltaOscilacao(
                        leitura, ultimaLeitura,
                        LIMIAR_OSCILACOES_BATIMENTOS,
                        LIMIAR_OSCILACOES_PASSOS);
                    enviar(temAltaOscilacao ? 1 : 0);

                    enviadas.add(leitura);
                    ultimaLeitura = leitura;

                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("não ocorreram diferenças significativas desde a última leitura");
            }
        }

        // (ii) O(N²) no cliente: calcula pares sobre as leituras enviadas
        int totalPares = detectarParesSimilares(enviadas);
        System.out.println("pares similares calculados no cliente (O(N²)): " + totalPares);

        try {
            enviar(totalPares);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
