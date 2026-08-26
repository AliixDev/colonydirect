-- V2__password_reset_token.sql
-- Not present in the Phase 8 doc's table list but required by the Phase 9
-- /auth/password-reset endpoints; added here as an additive migration rather
-- than editing V1, per the Phase 8 "forward-only migrations" design note.

CREATE TABLE password_reset_token (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  consumed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_password_reset_token_user ON password_reset_token(user_id);
