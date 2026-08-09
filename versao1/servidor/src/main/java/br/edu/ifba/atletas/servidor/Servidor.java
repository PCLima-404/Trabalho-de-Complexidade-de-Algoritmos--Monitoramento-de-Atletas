package br.edu.ifba.atletas.servidor;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/*
 * Ponto de entrada do SERVIDOR - VERSÃO 1 (sem otimizações).
 *
 * O servidor Grizzly gerencia internamente um pool de threads para atender
 * requisições HTTP concorrentes — cada requisição recebida das 10 threads
 * de atletas é processada de forma paralela pelo container.
 * O cálculo de pares similares O(N²) é executado aqui, no servidor,
 * sobre TODAS as leituras acumuladas (sem fila rotativa).
 *
 * Complexidade de iniciarServidor(): O(1)
 * Razão: apenas instancia o container HTTP e registra o pacote de rotas.
 * O custo de atendimento de requisições é gerenciado pelo Grizzly.
 *
 * Complexidade do main(): O(1)
 * Razão: inicia o servidor e bloqueia aguardando ENTER — nenhum laço.
 * Consequências: o gargalo de complexidade está em Rotas.detectarPares()
 * que delega para OperacoesImpl.detectarParesSimilares() em O(N²).
 */
public class Servidor {

    private static final String BASE_URL = "http://0.0.0.0:8080/";

    /*
     * Inicializa o container HTTP Grizzly com as rotas JAX-RS.
     *
     * Complexidade: O(1)
     * Razão: operação de configuração com custo fixo, independente de dados.
     * Nota sobre threads: o Grizzly cria e gerencia um pool de threads
     * interno para paralelizar o atendimento das requisições dos 10 clientes.
     */
    private static HttpServer iniciarServidor() {
        ResourceConfig configuracao = new ResourceConfig()
            .packages("br.edu.ifba.atletas.servidor");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URL), configuracao);
    }

    /*
     * Complexidade: O(1) — inicia servidor e aguarda sinal de encerramento.
     */
    public static void main(String[] args) throws IOException {
        HttpServer servidor = iniciarServidor();
        System.out.println("=================================================");
        System.out.println("  SERVIDOR - MONITORAMENTO DE ATLETAS [VERSÃO 1] ");
        System.out.println("  Aguardando leituras em: " + BASE_URL);
        System.out.println("  O(N²) executado no SERVIDOR (sem otimização)   ");
        System.out.println("  Pressione ENTER para encerrar.                 ");
        System.out.println("=================================================\n");
        System.in.read();
        servidor.shutdown();
    }
}
