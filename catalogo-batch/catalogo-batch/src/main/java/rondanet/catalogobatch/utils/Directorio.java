package rondanet.catalogobatch.utils;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Directorio {
    private String urlbase;
	@Value("${name-jar}")
	private String nameJar;
	public void crearDirectorio(String empresa) {
		String currentPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().split(nameJar)[0]
				.replace("file:/", "");
		String downloadFolder = "/" + empresa;
		String downloadPath = currentPath + downloadFolder;
		File newFolder = new File(downloadPath);
		newFolder.mkdir();
		newFolder = new File(downloadPath + "/Recibido");
		newFolder.mkdir();
		newFolder = new File(downloadPath + "/Logs");
		newFolder.mkdir();
		newFolder = new File(downloadPath + "/AEnviar");
		newFolder.mkdir();
		System.out.println(downloadPath);
		urlbase =  downloadPath;
	}
	public String getUrlbase() {
		return urlbase;
	}
	public void setUrlbase(String urlbase) {
		this.urlbase = urlbase;
	}
	
}
