import java.io.*;
import java.util.*;

/**
 * Processes the input for Visit Day Scheduler, including student preferences
 * and professor availability.
 * 
 * @author Katherine Sullivan
 */
public class InputProcessor {
	public static void main(String[] args) {
		File students, professors, schedule;
		if (args.length != 2 && args.length != 3) {
			System.out.println("Provide 2 arguments (filename of student preferences and filename of "
					+ "professor availability) or 3 arguments (filename of student preferences, filename "
					+ "of professor availability, and filename of existing schedule");
			return;
		}

		try {
			students = new File(args[0]);
		} catch (Exception e) {
			System.out.println("Error opening the students file. Please place the students file in the same directory "
					+ "as the Java program and check your spelling.");
			return;
		}
		try {
			professors = new File(args[1]);
		} catch (Exception e) {
			System.out.println("Error opening the professors file. Please place the professors file in the same "
					+ "directory as the Java program and check your spelling.");
			return;
		}

		if (args.length == 3) {
			try {
				schedule = new File(args[2]);
			} catch (Exception e) {
				System.out.println("Error opening the schedule file. Please place the schedule file in the same "
						+ "directory as the Java program and check your spelling.");
				return;
			}
		} else {
			processTwoInputs(students, professors);
		}
	}

	/*
	 * Used to actually process and store professor availability after the
	 * preprocessing function has verified that the timeslots are valid and that
	 * there are no duplicate names.
	 */
	public static HashMap<String, ArrayList<String>> processProfessorAvailability(File professors,
			HashMap<String, Integer> slotToIndex, int numSlots) throws Exception {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(professors));
		} catch (FileNotFoundException e1) {
			System.out.println("Professor filename \"" + professors + "\" not found.");
			System.exit(1);
		}

		String currLine;
		HashMap<String, ArrayList<String>> profToAvailability = new HashMap<>();

		csvReader.readLine(); // first line is just the column names

		while ((currLine = csvReader.readLine()) != null) {
			String[] data = currLine.split("\t"); // program assumes the file is a .tsv file
			ArrayList<String> list = new ArrayList<>();
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
			}
			profToAvailability.put(data[1], list); // program assumes names are in second column
		}
		csvReader.close();

		return profToAvailability;
	}

	/*
	 * Used to check for duplicate professor names and invalid timeslots. The
	 * program assumes that timeslots will be in 30-minute intervals starting no
	 * earlier than 8:00 AM and starting no later than 8:00 PM. This method goes
	 * through all professor availability and determines the earliest and latest
	 * timeslots, adjusting the timeslots used in the program accordingly.
	 */
	public static int preprocessProfessors(File professors, HashMap<String, Integer> slotToIndex,
			HashSet<String> profNames) throws Exception {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(professors));
		} catch (FileNotFoundException e1) {
			System.out.println("Professor filename \"" + professors + "\" not found.");
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
		// create a hashmap of timeslots to indices to use for professor availability
		for (int i = 0; i < timeslots.size(); i++) {
			slotToIndex.put(timeslots.get(i), i);
		}

		return timeslots.size(); // return number of timeslots to use in actual processing of availability
	}

	public static void processTwoInputs(File students, File professors) {
		HashMap<String, Integer> slotToIndex = new HashMap<>();
		HashSet<String> profNames = new HashSet<>();
		HashMap<String, ArrayList<String>> profToAvailability = new HashMap<>();
		HashMap<String, ArrayList<String>> studentToPreferences = new HashMap<>();
		try {
			int numSlots = preprocessProfessors(professors, slotToIndex, profNames);
			profToAvailability = processProfessorAvailability(professors, slotToIndex, numSlots);
			studentToPreferences = processStudents(students, profNames);
		} catch (Exception e) {
			System.out.println("Exception thrown in input processing method. Terminating program.");
			e.printStackTrace();
			System.exit(1);
		}

		for (Map.Entry<String, Integer> entry : slotToIndex.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			System.out.println(value + ": " + key);
		}

		for (Map.Entry<String, ArrayList<String>> entry : profToAvailability.entrySet()) {
			String key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			System.out.println("Professor Name: " + key);
			for (String s : value) {
				System.out.print(s + " ");
			}
			System.out.println();
		}

		for (Map.Entry<String, ArrayList<String>> entry : studentToPreferences.entrySet()) {
			String key = entry.getKey();
			ArrayList<String> value = entry.getValue();
			System.out.println("Student Name: " + key);
			for (String s : value) {
				System.out.print(s + " ");
			}
			System.out.println();
		}
	}

	/*
	 * Used to process student preferences. If duplicate student names are found or
	 * an unknown professor name is listed, the program will exit. If a student has
	 * duplicate preferences, only the first occurrence of the duplicate preference
	 * will be kept. For example, if the student wrote (Prof 1, Prof 2, Prof 1), the
	 * program will simplify this to (Prof 1, Prof 2).
	 */
	public static HashMap<String, ArrayList<String>> processStudents(File students, HashSet<String> profNames)
			throws Exception {
		BufferedReader csvReader = null;
		try {
			csvReader = new BufferedReader(new FileReader(students));
		} catch (FileNotFoundException e1) {
			System.out.println("Student filename \"" + students + "\" not found.");
			System.exit(1);
		}

		String currLine;
		HashSet<String> studentNames = new HashSet<>();
		HashMap<String, ArrayList<String>> studentToPreferences = new HashMap<>();

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
			Set<String> temp = new LinkedHashSet<>();
			// the program assumes the five preferences are in columns 6 to 10
			for (String s : data) {
				System.out.print(s + "|");
			}
			System.out.println();
			for (int i = 5; i < data.length; i++) {
				if (!data[i].isEmpty()) { // only add if not a blank preference
					if (!profNames.contains(data[i])) {
				          throw new Exception("Error: professor name " + data[i] + " not found.");
					}
					temp.add(data[i]);
				}
			}
			ArrayList<String> preferences = new ArrayList<>();
			preferences.addAll(temp);
			studentToPreferences.put(name, preferences);
		}
		csvReader.close();

		return studentToPreferences;
	}

	public void processThreeInputs() throws Exception {
		throw new Exception("Unimplemented");
	}

}
