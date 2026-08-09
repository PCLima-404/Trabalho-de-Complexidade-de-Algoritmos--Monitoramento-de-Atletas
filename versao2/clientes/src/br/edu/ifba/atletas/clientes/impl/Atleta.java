package br.edu.ifba.atletas.clientes.impl;

public class Atleta implements Comparable<Atleta> {

    private String identificacao = "";
    private String modelo = "";

    // O(1)
    public Atleta(String identificacao, String modelo) {
        this.identificacao = identificacao;
        this.modelo = modelo;
    }

    // O(1)
    public String getIdentificacao() {
        return identificacao;
    }

    // O(1)
    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }

    // O(1)
    public String getModelo() {
        return modelo;
    }

    // O(1)
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    // O(1)
    @Override
    public String toString() {
        return "atleta: " + identificacao;
    }

    // O(1)
    @Override
    public int compareTo(Atleta outroAtleta) {
        return identificacao.compareTo(outroAtleta.getIdentificacao());
    }

}
