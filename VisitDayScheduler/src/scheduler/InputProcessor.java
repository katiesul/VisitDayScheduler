package scheduler;
import java.io.*;
import java.util.*;

/**
 * Processes the input for Visit Day Scheduler, including student preferences
 * and professor availability.
 * 
 * @author Katherine Sullivan
 */
public class InputProcessor {
	private int numSlots;
	private HashMap<String, Integer> slotToIndex;
	private HashMap<Integer, String> indexToSlot;
	private HashSet<String> profNames;
	private HashMap<String, ArrayList<String>> profToAssignments;
	private HashMap<String, ArrayList<Integer>> profToAvailability;
	private HashMap<String, ArrayList<Professor>> studentToPreferences;
	private ArrayList<Student> students;
	private HashMap<String, Student> nameToStudent;
	private ArrayList<Professor> professors;
	private HashMap<String, Professor> nameToProf;

	public InputProcessor() {
		slotToIndex = new HashMap<>();
		indexToSlot = new HashMap<>();
		profNames = new HashSet<>();
		profToAssignments = new HashMap<>();
		studentToPreferences = new HashMap<>();
		profToAvailability = new HashMap<>();
		nameToStudent = new HashMap<>();
	}
	
	public HashMap<String, Student> getNameToStudent() {
		return nameToStudent;
	}
	
	public HashMap<String, Professor> getNameToProf() {
		return nameToProf;
	}
	
	public HashMap<Integer, String> getIndexToSlot() {
		return indexToSlot;
	}
	
	public ArrayList<Student> getStudents() {
		return students;
	}
	
	public ArrayList<Professor> getProfessors() {
		return professors;
	}
	
	public HashMap<String, Integer> getSlotToIndex() {
		return slotToIndex;
	}
	
	public HashSet<String> getProfNames() {
		return profNames;
	}
	
	public HashMap<String, ArrayList<String>> getProfToAssignments() {
		return profToAssignments;
	}
	
	public HashMap<String, ArrayList<Integer>> getProfToAvailability() {
		return profToAvailability;
	}
	
	public HashMap<String, ArrayList<Professor>> getStudentToPreferences() {
		return studentToPreferences;
	}
	
	public int getNumSlots() {
		return numSlots;
	}

