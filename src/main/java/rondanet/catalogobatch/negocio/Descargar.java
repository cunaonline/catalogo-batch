package rondanet.catalogobatch.negocio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import common.rondanet.catalogo.core.dto.EmpresaDTO;
import common.rondanet.catalogo.core.dto.ProductoBatchDTO;
import rondanet.catalogobatch.consumirservicios.RestManager;
import rondanet.catalogobatch.exeptions.ProductoException;
import rondanet.catalogobatch.exeptions.ProveedorException;
import rondanet.catalogobatch.utils.LogEjecucion;

@Component
public class Descargar {

	RestManager restManager;

	String usuario, password, empresa;
	
	@Autowired
	private LogEjecucion logEjecucion;

	public Descargar(RestManager restManager) {
		this.restManager = restManager;
	}

	public List<ProductoBatchDTO> descargarProductos(String empresasProveedoras, Map<String, String> parametros) {

		String token;
		List<ProductoBatchDTO> listaProductos = new ArrayList<ProductoBatchDTO>();
		List<EmpresaDTO> listaProveedores = new ArrayList<EmpresaDTO>();
		List<EmpresaDTO> listaProveedoresFiltrada = new ArrayList<EmpresaDTO>();
		System.out.println("=======================================================================\n"+"Iniciando descarga de productos del catálogo para la empresa: "+empresasProveedoras);
		logEjecucion.adicionarLogEjecucion("=======================================================================");		
		logEjecucion.adicionarLogEjecucion("Iniciando descarga de productos del catálogo para la empresa: "+empresasProveedoras);
		try {
			// Obtengo token para Login
			token = this.restManager.getLogin(parametros.get("usuario"), parametros.get("password"),
					parametros.get("empresa"));
			// Obtengo los proveedores mediante servicios
			listaProveedores = this.restManager.proveedoresByEmpresa(token);
			// Filtro Porveedores obtenidos por servicos con el obtenido del fichero
			listaProveedoresFiltrada = checkProveedores(listaProveedores, empresasProveedoras);
			// Obtengo productos por empresas
			for (EmpresaDTO empresaDTO : listaProveedoresFiltrada) {	
				listaProductos.addAll(this.restManager.productosByEmpresa(token, empresaDTO));
			}
			logEjecucion.adicionarLogEjecucion("Finaliza descarga de productos del catálogo  para la empresa: "+empresasProveedoras+", se obtuvieron : " + listaProductos.size()+" productos");
			logEjecucion.adicionarLogEjecucion("=======================================================================");		
			System.out.println("Finaliza descarga de productos del catálogo  para la empresa: "+empresasProveedoras+", se obtuvieron : " + listaProductos.size()+" productos\n=======================================================================");
			return listaProductos;

		} catch (ProveedorException e) {
			System.out.println("No se pudo obtener los productos para la empresa: "+empresasProveedoras);
			e.printStackTrace();
		}
		 catch (ProductoException e) {
				System.out.println("No se pudo obtener los productos para la empresa: "+empresasProveedoras);
				e.printStackTrace();
			}
		catch (Exception e) {
			System.out.println("Se produjo un error inesperado");
			e.printStackTrace();
		}
		return listaProductos;

	}

	private List<EmpresaDTO> checkProveedores(List<EmpresaDTO> listaProveedoresServicio,
			String empresasProveedorasFichero) {
		System.out.println("Verificar  proveedores");
		logEjecucion.adicionarLogEjecucion("Verificar  proveedores");
	
			List<EmpresaDTO> empresaValida = listaProveedoresServicio.stream().filter(t -> t.getGln().equals(empresasProveedorasFichero))
					.collect(Collectors.toList());

		System.out.println("Proveedores verificados: " + empresaValida.size());
		logEjecucion.adicionarLogEjecucion("Proveedores verificados: " + empresaValida.size());
		return empresaValida;
	}

}
