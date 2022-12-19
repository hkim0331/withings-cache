CREATE TABLE meas (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  user_id INTEGER, -- index of `users` table
  meas    INT, -- value colum in `measures` table
  val     FLOAT,
  dt      VARCHAR(30) -- timestamp? or datetime?
  );
