package br.edu.ifba.atletas.encriptacao.encriptador;

import java.security.KeyPair;

import br.edu.ifba.atletas.encriptacao.excecoes.FalhaEncriptacao;

/*
 * Classe abstrata base para encriptadores assimétricos.
 *
 * Define o contrato de encriptação com par de chaves (pública/privada).
 * Implementada por EncriptadorImpl com algoritmo RSA.
 */
public abstract class Encriptador {

    protected KeyPair chaves = null;
    protected String algoritmoDeEncriptacao = null;

    /*
     * Inicializa o encriptador com o par de chaves e o algoritmo.
     *
     * Complexidade: O(1)
     * Razão: apenas atribuições de referências, sem operações sobre dados.
     *
     * @param chaves par de chaves pública/privada gerado pelo GeradorDeChavesImpl
     * @param algoritmoDeEncriptacao nome do algoritmo (ex: "RSA")
     */
    public Encriptador(KeyPair chaves, String algoritmoDeEncriptacao) {
        this.chaves = chaves;
        this.algoritmoDeEncriptacao = algoritmoDeEncriptacao;
    }

    /*
     * Encripta os dados com a chave pública.
     * Complexidade: O(M) onde M = tamanho dos dados (M fixo por leitura → O(1)).
     * Ver EncriptadorImpl para análise detalhada.
     */
    public abstract String encriptar(String dados) throws FalhaEncriptacao;

    /*
     * Desencripta os dados com a chave privada.
     * Complexidade: O(M) onde M = tamanho dos dados (M fixo por leitura → O(1)).
     * Ver EncriptadorImpl para análise detalhada.
     */
    public abstract String desencriptar(String encriptacao) throws FalhaEncriptacao;

}
