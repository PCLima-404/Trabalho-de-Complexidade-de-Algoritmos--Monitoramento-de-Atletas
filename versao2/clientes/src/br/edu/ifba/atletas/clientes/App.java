package br.edu.ifba.atletas.clientes;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifba.atletas.clientes.impl.Atleta;
import br.edu.ifba.atletas.clientes.impl.ClienteImpl;
import br.edu.ifba.atletas.clientes.impl.SensoriamentoImpl;

/*
 * Ponto de entrada da aplicação CLIENTE - VERSÃO 2 (com otimizações e encriptação RSA).
 *
 * Otimizações aplicadas em relação à V1:
 *   (i)  Limiar de envio: só transmite leituras com variação significativa.
 *   (ii) O(N²) deslocado do servidor para o cliente: cada thread calcula
 *        seus próprios pares similares e envia apenas o inteiro resultante.
 *   (iii)Encriptação RSA: dados transmitidos com chave pública gerada a partir
 *        de frames do vídeo do aquário de água-vivas (aleatoriedade real).
 *
 * Complexidade do main(): O(N) onde N = TOTAL_ATLETAS (10, fixo).
 * Razão: dois laços sequenciais de tamanho N — criação/início de threads e join.
 * Consequências: inicialização em tempo constante na prática; o custo O(N²)
 * é distribuído entre as 10 threads dos clientes, não concentrado no servidor.
 */
public class App {

    private static final int TOTAL_ATLETAS = 10;

    /*
     * Complexidade: O(N) onde N = TOTAL_ATLETAS (10, fixo → O(1) prático).
     * Razão: dois laços de tamanho N — start das threads e join.
     * Consequências: custo fixo de inicialização; threads executam em paralelo.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  CLIENTE - MONITORAMENTO DE ATLETAS [VERSÃO 2]  ");
        System.out.println("  Otimizações: limiar + O(N²) no cliente         ");
        System.out.println("  Encriptação RSA com entropia real (aquário)     ");
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

        System.out.println("\n[CLIENTE V2] Concluído — leituras enviadas com otimizações e encriptação RSA.");
    }
}
