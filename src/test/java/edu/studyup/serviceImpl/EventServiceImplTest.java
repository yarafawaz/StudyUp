package edu.studyup.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.studyup.entity.Event;
import edu.studyup.entity.Location;
import edu.studyup.entity.Student;
import edu.studyup.util.DataStorage;
import edu.studyup.util.StudyUpException;

class EventServiceImplTest {

	EventServiceImpl eventServiceImpl;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		eventServiceImpl = new EventServiceImpl();
		// Create Student
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);

		// Create Event1
		Event event = new Event();
		event.setEventID(1);
		event.setDate(new Date());
		event.setName("Event 1");
		Location location = new Location(-122, 37);
		event.setLocation(location);
		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student);
		event.setStudents(eventStudents);

		DataStorage.eventData.put(event.getEventID(), event);
	}

	@AfterEach
	void tearDown() throws Exception {
		DataStorage.eventData.clear();
	}

	@Test
	void testUpdateEventName_GoodCase() throws StudyUpException {
		int eventID = 1;
		eventServiceImpl.updateEventName(eventID, "Renamed Event 1");
		assertEquals("Renamed Event 1", DataStorage.eventData.get(eventID).getName());
	}

	@Test
	void testUpdateEvent_WrongEventID_badCase() {
		int eventID = 3;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "Renamed Event 3");
		});
	}

	// Bug 1 - should be able to change event name to exactly 20 chars
	@Test
	void testUpdateEventName_Exactly20Chars() throws StudyUpException {
		int eventID = 1;
		String newName = "Rename Event 1 To 20";
		assert (newName.length() == 20);
		Assertions.assertDoesNotThrow(() -> {
			eventServiceImpl.updateEventName(eventID, newName);
		});
	} 

	// should throw exception for name longer than 20 chars
	@Test
	void testUpdateEventName_MoreThan20Chars() throws StudyUpException {
		int eventID = 1;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "Renamed Event 1 Longer than 20 Chars");
		});
	}

	@Test
	void testGetActiveEvents_GoodCase() {
		Event event = new Event();
		event.setEventID(2);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2000);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date futureDate = cal.getTime();
		event.setDate(futureDate);

		DataStorage.eventData.put(event.getEventID(), event);
		assert(eventServiceImpl.getActiveEvents().contains(event));
	}

	// Bug 2 - getActiveEvents is not supposed to return an event from the past
	@Test
	void testGetActiveEvents_BadCase() {
		Event event = new Event();
		event.setEventID(2);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2000);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date futureDate = cal.getTime();
		event.setDate(futureDate);

		DataStorage.eventData.put(event.getEventID(), event);
		assert(!eventServiceImpl.getActiveEvents().contains(event));
	}

	@Test
	void testGetPastEvents_GoodCase() {
		Event event = new Event();
		event.setEventID(2);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2000);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date futureDate = cal.getTime();
		event.setDate(futureDate);

		DataStorage.eventData.put(event.getEventID(), event);
		assert(eventServiceImpl.getPastEvents().contains(event));
	}

	// TODO - one more for getPastEvents
	// Bug 3
	@Test  
	void testGetPastEvents_FutureDate_BadCase() {
		Event event = new Event(); 
		event.setEventID(2);
		Calendar cal = Calendar.getInstance(); 
		cal.set(Calendar.YEAR, 2030); 
		cal.set(Calendar.MONTH, Calendar.JUNE);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date futureDate = cal.getTime();
		event.setDate(futureDate);
		
		DataStorage.eventData.put(event.getEventID(), event);
		assert(eventServiceImpl.getPastEvents().contains(event));
	}
	
	@Test 
	void testAddStudentToEvent_GoodCase() {
		Student student2 = new Student();
		int eventID2 = 2; 
		Event event2 = new Event();
		event2.setEventID(2);
		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student2);
		event2.setStudents(eventStudents);
		DataStorage.eventData.put(event2.getEventID(), event2);
		
		Assertions.assertDoesNotThrow(() -> {
			eventServiceImpl.addStudentToEvent(student2, eventID2);
		}); 
	}
	
	@Test  
	// Bug - addStudentToEvent doesn't check for the limit of students and will continue to add 
	void testAddStudentToEvent_BadCase() throws StudyUpException {
		Student student2 = new Student();
		int eventID2 = 2; 
		Event event2 = new Event();
		event2.setEventID(2);
		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student2);
		event2.setStudents(eventStudents);
		DataStorage.eventData.put(event2.getEventID(), event2);
		eventServiceImpl.addStudentToEvent(student2, eventID2);
		
		Student student3 = new Student();
		int eventID3 = 3; 
		Event event3 = new Event();
		event2.setEventID(2);
		List<Student> eventStudent = new ArrayList<>();
		eventStudent.add(student3);
		event2.setStudents(eventStudent);
		DataStorage.eventData.put(event3.getEventID(), event3);
		
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.addStudentToEvent(student3, eventID3); 
		});	
	}
	
	@Test 
	void testAddToEvent_GoodCase_StudentIsNull() throws StudyUpException{
		Student student2 = new Student();
		int eventID2 = 2; 
		Event event2 = new Event();
		event2.setEventID(2);
		DataStorage.eventData.put(event2.getEventID(), event2);

		Assertions.assertDoesNotThrow(() -> {
			eventServiceImpl.addStudentToEvent(student2, eventID2);
		});
	}
	
	@Test 
	void testDeleteEvent_GoodCase(){
		int eventID = 2; 
		assertNull(eventServiceImpl.deleteEvent(eventID));
	}

}
