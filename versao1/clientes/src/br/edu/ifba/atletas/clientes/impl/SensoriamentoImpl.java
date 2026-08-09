package br.edu.ifba.atletas.clientes.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.edu.ifba.atletas.clientes.sensoriamento.Sensoriamento;

/*
 * Implementação do sensoriamento — gera leituras simuladas aleatoriamente.
 * Simula sensores de batimentos cardíacos e passos por minuto de atletas.
 *
 * Usada em AMBAS as versões (V1 e V2) sem alteração.
 */
public class SensoriamentoImpl implements Sensoriamento<Leitura> {

    private static final int BATIMENTOS_NORMAL       = 120;
    private static final int PASSOS_NORMAL           = 160;
    private static final int OSCILACAO_MAXIMA_BATIMENTOS = 20;
    private static final int OSCILACAO_MAXIMA_PASSOS     = 20;

    /*
     * Gera N leituras com valores aleatórios de batimentos e passos.
     *
     * Complexidade: O(N) onde N = totalLeituras.
     * Razão: um único laço itera N vezes, cada iteração em O(1).
     * Consequências: custo cresce linearmente com o número de leituras
     * solicitado — sem degradação exponencial ou quadrática.
     *
     * @param totalLeituras quantidade de leituras a gerar
     * @return lista com N objetos Leitura com valores randomizados
     */
    @Override
    public List<Leitura> gerar(int totalLeituras) {
        List<Leitura> leituras = new ArrayList<>();
        Random randomizador = new Random();

        // O(N): um laço, cada iteração O(1)
        for (int i = 0; i < totalLeituras; i++) {
            int oscilacao = randomizador.nextInt(OSCILACAO_MAXIMA_BATIMENTOS);
            int batimentos = randomizador.nextBoolean()
                ? BATIMENTOS_NORMAL + oscilacao
                : BATIMENTOS_NORMAL - oscilacao;

            oscilacao = randomizador.nextInt(OSCILACAO_MAXIMA_PASSOS);
            int passos = randomizador.nextBoolean()
                ? PASSOS_NORMAL + oscilacao
                : PASSOS_NORMAL - oscilacao;

            leituras.add(new Leitura(batimentos, passos));
        }

        return leituras;
    }

}
