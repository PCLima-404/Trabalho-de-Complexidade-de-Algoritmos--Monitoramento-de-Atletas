package br.edu.ifba.atletas.servidor.impl;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import br.edu.ifba.atletas.servidor.operacoes.Operacoes;

/*
 * Versão 2: com fila rotativa e sem execução de O(N²) no servidor.
 * O total de pares é recebido pronto do cliente — o servidor apenas acumula.
 */
public class OperacoesImpl implements Operacoes<Atleta, Leitura> {

    // (ii) Fila rotativa: limite máximo de leituras por atleta
    private static final int LIMIAR_ROTACIONAMENTO_LEITURAS = 40;

    private Map<Atleta, Queue<Leitura>> bancoDeDados  = new TreeMap<>();
    private Map<Atleta, Integer>        paresPorAtleta = new TreeMap<>();

    /*
     * Grava uma leitura para o atleta usando fila rotativa.
     *
     * Complexidade: O(1) amortizado.
     * Razão: acesso ao TreeMap em O(log N) com N=10 fixo; inserção e poll na
     * Queue (LinkedList) em O(1). Memória limitada a 10×40 = 400 leituras máximas.
     * Consequências: ao contrário da V1, a memória não cresce indefinidamente —
     * o servidor mantém apenas as leituras mais recentes de cada atleta.
     */
    @Override
    public void gravar(Atleta atleta, Leitura leitura) {
        Queue<Leitura> leituras;

        if (bancoDeDados.containsKey(atleta)) {
            leituras = bancoDeDados.get(atleta);
        } else {
            leituras = new LinkedList<>();
            bancoDeDados.put(atleta, leituras);
        }

        try { Thread.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }

        // (ii) Fila rotativa: descarta a mais antiga quando o limite é atingido
        if (leituras.size() > LIMIAR_ROTACIONAMENTO_LEITURAS) {
            leituras.poll();
            System.out.println("limite de rotacionamento atingido, última leitura descartada");
        }
        leituras.add(leitura);

        System.out.println("gravada nova leitura para o atleta: " + atleta);
    }

    /*
     * Recebe e acumula o total de pares calculado pelo cliente.
     * O servidor NÃO executa O(N²) — recebe apenas o inteiro resultante.
     *
     * Complexidade: O(1)
     * Razão: acesso e atualização de mapa com N=10 entradas fixas.
     * Consequências: o gargalo O(N²) da V1 é completamente eliminado do servidor.
     */
    @Override
    public void gravar(Atleta atleta, int pares) {
        System.out.println(pares > 0
            ? "pares informados pelo atleta " + atleta + ": " + pares
            : "nenhum par informado pelo atleta: " + atleta);

        if (paresPorAtleta.containsKey(atleta)) {
            pares += paresPorAtleta.get(atleta);
        }
        paresPorAtleta.put(atleta, pares);
    }

    /*
     * Soma todos os pares recebidos dos clientes.
     *
     * Complexidade: O(N) onde N = número de atletas (10, fixo) → O(1) efetivo.
     * Razão: um único laço sobre o mapa de tamanho fixo.
     */
    @Override
    public int detectarAltasOscilacoes() {
        int contador = 0;
        for (Integer pares : paresPorAtleta.values()) {
            contador += pares;
        }
        return contador;
    }

}
