package rondanet.catalogobatch.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import rondanet.catalogobatch.consumirservicios.RestManager;
import rondanet.catalogobatch.ejecucion.Ejecucion;

@RestController
public class ServicesController {
	RestManager restManager;
	Ejecucion ejecucion;

	public ServicesController(RestManager restManager, Ejecucion ejecucion) {
		this.restManager = restManager;
		this.ejecucion = ejecucion;
	}

	@GetMapping("/comanndLine")
	public void LineaComando() throws Exception {
		String[] args = {
				"l=alalvarez9112@gmail.com:Ariam123" , "g=7730908000002", "f=CSV", "e=A", "d=01.01.2005" };

		this.ejecucion.runProgram(args);
	}

}