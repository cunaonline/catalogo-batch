package rondanet.catalogobatch.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rondanet.catalogobatch.exeptions.ColumnasException;
import rondanet.catalogobatch.exeptions.EmpresaException;

@Component
public class LoadConfigIni {
	@Value("${name-file-ini}")
	private String nameFileIni;
	@Value("${name-jar}")
	private String nameJar;
	private String initEmpresasYColumnasPath;
	private List<String> empresas;
	private List<String> columnas;
	private boolean lineasEmpresa;
	private boolean lineasColumnas;

	public void readFicheroIni() throws EmpresaException, ColumnasException {
		System.out.println("Leer empresas y columnas");
		String linea;
		empresas = new ArrayList<String>();
		columnas = new ArrayList<String>();

		String urlPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().split(nameJar)[0]
				.replace("file:/", "");
		initEmpresasYColumnasPath = urlPath + nameFileIni;
		System.out.println("Url: " + initEmpresasYColumnasPath);
		FileReader archivo;
		try {
			archivo = new FileReader(initEmpresasYColumnasPath);
			BufferedReader buffer = new BufferedReader(archivo);
			try {
				while ((linea = buffer.readLine()) != null) {

					if (linea.trim().equals("[Proveedores]")) {
						lineasEmpresa = true;
						lineasColumnas = false;
					}
					if (linea.trim().equals("[Columnas]")) {
						lineasEmpresa = false;
						lineasColumnas = true;
					}
					if (lineasEmpresa && linea.contains("=")) {
						empresas.add(procesarLinea(linea));
					}
					if (lineasColumnas && linea.contains("=")) {
						columnas.add(procesarLinea(linea));
					}

				}
				buffer.close();
			} catch (IOException e) {
				System.out.println("No se ha podido leer el fichero CatalogoBatch.ini");
				e.printStackTrace();
			}
			if (lineasEmpresa && empresas.isEmpty()) {
				throw new EmpresaException("No hay empresas en el fichero");
			}
			if (lineasColumnas && columnas.isEmpty()) {
				throw new ColumnasException("No hay columnas en el fichero");
			}
		} catch (FileNotFoundException e) {
			System.out.println("No se encuentra el fichero CatalogoBatch.ini en la ruta " + initEmpresasYColumnasPath);
			e.printStackTrace();
		}
	}

	public String procesarLinea(String linea) {
		return linea.split("=")[0];
	}

	public List<String> getEmpresas() {
		return empresas;
	}

	public void setEmpresas(List<String> empresas) {
		this.empresas = empresas;
	}

	public List<String> getColumnas() {
		return columnas;
	}

	public void setColumnas(List<String> columnas) {
		this.columnas = columnas;
	}

}
