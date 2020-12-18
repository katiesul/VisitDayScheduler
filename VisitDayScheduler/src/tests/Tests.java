package tests;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import scheduler.*;

import java.io.PrintWriter;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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
	String studentEmptyPrefs = "C:/Users/katie/VisitDayScheduler/VisitDayScheduler/src/STUDENTDATANOPREFS.tsv";

	ArrayList<String> studentNames = new ArrayList<>(Arrays.asList("Amanda A", "Bobby B", "Camila C", "Doug D",
			"Esther E", "Frances F", "George G", "Hugh H", "Isaiah I", "John J", "Kristen K", "Landon L", "Marie M",
			"Nancy N", "Olivia O", "Phoebe P", "Quinton Q", "Radley R", "Sam S", "Tanya T", "Ursula U", "Vanessa V",
			"Willa W", "Xavier X", "Yolanda Y", "Zach Z", "Ashley A", "Billy B", "Chris C", "Demi D", "Ezekiel E",
			"Frank F", "Gabby G", "Harry H", "Irene I", "Jake J", "Kameron K", "Laura L", "Mandy M", "Norah N",
			"Ophelia O", "Penelope P", "Quigley Q", "Randall R", "Sonya S", "Tabitha T", "Ulysses U", "Velma V",
			"William W", "XX X", "YY Y", "ZZ Z"));
	ArrayList<String> profNames = new ArrayList<>(Arrays.asList("Prof 1", "Prof 2", "Prof 3", "Prof 4", "Prof 5",
			"Prof 6", "Prof 7", "Prof 8", "Prof 9", "Prof 10", "Prof 11", "Prof 12", "Prof 13", "Prof 14", "Prof 15",
			"Prof 16", "Prof 17", "Prof 18", "Prof 19", "Prof 20", "Prof 21", "Prof 22", "Prof 23", "Prof 24",
			"Prof 25", "Prof 26", "Prof 27", "Prof 28", "Prof 29", "Prof 30"));

	ArrayList<String> timeslots = new ArrayList<>(Arrays.asList("8:00AM-8:30AM", "8:30AM-9:00AM", "9:00AM-9:30AM",
			"9:30AM-10:00AM", "10:00AM-10:30AM", "10:30AM-11:00AM", "11:00AM-11:30AM", "11:30AM-12:00PM",
			"12:00PM-12:30PM", "12:30PM-1:00PM", "1:00PM-1:30PM", "1:30PM-2:00PM", "2:00PM-2:30PM", "2:30PM-3:00PM",
			"3:00PM-3:30PM", "3:30PM-4:00PM", "4:00PM-4:30PM", "4:30PM-5:00PM", "5:00PM-5:30PM", "5:30PM-6:00PM",
			"6:00PM-6:30PM", "6:30PM-7:00PM", "7:00PM-7:30PM", "7:30PM-8:00PM"));

	// helper method for tests
	public boolean hasProf(ArrayList<Professor> profs, String name) {
		for (int i = 0; i < profs.size(); i++) {
			if (profs.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	// helper method for tests
	public boolean hasStudent(ArrayList<Student> students, String name) {
		for (int i = 0; i < students.size(); i++) {
			if (students.get(i).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	// helper method for test generator
	public String randomSlots() {
		Random rand = new Random(System.currentTimeMillis());
		HashSet<Integer> slots = new HashSet<>();
		String str = "";
		int numSlots = rand.nextInt((timeslots.size() - 1 - 5) + 1) + 5;
		for (int i = 0; i < numSlots; i++) {
			int randomSlot = rand.nextInt((timeslots.size() - 1 - 0) + 1) + 0;
			while (slots.contains(randomSlot)) {
				randomSlot = rand.nextInt((timeslots.size() - 1 - 0) + 1) + 0;
			}
			slots.add(randomSlot);
			str += timeslots.get(randomSlot);
			if (i != numSlots - 1) {
				str += ", ";
			}
		}
		return str;
	}

	// helper method for test generator
	public String randomPrefs(int currNumProfs) {
		Random rand = new Random(System.currentTimeMillis());
		String str = "";
		int numPrefs = rand.nextInt((5 - 1 - 0) + 1) + 0;
		for (int i = 0; i < numPrefs; i++) {
			// has to be within the first [# of professors] slots of this iteration
			int randomPref = rand.nextInt((currNumProfs - 1 - 1 - 0) + 1) + 0;
			str += profNames.get(randomPref);
			if (i != numPrefs - 1) {
				str += "\t";
			}
		}
		return str;
	}

	@Test
	public void testGenerator() {

		int numTests = 50;

		for (int i = 0; i < numTests; i++) {
			// randomly decide how many professors and students in this iteration
			Random rand = new Random(System.currentTimeMillis());
			int numProfs = rand.nextInt((profNames.size() - 1 - 5) + 1) + 5;
			int numStudents = rand.nextInt((studentNames.size() - 1 - 5) + 1) + 5;
			// first create professor file
			PrintWriter writer;
			String profFilename = "", studentFilename = "";
			try {
				profFilename = "C:/Users/katie/VisitDayScheduler/VisitDayScheduler/src/TESTINGPURPOSESPROFDATA.tsv";
				writer = new PrintWriter(profFilename, "UTF-8");
				writer.write("Timestamp\tFull Name\tTimeslots"); // write first line
				for (int j = 0; j < numProfs; j++) {
					writer.write("\n10/26/2020 20:55:28\t" + profNames.get(j) + "\t" + randomSlots());
				}
				writer.close();
			} catch (Exception e) {
				System.out.println("An error occurred creating TESTINGPURPOSESPROFDATA.tsv.");
				e.printStackTrace();
			}
			// next create student file
			try {
				studentFilename = "C:/Users/katie/VisitDayScheduler/VisitDayScheduler/src/TESTINGPURPOSESSTUDENTDATA.tsv";
				writer = new PrintWriter(studentFilename, "UTF-8");
				// write first line
				writer.write(
						"Timestamp\tLast Name\tFirst Name\tIndicate Events\tAllergies?\tFaculty Pref 1\tFaculty Pref 2\tFaculty Pref 3\tFaculty Pref 4\tFaculty Pref 5");
				for (int j = 0; j < numStudents; j++) {
					String firstName = studentNames.get(j).split("\\s+")[0];
					String lastName = studentNames.get(j).split("\\s+")[1];
					writer.write("\n10/26/2020 20:55:28\t" + lastName + "\t" + firstName
							+ "\tPre-Visit Day Reception\tno\t" + randomPrefs(numProfs));
				}
				writer.close();
			} catch (Exception e) {
				System.out.println("An error occurred creating TESTINGPURPOSESPROFDATA.tsv.");
				e.printStackTrace();
			}
			Scheduler scheduler = new Scheduler();
			scheduler.main(new String[] { studentFilename, profFilename });
			scheduler.cleanUpFiles();
//			assertEquals(true, scheduler.verifySchedule());
		}
	}

	@Test
	public void longExample() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { longStudentFileName, profFileName });
		scheduler.cleanUpFiles();
		assertEquals(34, scheduler.getStudents().size());
	}

	@Test
	public void testEmptyPrefs() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { studentEmptyPrefs, realisticProf });
		scheduler.cleanUpFiles();
	}

	@Test
	public void realisticExample() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { realisticStudent, realisticProf });
		scheduler.cleanUpFiles();
	}

	@Test
	public void simpleExample() {
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { studentFileName, profFileName });
		scheduler.cleanUpFiles();
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