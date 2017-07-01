DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `getAccountByEmail`(
IN in_email varchar(250),
OUT out_userid varchar(50),
OUT out_firstname varchar(100),
OUT out_lastname varchar(100)
)
BEGIN
SELECT userid, firstname, lastname
INTO out_userid,out_firstname,out_lastname
FROM account where email= in_email;

END;;
DELIMITER ;