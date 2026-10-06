-- Hibernate generated a CHECK constraint listing the original enum values.
-- ddl-auto=update never alters an existing constraint, so PENDING must be added by hand.
ALTER TABLE appointment DROP CONSTRAINT IF EXISTS appointment_status_check;
ALTER TABLE appointment ADD CONSTRAINT appointment_status_check
    CHECK (status IN ('PENDING', 'SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED'));
