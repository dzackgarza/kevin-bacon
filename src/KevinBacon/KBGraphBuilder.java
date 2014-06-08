package KevinBacon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

class KBGraphBuilder {
	// Actors to search for.
	String actor1 = "Kevin Bacon";
	String actor2 = "William Shatner";
	
	// 	File containing 156,467 actor/movie pairs. 
	static String fileIn = "actorfile.csv";
	
	// 	Hashes actor names to their associated vertex objects. Allows lookup by name.
	private HashMap<String, Actor> actorMap = new HashMap<String, Actor>();
	
	// 	Hashes movies to their cast lists. Used for edge creation between Actors.
	private HashMap<Movie, HashSet<Actor>> movieMap = new HashMap<Movie, HashSet<Actor>>();
	
	private HashMap<String, Movie> movieList = new HashMap<String, Movie>();
		
	/* 	Actors are vertices that maintain adjacency sets of coActors. */
	class Actor 
	{
		public String actorName;
		
		// 	Each Actor maintains a list of coActors, whose names hash to the movies shared with the Actor.
	    public HashMap<Actor, Movie> coActors = new HashMap<Actor, Movie>();
	    
	    /* 	Variables used for shortest path algorithms */
	    public int baconNumber = Integer.MAX_VALUE; // Keeps track of its distance from root in BFS.
	    public Actor previousInPath = null; 		// Used for backtracking.
	    
	    /* 	Default constructor, sets name. */
	    public Actor (String name) { actorName = name; }
	    
	    public boolean equals(Actor B) {
	    	if (this.actorName.equalsIgnoreCase(B.actorName)) return true;
	    	else return false;
	    }
	}
	
	class Graph 
	{
		/* 	Adds to an Actor's list of coActors. */
		public void addCoActor(Actor source, Actor destination, Movie movie) {
			source.coActors.put(destination, movie);
		}
		
		/* 	Creates new Actor objects. */
		public void addActor(String actorName, String movieName) {
			Actor a = getActor(actorName);
			updateMovieList(a, getMovie(movieName));
		}
		
		public Movie getMovie(String movieName) {
			Movie m = (Movie) movieList.get(movieName);
			if (m == null) {
				m = new Movie(movieName);
				movieList.put(movieName, m);
			}
			return m;
		}
		
		/* 	Lookup and return a Actor by name - also creates one if it doesn't exist. */
		public Actor getActor(String actorName) {
			Actor a = (Actor) actorMap.get(actorName);
			if (a == null) {
				a = new Actor(actorName);
				actorMap.put(actorName, a);
			}
			return a;
		}
		
		/* 	Given a movie, adds an actor to the list of actors for that movie. If the movie
		 * 	hasn't been added yet, adds it and initializes the list with the given actor. 
		 *  
		 *  The movie list is maintained to minimize the process of creating edges, which
		 *  will go as O(n^2) with the number of actors per movie.
		 */
		public void updateMovieList(Actor actor, Movie movie) 
		{
			HashSet<Actor> movieCast = (HashSet<Actor>) movieMap.get(movie);
			if (movieCast == null) {
				movieCast = new HashSet<Actor>();
				movieMap.put(movie, movieCast);
			}
			movieCast.add(actor);
		}
	}
	
	class Movie 
	{
		String movieName;
		public Movie(String name) { movieName = name; } 
	}
	
