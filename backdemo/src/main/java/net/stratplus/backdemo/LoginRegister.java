package net.stratplus.backdemo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;


import net.stratplus.model.Response;
import net.stratplus.model.User;

@Component
public class LoginRegister implements Persistence {

	private Connection connection;

	protected static final Logger LOGGER = LoggerFactory.getLogger(LoginRegister.class);
	protected static final String ERR_001 = "ERR_001 The database driver could not be loaded";
	protected static final String ERR_002 = "ERR_002 Could not get a connection to the database";
	protected static final String ERR_003 = "ERR_003 Could not close a connection to the database";
	protected static final String FINDBYUSER = "Select * From sampledb.Usuarios where UserId=?";
	protected static final String UPDATETRY = "Update sampledb.Usuarios set Intentos=? where UserId=?";
	protected static final String INSERTUSER = "Insert into sampledb.Usuarios (UserId,Intentos,Password) values(?,?,?)";

	@Value("${spring.datasource.password}")
	private String password;
	@Value("${spring.datasource.url}")
	private String url;
	@Value("${spring.datasource.classforname}")
	private String classForName;
	@Value("${spring.datasource.usuario}")
	private String usuario;

	public void inizializar() {
		try {
			Class.forName(classForName);
			connection = DriverManager.getConnection(url, usuario, password);
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error(ERR_001, cnfe);
		} catch (SQLException sqle) {
			LOGGER.error(ERR_002, sqle);
		}
	}

	private ObjectMapper mapper = new ObjectMapper();

	
	@Override
	public String findByUser(User usuario) throws Exception {
		 
		inizializar();
		PreparedStatement psSelect = connection.prepareStatement(FINDBYUSER);
		psSelect.setString(1, usuario.getUserId());
		ResultSet rs = psSelect.executeQuery();
		User user = new User();
		Integer intentos = 0;
		if (rs.next()) {
			user.setUserId(rs.getString("userId"));
			user.setPassword(rs.getString("password"));
			intentos = rs.getInt("intentos");
			if (intentos < 5) {
				if (usuario.getPassword().equals(user.getPassword())) {
					Response respuesta = new Response();
					respuesta.setStatus(202);
					respuesta.setMessage("Bienvenido");
					respuesta.setToken(generateJWT(user.userId));
					return mapper.writeValueAsString(respuesta);
				} else {
					PreparedStatement psUpdate = getConnection().prepareStatement(UPDATETRY);
					psUpdate.setInt(1, intentos + 1);
					psUpdate.setString(2, user.getUserId());
					psUpdate.execute();
					Response respuesta = new Response();
					respuesta.setStatus(401);
					respuesta.setMessage("Contraseña incorrecta, intente de nuevo");
					respuesta.setToken(null);
					return mapper.writeValueAsString(respuesta);
				}
			} else {
				Response respuesta = new Response();
				respuesta.setStatus(403);
				respuesta.setMessage("Usuario Bloqueado");
				respuesta.setToken(null);
				return mapper.writeValueAsString(respuesta);
			}
		} else {
			Response respuesta = new Response();
			respuesta.setStatus(404);
			respuesta.setMessage("Usuario no registrado por favor intente de nuevo");
			respuesta.setToken(null);
			return mapper.writeValueAsString(respuesta);
		}
	}

	@Override
	public String registerUser(User registraUsuario) throws Exception{

		PreparedStatement psSelect = getConnection().prepareStatement(FINDBYUSER);
		psSelect.setString(1, registraUsuario.getUserId());
		ResultSet rs = psSelect.executeQuery();

		if (rs.next()) {
			Response respuesta = new Response();
			respuesta.setStatus(500);
			respuesta.setMessage("Ya existe un usuario con ese ID");
			respuesta.setToken(null);
			return mapper.writeValueAsString(respuesta);
		} else {
			if (validatePassword(registraUsuario)) {
				PreparedStatement psInsert = getConnection().prepareStatement(INSERTUSER);
				psInsert.setString(1, registraUsuario.getUserId());
				psInsert.setInt(2, 0);
				psInsert.setString(3, registraUsuario.getPassword());
				psInsert.execute();
				Response respuesta = new Response();
				respuesta.setStatus(202);
				respuesta.setMessage("Usuario Registrado");
				respuesta.setToken(null);
				return mapper.writeValueAsString(respuesta);
			} else {
				Response respuesta = new Response();
				respuesta.setStatus(500);
				respuesta.setMessage("El Password es Invalido no cumple con los requsistos de seguridad o no coincide");
				respuesta.setToken(null);
				return mapper.writeValueAsString(respuesta);
			}
		}
	}

	@Override
	public boolean validatePassword(User registraUsuario) {
		String userPassword = registraUsuario.getPassword();

		char clave;
		Integer contNumero = 0, contLetraMay = 0;
		for (byte i = 0; i < userPassword.length(); i++) {
			clave = userPassword.charAt(i);
			String passValue = String.valueOf(clave);
			if (passValue.matches("[A-Z]")) {
				contLetraMay++;
			} else if (passValue.matches("[0-9]")) {
				contNumero++;
			}
		}

		if (userPassword.length() > 9 && userPassword.length() < 11 && contLetraMay > 0 && contNumero > 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public String generateJWT(String user) {

		String secretKey = "mySecretKey";

		String token = Jwts.builder().setId("softtekJWT").setSubject(user)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 600000))
				.signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

		return  token;

	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}