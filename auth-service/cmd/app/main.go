package main

import (
	"auth-service/internal/handler"
	"auth-service/internal/repository"
	"auth-service/internal/service"
	"log"
	"net/http"
	"time"
)

func main() {
	dsn := "postgres://postgres:1111@localhost:5555/auth_db?sslmode=disable"
	secretKey := "secret-key"
	db := repository.InitDB(dsn)
	defer db.Close()

	userRepo := repository.NewUserRepository(db)
	authService := service.NewAuth(userRepo, secretKey)
	authHandler := handler.NewAuthHandler(authService)

	mux := http.NewServeMux()

	mux.HandleFunc("POST /api/auth/register", authHandler.Register)
	mux.HandleFunc("POST /api/auth/login", authHandler.Login)

	server := &http.Server{
		Addr:         ":8885",
		Handler:      mux,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  120 * time.Second,
	}

	log.Printf("Auth Service успешно запущен на порту %s", server.Addr)
	if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatalf("Ошибка работы HTTP-сервера: %v", err)
	}
}
