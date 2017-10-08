INSERT INTO post VALUES (nextval('postid'), '00128', CURRENT_TIMESTAMP,
                         'Hi! this is Zhang!! Hi! this is Zhang!! Hi! this is Zhang!! Hi! this is Zhang!! Hi! this is Zhang!!');
INSERT INTO post VALUES (nextval('postid'), '00128', CURRENT_TIMESTAMP, 'I live in Mumbai.');
INSERT INTO post VALUES (nextval('postid'), '00128', CURRENT_TIMESTAMP, 'I like to sleep and eat');
INSERT INTO post VALUES (nextval('postid'), '00128', CURRENT_TIMESTAMP, 'I dont study much');
INSERT INTO post VALUES (nextval('postid'), '00128', CURRENT_TIMESTAMP, 'I play a lot of games');


INSERT INTO post VALUES (nextval('postid'), '12345', CURRENT_TIMESTAMP,
                         'Hi! This is Shankar!! Hi! This is Shankar!! Hi! This is Shankar!! Hi! This is Shankar!! Hi! This is Shankar!! Hi! This is Shankar!! ');
INSERT INTO post VALUES (nextval('postid'), '12345', CURRENT_TIMESTAMP, 'I live in Delhi');
INSERT INTO post VALUES (nextval('postid'), '12345', CURRENT_TIMESTAMP, 'I like to play games');
INSERT INTO post VALUES (nextval('postid'), '12345', CURRENT_TIMESTAMP, 'I watch a lot of movies');
INSERT INTO post VALUES (nextval('postid'), '12345', CURRENT_TIMESTAMP, 'I love to travel');


INSERT INTO post VALUES (nextval('postid'), '19991', CURRENT_TIMESTAMP, 'Hi! This is Brandt!!');
INSERT INTO post VALUES (nextval('postid'), '19991', CURRENT_TIMESTAMP, 'I live in Bareli');
INSERT INTO post VALUES (nextval('postid'), '19991', CURRENT_TIMESTAMP, 'I like to eat pan');
INSERT INTO post VALUES (nextval('postid'), '19991', CURRENT_TIMESTAMP, 'I play chess and ludo');
INSERT INTO post VALUES (nextval('postid'), '19991', CURRENT_TIMESTAMP, 'I love my country');
INSERT INTO post VALUES (nextval('postid'), '19991', CURRENT_TIMESTAMP, 'May be I can become a soldier one day');


INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'Hi! This is Chavez!!');
INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'I live in Calcutta');
INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'I love to eat fish');
INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'Sunday is my favrate day');
INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'I play football daily');
INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'We have the hugli river in my city');
INSERT INTO post VALUES (nextval('postid'), '23121', CURRENT_TIMESTAMP, 'Hi! This is Chavez!!');


INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'Hi! This is Peltier!!');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'I live in Patna!');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'There is so much hot here');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'I want to study in IIT');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'Can you help me in this?');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'One day I may become the PM of India');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'Watch out your weakness');
INSERT INTO post VALUES (nextval('postid'), '44553', CURRENT_TIMESTAMP, 'I am going to America');

INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '5', '12345', CURRENT_TIMESTAMP, 'This is a comment!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '5', '23121', CURRENT_TIMESTAMP, 'That is so cool!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '5', '44553', CURRENT_TIMESTAMP, 'Congratulations!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '5', '19991', CURRENT_TIMESTAMP, 'wow!!');


INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '1', '12345', CURRENT_TIMESTAMP, 'This is a comment!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '1', '23121', CURRENT_TIMESTAMP, 'That is so cool!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '1', '19991', CURRENT_TIMESTAMP, 'wow!!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '1', '19991', CURRENT_TIMESTAMP, 'super!!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '1', '19991', CURRENT_TIMESTAMP, 'I am so exicted!!');


INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '2', '12345', CURRENT_TIMESTAMP, 'This is a comment!');
INSERT INTO COMMENT (commentid, postid, uid, TIMESTAMP, TEXT)
VALUES (nextval('commentid'), '2', '23121', CURRENT_TIMESTAMP, 'That is so cool!');

SELECT *
FROM comment;







