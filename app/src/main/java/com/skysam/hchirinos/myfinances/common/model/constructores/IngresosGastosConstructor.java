package com.skysam.hchirinos.myfinances.common.model.constructores;

import java.util.Date;

public class IngresosGastosConstructor {
    private String concepto, tipoFrecuencia, idIngreso, idGasto;
    private double monto;
    private boolean dolar, mesActivo, pagado;
    private int duracionFrecuencia;
    private Date fechaIncial, fechaFinal;

    public IngresosGastosConstructor() {
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
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

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }
}
