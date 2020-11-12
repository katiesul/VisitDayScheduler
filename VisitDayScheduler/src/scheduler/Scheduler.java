package scheduler;
import java.io.File;
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
		calculateCurrentSatisfaction();
		
		if (currSatisfaction == maxSatisfaction) {
			System.out.println("DONE.");
		} else {
			int numCycles = 0;
			while (numCycles < CYCLES) {
				performRNA();
				numCycles++;
			}
		}
		
		printSchedule();
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
	
	public static void performRNA() {
//		for (Student s : students) {
//			s.saveState();
//		}
//		for (Professor p : professors) {
//			p.saveState();
//		}
		
		int noImprovements = 0;
		while (noImprovements < RNA_MAX) {
			noImprovements++;
			
			// randomly try a neighboring state
			Random rand = new Random(System.currentTimeMillis());
			Student randomStudent1 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			Student randomStudent2 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			while (randomStudent1.equals(randomStudent2)) {
				randomStudent2 = students.get(rand.nextInt(students.size() - 1 - 0) + 0);
			}
			int student1RandomSlot = rand.nextInt(randomStudent1.getSchedule().size() - 1 - 0) + 0;
			if (!randomStudent1.scheduleEmpty()) {
				while (randomStudent1.getSchedule().get(student1RandomSlot) != null) {
					student1RandomSlot = rand.nextInt(randomStudent1.getSchedule().size() - 1 - 0) + 0;
				}
			}
			int student2RandomSlot = rand.nextInt(randomStudent2.getSchedule().size() - 1 - 0) + 0;
			if (!randomStudent2.scheduleEmpty()) {
				while (randomStudent2.getSchedule().get(student1RandomSlot) != null) {
					student2RandomSlot = rand.nextInt(randomStudent2.getSchedule().size() - 1 - 0) + 0;
				}
			}
			int student1CurrentScore = 0;
			int temp;
			if ((temp = randomStudent1.getNumPreference(randomStudent1.getSchedule().get(student1RandomSlot))) != -1) {
				student1CurrentScore = temp;
			}
			int student2CurrentScore = 0;
			if ((temp = randomStudent2.getNumPreference(randomStudent2.getSchedule().get(student2RandomSlot))) != -1) {
				student2CurrentScore = temp;
			}
			int student1Potential = 0;
			if ((temp = randomStudent1.getNumPreference(randomStudent2.getSchedule().get(student2RandomSlot))) != -1) {
				student1Potential = temp;
			}
			int student2Potential = 0;
			if ((temp = randomStudent2.getNumPreference(randomStudent1.getSchedule().get(student1RandomSlot))) != -1) {
				student2Potential = temp;
			}
			// make the switch
			if ((student2Potential + student1Potential) - (student1CurrentScore + student2CurrentScore) >= 0) {
				
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

	}
	
	public static void calculateCurrentSatisfaction() {
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
		currSatisfaction = total;
	}

	public static void createInitialSchedule() {
		ArrayList<Student> primaryStudents = new ArrayList<>(students);
		ArrayList<Student> secondary = new ArrayList<>();
		ArrayList<Student> tempPrimary = new ArrayList<>();
		ArrayList<Student> tempSecondary = new ArrayList<>();
		
		for (int i = 0; i < 5; i++) {
			for (Student student : primaryStudents) {
				if (i < student.getPreferences().size() && 
						student.getPreferences().get(i).getFreeSlots().size() > 0 &&
						student.getPreferences().get(i).getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
					int len = student.getPreferences().get(i).getFreeSlots().size();
					Professor currProf = student.getPreferences().get(i);
					Random rand = new Random(System.currentTimeMillis());
					
					// randomly pick an available meeting slot and assign it
					int randomNum = rand.nextInt((len - 1 - 0) + 1) + 0;
					currProf.setMeeting(randomNum, student, i);
					tempSecondary.add(student);
				} else {
					// student didn't get this preference; add to priority
					if (i < student.getPreferences().size()) {
						tempPrimary.add(student);
					}
				}
			}
			for (Student student : secondary) {
				if (i < student.getPreferences().size() && 
						student.getPreferences().get(i).getFreeSlots().size() > 0 &&
						student.getPreferences().get(i).getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
					int len = student.getPreferences().get(i).getFreeSlots().size();
					Professor currProf = student.getPreferences().get(i);
					Random rand = new Random(System.currentTimeMillis());
					
					// randomly pick an available meeting slot and assign it
					int randomNum = rand.nextInt((len - 1 - 0) + 1) + 0;
					currProf.setMeeting(randomNum, student, i);
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
		// only consider professors with free slots and # meetings assigned under MAX_PROFESSOR_MEETINGS
		ArrayList<Professor> hasMoreSlots = new ArrayList<>();
		for (Professor p : professors) {
			if (p.getFreeSlots().size() > 0 && p.getNumMeetings() < MAX_PROFESSOR_MEETINGS) {
				hasMoreSlots.add(p);
			}
		}
		Collections.shuffle(needMoreMeetings);
		while (needMoreMeetings.size() > 0 && hasMoreSlots.size() > 0) {
			for (Student s : needMoreMeetings) {
				// randomly select a professor and a slot from the available list
				Random rand = new Random(System.currentTimeMillis());
				int randomProf = rand.nextInt((hasMoreSlots.size() - 1) + 1) + 0;
				int randomSlot = rand.nextInt((hasMoreSlots.get(randomProf).getFreeSlots().size() - 1) + 1) + 0;
				hasMoreSlots.get(randomProf).setMeeting(randomSlot, s, -1);
				
				// remove student if they have reached MIN_STUDENT_MEETINGS
				if (s.getMeetingsAssigned() >= MIN_STUDENT_MEETINGS) {
					needMoreMeetings.remove(s);
				}
				// remove professor if they no longer have any free slots or have reached MAX_PROFESSOR_MEETINGS
				if (hasMoreSlots.get(randomProf).getFreeSlots().size() == 0 ||
						hasMoreSlots.get(randomProf).getNumMeetings() >= MAX_PROFESSOR_MEETINGS) {
					hasMoreSlots.remove(randomProf);
				}
			}
			Collections.shuffle(needMoreMeetings);
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
