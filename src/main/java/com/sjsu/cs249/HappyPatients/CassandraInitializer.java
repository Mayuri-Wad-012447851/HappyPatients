/**
 * Java client to initialize cassandra connection and populate patient's database
 * @author Eric Han, Mayuri Wadkar, Sonali Mishra
 */

package com.sjsu.cs249.HappyPatients;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CassandraInitializer {

	public static void main(String args[]) throws IOException {
		
		CassandraConnector connector = new CassandraConnector();
		connector.connect("127.0.0.1", 9042);
		Session session = connector.getSession();
		KeyspaceRepository sr = new KeyspaceRepository(session);
		
		sr.createKeyspace("hospitalOps", "SimpleStrategy", 1);
		sr.useKeyspace("hospitalOps");
		
		PatientPersonalInfo ppi = new PatientPersonalInfo(session);
		ppi.deleteTable("PatientPersonalInfo");
		
		ppi.createTablePatients();
		Patient patient = new Patient(UUIDs.timeBased(), "John", "Doe", "09-09-1990", "555 SJSU drive San Jose, CA",
				"408-555-5555", "ICU");
		ppi.insertPatient(patient);
		Patient patient2 = new Patient(UUIDs.timeBased(), "Eric", "Han", "02-02-1921",
				"1256 Stevens Creek Blvd, Cupertino, CA", "408-609-8811", "Observation");
		ppi.insertPatient(patient2);
		Patient patient3 = new Patient(UUIDs.timeBased(), "Mayuri", "Wadkar", "12-03-1924",
				"1998 Brokaw Rd, San Jose, CA", "408-342-9921", "Discharged");
		ppi.insertPatient(patient3);
		Patient patient4 = new Patient(UUIDs.timeBased(), "Sonali", "Mishra", "11-11-2001", "Mumbai Blvd, India, IN",
				"668-952-3353", "Urgent Care");
		ppi.insertPatient(patient4);
		
		List<Patient> patients = ppi.selectAll();
		for (Patient p : patients) {
			System.out.println(p.getFirstName());
			System.out.println(p.getLastName());
			System.out.println(p.getAddress());
			System.out.println(p.getPhoneNumber());
			System.out.println(p.getBirthDate());
			System.out.println(p.getId());
			System.out.println(p.getStatus());
			System.out.println("-------------------------------------------");
		}
		ppi.selectById(UUID.fromString("f2a274a0-dc8a-11e7-8137-d9d45fa5e965"));
		connector.close();
	}
}
