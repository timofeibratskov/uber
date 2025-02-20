package com.example.passenger_service.dto;

public class PassengerDto {
    private Long id;
    private String name;
    private String gmail;
    private String password;
    private String phoneNumber;
    private Long cardId;
    private Float rating;
    private Long listOfRidesId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Long getListOfRidesId() {
        return listOfRidesId;
    }

    public void setListOfRidesId(Long listOfRidesId) {
        this.listOfRidesId = listOfRidesId;
    }
}