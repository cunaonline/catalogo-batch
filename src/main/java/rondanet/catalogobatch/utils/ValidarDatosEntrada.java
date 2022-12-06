package rondanet.catalogobatch.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rondanet.catalogobatch.exeptions.FormatoParametroException;

@Component
public class ValidarDatosEntrada {
	
	Map<String, String> parametros;

	public boolean validarDatosEntrada(String[] input) throws FormatoParametroException {
		 parametros = new HashMap<String, String>();
		 System.out.println("Inicia validacion parámetros de entrada");

		for (String parametro : input) {
			if (validarParametroFormato(parametro)) {
				// Obtener letra que identifica al parámetro y valor del parámetro
				String[] datos = parametro.split("=");

				switch (datos[0].toUpperCase()) {
				case "L":
					validarUsuarioPassword(datos[1]);
					break;
				case "G":
					guardarParametro("empresa", datos[1]);
					break;
				case "F":
					guardarParametro("formato", datos[1]);
					break;
				}
			} else {
				System.out.println("Parámetro con formato incorrecto: " + parametro);
				System.out.println("Finaliza validacion parámetros de entrada");
				throw new FormatoParametroException("Parámetro con formato incorrecto");
			}
		}
		System.out.println("Finaliza validacion parámetros de entrada");
		
		return parametros.get("usuario")!= null && parametros.get("password")!= null && parametros.get("empresa")!= null;
	}

	private boolean validarParametroFormato(String parametro) {
		return parametro.contains("=");
	}

	private void guardarParametro(String key, String parametro) {
		parametros.put(key, parametro);
	}

	private void validarUsuarioPassword(String usuariopassword) {

		String[] datos = usuariopassword.split(":");

		guardarParametro("usuario", datos[0]);
		guardarParametro("password", datos[1]);

	}


	public Map<String, String> getParametros() {
		return parametros;
	}

	public void setParametros(Map<String, String> parametros) {
		this.parametros = parametros;
	}
	
	
}
