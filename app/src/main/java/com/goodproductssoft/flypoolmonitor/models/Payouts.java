package com.goodproductssoft.flypoolmonitor.models;

/**
 * Created by user on 4/19/2018.
 */

public class Payouts{
    int total;
    double totalDuration, totalETH;

    String txHash, amount, paidOn, duration;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(String paidOn) {
        this.paidOn = paidOn;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getTotalDuraion() {
        return totalDuration;
    }

    public void setTotalDuration(double average) {
        this.totalDuration = average;
    }

    public double getTotalETH() {
        return totalETH;
    }

    public void setTotalETH(double totalETH) {
        this.totalETH = totalETH;
    }
}
