CREATE TABLE user_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    colony VARCHAR(100) NOT NULL, -- E.g., 'Defense Colony'
    block VARCHAR(50),
    street VARCHAR(100),
    house_number VARCHAR(50) NOT NULL,
    delivery_instructions TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses(user_id);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL, -- Relies on existing orders table from V1
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    custom_instruction VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Alter existing orders table to add checkout-specific fields if not present
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS delivery_address_id UUID REFERENCES user_addresses(id),
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) NOT NULL DEFAULT 'COD',
ADD COLUMN IF NOT EXISTS delivery_fee DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS grand_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00;