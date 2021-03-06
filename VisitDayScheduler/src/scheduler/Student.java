package scheduler;
import java.util.ArrayList;

public class Student {
	// backup variables
	private int numPreferencesAssignedBACKUP;
	private int numMeetingsAssignedBACKUP;
	private ArrayList<Boolean> preferenceReceivedBACKUP;
	private ArrayList<String> scheduleBACKUP;

	private int numPreferencesAssigned;
	private int numMeetingsAssigned;
	private String name;
	private ArrayList<Professor> preferences;
	private ArrayList<Boolean> preferenceReceived;
	private ArrayList<String> schedule;
	
	private ArrayList<String> originalAvailability;
	

	public Student(String name, ArrayList<Professor> preferences, int numSlots) {
		this.name = name;
		this.preferences = preferences;
		numPreferencesAssigned = 0;
		numMeetingsAssigned = 0;
		preferenceReceived = new ArrayList<>();
		preferenceReceivedBACKUP = new ArrayList<>();
		scheduleBACKUP = new ArrayList<>();
		
		int size = preferences.size();
		for (int i = 0; i < size; i++) {
			preferenceReceived.add(false);
		}
		
		schedule = new ArrayList<>();
		for (int i = 0; i < numSlots; i++) {
			schedule.add("AVAILABLE");
		}
		
		originalAvailability = new ArrayList<String>();
		for (String str : schedule) {
			originalAvailability.add(str);
		}
	}
	
	public boolean alreadyMeetingWith(Professor p) {
		for (String str : schedule) {
			if (str.equals(p.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<String> getOriginalAvailability() {
		return originalAvailability;
	}
	
	public ArrayList<String> getSchedule() {
		return schedule;
	}
	
	public void addToSchedule(Professor prof, int index) {
		schedule.set(index, prof.getName());
	}
	
	public void removeFromSchedule(int index) {
		schedule.set(index, null);
	}

	public int getNumPreference(Professor p) {
		if (p == null) {
			return -1;
		}
		for (int i = 0; i < preferences.size(); i++) {
			if (preferences.get(i).getName().equals(p.getName())) {
				return i;
			}
		}
		return -1;
	}

	public void restoreState() {
		numPreferencesAssigned = numPreferencesAssignedBACKUP;
		numMeetingsAssigned = numMeetingsAssignedBACKUP;
		preferenceReceived.clear();
		schedule.clear();
		for (Boolean bool : preferenceReceivedBACKUP) {
			preferenceReceived.add(bool);
		}
		for (String str : scheduleBACKUP) {
			schedule.add(str);
		}
	}

	/*
	 * This method saves the current state of the student in case the local search
	 * algorithm needs to revert to this state after potentially exploring other
	 * states.
	 */
	public void saveState() {
		numPreferencesAssignedBACKUP = numPreferencesAssigned;
		numMeetingsAssignedBACKUP = numMeetingsAssigned;
		preferenceReceivedBACKUP.clear();
		scheduleBACKUP.clear();
		for (Boolean bool : preferenceReceived) {
			preferenceReceivedBACKUP.add(bool);
		}
		for (String str : schedule) {
			scheduleBACKUP.add(str);
		}
	}

	public int getMeetingsAssigned() {
		return numMeetingsAssigned;
	}

	public void setMeetingsAssigned(int num) {
		numMeetingsAssigned = num;
	}
	
	public void setSchedule(int index, String str) {
		schedule.set(index, str);
	}

	public void setPreferenceReceived(int index, boolean bool) {
		preferenceReceived.set(index, bool);
	}

	public ArrayList<Boolean> getPreferenceReceived() {
		return preferenceReceived;
	}

	public String getName() {
		return name;
	}
	
//	public boolean scheduleEmpty() {
//		for (Professor p : schedule) {
//			if (p != null) {
//				return false;
//			}
//		}
//		return true;
//	}

	public ArrayList<Professor> getPreferences() {
		return preferences;
	}

	public int getNumPreferencesAssigned() {
		return numPreferencesAssigned;
	}

	public void setNumPreferencesAssigned(int num) {
		numPreferencesAssigned = num;
	}
	
	@Override
    public boolean equals(Object o) {  
        if (o == this) { 
            return true; 
        } 
  
        if (!(o instanceof Student)) { 
            return false; 
        } 
          
        Student s = (Student) o; 
        
        return s.getName().equals(this.getName());
    } 
}
