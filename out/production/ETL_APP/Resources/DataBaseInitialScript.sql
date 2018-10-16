CREATE SEQUENCE dept_seq START WITH 1;

CREATE TABLE COMMENTS (
                        id NUMBER PRIMARY KEY,
                        author VARCHAR2(4000),
                        commentContent VARCHAR2(4000),
                        creationDate VARCHAR2(4000),
                        creationDateDB DATE 
                      ); 
                      

SELECT * FROM COMMENTS;                        
                        
                        