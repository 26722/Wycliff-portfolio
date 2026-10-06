DO $$
DECLARE
    constraint_row RECORD;
    doctor_id_column SMALLINT;
    specialization_id_column SMALLINT;
BEGIN
    SELECT attnum INTO doctor_id_column
    FROM pg_attribute
    WHERE attrelid = 'doctor_specialization'::regclass
      AND attname = 'doctor_id';

    SELECT attnum INTO specialization_id_column
    FROM pg_attribute
    WHERE attrelid = 'doctor_specialization'::regclass
      AND attname = 'specialization_id';

    FOR constraint_row IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'doctor_specialization'::regclass
          AND contype = 'f'
          AND (
              (conkey = ARRAY[doctor_id_column]::SMALLINT[]
                  AND confrelid = 'specialization'::regclass)
              OR
              (conkey = ARRAY[specialization_id_column]::SMALLINT[]
                  AND confrelid = 'doctor'::regclass)
          )
    LOOP
        EXECUTE format(
            'ALTER TABLE doctor_specialization DROP CONSTRAINT %I',
            constraint_row.conname
        );
    END LOOP;
END $$;
