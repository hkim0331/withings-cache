-- create a mysql user, not withings users.
CREATE USER 'user'@'localhost' IDENTIFIED BY 'secret';
GRANT ALL ON withings.* TO 'user'@'localhost';
