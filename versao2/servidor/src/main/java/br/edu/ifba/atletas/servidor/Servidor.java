package br.edu.ifba.atletas.servidor;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/*
 * Ponto de entrada do SERVIDOR - VERSÃO 2 (com otimizações e desencriptação RSA).
 *
 * Diferenças em relação à V1:
 *   - Recebe dados encriptados com RSA; desencripta com a chave privada.
 *   - NÃO executa O(N²): recebe apenas o inteiro resultante calculado no cliente.
 *   - Usa fila rotativa (máx. 40 leituras/atleta) — memória limitada e estável.
 *
 * O servidor Grizzly mantém internamente um pool de threads para atender
 * as requisições concorrentes dos 10 clientes em paralelo — cada requisição
 * HTTP recebida é despachada a uma thread disponível do pool automaticamente.
 *
 * Complexidade de iniciarServidor(): O(1)
 * Razão: configuração do container com custo fixo, sem laços sobre dados.
 *
 * Complexidade do main(): O(1)
 * Razão: inicia servidor e bloqueia aguardando ENTER — sem laços.
 * Consequências: servidor completamente livre do gargalo O(N²) da V1;
 * todas as operações de recebimento são O(1) ou O(M) com M fixo por leitura.
 */
public class Servidor {

    private static final String BASE_URL = "http://0.0.0.0:8080/";

    /*
     * Inicializa o container HTTP Grizzly com as rotas JAX-RS da V2.
     *
     * Complexidade: O(1)
     * Razão: operação de configuração com custo fixo.
     * Nota sobre threads: o Grizzly cria um pool interno de threads de I/O
     * proporcional ao número de CPUs disponíveis para atender requisições
     * paralelas dos 10 clientes/threads sem bloqueio.
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
        System.out.println("  SERVIDOR - MONITORAMENTO DE ATLETAS [VERSÃO 2] ");
        System.out.println("  Aguardando leituras em: " + BASE_URL);
        System.out.println("  Desencriptação RSA com chave privada ativa      ");
        System.out.println("  O(N²) ELIMINADO — recebe só resultado do cliente");
        System.out.println("  Pressione ENTER para encerrar.                  ");
        System.out.println("=================================================\n");
        System.in.read();
        servidor.shutdown();
    }
}
