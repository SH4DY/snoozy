package com.shady.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by shady on 21/12/14.
 */
public class Seminar {
    UUID id;
    String name = "Seminarname";
    int allowedToMiss = 2;
    int missed = 0;
    List<Unit> units = new ArrayList<>();



    @Override
    public String toString(){
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getMissed() {
        return missed;
    }

    public void setMissed(int missed) {
        this.missed = missed;
    }

    public int getAllowedToMiss() {

        return allowedToMiss;
    }

    public void setAllowedToMiss(int allowedToMiss) {
        this.allowedToMiss = allowedToMiss;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int calculateAttendance(int total, int missed) {
        int a = (total - missed) / total;
        int b = 1 - a;

        if (allowedToMiss <= missed) {
            return 0;
        }
        return allowedToMiss - missed;
    }

    public void missedSession() {
        missed++;
    }

    public void unmissedSession() {
        missed--;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public List<Unit> addUnit(Unit unit) {
        units.add(unit);
        return units;
    }
}
