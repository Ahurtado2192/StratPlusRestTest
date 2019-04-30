package net.stratplus.backdemo;



import net.stratplus.model.Response;
import net.stratplus.model.User;


public interface Persistence {

	public String findByUser(User usuario) throws Exception;

	public String registerUser(User registraUsuario) throws Exception;

	public boolean validatePassword(User registraUsuario);

	public String generateJWT(String user);
}
