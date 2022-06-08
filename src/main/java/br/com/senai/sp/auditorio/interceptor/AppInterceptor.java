package br.com.senai.sp.auditorio.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.sql.DecodeCaseFragment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.senai.sp.auditorio.annotation.Administrador;
import br.com.senai.sp.auditorio.annotation.Professor;
import br.com.senai.sp.auditorio.annotation.Suporte;
import br.com.senai.sp.auditorio.model.TipoDeUsuario;
import br.com.senai.sp.auditorio.rest.UsuarioRestController;
import net.bytebuddy.asm.Advice.AssignReturned.Handler;

@Component
public class AppInterceptor implements HandlerInterceptor{
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// variável para obter a URI
		String uri= request.getRequestURI();
		// variável para sessão
		HttpSession session = request.getSession();
		// se for página de erro libera
		if (uri.startsWith("/error")) {
			return true;
		}
		
		// verificar se Handler é um HandlerMethod
		// o que indica é que ele está chamando o método 
		// em algum controller
		
		if (handler instanceof HandlerMethod) {
			// casting de Object para HandlerMehod
			HandlerMethod metodo = (HandlerMethod) handler;
			if (uri.startsWith("/api")) {
				//	variável para o token
				String token = request.getHeader("Authorization");
				// verificar se é um método do professor
		
				if (metodo.getMethodAnnotation(Professor.class) != null) {
					
					//se o método for privado recupera o token
					token = request.getHeader("Authorization");
					// cria o algoritmo para assinar
					Algorithm algoritmo = Algorithm.HMAC256(UsuarioRestController.SECRET);
					// objeto para verificar o token 
					JWTVerifier verifier = JWT.require(algoritmo).
							withIssuer(UsuarioRestController.EMISSOR).build();
					// decodifica o Token
					DecodedJWT jwt = verifier.verify(token);
					// recupera os dados do payload
					Map<String, Claim> claims = jwt.getClaims();
					TipoDeUsuario tipoUsuario = TipoDeUsuario.values()[Integer.parseInt(claims.get("tipo").toString())];
					if(tipoUsuario == tipoUsuario.PROFESSOR) {
						return true;
					} else {
						response.sendError(HttpStatus.UNAUTHORIZED.value(), "Acesso Negado");
						return false;
					}
					
				} else {
					if (metodo.getMethodAnnotation(Administrador.class) != null) {
					//se o método for privado recupera o token
					token = request.getHeader("Authorization");
					// cria o algoritmo para assinar
					Algorithm algoritmo = Algorithm.HMAC256(UsuarioRestController.SECRET);
					// objeto para verificar o token 
					JWTVerifier verifier = JWT.require(algoritmo).
							withIssuer(UsuarioRestController.EMISSOR).build();
					// decodifica o Token
					DecodedJWT jwt = verifier.verify(token);
					// recupera os dados do payload
					Map<String, Claim> claims = jwt.getClaims();
					TipoDeUsuario tipoUsuario = TipoDeUsuario.values()[Integer.parseInt(claims.get("tipo").toString())];
					if(tipoUsuario == tipoUsuario.PROFESSOR) {
						return true;
					} else {
						response.sendError(HttpStatus.UNAUTHORIZED.value(), "Acesso Negado");
						return false;
					}
					
				} else if (metodo.getMethodAnnotation(Suporte.class) != null) {
					
					//se o método for privado recupera o token
					token = request.getHeader("Authorization");
					// cria o algoritmo para assinar
					Algorithm algoritmo = Algorithm.HMAC256(UsuarioRestController.SECRET);
					// objeto para verificar o token 
					JWTVerifier verifier = JWT.require(algoritmo).
							withIssuer(UsuarioRestController.EMISSOR).build();
					// decodifica o Token
					DecodedJWT jwt = verifier.verify(token);
					// recupera os dados do payload
					Map<String, Claim> claims = jwt.getClaims();
					TipoDeUsuario tipoUsuario = TipoDeUsuario.values()[Integer.parseInt(claims.get("tipo").toString())];
					if(tipoUsuario == tipoUsuario.PROFESSOR) {
						return true;
					} else {
						response.sendError(HttpStatus.UNAUTHORIZED.value(), "Acesso Negado");
						return false;
					}
					
				}
				
				
				return true;
			}	
			}
		}
		
		return true;
	}
}
