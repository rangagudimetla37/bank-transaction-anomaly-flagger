package com.securebank.dto;

public class ReviewNoteRequest {

    private String notes;

    public ReviewNoteRequest() {
    }

    public ReviewNoteRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
