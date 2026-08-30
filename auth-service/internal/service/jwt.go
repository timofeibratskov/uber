package service

import (
	"time"

	"auth-service/internal/model"

	"github.com/golang-jwt/jwt/v5"
)

type CustomClaims struct {
	UserID   string         `json:"user_id"`
	Email    string         `json:"email"`
	UserType model.UserType `json:"user_type"`
	jwt.RegisteredClaims
}

func GenerateToken(user model.User, secretKey string) (string, error) {
	claims := CustomClaims{
		UserID:   user.ID.String(),
		Email:    user.Email,
		UserType: user.UserType,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(2 * time.Hour)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(secretKey))
}
