package rondanet.catalogobatch.consumirservicios;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import common.rondanet.catalogo.core.dto.EmpaqueDTO;
import common.rondanet.catalogo.core.dto.EmpresaDTO;
import common.rondanet.catalogo.core.entity.Empaque;
import common.rondanet.catalogo.core.entity.Empresa;
import common.rondanet.catalogo.core.exceptions.ServiceException;
import common.rondanet.catalogo.core.resources.UsuarioBasic;
import rondanet.catalogobatch.dto.ProductoBatchDTO;
import rondanet.catalogobatch.utils.LogEjecucion;

@Component
public class RestManager {
	@Value("${urlApiRest}")
	String urlApiRest;
	RestTemplate restTemplate;
	HttpHeaders headers;
	String token;
	String empresaId;
	String empresaGln;
	@Autowired
	private LogEjecucion logEjecucion;
	
	public RestManager(RestTemplate restTemplate) {

		this.restTemplate = restTemplate;
		this.headers = new HttpHeaders();
	}

	public String getLogin(String usuario, String password, String empresa) throws Exception {
		String tokenEmpresa = null;
		System.out.println("Login de usuario");

		getToken(usuario, password);
		
		if (empresaGln.equals(empresa)) {
			System.out.println("Login de empresa");
			tokenEmpresa = getDatosEmpresa(token, empresaId);
		}
		return tokenEmpresa;
	}

	@Retryable(value = { ServiceException.class }, maxAttempts = 5)
	public void getToken(String usuario, String password) throws Exception {
		logEjecucion.adicionarLogEjecucion("Login de usuario");
		headers.setContentType(MediaType.APPLICATION_JSON);
		UsuarioBasic usuarioBasic = new UsuarioBasic();
		usuarioBasic.setUsuario(usuario);
		usuarioBasic.setContrasena(password);
		try {
			HttpEntity request = new HttpEntity<>(usuarioBasic, headers);
			ResponseEntity response = restTemplate.exchange(urlApiRest + "/auth/login", HttpMethod.POST, request,
					Object.class);
			Map<?, ?> loginResponse = (Map<?, ?>) response.getBody();
			Map data = (LinkedHashMap) loginResponse.get("data");
			token = (String) data.get("token");
			ArrayList<LinkedHashMap> businesses = (ArrayList<LinkedHashMap>) data.get("businesses");

			empresaId = (String) businesses.get(0).get("id");
			empresaGln = (String) businesses.get(0).get("gln");

		} catch (Exception e) {
			System.out.println("No se pudo obtener el token, se intentara nuevamente. ");
			throw new ServiceException("No se pudo obtener el token");
		}
	}

	@Retryable(value = { ServiceException.class }, maxAttempts = 5)
	public String getDatosEmpresa(String token, String empresa) throws Exception {
		logEjecucion.adicionarLogEjecucion("Login de empresa");
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		try {
			HttpEntity request = new HttpEntity<>(headers);
			ResponseEntity response = restTemplate.exchange(urlApiRest + "/auth/empresa/" + empresa, HttpMethod.POST,
					request, Object.class);
			Map loginResponse = (LinkedHashMap) response.getBody();
			Map data = (LinkedHashMap) loginResponse.get("data");
			return (String) data.get("token");
		} catch (Exception e) {
			System.out.println("No se pudo obtener el token empresa, se intentara nuevamente. ");
			throw new ServiceException("No se pudo obener el token empresa");
		}
	}

