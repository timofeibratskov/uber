package service

import (
	"auth-service/internal/kafka"
	"auth-service/internal/model"
	"auth-service/internal/repository"
	"context"
	"errors"
	"log"
	"time"

	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
)

type Auth interface {
	Register(ctx context.Context,
		input model.RegisterRequest) (string, error)
	Login(ctx context.Context,
		input model.LoginRequest) (string, error)
}

type authService struct {
	secretKey string
	repo      repository.UserRepository
	producer  kafka.EventProducer
}

func NewAuth(
	secretKey string,
	repo repository.UserRepository,
	producer kafka.EventProducer) Auth {
	return &authService{
		secretKey: secretKey,
		repo:      repo,
		producer:  producer}
}

func (s *authService) Register(ctx context.Context,
	input model.RegisterRequest) (string, error) {
	if input.Email == "" {
		return "", errors.New("email не может быть пустым")
	}
	if len(input.Password) < 8 {
		return "", errors.New("password должен иметь минимум 8 символов")
	}
	if input.Password != input.ConfirmPassword {
		return "", errors.New("пароли не совпадают")
	}
	if input.UserType != model.Driver && input.UserType != model.Passenger {
		return "", errors.New("недопустимая роль пользователя")
	}

	u, _ := s.repo.FindByEmail(ctx, input.Email)
	if u != nil {
		return "", errors.New("такая почта уже зарегистрирована")
	}

	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(input.Password), bcrypt.DefaultCost)
	if err != nil {
		return "", errors.New("ошибка шифрования пароля")
	}

	user := model.User{
		ID:        uuid.New(),
		Email:     input.Email,
		Password:  string(hashedPassword),
		UserType:  input.UserType,
		CreatedAt: time.Now(),
	}

	err = s.repo.Create(ctx, user)
	if err != nil {
		return "", err
	}

	event := model.UserRegisteredEvent{
		UserID: user.ID,
		Email:  user.Email,
		Type:   string(user.UserType),
	}

	err = s.producer.Send(ctx, event)

	if err != nil {
		log.Printf("kafka producer error: %v", err)
	}

	return GenerateToken(user, s.secretKey)
}

func (s *authService) Login(ctx context.Context,
	input model.LoginRequest) (string, error) {
	if input.Email == "" || input.Password == "" {
		return "", errors.New("email и пароль не могут быть пустыми")
	}

	user, err := s.repo.FindByEmail(ctx, input.Email)
	if err != nil {
		return "", errors.New("неверный email или пароль")
	}

	err = bcrypt.CompareHashAndPassword(
		[]byte(user.Password),
		[]byte(input.Password))
	if err != nil {
		return "", errors.New("неверный email или пароль")
	}

	return GenerateToken(*user, s.secretKey)
}
