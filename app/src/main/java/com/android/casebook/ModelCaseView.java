package com.android.casebook;

public class ModelCaseView {

    private String item_id;

    private String case_title;
    private String party_name;
    private String adverse_advocate;
    private String adverse_contact;
    private String court_name;
    private String case_type;
    private String case_no;
    private String on_behalf;
    private String client_contact;
    private String respondent_name;
    private String filed_U_Sec;
    private String case_date;
    private String case_time;



    public ModelCaseView(String case_title, String party_name, String adverse_advocate, String adverse_contact,
                         String court_name, String case_type, String case_no, String on_behalf, String client_contact,
                         String respondent_name, String filed_U_Sec, String case_date, String case_time, String item_id) {
        this.case_title = case_title;
        this.party_name = party_name;
        this.adverse_advocate = adverse_advocate;
        this.adverse_contact = adverse_contact;
        this.court_name = court_name;
        this.case_type = case_type;
        this.case_no = case_no;
        this.on_behalf = on_behalf;
        this.client_contact = client_contact;
        this.respondent_name = respondent_name;
        this.filed_U_Sec = filed_U_Sec;
        this.case_date = case_date;
        this.case_time = case_time;
        this.item_id = item_id;
    }

    public ModelCaseView() {
    }


    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getCase_time() {
        return case_time;
    }

    public void setCase_time(String case_time) {
        this.case_time = case_time;
    }

    public String getCase_date() {
        return case_date;
    }

    public void setCase_date(String case_date) {
        this.case_date = case_date;
    }

    public String getAdverse_advocate() {
        return adverse_advocate;
    }

    public void setAdverse_advocate(String adverse_advocate) {
        this.adverse_advocate = adverse_advocate;
    }

    public String getAdverse_contact() {
        return adverse_contact;
    }

    public void setAdverse_contact(String adverse_contact) {
        this.adverse_contact = adverse_contact;
    }

    public String getCourt_name() {
        return court_name;
    }

    public void setCourt_name(String court_name) {
        this.court_name = court_name;
    }

    public String getCase_type() {
        return case_type;
    }

    public void setCase_type(String case_type) {
        this.case_type = case_type;
    }

    public String getCase_no() {
        return case_no;
    }

    public void setCase_no(String case_no) {
        this.case_no = case_no;
    }

    public String getOn_behalf() {
        return on_behalf;
    }

    public void setOn_behalf(String on_behalf) {
        this.on_behalf = on_behalf;
    }

    public String getClient_contact() {
        return client_contact;
    }

    public void setClient_contact(String client_contact) {
        this.client_contact = client_contact;
    }

    public String getRespondent_name() {
        return respondent_name;
    }

    public void setRespondent_name(String respondent_name) {
        this.respondent_name = respondent_name;
    }

    public String getFiled_U_Sec() {
        return filed_U_Sec;
    }

    public void setFiled_U_Sec(String filed_U_Sec) {
        this.filed_U_Sec = filed_U_Sec;
    }


    public String getCase_title() {
        return case_title;
    }

    public void setCase_title(String case_title) {
        this.case_title = case_title;
    }

    public String getParty_name() {
        return party_name;
    }

    public void setParty_name(String party_name) {
        this.party_name = party_name;
    }
}
