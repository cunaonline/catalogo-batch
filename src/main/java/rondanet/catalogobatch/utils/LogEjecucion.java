package rondanet.catalogobatch.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogEjecucion {
	@Autowired
	private Directorio directorioCarpeta;

	public void adicionarLogEjecucion(String linea) {
		String fileName = nombreArchivo();
		String urlLog = directorioCarpeta.getUrlbase() + "/Logs/" + fileName;
		
		try {
			
	        File file = new File(urlLog);

	        if (!file.exists())
	            file.createNewFile();

	        FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write(formatoLinea(linea));
	        bw.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Existe un problema en el fichero de logs  " + e);
			e.printStackTrace();
		}

	}

	private String formatoLinea(String linea) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now) + " " + linea+"\n";
	}

	private String nombreArchivo() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-M-dd");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now) + ".txt";
	}
}
