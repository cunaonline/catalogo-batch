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

	public List<ProductoBatchDTO> descargarProductos(EmpresaDTO empresaDTO, Map<String, String> parametros) {

		String token;
		List<ProductoBatchDTO> listaProductos = new ArrayList<ProductoBatchDTO>();
		System.out.println("=======================================================================\n"+"Iniciando descarga de productos del catálogo para la empresa: "+empresaDTO.getGln());
		logEjecucion.adicionarLogEjecucion("=======================================================================");		
		logEjecucion.adicionarLogEjecucion("Iniciando descarga de productos del catálogo para la empresa: "+empresaDTO.getGln());
		try {
			// Obtengo token para Login
			token = this.restManager.getLogin(parametros.get("usuario"), parametros.get("password"),
					parametros.get("empresa"));
		
			// Obtengo productos por empresas
		
				listaProductos.addAll(this.restManager.productosByEmpresa(token, empresaDTO));
			
			logEjecucion.adicionarLogEjecucion("Finaliza descarga de productos del catálogo  para la empresa: "+empresaDTO.getGln()+", se obtuvieron : " + listaProductos.size()+" productos");
			logEjecucion.adicionarLogEjecucion("=======================================================================");		
			System.out.println("Finaliza descarga de productos del catálogo  para la empresa: "+empresaDTO.getGln()+", se obtuvieron : " + listaProductos.size()+" productos\n=======================================================================");
			return listaProductos;

		} catch (ProveedorException e) {
			System.out.println("No se pudo obtener los productos para la empresa: "+empresaDTO.getGln());
			e.printStackTrace();
		}
		 catch (ProductoException e) {
				System.out.println("No se pudo obtener los productos para la empresa: "+empresaDTO.getGln());
				e.printStackTrace();
			}
		catch (Exception e) {
			System.out.println("Se produjo un error inesperado");
			e.printStackTrace();
		}
		return listaProductos;

	}

	public List<EmpresaDTO> verificarProveedoresCatalogo(List<String> empresasFicheroConfiguracion, Map<String, String> parametros) {

		String token;
		List<ProductoBatchDTO> listaProductos = new ArrayList<ProductoBatchDTO>();
		List<EmpresaDTO> listaProveedores = new ArrayList<EmpresaDTO>();
		List<EmpresaDTO> listaProveedoresFiltrada = new ArrayList<EmpresaDTO>();
		try {
			// Obtengo token para Login
			token = this.restManager.getLogin(parametros.get("usuario"), parametros.get("password"),
					parametros.get("empresa"));
			// Obtengo los proveedores mediante servicios
			listaProveedores = this.restManager.proveedoresByEmpresa(token);
			// Filtro Porveedores obtenidos por servicos con el obtenido del fichero
			System.out.println("Verificar proveedores obtenidos de configuracion");
			logEjecucion.adicionarLogEjecucion("Verificar proveedores obtenidos de configuracion");
			for (String empresaFicheroConfiguracion : empresasFicheroConfiguracion) {
				listaProveedoresFiltrada.addAll(checkProveedores(listaProveedores, empresaFicheroConfiguracion));
			}
			System.out.println("Cantidad proveedores verificados: "+listaProveedoresFiltrada.size());
			logEjecucion.adicionarLogEjecucion("Cantidad proveedores verificados: "+listaProveedoresFiltrada.size());	
		return listaProveedoresFiltrada;

		} 
		catch (Exception e) {
			System.out.println("Se produjo un error inesperado");
			e.printStackTrace();
		}
		return listaProveedoresFiltrada;

	}
	private List<EmpresaDTO> checkProveedores(List<EmpresaDTO> listaProveedoresServicio,
			String empresasProveedorasFichero) {

			List<EmpresaDTO> empresaValida = listaProveedoresServicio.stream().filter(t -> t.getGln().equals(empresasProveedorasFichero))
					.collect(Collectors.toList());

		System.out.println("Proveedor: "+ empresasProveedorasFichero + (empresaValida.size()>0 ? " Valido": " No Valido"));
		logEjecucion.adicionarLogEjecucion("Proveedor: "+ empresasProveedorasFichero + (empresaValida.size()>0 ? " Valido": " No Valido"));
		return empresaValida;
	}

}
