package scheduler;

import java.util.ArrayList;

public class Professor {
	// backup variables
	private ArrayList<String> availabilityBACKUP;
	private int numMeetingsBACKUP;
	private ArrayList<Integer> freeSlotsBACKUP;

	private String name;
	private ArrayList<String> availability;
	private int numMeetings; // number of meetings they are currently assigned to have
	private ArrayList<Integer> freeSlots; // contains indices of free slots

	private ArrayList<String> originalAvailability;

	public Professor(String name, ArrayList<String> availability, ArrayList<Integer> freeSlots) {
		this.name = name;
		this.availability = availability;
		numMeetings = 0;
		this.freeSlots = freeSlots;

		availabilityBACKUP = new ArrayList<String>();
		freeSlotsBACKUP = new ArrayList<Integer>();
		originalAvailability = new ArrayList<String>();
		for (String str : availability) {
			originalAvailability.add(str);
		}

//		System.out.println(name);
//		int j =0;
//		for (String s : availability) {
//			if (s.equals("AVAILABLE")) {
//				j++;
//			}
//			System.out.print(s + " ");
//		}
//		System.out.println();
//		if (j != freeSlots.size()) {
//			System.out.println("ERROR");
//		}
//		this.freeSlots = new ArrayList<>();
//		
//		System.out.println(name + " " + availability.size());
//		for (int i = 0; i < availability.size(); i++) {
//			System.out.println(availability.get(i));
//			if (availability.get(i).equals("AVAILABLE")) {
//				System.out.println("ADDED HERE");
//				this.freeSlots.add(i);
//			}
//		}
//		System.out.println(getFreeSlots());
	}

	public ArrayList<String> getOriginalAvailability() {
		return originalAvailability;
	}

	/*
	 * We pass in a number corresponding to what # preference the student got for
	 * studentPrefIndex, or -1 if the student was assigned to a professor that
	 * wasn't on their preference list.
	 */
	// INDEX IS ACTUAL INDEX OUT OF ALL INDICES
	public void setMeeting(int index, Student student, int studentPrefIndex) {
		if (student == null) {
			if (originalAvailability.get(index).equals("AVAILABLE")) {
				availability.set(index, "AVAILABLE");
			} else {
				availability.set(index, "UNAVAILABLE");
			}
		} else {
			numMeetings++;
			availability.set(index, student.getName());
			// TODO: check this
			removeFromFreeSlots(index); // since we scheduled a meeting at this slot, remove from freeSlots
			student.addToSchedule(this, index);
			if (studentPrefIndex != -1) {
				student.setPreferenceReceived(studentPrefIndex, true);
				student.setNumPreferencesAssigned(student.getNumPreferencesAssigned() + 1);
			}
			student.setMeetingsAssigned(student.getMeetingsAssigned() + 1); // increment student's number of meetings
		}
	}
	
	public void setSchedule(int index, String str) {
		availability.set(index, str);
	}

	// INDEX IS ACTUAL INDEX OUT OF ALL INDICES
	public void removeMeeting(int index, Student student) {
		// indicates there was no meeting at this slot to begin with
		if (student == null) {
			return;
		}
		numMeetings--;
		int studentPrefIndex = student.getNumPreference(this);
		if (studentPrefIndex != -1) {
			// student is now not assigned this preference
			student.setPreferenceReceived(studentPrefIndex, false);
			student.setNumPreferencesAssigned(student.getNumPreferencesAssigned() - 1);
		}
		student.setMeetingsAssigned(student.getMeetingsAssigned() - 1);
		student.removeFromSchedule(index);
		// TODO: also double check this
		if (originalAvailability.get(index).equals("AVAILABLE")) {
			availability.set(index, "AVAILABLE");
			if (!freeSlots.contains(index)) {
				freeSlots.add(index);
			}
		} else {
			availability.set(index, "UNAVAILABLE");
		}
	}

	public void restoreState() {
		numMeetings = numMeetingsBACKUP;
		availability.clear();
		for (String str : availabilityBACKUP) {
			availability.add(str);
		}
		freeSlots.clear();
		for (Integer i : freeSlotsBACKUP) {
			freeSlots.add(i);
		}
	}

	/*
	 * This method saves the current state of the professor in case the local search
	 * algorithm needs to revert to this state after potentially exploring other
	 * states.
	 */
	public void saveState() {
		numMeetingsBACKUP = numMeetings;
		availabilityBACKUP.clear();
		for (String str : availability) {
			availabilityBACKUP.add(str);
		}
		freeSlotsBACKUP.clear();
		for (Integer i : freeSlots) {
			freeSlotsBACKUP.add(i);
		}
	}

	public void removeFromFreeSlots(int index) {
		int i = 0;
		for (int curr : freeSlots) {
			if (curr == index) {
				freeSlots.remove(i);
				break;
			}
			i++;
		}
	}

	public ArrayList<Integer> getFreeSlots() {
		return freeSlots;
	}

	public String getName() {
		return name;
	}

	public int getNumMeetings() {
		return numMeetings;
	}

	public void setNumMeetings(int num) {
		this.numMeetings = num;
	}

	public ArrayList<String> getAvailability() {
		return availability;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Professor)) {
			return false;
		}

		Professor p = (Professor) o;

		return p.getName().equals(this.getName());
	}
}
