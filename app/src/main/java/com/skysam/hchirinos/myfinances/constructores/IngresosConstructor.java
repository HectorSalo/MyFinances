package com.skysam.hchirinos.myfinances.constructores;

import java.util.Date;

public class IngresosConstructor {
    private String concepto, tipoFrecuencia, idIngreso, idGasto;
    private double monto;
    private boolean dolar, mesActivo;
    private int duracionFrecuencia;
    private Date fechaIncial;

    public IngresosConstructor(String concepto, String tipoFrecuencia, String idIngreso, double monto, boolean dolar, int duracionFrecuencia, Date fechaIncial) {
        this.concepto = concepto;
        this.tipoFrecuencia = tipoFrecuencia;
        this.idIngreso = idIngreso;
        this.monto = monto;
        this.dolar = dolar;
        this.duracionFrecuencia = duracionFrecuencia;
        this.fechaIncial = fechaIncial;
    }

    public IngresosConstructor() {
    }

    public String getIdIngreso() {
        return idIngreso;
    }

    public void setIdIngreso(String idIngreso) {
        this.idIngreso = idIngreso;
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

    public String getIdGasto() {
        return idGasto;
    }

    public void setIdGasto(String idGasto) {
        this.idGasto = idGasto;
    }

    public boolean isMesActivo() {
        return mesActivo;
    }

    public void setMesActivo(boolean mesActivo) {
        this.mesActivo = mesActivo;
    }
}
