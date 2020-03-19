package com.skysam.hchirinos.myfinances.constructores;

import java.util.Date;

public class AhorrosConstructor {
    private String concepto, origen;
    private double monto;
    private boolean dolar, descontar;
    private Date fechaIngreso;

    public AhorrosConstructor(String concepto, String origen, double monto, boolean dolar, Date fechaIngreso, boolean descontar) {
        this.concepto = concepto;
        this.origen = origen;
        this.monto = monto;
        this.dolar = dolar;
        this.fechaIngreso = fechaIngreso;
        this.descontar = descontar;
    }

    public AhorrosConstructor() {
    }

    public boolean isDescontar() {
        return descontar;
    }

    public void setDescontar(boolean descontar) {
        this.descontar = descontar;
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

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
