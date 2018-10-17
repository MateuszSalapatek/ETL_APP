CREATE SEQUENCE id_seq START WITH 1;

CREATE TABLE COMMENTS (
                        id NUMBER  PRIMARY KEY  ,
                        author VARCHAR2(4000),
                        commentTitle VARCHAR2(4000),
                        commentContent VARCHAR2(4000),
                        filmRate VARCHAR2(4000),
                        creationDate VARCHAR2(4000),
                        filmTittle VARCHAR2(4000),
                        filmYear NUMBER,
                        filmTime VARCHAR2(4000),
                        creationDateDB DATE
                      ); 
                      
                      

                      
                        
                        