	/* 	Reads actors and movies from fileIn and constructs a graph. */ 
	public Graph buildVerticesFromFile() 
	{
		BufferedReader in = null;
		String line;
		Graph g = new Graph();
		
		System.out.println("Reading in file...");
		
		try {
			in = new BufferedReader(new FileReader(fileIn));
			while ((line = in.readLine()) != null)
			{
				String[] d = line.split(",");
				String actor = d[1];
				String film = d[3];
				g.addActor(actor, film);
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
		
		System.out.println("File read completed.");
		return g;
	}
	
	/* 	If two actors have been together in a given movie, builds an edge 
	 * 	between them. Short lists, but O(n^2) for actors. Might be able 
	 * 	to thread, since order doesn't matter. 
	 */
	public void buildEdges (Graph g) 
	{
		System.out.println("Building edges...");
		
		for (Entry<Movie, HashSet<Actor>> s : movieMap.entrySet()) {
			Movie movie = s.getKey();
			HashSet<Actor> actors = s.getValue();
			for (Actor A : actors) {
				for (Actor B : actors) {
					 if ( !A.equals(B) ) 
						 g.addCoActor(A, B, movie);
				}
			}
		}
		System.out.println("Completed.");
	}
	
	public void getShortestPath(Graph g, String actor1, String actor2) 
	{
		Actor A = g.getActor(actor1);
		Actor B = g.getActor(actor2);
		
		BFS(A, B);
		System.out.println(B.actorName + " has a Bacon number of " + B.baconNumber);
		
		printPath(A, B);
	}
	
	/*	Visits all nodes in a breadth-first search according to:
	 * 
	 * 	1: Enqueue Actor A.
	 * 	2: Dequeue a node and examine.
	 * 		2a: If Actor B is found, return.
	 * 		2b: Otherwise, enqueue coActors.
	 * 	3: Repeat until queue is empty.
	 */
	public void BFS(Actor A, Actor B) 
	{
		System.out.println("Finding shortest path..");
		
		long begin = System.currentTimeMillis();
		Queue<Actor> q = new LinkedList<Actor>();
		HashSet<Actor> visited = new HashSet<Actor>();
		
		A.baconNumber = 0;
		q.add(A); // Enqueue the root node.
		
		while (!q.isEmpty()) 
		{
			// Dequeue and examine.
			Actor current = (Actor) q.remove();
			if (current.equals(B)) return;
			
			// Not found, so mark as visited.
			visited.add(current);
			
			/// Queue all coActors.
			for (Actor a_i : current.coActors.keySet()) 
			{
				if (!visited.contains(a_i)) 
				{
					//  All of current's coActors are one level deeper in the tree.
					a_i.baconNumber = current.baconNumber + 1;
					//  Leave bread crumbs back to Actor A / root.
					a_i.previousInPath = current;
					q.add(a_i);
				}
			}
		}	
		
		System.out.print("\n");
		System.out.println("Traversal: Time elapsed " + 
				(System.currentTimeMillis() - begin)/1000.0 + " seconds.");
		visited.clear();
	} 	// End BFS
	
	public void BFS2 (Actor A, Actor B) 
	{
		long begin = System.currentTimeMillis();
		Queue<Actor> qA = new LinkedList<Actor>();
		Queue<Actor> qB = new LinkedList<Actor>();
		HashSet<Actor> visitedA = new HashSet<Actor>();
		HashSet<Actor> visitedB = new HashSet<Actor>();
		
		A.baconNumber = 0;
		
		qA.add(A);
		qB.add(B);
		
		boolean searchingA = false;
		
		while (!qA.isEmpty() && !qB.isEmpty()) {
			searchingA = !searchingA;
			if (searchingA) {
				
			}
			else 
			{
				
			}
		}
	}
	
	public void printPath (Actor A, Actor B) 
	{
		Actor start = A;
		Actor destination = B;
		
		while (destination != start) 
		{
			Actor closestActor = destination.previousInPath;
			System.out.println(
					destination.actorName 
					+ " was in " +
					(destination.coActors.get(closestActor)).movieName 
					+ " with " +
					closestActor.actorName);
			destination = destination.previousInPath;
		}
		
	}
	
	public static void main (String[] args)
	{
		KBGraphBuilder builder = new KBGraphBuilder();
		Graph g = builder.buildVerticesFromFile();
		builder.buildEdges(g);
		
		String actor1 = builder.actor1;
		String actor2 = builder.actor2;
		builder.getShortestPath(g, actor1, actor2);
	}
}





