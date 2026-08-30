package repository

import (
	"log"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/jmoiron/sqlx"
)

func InitDB(dsn string) *sqlx.DB {
	db, err := sqlx.Connect("pgx", dsn)
	if err != nil {
		log.Fatalf("DB connecting problem: %v", err)
	}

	db.SetMaxOpenConns(25)
	db.SetMaxIdleConns(10)

	return db
}
