CREATE TABLE search_logs (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    raw_query VARCHAR(500) NOT NULL,
    normalized_query VARCHAR(500) NOT NULL,
    detected_language VARCHAR(20) NOT NULL DEFAULT 'EN', -- 'EN', 'UR_PK', 'MIXED'
    result_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_search_logs_user_id ON search_logs(user_id);
CREATE INDEX idx_search_logs_query ON search_logs(normalized_query);

CREATE TABLE user_product_frequencies (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(id) ON DELETE CASCADE,
    purchase_count INT NOT NULL DEFAULT 1,
    last_purchased_at TIMESTAMP WITH TIME ZONE NOT NULL,
    average_interval_days INT NOT NULL DEFAULT 7,
    UNIQUE(user_id, product_id, variant_id)
);

CREATE INDEX idx_user_prod_freq_user ON user_product_frequencies(user_id);