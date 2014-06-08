package New;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;


public class Graph {
	String actorA = "Kevin Bacon";
	String actorB = "William Shatner";
	static String fileIn;
	
	private HashMap<String, Vertex> actorMap; 					// Name => Vertex
	private HashMap<String, HashSet<String>> movieCastLists;	// Movie => Cast List
		
	/** Constructor **/
	public Graph() {
		fileIn = "actorfile.csv";
		actorMap = new HashMap<String, Vertex>();
		movieCastLists = new HashMap<String, HashSet<String>>();
	}
	
	/** Members **/
	class Vertex {
		String actorName;
		int baconNumber;
		Edge reversePath;
		LinkedList<Edge> neighbors;
		HashSet<String> movieTitles;
		
		Vertex (String name) {
			actorName = name;
			baconNumber = Integer.MAX_VALUE;
			neighbors = new LinkedList<Edge>();
			movieTitles = new HashSet<String>();
		}
		
		boolean equals(Vertex B){
			if (this.actorName.equalsIgnoreCase(B.actorName)) return true;
			else return false;
		}
	}
	
	class Edge {
		Vertex endpoint;
		String movieName;
		
		public Edge(Vertex neighbor, String movieName) {
			this.endpoint = neighbor;
			this.movieName = movieName;
		}
		
	}
	
	
	/** Helper Functions **/
	void addVertex(String actorName, String movieName) 
	{
		Vertex v = (Vertex) getVertex(actorName);
		v.movieTitles.add(movieName);
		
		HashSet<String> castList = (HashSet<String>) movieCastLists.get(movieName);
		if (castList == null) {
			castList = new HashSet<String>();
		}
		castList.add(actorName);
		movieCastLists.put(movieName, castList);
	}
	
	Vertex getVertex (String actorName) {
		Vertex v = (Vertex) actorMap.get(actorName);
		if (v == null) {
			v = new Vertex(actorName);
			actorMap.put(actorName, v);
		}
		return v;
	}

	void addEdge(String source, String destination, String movieName) 
	{
		Vertex s = getVertex(source);
		Vertex d = getVertex(destination);
		s.neighbors.add(new Edge(d, movieName));
	}
	
	void buildGraph() {
		buildVerticesFromFile();
		//buildCastLists();
	}
	
	void buildVerticesFromFile()
	{
		BufferedReader in = null;
		String line;
		long begin = System.currentTimeMillis();
		
		System.out.println("Reading in file...");
		
		try {
			in = new BufferedReader(new FileReader(fileIn));
			while ((line = in.readLine()) != null)
			{
				String[] d = line.split(",");
				String actor = d[1];
				String film = d[3];
				addVertex(actor, film);
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
		System.out.println("File read. (" + (System.currentTimeMillis() - begin)/1000.0 +" s)");
	}
	
	/* public void buildCastLists () 
	{
		long begin = System.currentTimeMillis();
		System.out.println("Building edges...");
		
		for (Entry<String, HashSet<String>> s : movieCastLists.entrySet()) {
			String movie = s.getKey();
			HashSet<String> actors = s.getValue();
			for (String A : actors) {
				for (String B : actors) {
					 if ( !A.equals(B) ) 
						 addEdge(A, B, movie);
				}
			}
		}
		System.out.println("Completed. (" 
		+ (System.currentTimeMillis() - begin)/1000.0 + " s)");
	} */
	
	void shortestPath(String actorA, String actorB) 
	{
		Vertex A = (Vertex) getVertex(actorA);
		Vertex B = (Vertex) getVertex(actorB);
		BFS1(A,B);
		printPath(A,B);
	}
	
	void BFS1(Vertex A, Vertex B) 
	{
		System.out.println("Performing BFS...");
		
		long begin = System.currentTimeMillis();
		A.baconNumber = 0;
		HashSet<String> visitedByA = new HashSet<String>();
		HashSet<String> visitedByB = new HashSet<String>();
		HashSet<String> visistedByBoth = new HashSet<String>()
				;
		Queue<Vertex> qA = new LinkedList<Vertex>();
		Queue<Vertex> qB = new LinkedList<Vertex>();
		
		qA.add(A);
		qB.add(B);
		boolean searchA = false;
		while (!qA.isEmpty() && !qB.isEmpty()) {
			searchA = !searchA;
			
			if (searchA) {
				Vertex currentA = qA.remove();
				visitedByA.add(currentA.actorName);
				if (currentA.equals(B) | visitedByB.contains(currentA.actorName)) { 
					return;
				}
				else {
					for (Edge e : currentA.neighbors) {
						Vertex n = e.endpoint;
						if (!visitedByA.contains(n.actorName)) {
							n.baconNumber = currentA.baconNumber + 1;
							n.reversePath = new Edge(currentA, e.movieName);
							qA.add(n);
						}
					}
				}
			}
			else {
				Vertex currentB = qB.remove();
				visitedByB.add(currentB.actorName);
				if (currentB.equals(A) | visitedByA.contains(currentB.actorName)) { 
					return;
				}
				else {
					for (Edge e : currentB.neighbors) {
						Vertex n = e.endpoint;
						if (!visitedByA.contains(n.actorName)) {
							n.baconNumber = currentB.baconNumber + 1;
							n.reversePath = new Edge(currentB, e.movieName);
							qA.add(n);
						}
					}
				}
			}
		}
		
		System.out.println("Completed. (" + 
				(System.currentTimeMillis() - begin)/1000.0 + " s)");
	}
	
	void printPath (Vertex A, Vertex B) 
	{
		Vertex destination = B;
		while (!destination.equals(A)) {
			
			System.out.println(destination.actorName + " was in " 
					+ destination.reversePath.movieName  + " with "
					+ destination.reversePath.endpoint.actorName + ".");
			destination = destination.reversePath.endpoint;
		}
		System.out.println("----------------------------------------");
		System.out.println(B.actorName + "'s Bacon Number is " + B.baconNumber);
	}
	
	public static void main(String[] args) 
	{
		Graph g = new Graph();
		
		
		g.buildGraph();
		g.runTests();
		//g.shortestPath(actorA, actorB);
		
	}
	void runTests()
	{
		//testMovieList(); 	// Replaced by Cast List Hashes //
		//testMovieHash();
		testGetActorsFromMovieHash();
	}
	
	// 16,698 Movies --> addVertex() functions properly.
	/* void testMovieList()
	{
		System.out.println("Printing movies...");
		System.out.println("------------------");
		int count = 0;
		for (String s: allMovies) {
			System.out.println(s);
			count++;
		}
		System.out.println("------------------");
		System.out.println(count + " Movies added successfully.");

	}*/
	
	// 16,698 Movies --> addVertex() functions properly.
	void testMovieHash() 
	{
		System.out.println("Printing movies from Cast List Hash...");
		System.out.println("------------------");
		int count = 0;
		for (String s: movieCastLists.keySet()) {
			System.out.println(s);
			count++;
		}
		System.out.println("------------------");
		System.out.println(count + " Movies successfully Hashed.");
	}
	
	void testGetActorsFromMovieHash()
	{
		int movieCount = 0;
		for (Entry<String, HashSet<String>> s : movieCastLists.entrySet()) 
		{
			movieCount++;
			int actorCount = 0;
			String movie = s.getKey();
			HashSet<String> actors = s.getValue();
			System.out.println("Movie: " + movie);
			for (String A : actors) {
				actorCount++;
				System.out.println(A);
				
			}
			System.out.println("Total Actors: " + actorCount);
			System.out.println("----------------------");
		}
		System.out.println("Total Movies: " + movieCount);
	}
}
