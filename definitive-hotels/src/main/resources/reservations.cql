CREATE TYPE address (
    street                  TEXT,
    city                    TEXT,
    state_or_province       TEXT,
    postal_code             TEXT,
    country                 TEXT
);

CREATE TABLE reservations_by_hotel_date (
    hotel_id                TEXT,
    start_date              DATE,
    end_date                DATE,
    room_number             SMALLINT,
    confirm_number          TEXT,
    guest_id                UUID,
    PRIMARY KEY ((hotel_id, start_date), room_number)
) WITH comment = 'Q7. Find reservations by hotel and date';

CREATE MATERIALIZED VIEW reservations_by_confirmation AS
    SELECT * FROM reservations_by_hotel_date
    WHERE confirm_number IS NOT NULL and hotel_id IS NOT NULL and
        start_date IS NOT NULL and room_number IS NOT NULL
PRIMARY KEY (confirm_number, hotel_id, start_date, room_number);

CREATE TABLE reservations_by_guest (
    guest_last_name         TEXT,
    hotel_id                TEXT,
    start_date              DATE,
    end_date                DATE,
    room_number             SMALLINT,
    confirm_number          TEXT,
    guest_id                UUID,
    PRIMARY KEY ((guest_last_name), hotel_id)
) WITH comment = 'Q8. Find reservations by guest name';

CREATE TABLE guests (
    guest_id            UUID PRIMARY KEY,
    first_name          TEXT,
    last_name           TEXT,
    title               TEXT,
    emails              SET<TEXT>,
    phone_numbers       LIST<TEXT>,
    addresses           MAP<TEXT, FROZEN<address>>,
    confirm_number      TEXT
) WITH comment = 'Q9. Find guest by ID';
