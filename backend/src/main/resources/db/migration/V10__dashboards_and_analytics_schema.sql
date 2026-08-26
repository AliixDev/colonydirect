CREATE TABLE daily_metrics_snapshots (
    id UUID PRIMARY KEY,
    snapshot_date DATE NOT NULL UNIQUE,
    total_orders INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    active_customers INT NOT NULL DEFAULT 0,
    new_customers INT NOT NULL DEFAULT 0,
    canceled_orders INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_daily_metrics_date ON daily_metrics_snapshots(snapshot_date);