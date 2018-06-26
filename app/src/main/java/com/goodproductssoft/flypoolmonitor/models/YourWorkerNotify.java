package com.goodproductssoft.flypoolmonitor.models;

/**
 * Created by user on 4/24/2018.
 */

public class YourWorkerNotify {
    String idMiner, nameYourWorker;
    double currentHashrate;

    public double getCurrentHashrate() {
        return currentHashrate;
    }

    public void setCurrentHashrate(double currentHashrate) {
        this.currentHashrate = currentHashrate;
    }

    public String getIdMiner() {
        return idMiner;
    }

    public void setIdMiner(String idMiner) {
        this.idMiner = idMiner;
    }

    public String getNameYourWorker() {
        return nameYourWorker;
    }

    public void setNameYourWorker(String nameYourWorker) {
        this.nameYourWorker = nameYourWorker;
    }
}
