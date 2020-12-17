package scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

/**
 * Schedules the student-professor visit day meetings.
 * 
 * @author Katherine Sullivan
 */

//TODO: TOURS?
public class Scheduler {

	private static final int MAX_PROFESSOR_MEETINGS = 10;
	private static final int MIN_STUDENT_MEETINGS = 5;
	// TODO: PLAY WITH RNA_MAX AND CYCLES CONSTANTS BELOW.
	private static final int RNA_MAX = 25;
	private static final int CYCLES = 25;
	private static final int MEETING_WITH_SAME_PERSON_PENALTY = 5;
	private static final int NOT_AVAILABLE_THEN_PENALTY = 10;
	private static HashMap<Integer, String> indexToSlot;
	private static ArrayList<Professor> professors;
	private static ArrayList<Student> students;
	private static int currSatisfaction;
	private static int maxSatisfaction;
	private static int numSlots;
	private static boolean prevScheduleLoaded = false;
	private static HashMap<String, Student> nameToStudent;
	private static HashMap<String, Professor> nameToProf;
	private static ArrayList<String> outputFileNames;

	public static void main(String[] args) {
		InputProcessor processor;
		if (args.length > 0) {
			processor = processCommandLineArgs(args);
		} else {
			processor = processPromptedInput();
		}

		// populate data structures
		professors = processor.getProfessors();
		students = processor.getStudents();
		indexToSlot = processor.getIndexToSlot();
		numSlots = processor.getNumSlots();
		nameToStudent = processor.getNameToStudent();
		nameToProf = processor.getNameToProf();

		if (!prevScheduleLoaded) {
			createInitialSchedule();
		}

		calculateMaxSatisfaction();
		calculateCurrentSatisfaction(0);

		if (currSatisfaction != maxSatisfaction) {
			int numCycles = 0;
			while (numCycles < CYCLES) {
				performRNA();
				numCycles++;
			}
		}

		outputResults();
		System.out.println(currSatisfaction);
	}

	public static InputProcessor processPromptedInput() {
		File studentFile = null, profFile = null, prevStudentSchedule = null, prevProfessorSchedule = null;
		ArrayList<File> tourFiles = new ArrayList<>();
		Scanner scanner = new Scanner(System.in);
		Boolean prevSchedule = false;

		// load student preferences file
		System.out.println("Please type the full filepath of student preferences file, then press enter: ");
		String studentFilename = scanner.nextLine();
		try {
			studentFile = new File(studentFilename);
		} catch (Exception e) {
			System.out.println("Error opening the students file. Please enter the full filepath and check spelling.");
			System.exit(1); // terminate program
		}

		// load professor availability file
		System.out.println("Please type the full filepath of professor availability file, then press enter: ");
		String profFilename = scanner.nextLine();
		try {
			profFile = new File(profFilename);
		} catch (Exception e) {
			System.out.println("Error opening the professors file. Please enter the full filepath and check spelling.");
			System.exit(1); // terminate program
		}

		// load previous student/professor schedule if any
		System.out.println("Are you loading a previous schedule? If so, type Y and then press enter: ");
		String response = scanner.nextLine();
		if (response.toUpperCase().equals("Y")) { // accept "Y" and "y"
			System.out
					.println("Please type the full filepath of the previous STUDENT schedule file, then press enter:");
			String prevStudentScheduleFile = scanner.nextLine();
			try {
				prevStudentSchedule = new File(prevStudentScheduleFile);
			} catch (Exception e) {
				System.out.println(
						"Error opening the previous student schedule file. Please enter the full filepath and check spelling.");
				System.exit(1); // terminate program
			}
			System.out.println(
					"Please type the full filepath of the previous PROFESSOR schedule file, then press enter:");
			String prevProfScheduleFile = scanner.nextLine();
			try {
				prevProfessorSchedule = new File(prevProfScheduleFile);
			} catch (Exception e) {
				System.out.println(
						"Error opening the previous professor schedule file. Please enter the full filepath and check spelling.");
				System.exit(1); // terminate program
			}
			prevScheduleLoaded = true;
			prevSchedule = true;
		}

		System.out.println("Do you have any tour files to add? If so, type Y :");
		String response2 = scanner.nextLine();
		if (response2.toUpperCase().equals("Y")) { // accept "Y" and "y"
			int count = 1;
			while (true) {
				File currTourFile;
				System.out.println("Please enter the full filename of tour file #" + count + ":");
				String currTourFilename = scanner.nextLine();
				try {
					currTourFile = new File(currTourFilename);
					tourFiles.add(currTourFile);
				} catch (Exception e) {
					System.out.println("Error opening tour file #" + count
							+ ". Please enter the full filepath and check spelling.");
					System.exit(1); // terminate program
				}
				count++;
			}
		}

		InputProcessor processor = new InputProcessor();

		// first process the professor and student information
		processor.processTwoInputs(studentFile, profFile);

		// tour files need to be processed first in case there is a previous schedule
		if (tourFiles.size() > 0) {
			try {
				processor.processTourFiles(tourFiles);
			} catch (Exception e) {
				System.out.println("Error in processing tour files.");
				System.exit(1); // terminate program
			}
		}

		if (prevSchedule) {
			try {
				processor.processPreviousSchedule(prevStudentSchedule, prevProfessorSchedule);
			} catch (Exception e) {
				System.out.println("Error in processing previous schedule files.");
				System.exit(1); // terminate program
			}
		}

		return processor;
	}

