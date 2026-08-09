package br.edu.ifba.atletas.encriptacao.aleatoriedade;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import br.edu.ifba.atletas.encriptacao.excecoes.FalhaGeracaoDeChaves;

/*
 * Gera aleatoriedade real a partir de frames do vídeo do aquário de água-vivas.
 * Estende SecureRandom para ser usada diretamente no KeyPairGenerator do Java.
 *
 * Fonte de entropia: pixels de frames do vídeo (https://youtu.be/Ega1KWkngt8).
 * Cada frame captura o estado visual imprevisível das água-vivas em movimento,
 * produzindo bytes que não podem ser reproduzidos sem o vídeo e o instante exatos.
 *
 * Complexidade de nextInt() / nextLong(): O(P)
 * Razão: percorre os bytes P de um frame JPEG para montar o valor inteiro/longo.
 * P é o tamanho comprimido de um frame — fixo na prática, portanto O(1) efetivo.
 * Consequências: cada chamada realiza I/O de vídeo; adequado para geração de
 * chaves (poucas chamadas), não para geração massiva de números aleatórios.
 */
public class GeradorDeAleatoriedadeReal extends SecureRandom {

    private static final int TENTATIVAS_CAPTURA_DE_QUADRO = 100;

    private FFmpegFrameGrabber grabber;

    /*
     * Complexidade: O(1) — inicializa o grabber e abre o arquivo de vídeo.
     */
    public GeradorDeAleatoriedadeReal(String caminhoVideo) throws FalhaGeracaoDeChaves {
        Loader.load(org.bytedeco.opencv.global.opencv_core.class);

        grabber = new FFmpegFrameGrabber(caminhoVideo);
        try {
            grabber.start();
        } catch (Exception e) {
            throw new FalhaGeracaoDeChaves("falha de inicialização: " + e.getMessage());
        }
    }

    /*
     * Complexidade: O(1) — lê o próximo frame do vídeo.
     */
    private Frame proximoQuadro() throws FalhaGeracaoDeChaves {
        try {
            return grabber.grab();
        } catch (Exception e) {
            throw new FalhaGeracaoDeChaves("falha capturando quadro: " + e.getMessage());
        }
    }

    /*
     * Tenta obter um BufferedImage a partir do próximo frame válido.
     *
     * Complexidade: O(T) onde T = tentativas até encontrar frame com imagem.
     * Na prática T << TENTATIVAS_CAPTURA_DE_QUADRO, portanto O(1) efetivo.
     */
    private BufferedImage proximaImagem() throws FalhaGeracaoDeChaves {
        Java2DFrameConverter conversor = new Java2DFrameConverter();
        BufferedImage imagem = null;
        int tentativas = 0;

        do {
            tentativas++;
            Frame quadro = proximoQuadro();
            imagem = conversor.convert(quadro);
        } while (imagem == null && tentativas < TENTATIVAS_CAPTURA_DE_QUADRO);

        conversor.close();
        return imagem;
    }

    /*
     * Retorna os bytes JPEG do próximo frame como array de ints (0–255).
     *
     * Complexidade: O(P) onde P = bytes do frame JPEG comprimido.
     * Razão: percorre P bytes para converter byte[] em int[].
     * P é fixo (tamanho de um frame), portanto O(1) na prática.
     */
    private int[] getAleatoriedade() {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(proximaImagem(), "jpg", stream);
            byte[] bytes = stream.toByteArray();

            int[] aleatoriedade = new int[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                aleatoriedade[i] = bytes[i] & 0xff;
            }
            return aleatoriedade;
        } catch (IOException | FalhaGeracaoDeChaves e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    /*
     * Produz um int a partir de 4 bytes do frame.
     *
     * Complexidade: O(P) pelo getAleatoriedade() + O(1) para montar o int.
     * Efetivo: O(1).
     */
    @Override
    public int nextInt() {
        int val = 0;
        int[] a = getAleatoriedade();
        if (a.length >= 4) {
            val |= a[0] << 24;
            val |= a[1] << 16;
            val |= a[2] << 8;
            val |= a[3];
        }
        return val;
    }

    /*
     * Produz um long a partir de 8 bytes do frame.
     *
     * Complexidade: O(P) pelo getAleatoriedade() + O(1) para montar o long.
     * Efetivo: O(1).
     */
    @Override
    public long nextLong() {
        long val = 0;
        int[] a = getAleatoriedade();
        if (a.length >= 8) {
            val |= (long) a[0] << 56;
            val |= (long) a[1] << 48;
            val |= (long) a[2] << 40;
            val |= (long) a[3] << 32;
            val |= (long) a[4] << 24;
            val |= (long) a[5] << 16;
            val |= (long) a[6] << 8;
            val |= (long) a[7];
        }
        return val;
    }

    /*
     * Encerra o grabber e libera recursos.
     *
     * Complexidade: O(1)
     */
    public void finalizar() throws FalhaGeracaoDeChaves {
        try {
            grabber.stop();
            grabber.release();
        } catch (Exception e) {
            throw new FalhaGeracaoDeChaves("falha finalizando: " + e.getMessage());
        }
    }

}
