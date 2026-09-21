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

type TokenResponse struct {
	Token string `json:"token"`
}

// Register godoc
// @Summary      Регистрация нового пользователя
// @Description  Создает пользователя, генерирует JWT токен и отправляет событие в Kafka
// @Tags         Auth
// @Accept       json
// @Produce      json
// @Param        input  body      model.RegisterRequest  true  "Данные для регистрации"
// @Success      201    {object}  TokenResponse          "Пользователь создан, возвращается JWT"
// @Failure      400    {string}  string                 "Ошибка валидации входных данных"
// @Failure      500    {string}  string                 "Внутренняя ошибка сервера"
// @Router       /api/auth/register [post]
func (h *AuthHandler) Register(
	w http.ResponseWriter,
	r *http.Request,
) {
	var req model.RegisterRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}

	jwt, err := h.service.Register(r.Context(), req)
	if err != nil {
		if err.Error() == "пароли не совпадают" ||
			err.Error() == "email не может быть пустым" ||
			err.Error() == "недопустимая роль пользователя" ||
			err.Error() == "password должен иметь минимум 8 символов" ||
			err.Error() == "такая почта уже зарегистрирована" {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)

	json.NewEncoder(w).Encode(TokenResponse{Token: jwt})
}

// Login godoc
// @Summary      Авторизация пользователя
// @Description  Проверяет учётные данные и возвращает JWT токен
// @Tags         Auth
// @Accept       json
// @Produce      json
// @Param        input  body      model.LoginRequest  true  "Данные для входа"
// @Success      200    {object}  TokenResponse       "Успешная авторизация"
// @Failure      400    {string}  string              "Пустые поля ввода"
// @Failure      401    {string}  string              "Неверный email или пароль"
// @Failure      500    {string}  string              "Внутренняя ошибка сервера"
// @Router       /api/auth/login [post]
func (h *AuthHandler) Login(
	w http.ResponseWriter,
	r *http.Request,
) {
	var req model.LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Неверный формат JSON", http.StatusBadRequest)
		return
	}

	jwt, err := h.service.Login(r.Context(), req)
	if err != nil {
		if err.Error() == "email и пароль не могут быть пустыми" {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		if err.Error() == "неверный email или пароль" {
			http.Error(w, err.Error(), http.StatusUnauthorized)
			return
		}

		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)

	json.NewEncoder(w).Encode(TokenResponse{Token: jwt})
}
