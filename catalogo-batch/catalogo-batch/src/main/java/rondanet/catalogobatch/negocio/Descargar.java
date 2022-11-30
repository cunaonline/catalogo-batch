package rondanet.catalogobatch.negocio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import common.rondanet.catalogo.core.dto.EmpresaDTO;
import rondanet.catalogobatch.consumirservicios.RestManager;
import rondanet.catalogobatch.dto.ProductoBatchDTO;
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

	public List<ProductoBatchDTO> descargarProductos(List<String> empresasProveedoras, Map<String, String> parametros) {

		String token;
		List<ProductoBatchDTO> listaProductos = new ArrayList<ProductoBatchDTO>();
		List<EmpresaDTO> listaProveedores = new ArrayList<EmpresaDTO>();
		List<EmpresaDTO> listaProveedoresFiltrada = new ArrayList<EmpresaDTO>();
		System.out.println("Iniciando descarga de productos del catálogo");
		logEjecucion.adicionarLogEjecucion("Iniciando descarga de productos del catálogo");
		try {
			// Obtengo token para Login
			token = this.restManager.getLogin(parametros.get("usuario"), parametros.get("password"),
					parametros.get("empresa"));
			// Obtengo los proveedores mediante servicios
			listaProveedores = this.restManager.proveedoresByEmpresa(token);
			// Filtro Porveedores obtenidos por servicos con la lista obtenida del fichero
			listaProveedoresFiltrada = checkProveedores(listaProveedores, empresasProveedoras);
			// Obtengo productos por empresas
			for (EmpresaDTO empresaDTO : listaProveedoresFiltrada) {	
				listaProductos.addAll(this.restManager.productosByEmpresa(token, empresaDTO));
			}
			logEjecucion.adicionarLogEjecucion("Finaliza descarga de productos del catálogo, se obtuvieron : " + listaProductos.size()+" productos");
			System.out.println("Finaliza descarga de productos del catálogo");
			return listaProductos;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listaProductos;

	}

	private List<EmpresaDTO> checkProveedores(List<EmpresaDTO> listaProveedoresServicio,
			List<String> empresasProveedorasFichero) {
		List<EmpresaDTO> listaFiltrada = new ArrayList<EmpresaDTO>();
		System.out.println("Verificar  proveedores");
		logEjecucion.adicionarLogEjecucion("Verificar  proveedores");
		for (String empresa : empresasProveedorasFichero) {
			List<EmpresaDTO> empresaValida = listaProveedoresServicio.stream().filter(t -> t.getGln().equals(empresa))
					.collect(Collectors.toList());
			if (!listaFiltrada.containsAll(empresaValida)) {
				listaFiltrada.addAll(empresaValida);
			}
		}
		System.out.println("Proveedores verificados: " + listaFiltrada.size());
		logEjecucion.adicionarLogEjecucion("Proveedores verificados: " + listaFiltrada.size());
		return listaFiltrada;
	}

}
