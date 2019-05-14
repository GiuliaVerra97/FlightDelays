package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;		//grafo non orientato, semplice e pesato
	Map<Integer, Airport> mappaAereoporti;		//mappa di tutti gli aereoporti
	Map<Airport, Airport> mappaVisite;			//mappa di tutti gli aereoporti che si possono raggiungere da una dato aereoporto di partenza
	
	
	
	
	public Model() {
		grafo=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		mappaAereoporti=new HashMap<Integer, Airport>();
		mappaVisite=new HashMap<Airport, Airport>();			//come chiave il padre e come valore il figlio
	}
	
	
	
	public void creaGrafo(int distanzaMedia) {
		ExtFlightDelaysDAO dao=new ExtFlightDelaysDAO();
		dao.loadAllAirports(mappaAereoporti);		//anche se il metodo restituisce qualcosa, a noi non interessa, l'importsnte è che riempa la mappa
		
		//aggiungo vertici
		Graphs.addAllVertices(grafo, mappaAereoporti.values());
		
		//aggiungo archi
		for(Rotta r: dao.getRottes(distanzaMedia, mappaAereoporti)) {
			
			/*siccome il grafo non è orientato ed è semplice e devo vedere sia le rotte(archi) dall'aereoporto di partenza a un aereoporto di arrivo sia viceversa
			controllo se esiste già un arco tra due nodo:
			se esiste, aggiorno solo il peso
			se non esiste ancora, lo creo */
			
			DefaultWeightedEdge edge=grafo.getEdge(r.getPartenza(), r.getDestinazione());		//default è il tipo di arco
			if(edge==null) {
				Graphs.addEdge(grafo, r.getPartenza(), r.getDestinazione(), r.getDistanzaMedia());
			}else {
				double peso=grafo.getEdgeWeight(edge);		//prende il peso vecchio dell'arco
				double newPeso=(peso+r.getDistanzaMedia())/2;
				grafo.setEdgeWeight(edge, newPeso);		//aggiorna il peso
			}
			
			
		}
		
		System.out.println("Grafo creato");
		System.out.println("Con archi: "+grafo.edgeSet().size());


	}
	
	
	
	
	
	
	
	/**
	 * Metodo che cerca se è possibile raggiungere l'aereoporto di arrivo partendo dall'aereoporto di partenza passato come parametro
	 * @param a1 codice dell'aereoporto di partenza
	 * @param a2 codice dell'aereoporto di arrivo
	 * @return true se c'è è possibile raggiungere l'aereoporto di arrivo dall'aereoporto di partenza, false diversamente 
	 */
	
	public Boolean testConnessione(Integer a1, Integer a2) {
		
		Airport partenza=mappaAereoporti.get(a1);
		Airport destinazione=mappaAereoporti.get(a2);
		
		System.out.println("test connessione tra "+partenza+" e "+destinazione);
		
		Set<Airport> visitati= new HashSet<Airport>();

		//ricerca  dei vertici visitati in base all'ampiezza
		//passo all'iteratore la sorgente e il grafo
		BreadthFirstIterator<Airport, DefaultWeightedEdge> iteratore= new BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo, partenza); //gli passo anche la source
		
		while(iteratore.hasNext()) {					//scorre tutti i vertici collegati all'aereoporto di partenza e li aggiunge al set
			visitati.add(iteratore.next());
		}
			
		if(visitati.contains(destinazione)) {
			return true;
		}else {
			return false;
		}
		
		
	}
	
	
	
	
	
	/**
	 * Metodo che trova il percorso da un aereoporto di partenza a quello di destinazione scorrendo l'itaratore
	 * @param a1 codice dell'aereoporto di partenza
	 * @param a2 codice dell'aereoporto di arrivo
	 * @return lista di {@link Airport}
	 */
	
	public List<Airport> trovaPercorso(Integer a1, Integer a2){
		
		mappaVisite.clear();		//devo ricordarmi di pulire la mappa se no funziona tutto solo la prima volta e poi va in conflitto
		
		Airport partenza=mappaAereoporti.get(a1);
		Airport destinazione=mappaAereoporti.get(a2);
		
		List<Airport> percorso=new ArrayList<Airport>();
		
		System.out.println("Cerco percorso tra "+ partenza +" e "+ destinazione);
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> iteratore= new BreadthFirstIterator<Airport, DefaultWeightedEdge>(this.grafo, partenza); //gli passo anche la sourc
		
		mappaVisite.put(partenza, null);
		
		iteratore.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> ev) {
				
						Airport sorgente=grafo.getEdgeSource(ev.getEdge());
						Airport destinazione=grafo.getEdgeTarget(ev.getEdge());
						
						if(!mappaVisite.containsKey(destinazione) && mappaVisite.containsKey(sorgente)) {		//se contiene il padre, ma non il figlio
							mappaVisite.put(destinazione, sorgente);
						}else if(mappaVisite.containsKey(destinazione) && !mappaVisite.containsKey(sorgente)) {
							mappaVisite.put(sorgente, destinazione);
						}


			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		while(iteratore.hasNext()) {
			iteratore.next();
		}
			
			if(!mappaVisite.containsKey(partenza) || !mappaVisite.containsKey(destinazione)) {		//aereoporti non collegati
				return null;
			}
			
			Airport step=destinazione;	
			
			while(!step.equals(partenza)) {
				percorso.add(0,step);
				step=mappaVisite.get(step);
			}
			percorso.add(0,step);
			
			return percorso;
			
	}
	
	
	
	
	
	
	public Collection<Airport> getAirport(){
		ExtFlightDelaysDAO dao=new ExtFlightDelaysDAO();
		return dao.loadAllAirports(mappaAereoporti);
	}



	
	
	
	
}
