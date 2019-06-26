package it.polito.tdp.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.db.EventsDao;

public class Model {
	private EventsDao dao = new EventsDao();
	private SimpleWeightedGraph<Integer, DefaultWeightedEdge> grafo;
	
	public Model(){
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	}
	
	
	public List<Integer> getAllYears(){
		return dao.getAllYears();
	}
	
	public void createGraph(int anno) {
		List<Integer> allDistricts = dao.getAllDistricts();
		Graphs.addAllVertices(grafo, allDistricts);
		LatLng c1;
		LatLng c2;
		double peso;
		
		for(int d1 : allDistricts) {
			for(int d2 : allDistricts) {
				if(d1!=d2) {
					c1 = dao.getCentre(anno, d1);
					c2 = dao.getCentre(anno, d2);
					peso = calculateWeight(c1,c2);
					if(grafo.getEdge(d1, d2)==null) {
						DefaultWeightedEdge arco = grafo.addEdge(d1, d2);
						grafo.setEdgeWeight(arco, peso);
					}
				}
			}
		}
		
	}
	
	public double calculateWeight(LatLng c1, LatLng c2) {
		double peso =LatLngTool.distance(c1, c2, LengthUnit.KILOMETER);
		return peso;
	}
	
	public List<Vicino> findNeighbours(int d){
		if(grafo==null) {
			System.out.println("Prima devi creare il grafo");
		}
		
		List<Integer> viciniInt = Graphs.neighborListOf(grafo, d);
		List<Vicino> vicini = new LinkedList<Vicino>();
		
		for(Integer n : viciniInt) {
			DefaultWeightedEdge edge = grafo.getEdge(d, n);
			vicini.add(new Vicino(n, this.grafo.getEdgeWeight(edge)));
		}
		Collections.sort(vicini);
		return vicini;
	}


	public int getNumeroVertici() {
		return grafo.vertexSet().size();
	}
	
	public int getNumeroArchi() {
		return grafo.edgeSet().size();
	}
	
	public List<Integer> getAllDistricts(){
		return dao.getAllDistricts();
	}
	
	public void simula(Integer anno ,Integer mese, Integer giorno, Integer N) {
		Simulatore sim = new Simulatore();
		sim.init(N, anno, mese, giorno, grafo);
		sim.run();
	}
}
