package rondanet.catalogobatch.utils;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Directorio {
	private String urlbase;

	public void crearDirectorio(String empresa) {
		String urlPath = new File("").getAbsolutePath();

		String downloadPath = urlPath + "/" + empresa;
		File newFolder = new File(downloadPath);
		newFolder.mkdir();
		newFolder = new File(downloadPath + "/Recibido");
		newFolder.mkdir();
		newFolder = new File(downloadPath + "/Logs");
		newFolder.mkdir();
		newFolder = new File(downloadPath + "/AEnviar");
		newFolder.mkdir();
		System.out.println("Directorio de Carpetas " + downloadPath);
		urlbase = downloadPath;
	}

	public String getUrlbase() {
		return urlbase;
	}

	public void setUrlbase(String urlbase) {
		this.urlbase = urlbase;
	}

}
