package br.edu.ifba.atletas.clientes;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifba.atletas.clientes.impl.Atleta;
import br.edu.ifba.atletas.clientes.impl.ClienteImpl;
import br.edu.ifba.atletas.clientes.impl.SensoriamentoImpl;

/*
 * Ponto de entrada da aplicação CLIENTE - VERSÃO 1 (sem otimizações).
 *
 * Todas as leituras são enviadas sem filtro de limiar e em texto puro,
 * sem encriptação. O cálculo de pares similares (O(N²)) é executado
 * no SERVIDOR, não aqui.
 *
 * Complexidade do main(): O(N)
 * Razão: um único laço cria e inicia N threads (N = TOTAL_ATLETAS = 10).
 * O join() subsequente é igualmente O(N). Constante na prática.
 * Consequências: o custo aqui é mínimo; o gargalo real está no servidor
 * ao executar detectarParesSimilares() em O(N²) sobre todas as leituras.
 */
public class App {

    private static final int TOTAL_ATLETAS = 10;

    /*
     * Complexidade: O(N) onde N = TOTAL_ATLETAS (10, fixo).
     * Razão: dois laços sequenciais de tamanho N — criação de threads e join.
     * Consequências: inicialização rápida, independente do volume de leituras.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  CLIENTE - MONITORAMENTO DE ATLETAS [VERSÃO 1]  ");
        System.out.println("  Sem otimizações — todas as leituras enviadas   ");
        System.out.println("  Dados em texto puro — sem encriptação          ");
        System.out.println("=================================================\n");

        List<Thread> processos = new ArrayList<>();

        // O(N): cria 1 thread por atleta — requisito d.1 do enunciado
        for (int i = 0; i < TOTAL_ATLETAS; i++) {
            String id = "ATL-" + String.format("%02d", i + 1);

            ClienteImpl cliente = new ClienteImpl();
            cliente.configurar(new Atleta(id, "modelo"), new SensoriamentoImpl());

            Thread processo = new Thread(cliente);
            processos.add(processo);
            processo.start();
        }

        // O(N): aguarda todas as threads finalizarem
        for (Thread processo : processos) {
            processo.join();
        }

        System.out.println("\n[CLIENTE V1] Todas as leituras enviadas (sem filtro, sem encriptação).");
    }
}
