CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY,
    variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    transaction_type VARCHAR(50) NOT NULL, -- 'RESTOCK', 'SALE', 'ADJUSTMENT', 'RETURN', 'SHRINKAGE'
    quantity_change INT NOT NULL, -- Can be positive (restock) or negative (sale/shrinkage)
    reference_id UUID, -- Can be an order_id or an admin user_id
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_inventory_transactions_variant ON inventory_transactions(variant_id);
CREATE INDEX idx_inventory_transactions_type ON inventory_transactions(transaction_type);