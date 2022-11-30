package rondanet.catalogobatch.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rondanet.catalogobatch.dto.ProductoBatchDTO;

@Component
public class GenerarArchivo {

	@Autowired
	private Directorio directorioCarpeta;

	@Autowired
	private LogEjecucion logEjecucion;
	
	public void generarDatos(List<ProductoBatchDTO> listaProductos, List<String> columnas, String extencion) {
		logEjecucion.adicionarLogEjecucion("Iniciando generacion del archivo en formato: "+extencion.toLowerCase());
		// poner una extencion por defecto para generar el archivo;
		if (extencion == null) {
			extencion = "txt";
		}
		// Obtener path para generar el archivo.
		String path = directorioCarpeta.getUrlbase();
		switch (extencion.toUpperCase()) {
		case "CSV":
			generarCSV(listaProductos, columnas, path);

			break;
		case "XLS":
			generarXLS(listaProductos, columnas, path);

			break;
		case "TXT":
			generarTXT(listaProductos, columnas, path);
			break;

		}
	}

	private void generarTXT(List<ProductoBatchDTO> listaProductos, List<String> columnas, String path) {
		try {

			String fileName = nombreArchivo();
			// Whatever the file path is.
			File ficheroText = new File(path + "/Recibido/" + fileName +".txt");
			FileOutputStream is = new FileOutputStream(ficheroText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);

			for (ProductoBatchDTO productoBatchDTO : listaProductos) {
				// w.write(productoBatchDTO);
				w.write(armarLineaTxt(productoBatchDTO, columnas));

			}
			w.close();
			logEjecucion.adicionarLogEjecucion("Archivo generado con éxito");
		} catch (IOException e) {
			System.err.println("Problema para escribir en el fichero " + e);
		}
	}

	private void generarXLS(List<ProductoBatchDTO> listaProductos, List<String> columnas, String path) {
		String fileName = nombreArchivo();

		// Crear libro de trabajo en blanco
		Workbook workbook = new HSSFWorkbook();
		// Crea hoja nueva
		Sheet sheet = workbook.createSheet("Productos");

		int numeroRenglon = 0;
		for (ProductoBatchDTO producto : listaProductos) {
			Row row = sheet.createRow(numeroRenglon++);
			List<String> datosColumnas = mapeoColumnas(producto, columnas);

			int numeroCelda = 0;

			for (String columna : datosColumnas) {
				Cell cell = row.createCell(numeroCelda++);
				// if (obj instanceof String) {
				cell.setCellValue((String) columna);
				/*
				 * } else if (obj instanceof Integer) { cell.setCellValue((Integer) obj); }
				 */
			}

		}
		try {
			// Se genera el documento
			File ficheroXls = new File(path + "/Recibido/" + fileName +".xls");
			FileOutputStream out = new FileOutputStream(ficheroXls);

			workbook.write(out);
			out.close();
			logEjecucion.adicionarLogEjecucion("Archivo generado con éxito");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void generarCSV(List<ProductoBatchDTO> listaProductos, List<String> columnas, String path) {

		try {

			String fileName = nombreArchivo();
			// Whatever the file path is.
			File ficheroText = new File(path + "/Recibido/" + fileName+".csv");
			FileOutputStream is = new FileOutputStream(ficheroText);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);

			for (ProductoBatchDTO productoBatchDTO : listaProductos) {
				w.write(armarLineaCsv(productoBatchDTO, columnas, "^"));

			}
			w.close();
			logEjecucion.adicionarLogEjecucion("Archivo generado con éxito");
		} catch (IOException e) {
			System.err.println("Problema para escribir en el fichero " + e);
		}

	}

	private String nombreArchivo() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMddHHmmss");
		LocalDateTime now = LocalDateTime.now();
		System.out.println(dtf.format(now));
		return dtf.format(now) ;
	}

	private String armarLineaTxt(ProductoBatchDTO productoBatchDTO, List<String> columnas) {
		String linea = "";
		List<String> columnasResultado = mapeoColumnas(productoBatchDTO, columnas);

		for (String dato : columnasResultado) {
			linea += dato;
		}
		return linea + "\n";
	}

	private String armarLineaCsv(ProductoBatchDTO productoBatchDTO, List<String> columnas, String delimitador) {
		String linea = "";
		List<String> columnasResultado = mapeoColumnas(productoBatchDTO, columnas);

		for (String dato : columnasResultado) {
			linea += dato + delimitador;
		}
		return linea.substring(0, linea.length() - 1) + "\n";
	}

