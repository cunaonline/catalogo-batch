package rondanet.catalogobatch.ejecucion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import common.rondanet.catalogo.core.dto.EmpresaDTO;
import common.rondanet.catalogo.core.dto.ProductoBatchDTO;
import common.rondanet.catalogo.core.utils.generarArchivos.GenerarArchivoProducto;
import rondanet.catalogobatch.exeptions.ColumnasException;
import rondanet.catalogobatch.exeptions.EmpresaException;
import rondanet.catalogobatch.exeptions.FormatoParametroException;
import rondanet.catalogobatch.negocio.Descargar;
import rondanet.catalogobatch.utils.Directorio;

import rondanet.catalogobatch.utils.LoadConfigIni;
import rondanet.catalogobatch.utils.ValidarDatosEntrada;

@Component
public class Ejecucion {
	@Autowired
	private ValidarDatosEntrada validarDatos;
	@Autowired
	private Descargar desacargarInformacion;

	private GenerarArchivoProducto generarArchivo;
	@Autowired
	private LoadConfigIni loadConfigIni;
	@Autowired
	private Directorio directorioCarpeta;

	public Ejecucion(ValidarDatosEntrada validarDatos, Descargar desacargar,
			LoadConfigIni loadEmpresasYColumnasProperties) {
		this.validarDatos = validarDatos;
		this.loadConfigIni = loadEmpresasYColumnasProperties;
		this.desacargarInformacion = desacargar;
		this.generarArchivo = new GenerarArchivoProducto();
	}

	public void runProgram(String[] parametrosEntradaCommandLine) {

		System.out.println("Iniciando ejecución batch"); 

		// Validar parámetros de Entrada.

		boolean parametrosValidos;
		try {
			parametrosValidos = validarDatos.validarDatosEntrada(parametrosEntradaCommandLine);

			if (parametrosValidos) {

				// Crear directorio de Archivos

				directorioCarpeta.crearDirectorio(validarDatos.getParametros().get("empresa"));

				// Leer el fichero con las empresas y columnas

				loadConfigIni.readFicheroIni();

				//Verifico Proveedores de la Empresa vs Proveedores Fichero Configuracion
				List<EmpresaDTO>  empresasVerificadas = desacargarInformacion.verificarProveedoresCatalogo(loadConfigIni.getEmpresas(), validarDatos.getParametros());

				// Obtener Productos y generar Archivo con productos
				for (EmpresaDTO empresaProveedoreVerificada : empresasVerificadas) {
					// Obtener los productos por empresa

					List<ProductoBatchDTO> listaProductos = desacargarInformacion
							.descargarProductos(empresaProveedoreVerificada, validarDatos.getParametros());

					if(listaProductos.size()>0) {
					// Generar Archivos
					if (validarDatos.getParametros().get("formato").equals("CSV"))
						generarArchivo.crearArchivoCSV(listaProductos, loadConfigIni.getColumnas(),
								directorioCarpeta.getUrlbase(), empresaProveedoreVerificada.getGln());
					else if (validarDatos.getParametros().get("formato").equals("DBF"))
						generarArchivo.crearArchivoDBF(listaProductos, loadConfigIni.getColumnas(),
								directorioCarpeta.getUrlbase(), empresaProveedoreVerificada.getGln());
					}
				}
			} else {
				System.out.println("Faltan parámetros para completar la ejecución");
			}
		} catch (FormatoParametroException e) {
			System.out.println("El Formato de los parametros no son validos");
		}  catch (EmpresaException e) {
			System.out.println("No hay empresas validas para generar el archivo de productos");
		} catch (ColumnasException e) {
			System.out.println("No hay columnas validas para generar el archivo de productos");
		}catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Finaliza ejecución batch");

	}

}
