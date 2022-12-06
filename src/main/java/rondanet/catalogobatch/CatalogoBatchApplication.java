package rondanet.catalogobatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import rondanet.catalogobatch.ejecucion.Ejecucion;

@SpringBootApplication
public class CatalogoBatchApplication implements CommandLineRunner {
	@Autowired
	private Ejecucion ejecucion;

	public static void main(String[] args) {
		SpringApplication.run(CatalogoBatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

    // Ejecucion batch 
     this.ejecucion.runProgram(args);
	}
}
