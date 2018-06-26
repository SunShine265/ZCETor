package com.goodproductssoft.flypoolmonitor.models;

/**
 * Created by user on 4/19/2018.
 */

public class TotalYourWorker{
    double  Current, Reported, LastScreen, average;
    int YourWorkerCurrent, Valid, Stale, Invalid, YourWorkerActive;
    boolean isValue;

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public int getYourWorkerActive() {
        return YourWorkerActive;
    }

    public void setYourWorkerActive(int yourWorkerActive) {
        YourWorkerActive = yourWorkerActive;
    }

    public boolean isValue() {
        return isValue;
    }

    public void setValue(boolean value) {
        isValue = value;
    }

    public int getYourWorkerCurrent() {
        return YourWorkerCurrent;
    }

    public void setYourWorkerCurrent(int yourWorkerCurrent) {
        this.YourWorkerCurrent = yourWorkerCurrent;
    }

    public double getCurrent() {
        return Current;
    }

    public void setCurrent(double totalCurrent) {
        this.Current = totalCurrent;
    }

    public double getReported() {
        return Reported;
    }

    public void setReported(double reported) {
        this.Reported = reported;
    }

    public int getValid() {
        return Valid;
    }

    public void setValid(int valid) {
        this.Valid = valid;
    }

    public int getStale() {
        return Stale;
    }

    public void setStale(int stale) {
        this.Stale = stale;
    }

    public int getInvalid() {
        return Invalid;
    }

    public void setInvalid(int invalid) {
        this.Invalid = invalid;
    }

    public double getLastScreen() {
        return LastScreen;
    }

    public void setLastScreen(double lastScreen) {
        this.LastScreen = lastScreen;
    }
}