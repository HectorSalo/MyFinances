package com.skysam.hchirinos.myfinances.constructores;

import java.util.Date;

public class ItemGastosConstructor {

    private String concepto, idItem, idListItem;
    private double montoAproximado;
    private Date fechaAproximada, fechaIngreso;

    public ItemGastosConstructor() {
    }

    public String getIdItem() {
        return idItem;
    }

    public void setIdItem(String idItem) {
        this.idItem = idItem;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }


    public double getMontoAproximado() {
        return montoAproximado;
    }

    public void setMontoAproximado(double montoAproximado) {
        this.montoAproximado = montoAproximado;
    }

    public Date getFechaAproximada() {
        return fechaAproximada;
    }

    public void setFechaAproximada(Date fechaAproximada) {
        this.fechaAproximada = fechaAproximada;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getIdListItem() {
        return idListItem;
    }

    public void setIdListItem(String idListItem) {
        this.idListItem = idListItem;
    }
}


