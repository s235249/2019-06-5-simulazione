package it.polito.tdp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.model.Event;


public class EventsDao {
	
	public List<Event> listAllEventsByDate(Integer anno, Integer mese, Integer giorno){
		String sql = "SELECT * FROM events WHERE Year(reported_date) = ? AND Month(reported_date) = ? "
				+ "AND Day(reported_date) = ?" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setInt(1, anno);
			st.setInt(2, mese);
			st.setInt(3, giorno);
			List<Event> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<Integer> getAllYears(){
		String sql = "SELECT DISTINCT YEAR(reported_date) AS y FROM events ORDER BY y DESC" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Integer> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(res.getInt("y"));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}
	}

	public List<Integer> getAllDistricts() {
		String sql = "SELECT DISTINCT district_id AS d FROM EVENTS" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Integer> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(res.getInt("d"));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}
	}
	
	public LatLng getCentre(int anno, int district_id) {
		String sql = "SELECT YEAR(reported_date), district_id, AVG(geo_lon) AS x, AVG(geo_lat) AS y " + 
				"FROM EVENTS " + 
				"WHERE YEAR(reported_date) = ? " + 
				"AND district_id = ? " + 
				"GROUP BY YEAR(reported_date), district_id" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setInt(1, anno);
			st.setInt(2, district_id);
			LatLng c = null;
			ResultSet res = st.executeQuery() ;
			
			res.next(); 
			
			c = new LatLng(res.getDouble("y"), res.getDouble("x"));
			conn.close();
			return c ;

		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}
	}
	
	public Integer getDistrettoMin(Integer anno) {
		String sql = "select district_id " + 
				"from events " + 
				"where Year(reported_date) = ? " + 
				"group by district_id " + 
				"order by count(*) asc " + 
				"limit 1";
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setInt(1, anno);
			ResultSet res = st.executeQuery() ;

			if(res.next()) {
				conn.close();
				return res.getInt("district_id");
			}
			conn.close();
			return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	

}
