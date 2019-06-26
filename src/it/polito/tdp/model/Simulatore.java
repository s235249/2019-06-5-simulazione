package it.polito.tdp.model;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.db.EventsDao;
import it.polito.tdp.model.Evento.TipoEvento;

public class Simulatore {
	// Tipi di evento
		// 1. Evento criminoso
		//	1.1 La centrale seleziona l'agente più vicino
		//	1.2 Setta l'agente occupato
		// 2. Arriva agente
		//	2.1 Definisco quanto dura l'intervento
		//	2.2 Controlla se l'evento è mal gestito
		// 3. Crimine terminato
		//	3.1 Libero l'agente
		
		// Strutture dati che servono
		// Parametri + valori in output
		private Integer malGestiti;
		private Integer N;
		private Integer anno;
		private Integer mese;
		private Integer giorno; 
		private Graph<Integer, DefaultWeightedEdge> grafo;
		private PriorityQueue<Evento> queue;
		//Mappa di distretto-agenti
		private Map<Integer, Integer> agenti;
		
		public void init(Integer N, Integer anno, Integer mese, 
				Integer giorno, Graph<Integer, DefaultWeightedEdge> grafo) {
			this.N = N;
			this.anno = anno;
			this.mese = mese;
			this.giorno = giorno;
			this.grafo = grafo;
			
			this.malGestiti = 0;
			this.agenti = new HashMap<Integer, Integer>();
			for(Integer d : this.grafo.vertexSet()) {
				this.agenti.put(d, 0);
			}
			
			// devo scegliere dove sta la centrale: nel distretto dove c'è meno criminalità
			EventsDao dao = new EventsDao();
			Integer minD = dao.getDistrettoMin(anno);
			this.agenti.put(minD, this.N);
				
			
			// creazione della coda
			this.queue = new PriorityQueue<Evento>();
			
			for(Event e : dao.listAllEventsByDate(anno, mese, giorno)) {
				queue.add(new Evento(TipoEvento.CRIMINE,e.getReported_date(), e));
			}
		}
		
		public void run() {
			Evento e;
			
			while((e = queue.poll()) != null) {
				switch(e.getTipo()) {
				case CRIMINE:
					Integer partenza = null;
					partenza = cercaAgente(e.getCrimine().getDistrict_id());
					
					if(partenza != null) {
						if(partenza.equals(e.getCrimine().getDistrict_id())){
							//tempo di arrivo = 0;
							Long duration = getDuration(e.getCrimine().getOffense_category_id());
							this.queue.add(new Evento(TipoEvento.GESTITO, e.getData().plusSeconds(duration),e.getCrimine()));
						}else {
							Double distance = this.grafo.getEdgeWeight(this.grafo.getEdge(partenza, e.getCrimine().getDistrict_id()));
							
							Long seconds = (long) ((distance * 1000)/(60/3.6));
							this.queue.add(new Evento(TipoEvento.ARRIVA_AGENTE, e.getData().plusSeconds(seconds),
									e.getCrimine()));
						}
					}else {
						this.malGestiti++;
					}
					break;
				case ARRIVA_AGENTE:
					Long duration = getDuration(e.getCrimine().getOffense_category_id());
					this.queue.add(new Evento(TipoEvento.GESTITO, e.getData().plusSeconds(duration),e.getCrimine()));
					
					//Controllo se l'evento è mal gestito
					if(e.getData().isAfter(e.getCrimine().getReported_date().plusMinutes(15))) {
						this.malGestiti++;
					}
					
					break;
				case  GESTITO:
					this.agenti.put(e.getCrimine().getDistrict_id(), this.agenti.get(e.getCrimine().getDistrict_id())+1);
					break;
				}
			}
		}

		private Integer cercaAgente(Integer district_id) {
			Double distanza = Double.MAX_VALUE;
			Integer distretto = null;
			
			for(Integer d : this.agenti.keySet()) {
				if(this.agenti.get(d)>0) {
					if(district_id.equals(d)) {
						distanza = Double.valueOf(0);
						distretto = d;
					}else if(this.grafo.getEdgeWeight(this.grafo.getEdge(district_id, d)) < distanza) {
						distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(district_id, d));
						distretto = d;
					}
				}
			}
			
			return distretto;
		}

		private Long getDuration(String offence_category_id) {
			if(offence_category_id.equals("all_other_crimes")) {
				Random r = new Random();
				if(r.nextDouble() > 0.5) {
					return Long.valueOf(2*60*60);
				}else {
					return Long.valueOf(1*60*60);
				}
			}else {
				return Long.valueOf(2*60*60);
			}
		}
}