	private List<String> mapeoColumnas(ProductoBatchDTO productoBatchDTO, List<String> columnas) {
		List<String> listaFinal = new ArrayList<String>();
		for (String columna : columnas) {
			switch (columna) {
			case "GLN":
				listaFinal.add(productoBatchDTO.getGln());
				break;
			case "CPP":
				listaFinal.add(productoBatchDTO.getCpp());
				break;
			case "DESCRIP":
				listaFinal.add(productoBatchDTO.getDescripcion());
				break;
			case "MARCA":
				listaFinal.add(productoBatchDTO.getMarca());
				break;
			case "DIVISION":
				listaFinal.add(productoBatchDTO.getDivision());
				break;
			case "LINEA":
				listaFinal.add(productoBatchDTO.getLinea());
				break;
			case "SUBLINEA":
				listaFinal.add(productoBatchDTO.getSubLinea());
				break;
			case "GTIN13":
				listaFinal.add(productoBatchDTO.getGtin());
				break;
			case "PRESENT_TIPO":
				listaFinal.add(productoBatchDTO.getPresentTipo());
				break;
			case "PRESENT_CANT":
				listaFinal.add(productoBatchDTO.getPresentCantidad());
				break;
			case "PRESENT_UNIDAD":
				listaFinal.add(productoBatchDTO.getPresentUnidad());
				break;
			case "PIDE_UNIDAD":
				listaFinal.add(productoBatchDTO.getPideUnidad());
				break;
			case "GTIN14":
				listaFinal.add(productoBatchDTO.getEmpaquesGtin());
				break;
			case "UNIDADES":
				listaFinal.add(productoBatchDTO.getUnidades() != null ? productoBatchDTO.getUnidades().toString() : "");
				break;
			case "EMPAQUES":
				listaFinal.add(productoBatchDTO.getEmpaques() != null ? productoBatchDTO.getEmpaques().toString() : "");
				break;
			case "CAMADAS":
				listaFinal.add(productoBatchDTO.getCamadas() != null ? productoBatchDTO.getCamadas().toString() : "");
				break;
			case "PAIS_ID":
				listaFinal.add(productoBatchDTO.getPaisOrigen());
				break;
			case "PAIS_NOMBRE":
				listaFinal.add(productoBatchDTO.getPaisOrigenNombre());
				break;
			case "ESPROMO":
				listaFinal
						.add(productoBatchDTO.getEsPromo() != null ? (productoBatchDTO.getEsPromo() ? "S" : "N") : "");
				break;
			case "FECHA_SUSPEND1":
				listaFinal.add(productoBatchDTO.getFechaSuspendidoDesde());
				break;
			case "FECHA_SUSPEND2":
				listaFinal.add(productoBatchDTO.getFechaSuspendidoHasta());
				break;
			case "CPP_PROMO":
				listaFinal.add(productoBatchDTO.getCppPromo());
				break;
			case "FECHA_ALTA":
				listaFinal.add(productoBatchDTO.getFechaAlta());
				break;
			case "ESTADO":
				listaFinal.add(productoBatchDTO.getEstado());
				break;
			case "VIGENCIA":
				listaFinal.add(productoBatchDTO.getVigencia());
				break;
			case "TIPOIVA":
				listaFinal.add(productoBatchDTO.getTipoIva());
				break;
			case "MONEDA":
				listaFinal.add(productoBatchDTO.getMoneda());
				break;
			case "PRBASE":
				listaFinal.add(
						productoBatchDTO.getPrecioBase() != null ? productoBatchDTO.getPrecioBase().toString() : "");
				break;
			case "PRCONDTO":
				listaFinal.add(productoBatchDTO.getPrecioConDescuentos() != null
						? productoBatchDTO.getPrecioConDescuentos().toString()
						: "");
				break;
			case "PRTOTAL":
				listaFinal.add(
						productoBatchDTO.getPrecioTotal() != null ? productoBatchDTO.getPrecioTotal().toString() : "");
				break;
			case "PRSUGERIDO":
				listaFinal.add(
						productoBatchDTO.getPrecioSugerido() != null ? productoBatchDTO.getPrecioSugerido().toString()
								: "");
				break;
			}
		}
		return listaFinal;
	}
}
