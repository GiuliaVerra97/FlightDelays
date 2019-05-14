package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;
import it.polito.tdp.extflightdelays.model.Rotta;
import javafx.scene.effect.Light.Distant;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines(Map<Integer, Airport> mappa) {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
					result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
				
			}
			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database 1");
			throw new RuntimeException("Error Connection Database");
		}
	}

	
	
	
	/**
	 * Metodo che mi restituisce tutti gli aereoporti presenti nel DB
	 * @param mappa
	 * @return lista di {@link Airport}
	 */
	
	
	public List<Airport> loadAllAirports(Map<Integer, Airport> mappa) {
		String sql = "SELECT * FROM airports";
		List<Airport> result = new ArrayList<Airport>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				
				if(mappa.get(rs.getInt("ID"))==null) {

				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				
				mappa.put(airport.getId(),airport);
				result.add(airport);
				
				}else {
					result.add(mappa.get(rs.getInt("ID")));
				}
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database 2");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	
	
	/**
	 * Metodo che ricava tutte le rotte degli aerei ovvero tutti gli archi del grafo
	 * @param distanzaMedia
	 * @param mappaAereoporti
	 * @return lista di {@link Rotta}
	 */
	
	public List<Rotta> getRottes(int distanzaMedia, Map<Integer, Airport> mappaAereoporti){
		
		
		final String sql="SELECT ORIGIN_AIRPORT_ID as id1, DESTINATION_AIRPORT_ID as id2, AVG(DISTANCE) AS av " + 
				"FROM flights " + 
				"GROUP BY ORIGIN_AIRPORT_ID, DESTINATION_AIRPORT_ID " + 
				"HAVING AVG(DISTANCE)> ? ";
		
		List<Rotta> result=new ArrayList<>();
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, distanzaMedia);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport partenza=mappaAereoporti.get(rs.getInt("id1"));
				Airport destinazione=mappaAereoporti.get(rs.getInt("id2"));
				
				if(partenza==null || destinazione==null) {
					throw new RuntimeException("Problema in get rotte");
				}
				
				Rotta r=new Rotta(partenza, destinazione, rs.getDouble("av"));
				result.add(r);
				
			}
			
			conn.close();
			return result;
			
		}catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database 3");
			throw new RuntimeException("Error Connection Database");
		}
		
	}

}
