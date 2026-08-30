CREATE TABLE IF NOT EXISTS users (
                                     id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     email       VARCHAR(255) NOT NULL UNIQUE,
                                     password    VARCHAR(255) NOT NULL,
                                     user_type   VARCHAR(50)  NOT NULL,
                                     created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

select * from users