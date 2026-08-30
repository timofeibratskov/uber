package handler

import (
	"auth-service/internal/model"
	"auth-service/internal/service"
	"encoding/json"
	"net/http"
)

type AuthHandler struct {
	service service.Auth
}

func NewAuthHandler(service service.Auth) *AuthHandler {
	return &AuthHandler{service: service}
}

func (h *AuthHandler) Register(
	w http.ResponseWriter,
	r *http.Request) {

	var req model.RegisterRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}

	jwt, err := h.service.Register(r.Context(), req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)

	json.NewEncoder(w).Encode(TokenResponse{Token: jwt})
}

type TokenResponse struct {
	Token string `json:"token"`
}

func (h *AuthHandler) Login(
	w http.ResponseWriter,
	r *http.Request) {

	var req model.LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}

	jwt, err := h.service.Login(r.Context(), req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusUnauthorized)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)

	json.NewEncoder(w).Encode(TokenResponse{Token: jwt})
}
