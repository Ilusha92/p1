CREATE TABLE IF NOT EXISTS input (
    id SERIAL PRIMARY KEY,
    customer VARCHAR(256) NOT NULL,
    event_name VARCHAR(256) NOT NULL,
    event_location VARCHAR(256) NOT NULL,
    event_date DATE NOT NULL,
    event_work_start_time TIME NOT NULL,
    event_work_end_time TIME NOT NULL,
    visitors_count INT NOT NULL
    );
