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

public class Run {
	
	@Test
	public void HURSTTEST() {
		// INSTRUCTIONS: In the two String variables below inside the quotes, put your entire file name
		// BUT REPLACE EVERY \ WITH TWO \\ 
		// For example, C:\\Users\\katie\\VisitDayScheduler\\VisitDayScheduler\\src\\STUDENTDATA.tsv
		// You can also view lines 24-28 above where I have declared variables like studentFileName if you want more examples
		String HURSTSTUDENTFILENAME = "";
		String HURSTPROFFILENAME = "";
		Scheduler scheduler = new Scheduler();
		scheduler.main(new String[] { HURSTSTUDENTFILENAME, HURSTPROFFILENAME });
	}
}
