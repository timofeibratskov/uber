package model

import (
	"time"

	"github.com/google/uuid"
)

type UserType string

const (
	Driver    UserType = "DRIVER"
	Passenger UserType = "PASSENGER"
)

type User struct {
	ID        uuid.UUID `db:"id"`
	Email     string    `db:"email"`
	Password  string    `db:"password"`
	UserType  UserType  `db:"user_type"`
	CreatedAt time.Time `db:"created_at"`
}

type RegisterRequest struct {
	Email           string   `json:"email"`
	Password        string   `json:"password"`
	ConfirmPassword string   `json:"confirm_password"`
	UserType        UserType `json:"user_type"`
}
type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}
type UserRegisteredEvent struct {
	UserID uuid.UUID `json:"userId"`
	Email  string    `json:"email"`
	Type   string    `json:"type"`
}
