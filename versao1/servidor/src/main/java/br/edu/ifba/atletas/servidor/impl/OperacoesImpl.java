package br.edu.ifba.atletas.servidor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.edu.ifba.atletas.servidor.operacoes.Operacoes;

/*
 * Versão 1: sem fila rotativa e com O(N²) executado no servidor.
 * Problemas intencionais para contraste com a V2:
 *   1. Todas as leituras são acumuladas sem limite — memória cresce indefinidamente.
 *   2. detectarParesSimilares() roda O(N²) aqui, no servidor, sobre todos os dados.
 */
public class OperacoesImpl implements Operacoes<Atleta, Leitura> {

    // V1: lista sem limite de tamanho (sem fila rotativa)
    private Map<Atleta, List<Leitura>> bancoDeDados = new TreeMap<>();
    private Map<Atleta, Integer>       paresPorAtleta = new TreeMap<>();

    private static final double TOLERANCIA_SIMILARIDADE = 10.0;

    /*
     * Grava uma leitura para o atleta sem qualquer limite de armazenamento.
     *
     * Complexidade: O(1) amortizado.
     * Razão: acesso ao TreeMap em O(log N) com N=10 fixo; inserção na lista em O(1).
     * Consequências: sem limite de tamanho, a lista cresce indefinidamente ao longo
     * do tempo, consumindo memória de forma irrestrita.
     */
    @Override
    public void gravar(Atleta atleta, Leitura leitura) {
        List<Leitura> leituras;

        if (bancoDeDados.containsKey(atleta)) {
            leituras = bancoDeDados.get(atleta);
        } else {
            leituras = new ArrayList<>();
            bancoDeDados.put(atleta, leituras);
        }

        try { Thread.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }

        // V1: sem descarte — acumula tudo
        leituras.add(leitura);

        System.out.println("gravada nova leitura para o atleta: " + atleta);
    }

    /*
     * Registra o total de pares enviado pelo cliente (não usado na V1;
     * o cálculo é feito localmente em detectarParesSimilares).
     *
     * Complexidade: O(1)
     */
    @Override
    public void gravar(Atleta atleta, int pares) {
        if (paresPorAtleta.containsKey(atleta)) {
            pares += paresPorAtleta.get(atleta);
        }
        paresPorAtleta.put(atleta, pares);
    }

    /*
     * Detecta pares de leituras com desempenho similar (batimentos próximos).
     * Executado NO SERVIDOR na versão 1 — principal gargalo de complexidade.
     *
     * Complexidade: O(N²) onde N = total de leituras acumuladas de todos os atletas.
     * Razão: dois laços aninhados comparam cada par de leituras (i, j) com j > i,
     * resultando em N*(N-1)/2 comparações.
     * Consequências: com 10 atletas × 1000 leituras = 10.000 leituras totais,
     * há ~50 milhões de comparações executadas no servidor a cada chamada —
     * gargalo crítico que paralisa o serviço para entradas grandes.
     */
    @Override
    public int detectarParesSimilares() {
        List<Leitura> todas = new ArrayList<>();
        for (List<Leitura> lista : bancoDeDados.values()) {
            todas.addAll(lista);
        }

        int pares = 0;
        int n = todas.size();

        // O(N²): dois laços aninhados executados no servidor
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                double dif = Math.abs(
                    todas.get(i).getBatimentosCardiacos() -
                    todas.get(j).getBatimentosCardiacos()
                );
                if (dif <= TOLERANCIA_SIMILARIDADE) {
                    pares++;
                }
            }
        }

        System.out.println("pares similares detectados no servidor: " + pares
            + " (N=" + n + ", comparações=" + (n * (n - 1) / 2) + ")");
        return pares;
    }

}
