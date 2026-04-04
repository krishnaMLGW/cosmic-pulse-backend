CREATE TABLE IF NOT EXISTS solar_events (
    time            TIMESTAMPTZ      NOT NULL,
    kp_index        DOUBLE PRECISION NOT NULL,
    storm_category  VARCHAR(20)      NOT NULL,
    gps_score       INTEGER          NOT NULL,
    satellite_score INTEGER          NOT NULL,
    radio_score     INTEGER          NOT NULL,
    source          VARCHAR(50)      DEFAULT 'NOAA_SWPC'
);

SELECT create_hypertable('solar_events', 'time', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_solar_events_time     ON solar_events (time DESC);
CREATE INDEX IF NOT EXISTS idx_solar_events_category ON solar_events (storm_category, time DESC);
