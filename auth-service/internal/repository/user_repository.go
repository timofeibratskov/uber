package repository

import (
	"auth-service/internal/model"
	"context"
	"database/sql"
	"errors"

	"github.com/jmoiron/sqlx"
)

type UserRepository interface {
	Create(ctx context.Context, user model.User) error
	FindByEmail(ctx context.Context, email string) (*model.User, error)
}

type userRepository struct {
	db *sqlx.DB
}

func NewUserRepository(db *sqlx.DB) UserRepository {
	return &userRepository{db: db}
}

func (u userRepository) Create(ctx context.Context, user model.User) error {
	query := `
	insert into users (id, email, password, user_type, created_at)
	values (:id, :email, :password, :user_type, :created_at)
`
	_, err := u.db.NamedExecContext(ctx, query, user)
	return err
}

func (u userRepository) FindByEmail(ctx context.Context, email string) (*model.User, error) {
	var user model.User
	query := ` select *  from users where email = $1 limit 1`

	err := u.db.GetContext(ctx, &user, query, email)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, err
		}
	}
	return &user, nil
}
