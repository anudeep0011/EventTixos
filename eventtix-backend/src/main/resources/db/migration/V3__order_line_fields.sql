-- Persist tier + quantity on order so webhook can finalize inventory without external metadata
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS tier_id UUID REFERENCES ticket_tiers(id),
    ADD COLUMN IF NOT EXISTS quantity INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS hold_key VARCHAR(200);

CREATE INDEX IF NOT EXISTS idx_orders_tier ON orders(tier_id);
