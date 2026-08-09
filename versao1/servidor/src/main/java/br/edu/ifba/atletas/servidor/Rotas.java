package br.edu.ifba.atletas.servidor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifba.atletas.servidor.impl.Atleta;
import br.edu.ifba.atletas.servidor.impl.Leitura;
import br.edu.ifba.atletas.servidor.impl.OperacoesImpl;
import br.edu.ifba.atletas.servidor.operacoes.Operacoes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/*
 * Versão 1: recebe dados em texto puro (sem desencriptação).
 * O cálculo de pares de desempenho similar (O(N²)) é executado aqui no servidor.
 */
@Path("atletas")
public class Rotas {

    private static Operacoes<Atleta, Leitura> operacoes = null;

    private static Operacoes<Atleta, Leitura> getOperacoes() {
        if (operacoes == null) {
            operacoes = new OperacoesImpl();
        }
        return operacoes;
    }

    private static final String INFORMACOES = "serviço de atendimento a atletas, v1.0";

    /*
     * Complexidade: O(1)
     */
    @GET
    @Path("/")
    public Response getInformacoes() {
        return Response.ok(INFORMACOES, MediaType.TEXT_PLAIN).build();
    }

    /*
     * Recebe e grava uma leitura em texto puro.
     *
     * Complexidade: O(1) por chamada (amortizado).
     * Consequências: chamado N*L vezes sem filtro, o servidor acumula
     * todas as leituras sem limite de memória.
     */
    @POST
    @Path("/leituras/{dados}")
    public Response gravarLeitura(@PathParam("dados") String dados) {
        Response resposta = Response.serverError().build();

        System.out.println("dados recebidos (texto puro): " + dados);

        try {
            String json = java.net.URLDecoder.decode(dados, "UTF-8");

            ObjectMapper mapeador = new ObjectMapper();
            JsonNode dic = mapeador.readTree(json);

            Atleta atleta = new Atleta(dic.get("id").asText(), "modelo");
            Leitura leitura = new Leitura(
                dic.get("batimentos").asInt(),
                dic.get("passos").asInt()
            );

            getOperacoes().gravar(atleta, leitura);
            resposta = Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resposta;
    }

    /*
     * Executa o cálculo de pares de desempenho similar no servidor — O(N²).
     * Este é o principal gargalo da versão 1.
     *
     * Complexidade: O(N²) onde N = total de leituras acumuladas.
     * Consequências: cresce quadraticamente com o volume de dados;
     * para muitas leituras o servidor fica sobrecarregado.
     */
    @GET
    @Path("/pares")
    public Response detectarPares() {
        int pares = getOperacoes().detectarParesSimilares();
        return Response.ok(pares + "", MediaType.TEXT_PLAIN).build();
    }

}
