package it.polito.tdp.extflightdelays.model;

import java.util.List;

public class TestModel {

	public static void main(String[] args) {
		
		Model model = new Model();
		model.creaGrafo(400);

		if(model.testConnessione(11, 297)) {
			System.out.println("Connessi");
		}else {
			System.out.println("Non Connessi");
		}
		
		
		List<Airport> lista=model.trovaPercorso(11,297);
		System.out.println(lista);
		
	}

}
