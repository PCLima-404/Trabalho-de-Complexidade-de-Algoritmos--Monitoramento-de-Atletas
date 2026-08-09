package br.edu.ifba.atletas.clientes.impl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifba.atletas.clientes.comunicacao.Cliente;
import br.edu.ifba.atletas.clientes.comunicacao.Resultado;
import br.edu.ifba.atletas.clientes.sensoriamento.Sensoriamento;

/*
 * Versão 1: sem otimizações e sem encriptação.
 * Todas as leituras são enviadas ao servidor em texto puro.
 * O cálculo de pares de desempenho similar (O(N²)) é executado no servidor.
 *
 * Complexidade geral do run(): O(L)
 * Razão: L leituras geradas, todas enviadas sem filtro de limiar.
 * Consequências: o servidor recebe todas as leituras, inclusive as de
 * variação mínima, desperdiçando largura de banda e processando dados redundantes.
 */
public class ClienteImpl implements Cliente<Atleta, Leitura>, Runnable {

    private static final int TOTAL_DE_LEITURAS = 1000;

    private static final String URL_SERVIDOR = "http://localhost:8080";
    private static final String URL_ATLETAS  = URL_SERVIDOR + "/atletas/";

    private Atleta atleta                      = null;
    private Sensoriamento<Leitura> sensoriamento = null;

    /*
     * Complexidade: O(1)
     * Razão: apenas atribuição de referências.
     */
    @Override
    public void configurar(Atleta atleta, Sensoriamento<Leitura> sensoriamento) throws Exception {
        this.atleta        = atleta;
        this.sensoriamento = sensoriamento;
    }

    /*
     * Verifica se a variação entre duas leituras ultrapassa os limiares informados.
     *
     * Complexidade: O(1)
     * Razão: apenas subtrações e comparações de valores inteiros.
     */
    @Override
    public boolean ocorreuAltaOscilacao(Leitura leituraAtual, Leitura ultimaLeitura,
            int limiarOscilacaoBatimentos, int limiarOscilacaoPassos) {
        int oscilacaoBatimentos = Math.abs(leituraAtual.getBatimentosCardiacos()
            - ultimaLeitura.getBatimentosCardiacos());
        int oscilacaoPassos = Math.abs(leituraAtual.getPassosPorMinuto()
            - ultimaLeitura.getPassosPorMinuto());

        return (oscilacaoBatimentos > limiarOscilacaoBatimentos
             || oscilacaoPassos    > limiarOscilacaoPassos);
    }

    /*
     * Envia uma leitura ao servidor em texto puro (sem encriptação).
     *
     * Complexidade: O(1)
     * Razão: uma única requisição HTTP por chamada.
     * Consequências: chamado L vezes sem filtro = O(L) total por thread,
     * incluindo leituras com variação mínima que seriam descartadas na V2.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Resultado enviar(Leitura leitura) throws Exception {
        Map<String, String> json = new HashMap<>();
        json.put("id", atleta.getIdentificacao());
        json.put("batimentos", leitura.getBatimentosCardiacos() + "");
        json.put("passos", leitura.getPassosPorMinuto() + "");

        ObjectMapper mapeador = new ObjectMapper();
        // V1: dados em texto puro na URL, sem encriptação
        String payload = mapeador.writeValueAsString(json);
        URL urlEnvio = new URL(URL_ATLETAS + "leituras/" + java.net.URLEncoder.encode(payload, "UTF-8"));

        HttpURLConnection conexao = (HttpURLConnection) urlEnvio.openConnection();
        conexao.setRequestMethod("POST");
        if (conexao.getResponseCode() != 200) {
            throw new Exception("erro de conexão com o servidor");
        }
        conexao.disconnect();

        return Resultado.SUCESSO;
    }

    /*
     * Envia o total de pares de desempenho similar ao servidor.
     * Na V1 este valor é calculado no SERVIDOR (não aqui); este método
     * não é utilizado no run() da versão 1.
     *
     * Complexidade: O(1)
     */
    @SuppressWarnings("deprecation")
    @Override
    public Resultado enviar(int pares) throws Exception {
        Map<String, String> json = new HashMap<>();
        json.put("id", atleta.getIdentificacao());
        json.put("pares", pares + "");

        ObjectMapper mapeador = new ObjectMapper();
        String payload = mapeador.writeValueAsString(json);
        URL urlEnvio = new URL(URL_ATLETAS + "pares/" + java.net.URLEncoder.encode(payload, "UTF-8"));

        HttpURLConnection conexao = (HttpURLConnection) urlEnvio.openConnection();
        conexao.setRequestMethod("POST");
        if (conexao.getResponseCode() != 200) {
            throw new Exception("erro de conexão com o servidor");
        }
        conexao.disconnect();

        return Resultado.SUCESSO;
    }

    /*
     * Método executado pela Thread do atleta.
     * Envia TODAS as leituras geradas sem qualquer filtro de limiar.
     *
     * Complexidade: O(L) geração + O(L) envio = O(L)
     * Razão: dois laços sequenciais sobre L leituras.
     * Consequências: sem limiar, 100% das leituras são transmitidas,
     * incluindo variações mínimas — gera tráfego desnecessário e o servidor
     * acumula dados redundantes indefinidamente (sem fila rotativa).
     */
    @Override
    public void run() {
        List<Leitura> leituras = sensoriamento.gerar(TOTAL_DE_LEITURAS);

        for (Leitura leitura : leituras) {
            System.out.println("leitura sendo enviada sem filtro...");

            try {
                enviar(leitura);
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
