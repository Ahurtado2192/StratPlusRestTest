CREATE DATABASE sampledb;
    
CREATE TABLE `Usuarios` (
	`userId` VARCHAR(50) NOT NULL,
	`password` VARCHAR(50) NOT NULL,
	`intentos` INT NOT NULL,
	PRIMARY KEY (`userId`)
);
