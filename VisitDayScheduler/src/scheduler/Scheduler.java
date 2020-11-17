package scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * Schedules the student-professor visit day meetings.
 * 
 * @author Katherine Sullivan
 */
public class Scheduler {
	private static final int MAX_PROFESSOR_MEETINGS = 10;
	private static final int MIN_STUDENT_MEETINGS = 5;
	private static final int RNA_MAX = 10;
	private static final int CYCLES = 10;
	private static final int MEETING_WITH_SAME_PERSON_PENALTY = 5;
	private static final int NOT_AVAILABLE_THEN_PENALTY = 10;
	private static HashMap<Integer, String> indexToSlot;
//	private static HashMap<String, Integer> slotToIndex;
//	private static HashSet<String> profNames;
//	private static HashMap<String, ArrayList<String>> profToAssignments;
//	private static HashMap<String, ArrayList<Integer>> profToAvailability;
//	private static HashMap<String, ArrayList<Professor>> studentToPreferences;
//	private static HashMap<String, Integer> profNumAssignments;
//	private static HashMap<String, ArrayList<Boolean>> studentAssigned;
	private static ArrayList<Professor> professors;
	private static ArrayList<Student> students;
	private static int currSatisfaction;
	private static int maxSatisfaction;
	private static int numSlots;
	private static HashMap<String, Student> nameToStudent;
	private static HashMap<String, Professor> nameToProf;

