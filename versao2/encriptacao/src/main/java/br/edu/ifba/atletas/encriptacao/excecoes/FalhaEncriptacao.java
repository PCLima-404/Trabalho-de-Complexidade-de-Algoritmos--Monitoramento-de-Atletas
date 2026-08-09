package br.edu.ifba.atletas.encriptacao.excecoes;

/*
 * Exceção lançada quando a encriptação ou desencriptação de dados falha.
 *
 * Causas comuns: chave incorreta, dados corrompidos, algoritmo inválido,
 * ou tamanho de bloco excedido (RSA 1024 bits suporta até 117 bytes/bloco).
 *
 * Complexidade do construtor: O(1) — apenas delega para Exception.
 */
public class FalhaEncriptacao extends Exception {

    /*
     * Complexidade: O(1) — chamada ao construtor da superclasse.
     *
     * @param mensagem descrição do erro de encriptação ocorrido
     */
    public FalhaEncriptacao(String mensagem) {
        super(mensagem);
    }

}
