package main

import (
	"auth-service/internal/handler"
	"auth-service/internal/kafka"
	"auth-service/internal/repository"
	"auth-service/internal/service"
	"log"
	"net/http"
	"time"

	_ "auth-service/docs" // 1. Импорт сгенерированной Swagger-документации

	httpSwagger "github.com/swaggo/http-swagger" // 2. Пакет для Swagger UI
)

// @title           Auth Service API
// @version         1.0
// @description     Микросервис авторизации и регистрации пользователей.
// @host            localhost:8885
// @BasePath        /
func main() {
	dsn := "postgres://postgres:1111@localhost:5555/auth_db?sslmode=disable"
	db := repository.InitDB(dsn)
	defer db.Close()

	secretKey := "secret-key"

	userRepo := repository.NewUserRepository(db)

	kafkaProducer := kafka.NewProducer("localhost:9092", "rides.users")
	defer kafkaProducer.Close()

	authService := service.NewAuth(secretKey, userRepo, kafkaProducer)
	authHandler := handler.NewAuthHandler(authService)

	mux := http.NewServeMux()

	// 3. Подключаем маршрут для Swagger UI
	mux.HandleFunc("GET /swagger/", httpSwagger.WrapHandler)

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
