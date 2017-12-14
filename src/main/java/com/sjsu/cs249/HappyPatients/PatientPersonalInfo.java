/**
 * Class which operates on Patient model and responsible for database operations.
 * @author Eric Han, Mayuri Wadkar, Sonali Mishra
 */

package com.sjsu.cs249.HappyPatients;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.sjsu.cs249.Services.HappyPatientsService;
import com.sjsu.cs249.Services.Memcached;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

public class PatientPersonalInfo {

	private static final String TABLE_NAME = "PatientPersonalInfo";
	private static final Logger logger = Logger.getLogger(PatientPersonalInfo.class);
	Memcached cache;
	private Session session;
	
	public PatientPersonalInfo(Session session) throws IOException {
		cache = new Memcached();
		cache.start();
		this.session = session;
	}
	
	/**
	 * Method to carry out table creation operation
	 */
	public void createTablePatients() {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME).append("(")
				.append("id uuid PRIMARY KEY, ").append("firstName text,").append("lastName text,")
				.append("birthDate text,").append("phoneNumber text,").append("address text,").append("status text);");

		final String query = sb.toString();
		session.execute(query);
	}
	
	/**
	 * Method to execute alter table cql operation.
	 * @param columnName
	 * @param columnType
	 */
	public void alterTablePatients(String columnName, String columnType) {
		StringBuilder sb = new StringBuilder("ALTER TABLE ").append(TABLE_NAME).append(" ADD ").append(columnName)
				.append(" ").append(columnType).append(";");

		final String query = sb.toString();
		session.execute(query);
	}
	
	/**
	 * Method to carry out insert Patient object operation.
	 * @param patient
	 */
	public void insertPatient(Patient patient) {
		StringBuilder sb = new StringBuilder("INSERT INTO ").append(TABLE_NAME)
				.append("(id, firstName, lastName, birthDate, address, phoneNumber, status) ").append("VALUES (")
				.append(patient.getId()).append(", '").append(patient.getFirstName()).append("', '")
				.append(patient.getLastName()).append("', '").append(patient.getBirthDate()).append("', '")
				.append(patient.getAddress()).append("', '").append(patient.getPhoneNumber()).append("', '")
				.append(patient.getStatus()).append("');");
		final String query = sb.toString();
		session.execute(query);
	}
	
	/**
	 * Method to carry out update Patient object operation.
	 * @param id
	 * @param patient
	 */
	public void updatePatient(String id, Patient patient) {
		UUID uuid = UUID.fromString(id);
		StringBuilder sb = new StringBuilder("UPDATE ").append(TABLE_NAME).append(" SET firstName='")
				.append(patient.getFirstName()).append("',").append(" lastName='").append(patient.getLastName())
				.append("',").append(" address='").append(patient.getAddress()).append("',").append(" phoneNumber='")
				.append(patient.getPhoneNumber()).append("',").append(" birthDate='").append(patient.getBirthDate())
				.append("',").append(" status='").append(patient.getStatus()).append("' ").append(" WHERE id=")
				.append(uuid).append(" IF EXISTS;");
		final String query = sb.toString();
		logger.debug("UPDATE Query:");
		logger.debug(query);
		session.execute(query);
	}
	
	/**
	 * Method to carry out operation to get patient information given patient's id.
	 * @param id
	 * @return
	 */
	public Patient selectById(UUID id) {
		StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME).append(" WHERE id = ").append(id)
				.append(";");
		final String query = sb.toString();
		ResultSet rs = session.execute(query);
		List<Patient> patients = new ArrayList<Patient>();
		for (Row r : rs) {
			Patient p = new Patient(r.getUUID("id"), r.getString("firstName"), r.getString("lastName"),
					r.getString("birthDate"), r.getString("address"), r.getString("phoneNumber"),
					r.getString("status"));
			patients.add(p);
		}
		if (patients.size() > 0) {
			return patients.get(0);
		} else {
			return null;
		}
	}
	
	/**
	 * Method to retrieve all records from database.
	 * @return
	 * @throws IOException
	 */
	public List<Patient> selectAll() throws IOException {
		StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME);
		final String query = sb.toString();
		ResultSet rs = session.execute(query);
		List<Patient> patients = new ArrayList<Patient>();
		for (Row r : rs) {
			Patient p = new Patient(r.getUUID("id"), r.getString("firstName"), r.getString("lastName"),
					r.getString("birthDate"), r.getString("address"), r.getString("phoneNumber"),
					r.getString("status"));
			if (p.getStatus().equalsIgnoreCase("ICU")) {
				HashMap<String, String> map = new HashMap<>();
				System.out.println("Putting patient " + p.getFirstName() + " in cache");
				map.put("id", p.getId().toString());
				map.put("firstName", p.getFirstName());
				map.put("lastName", p.getLastName());
				map.put("status", p.getStatus());
				map.put("address", p.getAddress());
				map.put("phoneNumber", p.getPhoneNumber());
				cache.putContentInCache(p.getId().toString(), map);
			}
			patients.add(p);
		}
		return patients;
	}
	
	/**
	 * Method to delete Patient object given patient's id.
	 * @param id
	 */
	public void deletePatientById(UUID id) {
		StringBuilder sb = new StringBuilder("DELETE FROM ").append(TABLE_NAME).append(" WHERE id = ").append(id)
				.append(";");
		final String query = sb.toString();
		session.execute(query);
	}
	
	/**
	 * Method to delete table given table name.
	 * @param tableName
	 */
	public void deleteTable(String tableName) {
		StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS ").append(tableName);
		final String query = sb.toString();
		session.execute(query);
	}

}
