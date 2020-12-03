package com.android.casebook;

public class ModelAdjournDate {
    private String adjourn_date;
    private String adjourn_reason;

    public ModelAdjournDate(String adjourn_date, String adjourn_reason) {
        this.adjourn_date = adjourn_date;
        this.adjourn_reason = adjourn_reason;
    }

    public ModelAdjournDate() {
    }

    public String getAdjourn_date() {
        return adjourn_date;
    }

    public void setAdjourn_date(String adjourn_date) {
        this.adjourn_date = adjourn_date;
    }

    public String getAdjourn_reason() {
        return adjourn_reason;
    }

    public void setAdjourn_reason(String adjourn_reason) {
        this.adjourn_reason = adjourn_reason;
    }

}
