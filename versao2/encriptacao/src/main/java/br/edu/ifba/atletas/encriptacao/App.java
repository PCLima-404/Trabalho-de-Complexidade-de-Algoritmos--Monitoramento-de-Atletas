package br.edu.ifba.atletas.encriptacao;

import java.security.SecureRandom;

import br.edu.ifba.atletas.encriptacao.aleatoriedade.GeradorDeAleatoriedadeReal;
import br.edu.ifba.atletas.encriptacao.chaves.GeradorDeChaves;
import br.edu.ifba.atletas.encriptacao.impl.GeradorDeChavesImpl;

/*
 * Gerador de chaves RSA a partir do vídeo do aquário de água-vivas.
 * EXECUTAR ANTES do servidor e do cliente da Versão 2.
 *
 * INSTRUÇÕES:
 *   1. Baixe o vídeo de https://youtu.be/Ega1KWkngt8 (ex: via yt-dlp ou
 *      qualquer conversor online) e salve em:
 *        versao2/encriptacao/video/aquario.mp4
 *   2. Ajuste CAMINHO_DO_VIDEO abaixo se necessário.
 *   3. Execute: mvn exec:java  (dentro da pasta versao2/encriptacao/)
 *   4. Serão gerados dois arquivos:
 *        versao2/clientes/chave/ch_publica.chv   → usada pelo CLIENTE para encriptar
 *        versao2/servidor/chave/ch_privada.chv   → usada pelo SERVIDOR para desencriptar
 *
 * COMO FUNCIONA A ALEATORIEDADE REAL:
 *   - O GeradorDeAleatoriedadeReal lê frames do vídeo do aquário via JavaCV/FFmpeg.
 *   - Cada frame contém pixels de água-vivas em movimento — entropia visual real.
 *   - Um deslocamento aleatório (SecureRandom padrão) escolhe o frame de partida,
 *     garantindo que cada execução use um frame diferente e produza chaves únicas.
 *   - O KeyPairGenerator do Java usa esses bytes como semente do RSA — chaves
 *     irreproduzíveis sem o vídeo e o deslocamento exatos.
 *
 * Complexidade do main(): O(D + K)
 *   D = deslocamento de frames (até DESLOCAMENTO_MAXIMO = 100) → O(1) prático.
 *   K = custo de geração RSA (fixo por tamanho de chave 1024 bits) → O(1).
 *   Consequências: execução rápida e custo fixo — operação única de setup.
 */
public class App {

       private static final String CAMINHO_DO_VIDEO = "video/aquario.mp4";
    private static final String ALGORITMO_DE_ENCRIPTACAO = "RSA";
    private static final String CAMINHO_CHAVE_PUBLICA =
    "../clientes/chave/ch_publica.chv";

private static final String CAMINHO_CHAVE_PRIVADA =
    "../servidor/chave/ch_privada.chv";

    // Máximo de frames a pular antes de usar o frame como semente da chave
    private static final int DESLOCAMENTO_MAXIMO = 100;

    /*
     * Complexidade: O(D + K)
     *   D = deslocamento de frames (máx. 100, fixo → O(1) prático).
     *   K = geração RSA 1024 bits (fixo → O(1) prático).
     * Razão: um laço de deslocamento seguido de uma chamada de geração de chaves.
     * Consequências: execução única antes de iniciar cliente e servidor.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  GERADOR DE CHAVES RSA - VERSÃO 2               ");
        System.out.println("  Fonte: frames do aquário de água-vivas          ");
        System.out.println("  https://youtu.be/Ega1KWkngt8                   ");
        System.out.println("=================================================\n");

        // Inicializa o leitor de frames do vídeo do aquário
        GeradorDeAleatoriedadeReal geradorDeAleatoriedadeReal =
            new GeradorDeAleatoriedadeReal(CAMINHO_DO_VIDEO);

        GeradorDeChaves<GeradorDeAleatoriedadeReal> geradorDeChaves = new GeradorDeChavesImpl();
        geradorDeChaves.inicializar(geradorDeAleatoriedadeReal, ALGORITMO_DE_ENCRIPTACAO);

        // Desloca aleatoriamente entre 0 e 99 frames — garante frame diferente a cada execução
        // O(D): D escolhido aleatoriamente por SecureRandom padrão (não pelo vídeo)
        SecureRandom randomizador = new SecureRandom();
        int deslocamento = randomizador.nextInt(DESLOCAMENTO_MAXIMO);
        System.out.println("Deslocamento aleatório: " + deslocamento + " frames pulados.");

        for (int i = 0; i <= deslocamento; i++) {
            System.out.println("  deslocando frame " + (i + 1) + " de " + (deslocamento + 1) + "...");
            geradorDeAleatoriedadeReal.nextInt();
        }

        // Gera e persiste o par de chaves RSA
        geradorDeChaves.gerarChaves(CAMINHO_CHAVE_PRIVADA, CAMINHO_CHAVE_PUBLICA);
        geradorDeChaves.finalizar();

        System.out.println("\nChaves geradas com sucesso:");
        System.out.println("  Chave pública  → " + CAMINHO_CHAVE_PUBLICA);
        System.out.println("  Chave privada  → " + CAMINHO_CHAVE_PRIVADA);
        System.out.println("\nAgora execute o SERVIDOR e depois o CLIENTE da Versão 2.");
    }

}