	/*
	 * Used to actually process and store professor availability after the
	 * preprocessing function has verified that the timeslots are valid and that
	 * there are no duplicate names.
	 */
	public void processProfessorAvailability(File profName, int numSlots)
			throws Exception {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(profName));
		} catch (FileNotFoundException e1) {
			System.out.println("Professor filename \"" + profName + "\" not found.");
			System.exit(1);
		}

		String currLine;
		
		csvReader.readLine(); // first line is just the column names
		nameToProf = new HashMap<>();
		professors = new ArrayList<>();

		while ((currLine = csvReader.readLine()) != null) {
			String[] data = currLine.split("\t"); // program assumes the file is a .tsv file
			ArrayList<String> list = new ArrayList<>();
			ArrayList<Integer> availSlots = new ArrayList<>();
			// initialize all timeslots to unavailable for the professor
			for (int i = 0; i < numSlots; i++) {
				list.add("UNAVAILABLE");
			}

			/*
			 * Now fill in the actual availability. The program assumes there is a
			 * comma-separated list of available timeslots stored in the third column from
			 * left.
			 */
			String[] slots = data[2].split(",");
			for (String s : slots) {
				String str = s.replaceAll("\\s+", ""); // remove whitespace in timeslot string
				int index = slotToIndex.get(str);
				list.set(index, "AVAILABLE"); // update to show professor available at this timeslot
				availSlots.add(index);
			}
			Professor prof = new Professor(data[1], list, availSlots);
			professors.add(prof);
			nameToProf.put(data[1], prof);
			profToAssignments.put(data[1], list); // program assumes names are in second column
			profToAvailability.put(data[1], availSlots);
		}
		csvReader.close();
	}

	/*
	 * Used to check for duplicate professor names and invalid timeslots. The
	 * program assumes that timeslots will be in 30-minute intervals starting no
	 * earlier than 8:00 AM and starting no later than 8:00 PM. This method goes
	 * through all professor availability and determines the earliest and latest
	 * timeslots, adjusting the timeslots used in the program accordingly.
	 */
	public int preprocessProfessors(File profFile) throws Exception {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(profFile));
		} catch (FileNotFoundException e1) {
			System.out.println("Professor filename \"" + profFile + "\" not found.");
			System.exit(1);
		}

		ArrayList<String> timeslots = new ArrayList<>(Arrays.asList("8:00AM-8:30AM", "8:30AM-9:00AM", "9:00AM-9:30AM",
				"9:30AM-10:00AM", "10:00AM-10:30AM", "10:30AM-11:00AM", "11:00AM-11:30AM", "11:30AM-12:00PM",
				"12:00PM-12:30PM", "12:30PM-1:00PM", "1:00PM-1:30PM", "1:30PM-2:00PM", "2:00PM-2:30PM", "2:30PM-3:00PM",
				"3:00PM-3:30PM", "3:30PM-4:00PM", "4:00PM-4:30PM", "4:30PM-5:00PM", "5:00PM-5:30PM", "5:30PM-6:00PM",
				"6:00PM-6:30PM", "6:30PM-7:00PM", "7:00PM-7:30PM", "7:30PM-8:00PM"));

		Set<String> usedSlots = new HashSet<String>(); // use to see which timeslots are actually needed
		String currLine;

		csvReader.readLine(); // first line is just the column names

		while ((currLine = csvReader.readLine()) != null) {
			String[] data = currLine.split("\t"); // program assumes the file is a .tsv file

			// program assumes professors' names will be in the second column from left
			if (profNames.contains(data[1])) {
				throw new Exception("Error: duplicate professor name found for the name \"" + data[1]
						+ "\". Please remove if it is a duplicate or add some distinguishing character to the names, "
						+ "such as \"John Smith1\" and \"John Smith2\".");
			} else {
				profNames.add(data[1]);
			}

			/*
			 * The program assumes there is a comma-separated list of available timeslots
			 * stored in the third column from left.
			 */
			String[] slots = data[2].split(",");
			for (String s : slots) {
				String str = s.replaceAll("\\s+", ""); // remove whitespace in timeslot string
				if (!timeslots.contains(str)) {
					throw new Exception("Error: the program does not recognize the timeslot " + s + " in the professor "
							+ "availability file for professor " + data[1] + ".");
				}
				usedSlots.add(str);
			}
		}
		csvReader.close();

		/*
		 * timeslots are in order so we check in the front and back to see which
		 * timeslots are too early/too late and are unused
		 */
		while (!usedSlots.contains(timeslots.get(0))) {
			timeslots.remove(0);
		}
		while (!usedSlots.contains(timeslots.get(timeslots.size() - 1))) {
			timeslots.remove(timeslots.size() - 1);
		}
		// create a hashmap of timeslots to indices (and vice versa) to use for professor availability
		for (int i = 0; i < timeslots.size(); i++) {
			slotToIndex.put(timeslots.get(i), i);
			indexToSlot.put(i, timeslots.get(i));
		}

		return timeslots.size(); // return number of timeslots to use in actual processing of availability
	}

	public void processTwoInputs(File studentFile, File profFile) {
		try {
			numSlots = preprocessProfessors(profFile);
			processProfessorAvailability(profFile, numSlots); // fill out profToAssignments, profToAvailability
			processStudents(studentFile); // fill out studentToPreferences
		} catch (Exception e) {
			System.out.println("Exception thrown in input processing method. Terminating program.");
			e.printStackTrace();
			System.exit(1);
		}
		
		// populate list of students to pass to Scheduler
		students = new ArrayList<>();
		for (Map.Entry<String, ArrayList<Professor>> entry : studentToPreferences.entrySet()) {
			Student student = new Student(entry.getKey(), entry.getValue(), numSlots);
			students.add(student);
			nameToStudent.put(entry.getKey(), student);
		}
		
/*		System.out.println("SLOTTOINDEX:");
		for (Map.Entry<String, Integer> entry : slotToIndex.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			System.out.println(value + ": " + key);
		}

		for (Map.Entry<String, ArrayList<String>> entry : profToAssignments.entrySet()) {
			String key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			System.out.println("Professor Name: " + key);
			for (String s : value) {
				System.out.print(s + " ");
			}
			System.out.println();
		}

		for (Map.Entry<String, ArrayList<Professor>> entry : studentToPreferences.entrySet()) {
			String key = entry.getKey();
			ArrayList<Professor> value = entry.getValue();
			System.out.println("Student Name: " + key);
			for (Professor p : value) {
				System.out.print(p.getName() + " ");
			}
			System.out.println();
		}
		*/
	}

	/*
	 * Used to process student preferences. If duplicate student names are found or
	 * an unknown professor name is listed, the program will exit. If a student has
	 * duplicate preferences, only the first occurrence of the duplicate preference
	 * will be kept. For example, if the student wrote (Prof 1, Prof 2, Prof 1), the
	 * program will simplify this to (Prof 1, Prof 2).
	 */
	public void processStudents(File studentFile)
			throws Exception {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(studentFile));
		} catch (FileNotFoundException e1) {
			System.out.println("Student filename \"" + studentFile + "\" not found.");
			System.exit(1);
		}

		String currLine;
		HashSet<String> studentNames = new HashSet<>();

		csvReader.readLine(); // first line is just the column names

		while ((currLine = csvReader.readLine()) != null) {
			String[] data = currLine.split("\t"); // program assumes the file is a .tsv file
			String name = data[2] + " " + data[1]; // program assumes first and last names in third and second columns
			if (studentNames.contains(name)) {
				throw new Exception("Error: duplicate student name found for the name \"" + data[2] + " " + data[1]
						+ "\". Please remove if it is a duplicate or add some distinguishing character to the names, "
						+ "such as \"John Smith1\" and \"John Smith2\".");
			}
			studentNames.add(name);
			Set<Professor> temp = new LinkedHashSet<>();
			// the program assumes the five preferences are in columns 6 to 10
		/*	for (String s : data) {
				System.out.print(s + "|");
			}
			System.out.println(); */
			for (int i = 5; i < data.length; i++) {
				if (!data[i].isEmpty()) { // only add if not a blank preference
					if (!profNames.contains(data[i])) {
				          throw new Exception("Error: professor name " + data[i] + " not found.");
					}
				}
			}
			ArrayList<Professor> preferences = new ArrayList<>();
			preferences.addAll(temp);
			studentToPreferences.put(name, preferences);
		}
		csvReader.close();
	}

	public void processThreeInputs(File studentFile, File profFile, File schedule) throws Exception {
		throw new Exception("Unimplemented");
	}

}
