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

import common.rondanet.catalogo.core.dto.EmpresaDTO;
import common.rondanet.catalogo.core.dto.ProductoBatchDTO;
import common.rondanet.catalogo.core.exceptions.ServiceException;
import common.rondanet.catalogo.core.resources.UsuarioBasic;
import rondanet.catalogobatch.exeptions.ProductoException;
import rondanet.catalogobatch.exeptions.ProveedorException;
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

		getToken(usuario, password);

		if (empresaGln.equals(empresa)) {
			tokenEmpresa = getDatosEmpresa(token, empresaId);
		}
		return tokenEmpresa;
	}

	@Retryable(value = { ServiceException.class }, maxAttempts = 5)
	public void getToken(String usuario, String password) throws Exception {
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
			System.out.println("No se pudo loguear el Usuario, se intentara nuevamente. ");
			throw new ServiceException("No se pudo loguear el Usuario");
		}
	}

	@Retryable(value = { ServiceException.class }, maxAttempts = 5)
	public String getDatosEmpresa(String token, String empresa) throws Exception {
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
			System.out.println("No se pudo loguear la empresa, se intentara nuevamente. ");
			throw new ServiceException("No se pudo loguear la empresa");
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
	public LinkedHashMap getProveedoresDeCatalogo(String token, Integer page) throws Exception {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		try {
			HttpEntity request = new HttpEntity<>(headers);
			ResponseEntity response = restTemplate.exchange(urlApiRest + "/api/acciones/empresasCatalogo?limit=" + page,
					HttpMethod.GET, request, Object.class);

			return (LinkedHashMap) response.getBody();

		} catch (Exception e) {
			throw new ServiceException("No se pudo obtener los proveedores");
		}
	}

	public List<EmpresaDTO> proveedoresByEmpresa(String token) throws ProveedorException {
		// Realizo la primera peticion
		System.out.println("Obtener proveedores de Catalogo");
		logEjecucion.adicionarLogEjecucion("Obtener proveedores de Catalogo");
		try {
			LinkedHashMap empresaProveedorasResponse = getProveedoresDeCatalogo(token, 1);
			// Obtengo las empresas
			ArrayList<LinkedHashMap> data = (ArrayList<LinkedHashMap>) empresaProveedorasResponse.get("data");
			// Mapeo a empresas
			List<EmpresaDTO> empresasProveedoreas = mapEmpresas(data);
			// Verifico si tiene mas paginas
			Integer total, page, limit, paginasFaltantes;
			total = (Integer) empresaProveedorasResponse.get("total");
			limit = (Integer) empresaProveedorasResponse.get("limit");
			page = (Integer) empresaProveedorasResponse.get("page");
			System.out.println("Cantidad de proveedores de Catalogo disponibles: " + total);
			logEjecucion.adicionarLogEjecucion("Cantidad de proveedores de Catalogo disponibles: " + total);
			paginasFaltantes = ((Integer) total / limit) + 1;
			while (paginasFaltantes > page) {
				// consulto para otras paginas
				empresaProveedorasResponse = getProveedoresDeCatalogo(token, page + 1);
				// Actualizo el numero de pagina
				page = (Integer) empresaProveedorasResponse.get("page");
				// Obtengo la informacion de los provvedores
				data = (ArrayList<LinkedHashMap>) empresaProveedorasResponse.get("data");
				empresasProveedoreas.addAll(mapEmpresas(data));
			}
			return empresasProveedoreas;
		} catch (Exception e) {
			throw new ProveedorException("No se pudo obtener los proveedores");
		}

	}

	public List<ProductoBatchDTO> productosByEmpresa(String token, EmpresaDTO empresaDTO) throws ProductoException {
		System.out.println(
				"Obtener productos para la empresa: Gln " + empresaDTO.getGln() + "/ Rut " + empresaDTO.getRut());
		logEjecucion.adicionarLogEjecucion(
				"Obtener productos para la empresa: Gln " + empresaDTO.getGln() + "/ Rut " + empresaDTO.getRut());
		try {
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
			System.out.println("Cantidad de productos: " + total);
			logEjecucion.adicionarLogEjecucion("Cantidad de productos: " + total);
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
		} catch (Exception e) {
			logEjecucion.adicionarLogEjecucion("No se pudo obtener los productos");
			System.out.println("No se pudo obtener los productos");
			throw new ProductoException("No se pudo obtener los productos");
		}
	}

	private List<EmpresaDTO> mapEmpresas(ArrayList<LinkedHashMap> data) {
		return data.stream()
				.map(map -> new EmpresaDTO((String) map.get("gln"), (String) map.get("razonSocial"),
						(String) map.get("nombre"), (String) map.get("rut"), (String) map.get("foto")))
				.collect(Collectors.toList());
	}

	private static ProductoBatchDTO convertirNuevoProducto(LinkedHashMap data) {
	try {	String gtin14, unidades,empaques, camadas, estado, esPromo;
		Boolean esPromoBool;

		ArrayList<LinkedHashMap> dataIntermedia = (ArrayList<LinkedHashMap>) data.get("empaques");

		gtin14 = dataIntermedia.size() > 0 ? (String) dataIntermedia.get(0).get("gtin") : "";

		unidades = dataIntermedia.size() > 0 ? (String) dataIntermedia.get(0).get("cantidad").toString() : "";

		LinkedHashMap dataIntermedia1 = (LinkedHashMap) data.get("pallet");

		camadas = dataIntermedia1 != null
				? dataIntermedia1.get("camadas") != null ? (String) dataIntermedia1.get("camadas") : ""
				: "";

		empaques = dataIntermedia1 != null
				? dataIntermedia1.get("cajas") != null ? (String) dataIntermedia1.get("cajas") : ""
				: "";
		esPromo = data.get("esPromo") != null ? (Boolean) data.get("esPromo") ? "S" : "N" : "N";

		estado = (String) data.get("estado");

		if (estado.equals("ACTIVO")) {
			estado = "A";
		} else if (estado.equals("DISCONTINUADO")) {
			estado = "D";
		} else if (estado.equals("SUSPENDIDO")) {
			estado = "S";
		}
		return new ProductoBatchDTO((String) ((LinkedHashMap) data.get("empresa")).get("gln"), // GLN
				(String) data.get("cpp"), // CPP
				(String) data.get("gtin"), // GTIN13
				(String) data.get("descripcion"), // DESCRIP
				estado, // ESTADO
				(String) data.get("paisOrigen"), // PAIS_ID
				"", // PAIS_NOMBRE
				(String) data.get("marca"), // MARCA
				(String) data.get("division"), // DIVISION
				(String) data.get("linea"), // LINEA
				"", // SUBLINEA
				"", // PRESENT_TIPO
				"", // PRESENT_CANT
				"", // PRESENT_UNIDAD
				"", // PIDE_UNIDAD
				gtin14, // GTIN14
				unidades, // UNIDADES
				empaques, // EMPAQUES
				camadas, // CAMADAS
				esPromo, // ESPROMO
				"", // FECHA_SUSPEND1
				"", // FECHA_SUSPEND2
				"", // CPP_PROMO
				"", // FECHA_ALTA
				"", // VIGENCIA
				"", // TIPOIVA
				"", // MONEDA
				null, // PRBASE
				null, // PRCONDTO
				null, // PRTOTAL
				null // PRSUGERIDO
		);}
	catch (Exception e) {
			e.printStackTrace();
		}
	return new ProductoBatchDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	private List<ProductoBatchDTO> mapProductos(ArrayList<LinkedHashMap> data) {
		return data.stream().map(map -> convertirNuevoProducto(map)).collect(Collectors.toList());
	}
}