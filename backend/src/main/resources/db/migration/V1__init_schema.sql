-- V1__init_schema.sql
-- Generated from Phase 8 Database Design doc (source of truth).
-- Table order adjusted so FK targets (subscription before orders) precede references.

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE area_node (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  parent_id UUID REFERENCES area_node(id),
  level VARCHAR(20) NOT NULL CHECK (level IN ('COUNTRY','CITY','COLONY','BLOCK','STREET','HOUSE')),
  name VARCHAR(255) NOT NULL,
  metadata JSONB DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_area_node_parent ON area_node(parent_id);
CREATE INDEX idx_area_node_level ON area_node(level);

CREATE TABLE app_user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(20) UNIQUE,
  password_hash VARCHAR(255),
  google_sub VARCHAR(255) UNIQUE,
  full_name VARCHAR(255) NOT NULL,
  preferred_language VARCHAR(5) NOT NULL DEFAULT 'en',
  role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER','RIDER','ADMIN','SUPER_ADMIN')),
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

CREATE TABLE refresh_token (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  device_label VARCHAR(255),
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);

CREATE TABLE address (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  colony_id UUID NOT NULL REFERENCES area_node(id),
  label VARCHAR(20) NOT NULL CHECK (label IN ('HOME','OFFICE','OTHER')),
  line1 VARCHAR(255) NOT NULL,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL,
  precise_pin_set BOOLEAN NOT NULL DEFAULT false,
  is_default BOOLEAN NOT NULL DEFAULT false,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);
CREATE INDEX idx_address_user ON address(user_id);
CREATE INDEX idx_address_colony ON address(colony_id);

CREATE TABLE category (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  colony_id UUID REFERENCES area_node(id), -- nullable = global category
  name VARCHAR(255) NOT NULL,
  parent_id UUID REFERENCES category(id)
);

CREATE TABLE product (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  colony_id UUID NOT NULL REFERENCES area_node(id),
  category_id UUID NOT NULL REFERENCES category(id),
  name VARCHAR(255) NOT NULL,
  description TEXT,
  barcode VARCHAR(64),
  image_url TEXT,
  is_active BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);
CREATE INDEX idx_product_colony_category ON product(colony_id, category_id);
CREATE INDEX idx_product_name_trgm ON product USING gin (name gin_trgm_ops); -- fuzzy search

CREATE TABLE product_variant (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id UUID NOT NULL REFERENCES product(id),
  weight_label VARCHAR(50) NOT NULL, -- e.g. '500g','1kg'
  price_minor_units BIGINT NOT NULL, -- store money as integer minor units (paisa)
  stock_qty INTEGER NOT NULL DEFAULT 0,
  expiry_date DATE,
  is_out_of_stock BOOLEAN GENERATED ALWAYS AS (stock_qty <= 0) STORED,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_variant_product ON product_variant(product_id);

CREATE TABLE offer (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  colony_id UUID NOT NULL REFERENCES area_node(id),
  scope VARCHAR(20) NOT NULL CHECK (scope IN ('PRODUCT','CATEGORY','CART')),
  target_id UUID, -- product_id or category_id depending on scope; null for CART
  discount_type VARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT','FLAT')),
  discount_value NUMERIC(10,2) NOT NULL,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE coupon (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(50) UNIQUE NOT NULL,
  discount_type VARCHAR(10) NOT NULL CHECK (discount_type IN ('PERCENT','FLAT')),
  discount_value NUMERIC(10,2) NOT NULL,
  max_discount_minor_units BIGINT,
  per_user_limit INTEGER NOT NULL DEFAULT 1,
  total_budget_minor_units BIGINT,
  budget_consumed_minor_units BIGINT NOT NULL DEFAULT 0,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE subscription (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID NOT NULL REFERENCES app_user(id),
  product_variant_id UUID NOT NULL REFERENCES product_variant(id),
  cadence VARCHAR(10) NOT NULL CHECK (cadence IN ('DAILY','WEEKLY','MONTHLY')),
  charge_model VARCHAR(20) NOT NULL DEFAULT 'PER_DELIVERY' CHECK (charge_model IN ('PER_DELIVERY','PREPAY')),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','PAUSED','CANCELLED')),
  consecutive_failures INTEGER NOT NULL DEFAULT 0,
  next_run_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('FRESH_MARKET','GROCERY_DELIVERY')),
  customer_id UUID NOT NULL REFERENCES app_user(id),
  address_id UUID NOT NULL REFERENCES address(id),
  colony_id UUID NOT NULL REFERENCES area_node(id),
  delivery_mode VARCHAR(20) NOT NULL CHECK (delivery_mode IN ('IMMEDIATE','SCHEDULED','SUBSCRIPTION')),
  scheduled_window_start TIMESTAMPTZ,
  scheduled_window_end TIMESTAMPTZ,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
  -- store name only for GROCERY_DELIVERY
  store_name VARCHAR(255),
  estimated_ceiling_minor_units BIGINT,
  actual_total_minor_units BIGINT,
  receipt_photo_url TEXT,
  variance_review_status VARCHAR(20), -- null unless GROCERY_DELIVERY breach
  delivery_fee_minor_units BIGINT NOT NULL DEFAULT 0,
  coupon_id UUID REFERENCES coupon(id),
  subscription_id UUID REFERENCES subscription(id),
  otp_code_hash VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_colony_status ON orders(colony_id, status);
CREATE INDEX idx_orders_variance_review ON orders(variance_review_status) WHERE variance_review_status IS NOT NULL;

CREATE TABLE order_item (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id),
  product_variant_id UUID REFERENCES product_variant(id), -- null for freeform grocery items
  freeform_description TEXT, -- e.g. "2 Coke"
  quantity INTEGER NOT NULL,
  unit_price_minor_units BIGINT, -- null for grocery items until reconciled
  line_total_minor_units BIGINT
);
CREATE INDEX idx_order_item_order ON order_item(order_id);

CREATE TABLE payment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id),
  method VARCHAR(20) NOT NULL CHECK (method IN ('COD','EASYPAISA','JAZZCASH','BANK_TRANSFER')),
  amount_minor_units BIGINT NOT NULL,
  idempotency_key VARCHAR(100) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL CHECK (status IN ('AUTHORIZED','CAPTURED','FAILED','REFUNDED')),
  gateway_reference VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_order ON payment(order_id);

CREATE TABLE payment_audit_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  payment_id UUID NOT NULL REFERENCES payment(id),
  previous_status VARCHAR(20),
  new_status VARCHAR(20) NOT NULL,
  actor_user_id UUID REFERENCES app_user(id), -- null for system actor
  note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rider_profile (
  user_id UUID PRIMARY KEY REFERENCES app_user(id),
  colony_id UUID NOT NULL REFERENCES area_node(id),
  is_online BOOLEAN NOT NULL DEFAULT false,
  vehicle_type VARCHAR(30),
  last_lat DOUBLE PRECISION,
  last_lng DOUBLE PRECISION,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rider_assignment (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id),
  rider_id UUID NOT NULL REFERENCES app_user(id),
  offered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  responded_at TIMESTAMPTZ,
  response VARCHAR(20) CHECK (response IN ('ACCEPTED','REJECTED','TIMEOUT')),
  attempt_number INTEGER NOT NULL DEFAULT 1
);
CREATE INDEX idx_rider_assignment_order ON rider_assignment(order_id);
CREATE INDEX idx_rider_assignment_rider ON rider_assignment(rider_id);

CREATE TABLE rider_earning (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  rider_id UUID NOT NULL REFERENCES app_user(id),
  order_id UUID NOT NULL REFERENCES orders(id),
  amount_minor_units BIGINT NOT NULL,
  earned_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_rider_earning_rider_date ON rider_earning(rider_id, earned_at);

CREATE TABLE colony_waypoint ( -- resolves Phase 7's navigation-precision design
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  colony_id UUID NOT NULL REFERENCES area_node(id),
  name VARCHAR(255) NOT NULL,
  latitude DOUBLE PRECISION NOT NULL,
  longitude DOUBLE PRECISION NOT NULL
);

CREATE TABLE review (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id),
  customer_id UUID NOT NULL REFERENCES app_user(id),
  rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notification_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES app_user(id),
  channel VARCHAR(20) NOT NULL CHECK (channel IN ('PUSH','WHATSAPP','EMAIL')),
  category VARCHAR(30) NOT NULL CHECK (category IN ('ORDER_STATUS','PROMOTION','SUBSCRIPTION_REMINDER')),
  payload JSONB NOT NULL,
  sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE admin_audit_log (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_user_id UUID NOT NULL REFERENCES app_user(id),
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id UUID NOT NULL,
  before_state JSONB,
  after_state JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