	// TODO: take into account student availability when assigning meetings

	/*
	 * 2 modes supported using command line arguments: only entering
	 * professor/student info; entering professor/student info as well as student
	 * tour files
	 */
	public static InputProcessor processCommandLineArgs(String[] args) {
		File studentFile = null, profFile = null;
		ArrayList<File> tourFiles = new ArrayList<>();
		if (args.length < 2) {
			System.out.println("Provide 2 arguments (filepath of student preferences and filepath of "
					+ "professor availability) or more (filepath of student preferences, filepath of professor availability, and "
					+ "filepath of any tour information files");
			System.exit(1); // terminate program
		}

		try {
			studentFile = new File(args[0]);
		} catch (Exception e) {
			System.out.println("Error opening the students file. Please enter the full filepath and check spelling.");
			System.exit(1); // terminate program
		}
		try {
			profFile = new File(args[1]);
		} catch (Exception e) {
			System.out.println("Error opening the professors file. Please enter the full filepath and check spelling.");
			System.exit(1); // terminate program
		}

		InputProcessor processor = new InputProcessor();

		// first process the professor and student information
		processor.processTwoInputs(studentFile, profFile);

		// process tour files, if any
		int count = 1;
		if (args.length > 2) {
			int i = 2; // start at 3rd command line argument (first tour filepath)
			while (i < args.length) {
				try {
					File tourFile = new File(args[i]);
					tourFiles.add(tourFile);
				} catch (Exception e) {
					System.out.println("Error opening tour file #" + count
							+ ". Please enter the full filepath and check spelling.");
					System.exit(1); // terminate program
				}
				count++;
			}
			try {
				processor.processTourFiles(tourFiles);
			} catch (Exception e) {
				System.out.println("Error in processing tour files.");
				System.exit(1); // terminate program
			}
		}

		return processor;
	}

	public static void printSchedule() {
		populateStudentSchedule();
		System.out.print("\t");
		for (int i = 0; i < numSlots; i++) {
			System.out.print(indexToSlot.get(i));
			if (i != numSlots - 1) {
				System.out.print("\t");
			}
		}
		System.out.println();
		for (Student s : students) {
			System.out.print(s.getName());
			for (String str : s.getSchedule()) {
				if (str != null) {
					System.out.print("\t" + str);
				} else {
					System.out.print("\t----------");
				}
			}
			System.out.print("\t");
			System.out.println();
		}
	}

	// do we need this?
	public static void populateStudentSchedule() {
		for (Professor p : professors) {
			int i = 0;
			for (String str : p.getAvailability()) {
				Student s = nameToStudent.get(str);
				if (s != null) {
					s.addToSchedule(p, i);
				}
				i++;
			}
		}
	}

