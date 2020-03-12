package com.skysam.hchirinos.myfinances.constructores;

import java.util.Date;

public class IngresosConstructor {
    String concepto, tipoFrecuencia;
    double monto;
    boolean dolar;
    int duracionFrecuencia;
    Date fechaIncial;

    public IngresosConstructor(String concepto, String tipoFrecuencia, double monto, boolean dolar, int duracionFrecuencia, Date fechaIncial) {
        this.concepto = concepto;
        this.tipoFrecuencia = tipoFrecuencia;
        this.monto = monto;
        this.dolar = dolar;
        this.duracionFrecuencia = duracionFrecuencia;
        this.fechaIncial = fechaIncial;
    }

    public IngresosConstructor() {
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getTipoFrecuencia() {
        return tipoFrecuencia;
    }

    public void setTipoFrecuencia(String tipoFrecuencia) {
        this.tipoFrecuencia = tipoFrecuencia;
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

    public int getDuracionFrecuencia() {
        return duracionFrecuencia;
    }

    public void setDuracionFrecuencia(int duracionFrecuencia) {
        this.duracionFrecuencia = duracionFrecuencia;
    }

    public Date getFechaIncial() {
        return fechaIncial;
    }

    public void setFechaIncial(Date fechaIncial) {
        this.fechaIncial = fechaIncial;
    }
}
