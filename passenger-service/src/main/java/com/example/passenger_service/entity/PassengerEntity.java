package com.example.passenger_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PassengerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gmail;
    private String password;
    private String phoneNumber;
    private Long cardId;
    private Float rating;
    private Long listOfRidesId;

    public PassengerEntity() {
    }

    public PassengerEntity(Long id, String name, String gmail, String password, String phoneNumber, Long cardId, Float rating, Long listOfRidesId) {
        this.id = id;
        this.name = name;
        this.gmail = gmail;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.cardId = cardId;
        this.rating = rating;
        this.listOfRidesId = listOfRidesId;
    }

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

    @Override
    public String toString() {
        return "Passenger{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gmail='" + gmail + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", cardId=" + cardId +
                ", rating=" + rating +
                ", listOfRidesId=" + listOfRidesId +
                '}';
    }
}