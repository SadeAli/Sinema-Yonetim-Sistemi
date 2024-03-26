package biletsatis;

import java.time.LocalDate;

public class biletsatis {

	public static void main(String[] args) {

		Auditorium aud1 = new Auditorium();
		Auditorium aud2 = new Auditorium();
		Auditorium aud3 = new Auditorium();
		Auditorium aud4 = new Auditorium();
		
		System.out.println(aud1);
		System.out.println(aud2);
		System.out.println(aud3);
		System.out.println(aud4);
		
		Movie mov1 = new Movie("Recep İvedik 3", 90, LocalDate.now(), LocalDate.now().plusDays(60));
		Movie mov2 = new Movie("Makinist", 130, LocalDate.now(), LocalDate.now().plusDays(60));
		Movie mov3 = new Movie("Oppenheimer", 200, LocalDate.now(), LocalDate.now().plusDays(60));

		System.out.println(mov1);
		System.out.println(mov2);
		System.out.println(mov3);
		
		aud1.addMovieToDate(LocalDate.now(), mov1.getID());
		aud2.addMovieToDate(LocalDate.now(), mov3.getID());
		aud3.addMovieToDate(LocalDate.now(), mov1.getID());
		aud4.addMovieToDate(LocalDate.now(), mov2.getID());
		aud4.addMovieToDate(LocalDate.now().plusDays(1), mov3.getID());
		System.out.println("\n");
			
		aud1.printSessions(LocalDate.now());
		System.out.println("\n");
		aud2.printSessions(LocalDate.now());
		System.out.println("\n");
		aud3.printSessions(LocalDate.now());
		System.out.println("\n");
		aud4.printSessions(LocalDate.now());
		System.out.println("\n");
		aud4.printSessions(LocalDate.now().plusDays(1));


	}

}
