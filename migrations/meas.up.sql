CREATE TABLE meas (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  user_id INTEGER,  -- index of `users` table
  type    INT,      -- value colum in `measures` table
  measure FLOAT,    -- :measures [{:value 94400, :type 1, :unit -3, :algo 0, :fm 5}]
  created TIMESTAMP -- from_unixtime(1667176189)
  );
