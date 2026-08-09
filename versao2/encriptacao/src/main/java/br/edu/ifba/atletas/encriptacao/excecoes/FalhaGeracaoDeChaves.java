package br.edu.ifba.atletas.encriptacao.excecoes;

/*
 * Exceção lançada quando a geração de chaves criptográficas falha.
 *
 * Causas comuns: vídeo do aquário não encontrado, frame inválido,
 * algoritmo não suportado pela JVM, ou falha de I/O ao gravar as chaves.
 *
 * Complexidade do construtor: O(1) — apenas delega para Exception.
 */
public class FalhaGeracaoDeChaves extends Exception {

    /*
     * Complexidade: O(1) — chamada ao construtor da superclasse.
     *
     * @param mensagem descrição do erro ocorrido
     */
    public FalhaGeracaoDeChaves(String mensagem) {
        super(mensagem);
    }

}