	public static void main(String[] args) {
		// TODO: change command line arguments to be user inputs
		File studentFile, profFile, schedule;
		if (args.length != 2 && args.length != 3) {
			System.out.println("Provide 2 arguments (filename of student preferences and filename of "
					+ "professor availability) or 3 arguments (filename of student preferences, filename "
					+ "of professor availability, and filename of existing schedule");
			return;
		}

		try {
			studentFile = new File(args[0]);
		} catch (Exception e) {
			System.out.println("Error opening the students file. Please place the students file in the same directory "
					+ "as the Java program and check your spelling.");
			return;
		}
		try {
			profFile = new File(args[1]);
		} catch (Exception e) {
			System.out.println("Error opening the professors file. Please place the professors file in the same "
					+ "directory as the Java program and check your spelling.");
			return;
		}

		InputProcessor processor = new InputProcessor();

		if (args.length == 3) {
			try {
				schedule = new File(args[2]);
				processor.processThreeInputs(studentFile, profFile, schedule);
			} catch (Exception e) {
				System.out.println("Error opening the schedule file. Please place the schedule file in the same "
						+ "directory as the Java program and check your spelling.");
				return;
			}
		} else {
			processor.processTwoInputs(studentFile, profFile);
		}

		// populate data structures
		professors = processor.getProfessors();
		students = processor.getStudents();

		indexToSlot = processor.getIndexToSlot();
		numSlots = processor.getNumSlots();
		nameToStudent = processor.getNameToStudent();
		nameToProf = processor.getNameToProf();
//		profNames = processor.getProfNames();
//		profToAssignments = processor.getProfToAssignments();
//		profToAvailability = processor.getProfToAvailability();
//		studentToPreferences = processor.getStudentToPreferences();
//		profNumAssignments = new HashMap<>();
//		studentAssigned = new HashMap<>();

		createInitialSchedule();
		calculateMaxSatisfaction();
		calculateCurrentSatisfaction(0);

		if (currSatisfaction == maxSatisfaction) {
			System.out.println("DONE.");
		} else {
			int numCycles = 0;
			while (numCycles < CYCLES) {
				performRNA();
				numCycles++;
			}
		}

//		printSchedule();
		outputResults();
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
			for (Professor p : s.getSchedule()) {
				if (p != null) {
					System.out.print("\t" + p.getName());
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
		PrintWriter writer;
		try {
			writer = new PrintWriter("StudentSchedule.tsv", "UTF-8");
			writer.print("\t");
			for (int i = 0; i < numSlots; i++) {
				writer.print(indexToSlot.get(i));
				if (i != numSlots - 1) {
					writer.print("\t");
				}
			}
			writer.println();
			for (Student s : students) {
				writer.print(s.getName());
				for (Professor p : s.getSchedule()) {
					if (p != null) {
						writer.print("\t" + p.getName());
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
			writer = new PrintWriter("ProfessorSchedule.tsv", "UTF-8");
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
					// TODO: check with Tom and ask if it would be helpful to output AVAILABLE so he
					// knows their free slots
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
			writer = new PrintWriter("UnreceivedStudentPreferences.txt", "UTF-8");
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
						str += s.getPreferences().get(i).getName() + ", ";
					}
					i++;
				}
				if (str.length() > 1) {
					writer.write(str.substring(0, str.length()-2) + "\n");
				}
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("UnreceivedStudentPreferences.txt");
			e.printStackTrace();
		}

		System.out.println("Student schedule written to StudentSchedule.tsv.");
		System.out.println("Professor schedule written to ProfessorSchedule.tsv.");
		if (atLeastOneStudentPrinted) {
			System.out.println(
					"Students and the preferences they did not receive written to UnreceivedStudentPreferences.txt");
		}
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
		// TODO: IMPLEMENT BELOW
		if (studentsOrProfessors == 0) {
			// randomly select two distinct students
			Student randomStudent1 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			Student randomStudent2 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			while (randomStudent1.equals(randomStudent2)) {
				randomStudent2 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			}
			// randomly select timeslots
			int student1RandomSlot = rand.nextInt(randomStudent1.getSchedule().size() - 1 - 0) + 0;
//			if (!randomStudent1.scheduleEmpty()) {
//				while (randomStudent1.getSchedule().get(student1RandomSlot) != null) {
//					student1RandomSlot = rand.nextInt(randomStudent1.getSchedule().size() - 1 - 0) + 0;
//				}
//			}
			int student2RandomSlot = rand.nextInt(randomStudent2.getSchedule().size() - 1 - 0) + 0;
//			if (!randomStudent2.scheduleEmpty()) {
//				while (randomStudent2.getSchedule().get(student1RandomSlot) != null) {
//					student2RandomSlot = rand.nextInt(randomStudent2.getSchedule().size() - 1 - 0) + 0;
//				}
//			}
		} else {
			// randomly select two distinct professors
			Professor randomProf1 = professors.get(rand.nextInt(professors.size() - 1 - 0) + 0);
			Professor randomProf2 = professors.get(rand.nextInt(professors.size() - 1 - 0) + 0);
			while (randomProf1.equals(randomProf2)) {
				randomProf2 = professors.get(rand.nextInt(professors.size() - 1 - 0) + 0);
			}
			// randomly select timeslots
			int prof1Slot = rand.nextInt(randomProf1.getAvailability().size() - 1 - 0) + 0;
			int prof2Slot = rand.nextInt(randomProf2.getAvailability().size() - 1 - 0) + 0; // make the switch
			Student prof1OldStudent = nameToStudent.get(randomProf1.getAvailability().get(prof1Slot));
			Student prof2OldStudent = nameToStudent.get(randomProf2.getAvailability().get(prof2Slot));
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
			// now calculate if the professors were not available at that time
			if (randomProf1.getOriginalAvailability().get(prof2Slot).equals("UNAVAILABLE")) {
				penalty += NOT_AVAILABLE_THEN_PENALTY;
			}
			if (randomProf2.getOriginalAvailability().get(prof1Slot).equals("UNAVAILABLE")) {
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
			if (currSatisfaction < prevSatisfaction) {
				// unaccepted; reset to previous state
				for (Student s : students) {
					s.restoreState();
				}
				for (Professor p : professors) {
					p.restoreState();
				}
				currSatisfaction = prevSatisfaction;
			} else {
				System.out.println("Switch made");
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

	public static void createInitialSchedule() {
		ArrayList<Student> primaryStudents = new ArrayList<>(students);
		ArrayList<Student> secondary = new ArrayList<>();
		ArrayList<Student> tempPrimary = new ArrayList<>();
		ArrayList<Student> tempSecondary = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			for (Student student : primaryStudents) {
				if (i < student.getPreferences().size() && student.getPreferences().get(i).getFreeSlots().size() > 0
						&& student.getPreferences().get(i).getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
					int len = student.getPreferences().get(i).getFreeSlots().size();
					Professor currProf = student.getPreferences().get(i);
					Random rand = new Random(System.currentTimeMillis());

					// randomly pick an available meeting slot and assign it
					int randomNum = rand.nextInt((len - 1 - 0) + 1) + 0;
					currProf.setMeeting(currProf.getFreeSlots().get(randomNum), student, i);
					tempSecondary.add(student);
				} else {
					// student didn't get this preference; add to priority
					if (i < student.getPreferences().size()) {
						tempPrimary.add(student);
					}
				}
			}
			for (Student student : secondary) {
				if (i < student.getPreferences().size() && student.getPreferences().get(i).getFreeSlots().size() > 0
						&& student.getPreferences().get(i).getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
					int len = student.getPreferences().get(i).getFreeSlots().size();
					Professor currProf = student.getPreferences().get(i);
					Random rand = new Random(System.currentTimeMillis());

					// randomly pick an available meeting slot and assign it
					int randomNum = rand.nextInt((len - 1 - 0) + 1) + 0;
					currProf.setMeeting(currProf.getFreeSlots().get(randomNum), student, i);
					tempSecondary.add(student);
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
			Collections.shuffle(tempSecondary);
			tempPrimary.clear();
			tempSecondary.clear();
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

		// TODO: CHECK THEY AREN'T MEETING WITH THEM ALREADY.
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
					for (Professor prof : stu.getSchedule()) {
						if (prof == null && p.getAvailability().get(i).equals("AVAILABLE")
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