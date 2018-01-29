CREATE TABLE client_charges
(
  id           INT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  booking_date DATETIME     NOT NULL,
  power        DECIMAL      NOT NULL
);

CREATE TABLE hourly_power
(
  fk_provider_id INT     NOT NULL,
  power          DECIMAL NOT NULL,
  hour           TINYINT NOT NULL
);

CREATE TABLE providers
(
  id   INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL
);
