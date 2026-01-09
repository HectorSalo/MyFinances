package com.skysam.hchirinos.myfinances.common.model.constructores;

import java.util.Date;

public class AhorrosConstructor {
    private String concepto, origen, idAhorro, idDeuda, prestamista;
    private double monto;
    private boolean dolar, capital;
    private Date fechaIngreso;

    public AhorrosConstructor(String idAhorro, String concepto, String origen, double monto, boolean dolar, Date fechaIngreso) {
        this.idAhorro = idAhorro;
        this.concepto = concepto;
        this.origen = origen;
        this.monto = monto;
        this.dolar = dolar;
        this.fechaIngreso = fechaIngreso;
    }


    public String getIdAhorro() {
        return idAhorro;
    }

    public void setIdAhorro(String idAhorro) {
        this.idAhorro = idAhorro;
    }

    public AhorrosConstructor() {
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public boolean isDolar() {
        return dolar;
    }

    public void setDolar(boolean dolar) {
        this.dolar = dolar;
    }
    public boolean isCapital() {
        return capital;
    }
    public void setCapital(boolean capital) {
        this.capital = capital;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getIdDeuda() {
        return idDeuda;
    }

    public void setIdDeuda(String idDeuda) {
        this.idDeuda = idDeuda;
    }

    public String getPrestamista() {
        return prestamista;
    }

    public void setPrestamista(String prestamista) {
        this.prestamista = prestamista;
    }
}