	@Retryable(value = { ServiceException.class }, maxAttempts = 5)
	public LinkedHashMap getProductos(String token, String rutEmpresa, Integer page) throws Exception {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);
		try {
			HttpEntity request = new HttpEntity<>(headers);
			ResponseEntity response = restTemplate.exchange(
					urlApiRest + "/api/acciones/productos/empresa/" + rutEmpresa + "?page=" + page, HttpMethod.GET,
					request, Object.class);
			return (LinkedHashMap) response.getBody();
		} catch (Exception e) {
			System.out.println("No se pudo optener los productos para la empresa , se intentara nuevamente. ");
			throw new ServiceException("No se pudo obtener los productos");
		}
	}

	@Retryable(value = { ServiceException.class }, maxAttempts = 5)
	public LinkedHashMap getProveedores(String token, Integer page) throws Exception {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		try {
			HttpEntity request = new HttpEntity<>(headers);
			ResponseEntity response = restTemplate.exchange(urlApiRest + "/api/acciones/proveedores?limit=" + page,
					HttpMethod.GET, request, Object.class);

			return (LinkedHashMap) response.getBody();

		} catch (Exception e) {
			System.out.println("No se pudo obtener los proveedores para la empresa: ");
			throw new ServiceException("No se pudo obtener los proveedores");
		}
	}

	public List<EmpresaDTO> proveedoresByEmpresa(String token) throws Exception {
		// Realizo la primera peticion
		System.out.println("Obtener proveedores");
		logEjecucion.adicionarLogEjecucion("Obtener proveedores");
		LinkedHashMap empresaProveedorasResponse = getProveedores(token, 1);
		// Obtengo las empresas
		ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) empresaProveedorasResponse.get("data");
		// Mapeo a empresas
		List<EmpresaDTO> empresasProveedoreas = mapEmpresas(data);
		// Verifico si tiene mas paginas
		Integer total, page, limit, paginasFaltantes;
		total = (Integer) empresaProveedorasResponse.get("total");
		limit = (Integer) empresaProveedorasResponse.get("limit");
		page = (Integer) empresaProveedorasResponse.get("page");
		System.out.println("Cantidad de proveedores: " + total );
		logEjecucion.adicionarLogEjecucion("Cantidad de proveedores: " + total  );
		paginasFaltantes = ((Integer) total / limit) + 1;
		while (paginasFaltantes > page) {
			// consulto para otras paginas
			empresaProveedorasResponse = getProveedores(token, page + 1);
			// Actualizo el numero de pagina
			page = (Integer) empresaProveedorasResponse.get("page");
			logEjecucion.adicionarLogEjecucion("Numero de pagina: " + total );
			// Obtengo la informacion de los provvedores
			data = (ArrayList<LinkedHashMap>) empresaProveedorasResponse.get("data");
			empresasProveedoreas.addAll(mapEmpresas(data));
		}

		return empresasProveedoreas;

	}

	public List<ProductoBatchDTO> productosByEmpresa(String token, EmpresaDTO empresaDTO) throws Exception {
		System.out.println("Obtener productos para la empresa: Gln " + empresaDTO.getGln()+"/ Rut "+empresaDTO.getRut());
		logEjecucion.adicionarLogEjecucion("Obtener productos para la empresa: Gln " + empresaDTO.getGln()+"/ Rut "+empresaDTO.getRut());
		// Realizo la primera peticion
		LinkedHashMap productosResponse = getProductos(token, empresaDTO.getRut(), 1);
		// Obtengo las empresas
		ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) productosResponse.get("data");
		// Mapeo a empresas
		List<ProductoBatchDTO> productosEmpresa = mapProductos(data);
		// Verifico si tiene mas paginas
		Integer total, page, limit, paginasFaltantes;
		total = (Integer) productosResponse.get("total");
		limit = (Integer) productosResponse.get("limit");
		page = (Integer) productosResponse.get("page");
		System.out.println("Cantidad de productos: " + total );
		logEjecucion.adicionarLogEjecucion("Cantidad de productos: " + total );
		paginasFaltantes = ((Integer) total / limit) + 1;
		while (paginasFaltantes > page) {
			// consulto para otras paginas
			productosResponse = getProductos(token, empresaDTO.getRut(), page + 1);
			// Actualizo el numero de pagina
			page = (Integer) productosResponse.get("page");
			// Obtengo la informacion de los provvedores
			data = (ArrayList<LinkedHashMap>) productosResponse.get("data");
			productosEmpresa.addAll(mapProductos(data));
		}
		return productosEmpresa;

	}

	private List<EmpresaDTO> mapEmpresas(ArrayList<LinkedHashMap> data) {
		return data.stream()
				.map(map -> new EmpresaDTO((String) map.get("gln"), (String) map.get("razonSocial"),
						(String) map.get("nombre"), (String) map.get("rut"), (String) map.get("foto")))
				.collect(Collectors.toList());
	}

	private List<ProductoBatchDTO> mapProductos(ArrayList<LinkedHashMap> data) {
		return  data.stream().map(map -> new ProductoBatchDTO(
			     (String) ((LinkedHashMap)map.get("empresa")).get("gln"),
			    (String) map.get("cpp"),
			    (String) map.get("gtin"),
			    (String) map.get("descripcion"),
			    (String) map.get("estado"), 
			    (String) map.get("paisOrigen"),
				 null,
				(String) map.get("marca"),
			    (String) map.get("division"),
			    (String) map.get("linea"), 
				null,
				null,
			    null,
				null,
				null,
				null, //((Empaque) map.get("empaques")).getGtin(),
				null, //unidades
				null, // empaques
				null, // camadas
				(Boolean) map.get("esPromo"),  
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null)).collect(Collectors.toList());
	}
}