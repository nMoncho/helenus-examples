CREATE TYPE IF NOT EXISTS address (
    street              TEXT,
    city                TEXT,
    state_or_province   TEXT,
    postal_code         TEXT,
    country             TEXT
);

CREATE TABLE IF NOT EXISTS hotels_by_poi (
    poi_name        TEXT,
    hotel_id        TEXT,
    name            TEXT,
    phone           TEXT,
    address         FROZEN<address>,
    PRIMARY KEY ((poi_name), hotel_id)
) WITH comment = 'Q1. Find hotels near given poi'
AND CLUSTERING ORDER BY (hotel_id ASC);

CREATE TABLE IF NOT EXISTS hotels (
    id          TEXT PRIMARY KEY,
    name        TEXT,
    phone       TEXT,
    address     FROZEN<address>,
    pois        SET<TEXT>
) WITH comment = 'Q2. Find information about a hotel';

CREATE TABLE IF NOT EXISTS pois_by_hotel (
    poi_name        TEXT,
    hotel_id        TEXT,
    description     TEXT,
    PRIMARY KEY ((hotel_id), poi_name)
) WITH comment = 'Q3. Find pois near a hotel';

CREATE TABLE IF NOT EXISTS available_rooms_by_hotel_date (
    hotel_id            TEXT,
    date                DATE,
    room_number         SMALLINT,
    is_available        BOOLEAN,
    PRIMARY KEY ((hotel_id), date, room_number)
) WITH comment = 'Q4. Find available rooms by hotel / date';

CREATE TABLE IF NOT EXISTS amenities_by_room (
    hotel_id            TEXT,
    room_number         SMALLINT,
    amenity_name        TEXT,
    description         TEXT,
    PRIMARY KEY ((hotel_id, room_number), amenity_name)
) WITH comment = 'Q5. Find amenities for a room';
