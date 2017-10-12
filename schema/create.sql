DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS password;
DROP TABLE IF EXISTS "user";
DROP SEQUENCE IF EXISTS postid;
DROP SEQUENCE IF EXISTS commentid;

CREATE SEQUENCE IF NOT EXISTS postid START 1;
CREATE SEQUENCE IF NOT EXISTS commentid START 1;

CREATE TABLE "user" (
  uid   VARCHAR(20),
  name  VARCHAR(20) NOT NULL,
  email VARCHAR(30),
  latest_post_offset INT DEFAULT 0,
  PRIMARY KEY (uid)
);

CREATE TABLE password (
  id       VARCHAR(20),
  password VARCHAR(20),
  PRIMARY KEY (id),
  FOREIGN KEY (id) REFERENCES "user" (uid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

CREATE TABLE follows (
  uid1 VARCHAR(20),
  uid2 VARCHAR(20),
  PRIMARY KEY (uid1, uid2),
  FOREIGN KEY (uid1) REFERENCES "user" (uid)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  FOREIGN KEY (uid2) REFERENCES "user" (uid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

CREATE TABLE post (
  postid    INT PRIMARY KEY DEFAULT nextval('postid'),
  uid       VARCHAR(20),
  timestamp TIMESTAMP,
  text      TEXT,
  image     TEXT            DEFAULT '',
  FOREIGN KEY (uid) REFERENCES "user" (uid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);


CREATE TABLE comment (
  commentid INT PRIMARY KEY DEFAULT nextval('commentid'),
  postid    INT REFERENCES post ON DELETE CASCADE,
  uid       VARCHAR(20),
  timestamp TIMESTAMP,
  text      TEXT,
  FOREIGN KEY (uid) REFERENCES "user" (uid)
  ON DELETE CASCADE
  ON UPDATE CASCADE,
  FOREIGN KEY (postid) REFERENCES post (postid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

INSERT INTO "user" VALUES ('00128', 'Zhang', 'user1@gmail.com');
INSERT INTO "user" VALUES ('12345', 'Shankar', 'user2@gmail.com');
INSERT INTO "user" VALUES ('19991', 'Brandt', 'user3@gmail.com');
INSERT INTO "user" VALUES ('23121', 'Chavez', 'user4@gmail.com');
INSERT INTO "user" VALUES ('44553', 'Peltier', 'user5@gmail.com');

INSERT INTO password VALUES ('00128', 'user1');
INSERT INTO password VALUES ('12345', 'user2');
INSERT INTO password VALUES ('19991', 'user3');
INSERT INTO password VALUES ('23121', 'user4');
INSERT INTO password VALUES ('44553', 'user5');

INSERT INTO follows VALUES ('00128', '12345');
INSERT INTO follows VALUES ('00128', '19991');
INSERT INTO follows VALUES ('00128', '44553');
INSERT INTO follows VALUES ('12345', '00128');
INSERT INTO follows VALUES ('12345', '23121');
INSERT INTO follows VALUES ('23121', '12345');
INSERT INTO follows VALUES ('23121', '00128');
INSERT INTO follows VALUES ('44553', '00128');
INSERT INTO follows VALUES ('44553', '12345');
INSERT INTO follows VALUES ('44553', '19991');
INSERT INTO follows VALUES ('44553', '23121');
