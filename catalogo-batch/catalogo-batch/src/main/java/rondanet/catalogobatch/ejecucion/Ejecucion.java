package rondanet.catalogobatch.ejecucion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rondanet.catalogobatch.dto.ProductoBatchDTO;
import rondanet.catalogobatch.exeptions.ColumnasException;
import rondanet.catalogobatch.exeptions.EmpresaException;
import rondanet.catalogobatch.exeptions.FormatoParametroException;
import rondanet.catalogobatch.negocio.Descargar;
import rondanet.catalogobatch.utils.Directorio;
import rondanet.catalogobatch.utils.GenerarArchivo;
import rondanet.catalogobatch.utils.LoadConfigIni;
import rondanet.catalogobatch.utils.ValidarDatosEntrada;

@Component
public class Ejecucion {
	@Autowired
	private ValidarDatosEntrada validarDatos;
	@Autowired
	private Descargar desacargarInformacion;
	@Autowired
	private GenerarArchivo generarArchivo;
	@Autowired
	private LoadConfigIni loadConfigIni;
	@Autowired
	private Directorio directorioCarpeta;


	public Ejecucion(ValidarDatosEntrada validarDatos, Descargar desacargar,
			LoadConfigIni loadEmpresasYColumnasProperties) {
		this.validarDatos = validarDatos;
		this.loadConfigIni = loadEmpresasYColumnasProperties;
		this.desacargarInformacion = desacargar;
	}

	public void runProgram(String[] parametrosEntradaCommandLine) {

		System.out.println("Iniciando ejecución batch");
		

		// Validar parámetros de Entrada.

		boolean parametrosValidos;
		try {
			parametrosValidos = validarDatos.validarDatosEntrada(parametrosEntradaCommandLine);

			if (parametrosValidos) {
				
				// Leer el fichero con las empresas y columnas

				loadConfigIni.readFicheroIni();
				
				//Crear directorio de Archivos
				
			    directorioCarpeta.crearDirectorio(validarDatos.getParametros().get("empresa"));
		       
				// Obtener Productos

				List<ProductoBatchDTO> listaProductos = desacargarInformacion.descargarProductos(loadConfigIni.getEmpresas(),
						validarDatos.getParametros());

				// Generar Archivos

				generarArchivo.generarDatos(listaProductos, loadConfigIni.getColumnas(),
						validarDatos.getParametros().get("formato"));

				
			}else {
				System.out.println("Faltan parámetros para completar la ejecución");
			}
		} catch (FormatoParametroException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmpresaException | ColumnasException e) {
			// TODO: handle exception

		}

		System.out.println("Finaliza ejecución batch");

	}

}
