package br.edu.ifba.atletas.clientes.impl;

public class Leitura {

    private Integer batimentosCardiacos = 0;
    private Integer passosPorMinuto = 0;

    // O(1)
    public Leitura(Integer batimentosCardiacos, Integer passosPorMinuto) {
        this.batimentosCardiacos = batimentosCardiacos;
        this.passosPorMinuto = passosPorMinuto;
    }

    // O(1)
    public Integer getBatimentosCardiacos() {
        return batimentosCardiacos;
    }

    // O(1)
    public void setBatimentosCardiacos(Integer batimentosCardiacos) {
        this.batimentosCardiacos = batimentosCardiacos;
    }

    // O(1)
    public Integer getPassosPorMinuto() {
        return passosPorMinuto;
    }

    // O(1)
    public void setPassosPorMinuto(Integer passosPorMinuto) {
        this.passosPorMinuto = passosPorMinuto;
    }

    // O(1)
    @Override
    public String toString() {
        return "batimentos: " + batimentosCardiacos + ", passos/min: " + passosPorMinuto;
    }

}
