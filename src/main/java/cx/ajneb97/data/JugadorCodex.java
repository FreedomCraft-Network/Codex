package cx.ajneb97.data;

import cx.ajneb97.managers.CategoriasManager;
import cx.ajneb97.model.CategoriaCodex;

import java.util.*;

public class JugadorCodex {

	private UUID uuid;
	private String name;
	private List<String> discoveries;
	public JugadorCodex(UUID uuid, String name) {
		this.discoveries = new ArrayList<String>();
		this.uuid = uuid;
		this.name = name;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getDiscoveries() {
		return discoveries;
	}
	public void setDiscoveries(List<String> discoveries) {
		this.discoveries = discoveries;
	}
	
	//Retorna false si ya existe
	public boolean agregarEntrada(String categoria,String discovery) {
		String linea = categoria.replace(".yml", "")+";"+discovery;
		if(discoveries.contains(linea)) {
			return false;
		}else {
			discoveries.add(linea);
			return true;
		}
	}
	
	public boolean tieneEntrada(String categoria,String discovery) {
		String linea = categoria.replace(".yml", "")+";"+discovery;
		if(discoveries.contains(linea)) {
			return true;
		}else {
			return false;
		}
	}
	
	public void resetearEntrada(String categoria,String discovery) {
		String linea = categoria.replace(".yml", "")+";"+discovery;
		discoveries.remove(linea);
	}
	
	public void resetearEntradas() {
		discoveries = new ArrayList<String>();
	}
	
	public int getEntradasDesbloqueadas(CategoriaCodex categoriaCodex) {
		String categoryName = categoriaCodex.getPath().replace(".yml", "");
		int cantidad = 0;
		for(String linea : discoveries) {
			String[] sep = linea.split(";");
			if(sep[0].equals(categoryName) && categoriaCodex.getEntrada(sep[1]) != null) {
				cantidad++;
			}
		}
		return cantidad;
	}
	
	public int getEntradasDesbloqueadas(CategoriasManager categoriasManager) {
		Map<String, CategoriaCodex> categoriaCodexes = new HashMap<>();
		int cantidad = 0;
		for(String linea : discoveries) {
			String[] sep = linea.split(";");
			CategoriaCodex categoriaCodex = categoriaCodexes.computeIfAbsent(sep[0], categoriasManager::getCategoria);
			if(categoriaCodex != null && categoriaCodex.getEntrada(sep[1]) != null) {
				cantidad++;
			}
		}
		return cantidad;
	}
}
