ALTER TABLE shipments
    ADD COLUMN origin_hub_id INTEGER;

ALTER TABLE shipments
    ADD COLUMN destination_hub_id INTEGER;