	public static void outputResults() {
		HashMap<Integer, ArrayList<Student>> studentToNumMeetings = new HashMap<>();
		outputFileNames = new ArrayList<>();
		Random rand = new Random(System.currentTimeMillis());
		int randomNum = rand.nextInt(5000);
		PrintWriter writer;
		String filename;
		try {
			filename = randomNum + "StudentSchedule.tsv";
			outputFileNames.add(filename);
			writer = new PrintWriter(filename, "UTF-8");
			writer.print("\t");
			for (int i = 0; i < numSlots; i++) {
				writer.print(indexToSlot.get(i));
				if (i != numSlots - 1) {
					writer.print("\t");
				}
			}
			writer.println();
			for (Student s : students) {
				// we want to be able to sort students by # of meetings in an output file
				if (studentToNumMeetings.get(s.getMeetingsAssigned()) == null) {
					studentToNumMeetings.put(s.getMeetingsAssigned(), new ArrayList<Student>());
				}
				studentToNumMeetings.get(s.getMeetingsAssigned()).add(s);
				writer.print(s.getName());
				for (String str : s.getSchedule()) {
					if (str != null) {
						writer.print("\t" + str);
					} else {
						writer.print("\t----------");
					}
				}
				writer.print("\t");
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("Error creating StudentSchedule.tsv");
			e.printStackTrace();
		}

		try {
			filename = randomNum + "ProfessorSchedule.tsv";
			outputFileNames.add(filename);
			writer = new PrintWriter(filename, "UTF-8");
			writer.print("\t");
			for (int i = 0; i < numSlots; i++) {
				writer.print(indexToSlot.get(i));
				if (i != numSlots - 1) {
					writer.print("\t");
				}
			}
			writer.println();
			for (Professor p : professors) {
				writer.print(p.getName());
				for (String str : p.getAvailability()) {
					if (!str.equals("UNAVAILABLE")) {
						writer.print("\t" + str);
					} else {
						writer.print("\t----------");
					}
				}
				writer.print("\t");
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("Error creating ProfessorSchedule.tsv");
			e.printStackTrace();
		}

		boolean atLeastOneStudentPrinted = false;

		try {
			filename = randomNum + "UnreceivedStudentPreferences.txt";
			outputFileNames.add(filename);
			writer = new PrintWriter(filename, "UTF-8");
			for (Student s : students) {
				boolean flag = false;
				int i = 0;
				String str = "";
				for (boolean bool : s.getPreferenceReceived()) {
					if (bool == false) {
						atLeastOneStudentPrinted = true;
						// we only print names if they have at least one preference not received
						if (flag == false) {
							writer.print(s.getName() + ": ");
							flag = true;
						}
						str += s.getPreferences().get(i).getName() + " (" + i + "), ";
					}
					i++;
				}
				if (str.length() > 1) {
					writer.write(str.substring(0, str.length() - 2) + "\n");
				}
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("Error creating UnreceivedStudentPreferences.txt");
			e.printStackTrace();
		}

		try {
			filename = randomNum + "StudentsAndNumberOfMeetings.txt";
			outputFileNames.add(filename);
			writer = new PrintWriter(filename, "UTF-8");
			// sort by increasing # of meetings
			ArrayList<Integer> nums = new ArrayList<>(studentToNumMeetings.keySet());
			Collections.sort(nums);
			writer.write("[Number of Meetings]: [Names of Students]\n\n");
			for (Integer currNum : nums) {
				writer.print(currNum + ": ");
				ArrayList<Student> currList = studentToNumMeetings.get(currNum);
				for (int j = 0; j < currList.size(); j++) {
					writer.print(currList.get(j).getName());
					if (j != currList.size() - 1) {
						writer.print(", ");
					}
				}
				writer.print("\n\n");
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("Error creating StudentsAndNumberOfMeetings.txt");
			e.printStackTrace();
		}

		System.out.println("Student schedule written to " + randomNum + "StudentSchedule.tsv.");
		System.out.println("Professor schedule written to " + randomNum + "ProfessorSchedule.tsv.");
		if (atLeastOneStudentPrinted) {
			System.out.println("Students and the preferences they did not receive written to " + randomNum
					+ "UnreceivedStudentPreferences.txt");
		}
		System.out.println("Students sorted by number of meetings written to " + randomNum + "StudentsAndNumberOfMeetings.txt");

		System.out.println();
	}

	public static void calculateMaxSatisfaction() {
		int totalPossible = 0;
		for (Student s : students) {
			int val = 5, size = s.getPreferenceReceived().size();
			for (int i = 0; i < size; i++) {
				totalPossible += val;
				val--;
			}
		}
		maxSatisfaction = totalPossible;
	}

	public static void randomMove() {
		// randomly try a neighboring state

		// randomly decide whether to switch from students' or professors' perspective
		Random rand = new Random(System.currentTimeMillis());
		int studentsOrProfessors = rand.nextInt() % 2;
		if (studentsOrProfessors == 0) {
			// randomly select two distinct students
			Student randomStudent1 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			Student randomStudent2 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			while (randomStudent1.equals(randomStudent2)) {
				randomStudent2 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			}
			// randomly select timeslots
			int student1RandomSlot = rand.nextInt(randomStudent1.getSchedule().size() - 1 - 0) + 0;

			int student2RandomSlot = rand.nextInt(randomStudent2.getSchedule().size() - 1 - 0) + 0;
			Professor prof1 = nameToProf.get(randomStudent1.getSchedule().get(student1RandomSlot));
			Professor prof2 = nameToProf.get(randomStudent2.getSchedule().get(student2RandomSlot));
			if (prof1 != null) {
				prof1.removeMeeting(student1RandomSlot, randomStudent1);
				prof1.setMeeting(student2RandomSlot, randomStudent2, randomStudent2.getNumPreference(prof1));
			}
			if (prof2 != null) {
				prof2.removeMeeting(student2RandomSlot, randomStudent2);
				prof2.setMeeting(student1RandomSlot, randomStudent1, randomStudent1.getNumPreference(prof2));
			}

			// calculate potential penalty
			// first calculate if professors are meeting with the same student > 1 time
			int penalty = 0;
			if (prof1 != null) {
				int count = 0;
				for (String str : prof1.getAvailability()) {
					if (str.equals(randomStudent2.getName())) {
						count++;
					}
				}
				if (count > 1) {
					penalty += MEETING_WITH_SAME_PERSON_PENALTY;
				}
			}
			if (prof2 != null) {
				int count = 0;
				for (String str : prof2.getAvailability()) {
					if (str.equals(randomStudent1.getName())) {
						count++;
					}
				}
				if (count > 1) {
					penalty += MEETING_WITH_SAME_PERSON_PENALTY;
				}
			}
			// calculate if the students were not available at that time
			if (randomStudent1 != null
					&& !randomStudent1.getOriginalAvailability().get(student2RandomSlot).equals("AVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}
			if (randomStudent2 != null
					&& !randomStudent2.getOriginalAvailability().get(student1RandomSlot).equals("AVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}
			// now calculate if the professors were not available at that time
			if (prof1 != null) {
				if (prof1.getOriginalAvailability().get(student2RandomSlot).equals("UNAVAILABLE")) {
					penalty += NOT_AVAILABLE_THEN_PENALTY;
				}
			}
			if (prof2 != null) {
				if (prof2.getOriginalAvailability().get(student1RandomSlot).equals("UNAVAILABLE")) {
					penalty += NOT_AVAILABLE_THEN_PENALTY;
				}
			}

			calculateCurrentSatisfaction(penalty);
		} else {
			// randomly select two distinct professors
			rand = new Random(System.currentTimeMillis());
			Professor randomProf1 = professors.get(rand.nextInt(professors.size() - 1 - 0) + 0);
			rand = new Random(System.currentTimeMillis());
			Professor randomProf2 = professors.get(rand.nextInt(professors.size() - 1 - 0) + 0);
			while (randomProf1.equals(randomProf2)) {
				randomProf2 = professors.get(rand.nextInt(professors.size() - 1 - 0) + 0);
			}
			// randomly select timeslots
			int prof1Slot = rand.nextInt(randomProf1.getAvailability().size() - 1 - 0) + 0;
			int prof2Slot = rand.nextInt(randomProf2.getAvailability().size() - 1 - 0) + 0; // make the switch
			Student prof1OldStudent = nameToStudent.get(randomProf1.getAvailability().get(prof1Slot));
//			System.out.println("name: " + randomProf1.getAvailability().get(prof1Slot) + " result: " + prof1OldStudent);
			Student prof2OldStudent = nameToStudent.get(randomProf2.getAvailability().get(prof2Slot));
//			System.out.println("name: " + randomProf2.getAvailability().get(prof2Slot) + " result: " + prof2OldStudent + "\n");
			randomProf1.removeMeeting(prof1Slot, prof1OldStudent);
			randomProf2.removeMeeting(prof2Slot, prof2OldStudent);
			int prof1OldStudentPref = -1, prof2OldStudentPref = -1;
			if (prof1OldStudent != null) {
				prof1OldStudentPref = prof1OldStudent.getNumPreference(randomProf2);
			}
			if (prof2OldStudent != null) {
				prof2OldStudentPref = prof2OldStudent.getNumPreference(randomProf1);
			}
			randomProf1.setMeeting(prof2Slot, prof2OldStudent, prof2OldStudentPref);
			randomProf2.setMeeting(prof1Slot, prof1OldStudent, prof1OldStudentPref);
			// calculate potential penalty
			// first calculate if professors are meeting with the same student > 1 time
			int penalty = 0;
			if (prof1OldStudent != null) {
				int count = 0;
				for (String str : randomProf2.getAvailability()) {
					if (str.equals(prof1OldStudent.getName())) {
						count++;
					}
				}
				if (count > 1) {
					penalty += MEETING_WITH_SAME_PERSON_PENALTY;
				}
			}
			if (prof2OldStudent != null) {
				int count = 0;
				for (String str : randomProf1.getAvailability()) {
					if (str.equals(prof2OldStudent.getName())) {
						count++;
					}
				}
				if (count > 1) {
					penalty += MEETING_WITH_SAME_PERSON_PENALTY;
				}
			}
			// calculate if the students were not available at that time
			if (prof2OldStudent != null
					&& !prof2OldStudent.getOriginalAvailability().get(prof1Slot).equals("AVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}
			if (prof1OldStudent != null
					&& !prof1OldStudent.getOriginalAvailability().get(prof2Slot).equals("AVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}
			// now calculate if the professors were not available at that time
			if (randomProf1 != null && randomProf1.getOriginalAvailability().get(prof2Slot).equals("UNAVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}
			if (randomProf2 != null && randomProf2.getOriginalAvailability().get(prof1Slot).equals("UNAVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}

			calculateCurrentSatisfaction(penalty);
		}

	}

	public static void performRNA() {
		for (Student s : students) {
			s.saveState();
		}
		for (Professor p : professors) {
			p.saveState();
		}

		int noImprovements = 0;
		while (noImprovements < RNA_MAX) {
			noImprovements++;
			int prevSatisfaction = currSatisfaction;

			// randomly try a neighboring state
			randomMove();

			/***********************/
			/* SIMULATED ANNEALING */
			/***********************/

			if (currSatisfaction < prevSatisfaction) {
				double percentDecrease = 0;
				if (prevSatisfaction != 0) {
					percentDecrease = 1 - ((prevSatisfaction - currSatisfaction) / prevSatisfaction);
				}
				double chanceAccepted = (1 - percentDecrease) / 2;
				Random rand = new Random(System.currentTimeMillis());
				int randomNum = rand.nextInt(99) + 0;
				if (randomNum / 10 <= chanceAccepted) {
					// accepted
					noImprovements = 0;
				} else {

					// unaccepted; reset to previous state
					for (Student s : students) {
						s.restoreState();
					}
					for (Professor p : professors) {
						p.restoreState();
					}
					currSatisfaction = prevSatisfaction;
				}

			} else {
//				System.out.println("Switch made");
				// reset counter
				noImprovements = 0;
			}
		}

	}

//		while (NoImprovements < RNAmax)
//		do begin (* RNA phase *)
//		NoImprovements := NoImprovements + 1;
//		DoubleMove := RandomDoubleMove;
//		if Delta(DoubleMove) <= 0
//		(* Delta is the variation of the objective function *)
//		then begin
//		UpdateCurrentTimeTable(DoubleMove);
//		if Delta(DoubleMove) < 0
//		then begin
//		NoImprovements := 0
//		IdleCycles := 0
//		end
//		end
//		end;

	public static int calculateCurrentSatisfaction(int penalty) {
		int total = 0;
		for (Student s : students) {
			int val = 5;
			for (Boolean bool : s.getPreferenceReceived()) {
				if (bool == true) {
					total += val;
				}
				val--;
			}
		}
		currSatisfaction = total - penalty;
		return currSatisfaction;
	}

	public static int getRandomIndex(Student s, Professor p) {
		ArrayList<Integer> bothAreFree = new ArrayList<>();
		for (int i = 0; i < numSlots; i++) {
			if (p.getAvailability().get(i).equals("AVAILABLE") && s.getSchedule().get(i).equals("AVAILABLE")) {
				bothAreFree.add(i);
			}
		}
		if (bothAreFree.size() == 0) {
			return -1;
		} else {
			Random rand = new Random(System.currentTimeMillis());
			return bothAreFree.get(rand.nextInt((bothAreFree.size() - 1 - 0) + 1) + 0);
		}
	}
	
	/*
	 * Removes output files so they do not clog computer memory when testing. 
	 */
	public static void cleanUpFiles() {
		for (String filename : outputFileNames) {
		    File currFile = new File(filename); 
		    if (!currFile.delete()) {
		    	System.out.println("Problem deleting " + filename);
		    }
		}
	}

	public static void createInitialSchedule() {
		ArrayList<Student> primaryStudents = new ArrayList<>(students);
		ArrayList<Student> secondary = new ArrayList<>();
		ArrayList<Student> tempPrimary = new ArrayList<>();
		ArrayList<Student> tempSecondary = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			for (Student student : primaryStudents) {
				if (i < student.getPreferences().size() && student.getPreferences().get(i).getFreeSlots().size() > 0
						&& student.getPreferences().get(i).getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
					Professor currProf = student.getPreferences().get(i);

					// randomly pick an available meeting slot and assign it
					int theIndex = getRandomIndex(student, currProf);
					if (theIndex == -1) {
						// student didn't get this preference; add to priority
						if (i < student.getPreferences().size()) {
							tempPrimary.add(student);
						}
					} else {
						currProf.setMeeting(theIndex, student, i);
						tempSecondary.add(student);
					}
				} else {
					// student didn't get this preference; add to priority, but only if they still
					// have more preferences to go
					if (i < student.getPreferences().size()) {
						tempPrimary.add(student);
					}
				}
			}
			for (Student student : secondary) {
				if (i < student.getPreferences().size() && student.getPreferences().get(i).getFreeSlots().size() > 0
						&& student.getPreferences().get(i).getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
					Professor currProf = student.getPreferences().get(i);

					// randomly pick an available meeting slot and assign it
					int theIndex = getRandomIndex(student, currProf);
					if (theIndex == -1) {
						// student didn't get this preference; add to priority
						if (i < student.getPreferences().size()) {
							tempPrimary.add(student);
						}
					} else {
						currProf.setMeeting(theIndex, student, i);
						tempSecondary.add(student);
					}
				} else {
					// student didn't get this preference; add to priority
					if (i < student.getPreferences().size()) {
						tempPrimary.add(student);
					}
				}
			}
			if (i == 4) {
				break;
			}
			primaryStudents.clear();
			secondary.clear();
			// copy temps over to real and clear temps for next iteration
			for (Student s : tempPrimary) {
				primaryStudents.add(s);
			}
			for (Student s : tempSecondary) {
				secondary.add(s);
			}
			Collections.shuffle(primaryStudents);
			Collections.shuffle(secondary);
			tempPrimary.clear();
			tempSecondary.clear();

//			printSchedule();
		}

		/*******************************************************************/
		/* ADD ADDITIONAL MEETINGS FOR STUDENTS BELOW MIN_STUDENT_MEETINGS */
		/*******************************************************************/
		ArrayList<Student> needMoreMeetings = new ArrayList<>();
		for (Student s : students) {
			if (s.getMeetingsAssigned() < MIN_STUDENT_MEETINGS) {
				needMoreMeetings.add(s);
			}
		}
		// only consider professors with free slots and # meetings assigned under
		// MAX_PROFESSOR_MEETINGS
		ArrayList<Professor> hasMoreSlots = new ArrayList<>();
		for (Professor p : professors) {
			if (p.getFreeSlots().size() > 0 && p.getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
				hasMoreSlots.add(p);
			}
		}

		int k = 0;
		while (needMoreMeetings.size() > 0 && hasMoreSlots.size() > 0 && k < 1000) {
			// randomly select a student
			Random rand = new Random(System.currentTimeMillis());
			int randomStudent = rand.nextInt((needMoreMeetings.size() - 1) + 1) + 0;
			Student s = needMoreMeetings.get(randomStudent);

			// randomly select a professor and a slot
			int randomProf = rand.nextInt((hasMoreSlots.size() - 1) + 1) + 0;
			Professor p = hasMoreSlots.get(randomProf);
			int randomSlot = rand.nextInt((p.getFreeSlots().size() - 1) + 1) + 0;

			// schedule if the student is free and is not already meeting with the professor
			if (s.getSchedule().get(randomSlot) == null && !s.alreadyMeetingWith(p)) {
				p.setMeeting(p.getFreeSlots().get(randomSlot), s, s.getNumPreference(p));
				// remove student if they have reached MIN_STUDENT_MEETINGS
				if (s.getMeetingsAssigned() >= MIN_STUDENT_MEETINGS) {
					needMoreMeetings.remove(s);
				}
				// remove professor if they no longer have any free slots or have reached
				// MAX_PROFESSOR_MEETINGS
				if (p.getFreeSlots().size() == 0 || p.getNumMeetings() >= MAX_PROFESSOR_MEETINGS) {
					hasMoreSlots.remove(randomProf);
				}
			} else {
				// see if current professor should be removed from consideration
				boolean remove = true;
				for (Student stu : students) {
					int i = 0;
					for (String str : stu.getSchedule()) {
						// see if it's possible for a student to have a meeting with the current prof
						if (str.equals("AVAILABLE") && p.getAvailability().get(i).equals("AVAILABLE")
								&& !stu.alreadyMeetingWith(p)) {
							remove = false;
							break;
						}
						i++;
					}
				}
				if (remove) {
					hasMoreSlots.remove(randomProf);
				}
			}

			k++;
		}
	}

	// TODO: fix
	/*
	 * Ensures that the schedule is valid (professor and student meetings match up;
	 * nobody meets with the same person twice; nobody is scheduled when they are
	 * not available).
	 */
	public boolean verifySchedule() {
		for (Student s : students) {
			HashSet<Professor> professorsMetWith = new HashSet<>();
			ArrayList<String> originalAvailability = s.getOriginalAvailability();
			for (int i = 0; i < originalAvailability.size(); i++) {
				if (!originalAvailability.get(i).equals("AVAILABLE")) { // tour
					if (!s.getSchedule().get(i).equals(originalAvailability.get(i))) {
						System.out.println("INVALID: Student " + s.getName() + " not scheduled to take their tour.");
						return false;
					}
				} else if (!s.getSchedule().get(i).equals("AVAILABLE")) { // student should be scheduled with a
																			// professor
					if (nameToProf.get(s.getSchedule().get(i)) == null) {
						System.out.println("INVALID: Invalid professor name or given a tour when not supposed to.");
						return false;
					}
					Professor currProf = nameToProf.get(s.getSchedule().get(i));
					if (!currProf.getAvailability().get(i).equals(s.getName())) {
						System.out.println("INVALID: Student " + s.getName() + " meeting with professor "
								+ currProf.getName() + " at slot " + i + " but not vice versa.");
						return false;
					}
					if (professorsMetWith.contains(currProf)) {
						System.out.println("INVALID: Student " + s.getName() + " already meeting with professor "
								+ currProf.getName() + ".");
						return false;
					}
					professorsMetWith.add(currProf);
				}
			}
		}

		for (Professor p : professors) {
			HashSet<Student> studentsMetWith = new HashSet<>();
			ArrayList<String> originalAvailability = p.getOriginalAvailability();
			for (int i = 0; i < originalAvailability.size(); i++) {
				if (originalAvailability.get(i).equals("UNAVAILABLE")
						&& !p.getAvailability().get(i).equals("UNAVAILABLE")) {
					System.out.println("Invalid: Professor " + p.getName() + " scheduled at slot " + i
							+ " but is unavailable then.");
					return false;
				} else if (!p.getAvailability().get(i).equals("AVAILABLE")) { // has student meeting
					if (nameToStudent.get(p.getAvailability().get(i)) == null) {
						System.out.println("Invalid: Not a valid student name for professor " + p.getName()
								+ " at slot " + i + ".");
						return false;
					}
					Student currStudent = nameToStudent.get(p.getAvailability().get(i));
					if (studentsMetWith.contains(currStudent)) {
						System.out.println("Invalid: Professor " + p.getName() + " already meeting with student "
								+ currStudent.getName() + ".");
						return false;
					}
					if (!currStudent.getSchedule().get(i).equals(p.getName())) {
						System.out.println("Invalid: Professor " + p.getName() + " meeting with student "
								+ currStudent.getName() + " at slot " + i + " but not vice versa.");
						return false;
					}
					studentsMetWith.add(currStudent);
				}

			}
		}
		return true;
	}

	public HashMap<String, Student> getNameToStudent() {
		return nameToStudent;
	}

	public HashMap<String, Professor> getNameToProf() {
		return nameToProf;
	}

	public ArrayList<Student> getStudents() {
		return students;
	}

	public ArrayList<Professor> getProfessors() {
		return professors;
	}
}