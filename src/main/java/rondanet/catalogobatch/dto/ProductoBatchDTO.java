package rondanet.catalogobatch.dto;

import java.math.BigDecimal;

public class ProductoBatchDTO {

	private String gln;

	private String cpp;

	private String gtin;

	private String descripcion;

	private String estado;


	private String paisOrigen;

	private String paisOrigenNombre;

	private String marca;

	private String division;

	private String linea;

	private String subLinea;

	private String presentTipo;

	private String presentCantidad;

	private String presentUnidad;

	private String pideUnidad;

	private String empaquesGtin;

	private Integer unidades;

	private Integer empaques;

	private Integer camadas;

	private Boolean esPromo;

	private String fechaSuspendidoDesde;

	private String fechaSuspendidoHasta;

	private String cppPromo;

	private String fechaAlta;

	private String vigencia;

	private String tipoIva;

	private String moneda;

	private BigDecimal precioBase;

	private BigDecimal precioConDescuentos;

	private BigDecimal precioTotal;

	private BigDecimal precioSugerido;
	
	
	public ProductoBatchDTO(String gln, String cpp, String gtin, String descripcion, String estado, String paisOrigen,
			String paisOrigenNombre, String marca, String division, String linea, String subLinea, String presentTipo,
			String presentCantidad, String presentUnidad, String pideUnidad, String empaquesGtin, Integer unidades,
			Integer empaques, Integer camadas, Boolean esPromo, String fechaSuspendidoDesde,
			String fechaSuspendidoHasta, String cppPromo, String fechaAlta, String vigencia, String tipoIva,
			String moneda, BigDecimal precioBase, BigDecimal precioConDescuentos, BigDecimal precioTotal,
			BigDecimal precioSugerido) {

		this.gln = gln;
		this.cpp = cpp;
		this.gtin = gtin;
		this.descripcion = descripcion;
		this.estado = estado;
		this.paisOrigen = paisOrigen;
		this.paisOrigenNombre = paisOrigenNombre;
		this.marca = marca;
		this.division = division;
		this.linea = linea;
		this.subLinea = subLinea;
		this.presentTipo = presentTipo;
		this.presentCantidad = presentCantidad;
		this.presentUnidad = presentUnidad;
		this.pideUnidad = pideUnidad;
		this.empaquesGtin = empaquesGtin;
		this.unidades = unidades;
		this.empaques = empaques;
		this.camadas = camadas;
		this.esPromo = esPromo;
		this.fechaSuspendidoDesde = fechaSuspendidoDesde;
		this.fechaSuspendidoHasta = fechaSuspendidoHasta;
		this.cppPromo = cppPromo;
		this.fechaAlta = fechaAlta;
		this.vigencia = vigencia;
		this.tipoIva = tipoIva;
		this.moneda = moneda;
		this.precioBase = precioBase;
		this.precioConDescuentos = precioConDescuentos;
		this.precioTotal = precioTotal;
		this.precioSugerido = precioSugerido;
	}

	public String getGln() {
		return gln;
	}

	public void setGln(String gln) {
		this.gln = gln;
	}

	public String getCpp() {
		return cpp;
	}

	public void setCpp(String cpp) {
		this.cpp = cpp;
	}

	public String getGtin() {
		return gtin;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}



	public String getPaisOrigen() {
		return paisOrigen;
	}

	public void setPaisOrigen(String paisOrigen) {
		this.paisOrigen = paisOrigen;
	}

	public String getPaisOrigenNombre() {
		return paisOrigenNombre;
	}

	public void setPaisOrigenNombre(String paisOrigenNombre) {
		this.paisOrigenNombre = paisOrigenNombre;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public String getLinea() {
		return linea;
	}

	public void setLinea(String linea) {
		this.linea = linea;
	}

	public String getSubLinea() {
		return subLinea;
	}

	public void setSubLinea(String subLinea) {
		this.subLinea = subLinea;
	}

	public String getPresentTipo() {
		return presentTipo;
	}

	public void setPresentTipo(String presentTipo) {
		this.presentTipo = presentTipo;
	}

	public String getPresentCantidad() {
		return presentCantidad;
	}

	public void setPresentCantidad(String presentCantidad) {
		this.presentCantidad = presentCantidad;
	}

	public String getPresentUnidad() {
		return presentUnidad;
	}

	public void setPresentUnidad(String presentUnidad) {
		this.presentUnidad = presentUnidad;
	}

	public String getPideUnidad() {
		return pideUnidad;
	}

	public void setPideUnidad(String pideUnidad) {
		this.pideUnidad = pideUnidad;
	}

	public String getEmpaquesGtin() {
		return empaquesGtin;
	}

	public void setEmpaquesGtin(String empaquesGtin) {
		this.empaquesGtin = empaquesGtin;
	}

	public Integer getUnidades() {
		return unidades;
	}

	public void setUnidades(Integer unidades) {
		this.unidades = unidades;
	}

	public Integer getEmpaques() {
		return empaques;
	}

	public void setEmpaques(Integer empaques) {
		this.empaques = empaques;
	}

	public Integer getCamadas() {
		return camadas;
	}

	public void setCamadas(Integer camadas) {
		this.camadas = camadas;
	}

	public Boolean getEsPromo() {
		return esPromo;
	}

	public void setEsPromo(Boolean esPromo) {
		this.esPromo = esPromo;
	}

	public String getFechaSuspendidoDesde() {
		return fechaSuspendidoDesde;
	}

	public void setFechaSuspendidoDesde(String fechaSuspendidoDesde) {
		this.fechaSuspendidoDesde = fechaSuspendidoDesde;
	}

	public String getFechaSuspendidoHasta() {
		return fechaSuspendidoHasta;
	}

	public void setFechaSuspendidoHasta(String fechaSuspendidoHasta) {
		this.fechaSuspendidoHasta = fechaSuspendidoHasta;
	}

	public String getCppPromo() {
		return cppPromo;
	}

	public void setCppPromo(String cppPromo) {
		this.cppPromo = cppPromo;
	}

	public String getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(String fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public String getVigencia() {
		return vigencia;
	}

	public void setVigencia(String vigencia) {
		this.vigencia = vigencia;
	}

	public String getTipoIva() {
		return tipoIva;
	}

	public void setTipoIva(String tipoIva) {
		this.tipoIva = tipoIva;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public BigDecimal getPrecioBase() {
		return precioBase;
	}

	public void setPrecioBase(BigDecimal precioBase) {
		this.precioBase = precioBase;
	}

	public BigDecimal getPrecioConDescuentos() {
		return precioConDescuentos;
	}

	public void setPrecioConDescuentos(BigDecimal precioConDescuentos) {
		this.precioConDescuentos = precioConDescuentos;
	}

	public BigDecimal getPrecioTotal() {
		return precioTotal;
	}

	public void setPrecioTotal(BigDecimal precioTotal) {
		this.precioTotal = precioTotal;
	}

	public BigDecimal getPrecioSugerido() {
		return precioSugerido;
	}

	public void setPrecioSugerido(BigDecimal precioSugerido) {
		this.precioSugerido = precioSugerido;
	}

}
