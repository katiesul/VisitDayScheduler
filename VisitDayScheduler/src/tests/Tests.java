package tests;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import scheduler.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

public class Tests {
	/*
	 * The below filenames are hardcoded so would need to be changed if run on a
	 * different machine.
	 */
	String studentFileName = "C:/Users/katie/VisitDayScheduler/VisitDayScheduler/src/STUDENTDATA.tsv";
	String longStudentFileName = "C:\\Users\\katie\\VisitDayScheduler\\VisitDayScheduler\\src\\STUDENTDATA1.tsv";
	String profFileName = "C:\\Users\\katie\\VisitDayScheduler\\VisitDayScheduler\\src\\PROFDATA.tsv";
	String realisticStudent = "C:\\Users\\katie\\VisitDayScheduler\\VisitDayScheduler\\src\\REALISTICSTUDENTDATA.tsv";
	String realisticProf = "C:\\Users\\katie\\VisitDayScheduler\\VisitDayScheduler\\src\\REALISTICPROFDATA.tsv";

	public boolean hasProf(ArrayList<Professor> profs, String name) {
		for (int i = 0; i < profs.size(); i++) {
			if (profs.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasStudent(ArrayList<Student> students, String name) {
		for (int i = 0; i < students.size(); i++) {
			if (students.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void longExample() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { longStudentFileName, profFileName });
		assertEquals(34, scheduler.getStudents().size());
	}

	@Test
	public void realisticExample() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { realisticStudent, realisticProf });
	}

	@Test
	public void simpleExample() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { studentFileName, profFileName });
		ArrayList<Student> students = scheduler.getStudents();
		ArrayList<Professor> professors = scheduler.getProfessors();
		assertEquals(hasProf(professors, "Adam Davis"), true);
		assertEquals(hasProf(professors, "Timothy Baker"), true);
		assertEquals(hasProf(professors, "Alice Smith"), true);
		assertEquals(hasProf(professors, "Jane Doe"), true);
		assertEquals(hasProf(professors, "Amy Brown"), true);
		assertEquals(hasStudent(students, "Katie S"), true);
		assertEquals(hasStudent(students, "Amanda G"), true);
		assertEquals(hasStudent(students, "Steve O"), true);
		assertEquals(hasStudent(students, "Bill A"), true);
		assertEquals(hasStudent(students, "John D"), true);
		assertEquals(hasStudent(students, "Joe E"), true);
		assertEquals(hasStudent(students, "Alice V"), true);
//		for (Student s : students) {
//			System.out.println(s.getName());
//			System.out.println(s.getMeetingsAssigned());
//			System.out.println(s.getNumPreferencesAssigned());
//			System.out.println();
//		}
		HashMap<String, Student> nameToStudent = scheduler.getNameToStudent();
		HashMap<String, Professor> nameToProf = scheduler.getNameToProf();
		ArrayList<Professor> list = nameToStudent.get("Katie S").getPreferences();
		assertEquals(list.size(), 5);
		assertEquals(nameToStudent.containsKey("Katie S"), true);
		assertEquals(nameToStudent.containsKey("Amanda G"), true);
		assertEquals(nameToStudent.containsKey("Steve O"), true);
		assertEquals(nameToStudent.containsKey("Bill A"), true);
		assertEquals(nameToStudent.containsKey("John D"), true);
		assertEquals(nameToStudent.containsKey("Joe E"), true);
		assertEquals(nameToStudent.containsKey("Alice V"), true);

//		ArrayList<Professor> list = nameToStudent.get("Katie S").getPreferences();
//		assertEquals(list.size(), 5);
		assertEquals(list.get(0).getName(), "Alice Smith");
		assertEquals(list.get(1).getName(), "Jane Doe");
		assertEquals(list.get(2).getName(), "Amy Brown");
		assertEquals(list.get(3).getName(), "Adam Davis");
		assertEquals(list.get(4).getName(), "Timothy Baker");
	}

}