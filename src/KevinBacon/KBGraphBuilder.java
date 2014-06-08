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
	String actor2 = "Adam Hendershott";
	
	// 	File containing 156,467 actor/movie pairs. 
	static String fileIn = "actorfile.csv";
	
	// 	Hashes actor names to their associated vertex objects.
	private HashMap<String, Actor> actorMap = new HashMap<String, Actor>();
	
	// 	Hashes movies to lists of their cast lists.
	private HashMap<String, HashSet<String>> movieMap = new HashMap<String, HashSet<String>>();
		
	/* 	Actors are vertices that maintain adjacency sets of coActors. */
	class Actor {
		public String actorName;
		// 	Each actor maintains a list of movies they have been in.
		public HashSet<String> movies;
		// 	Each Actor maintains a list of coactors, whose names hash to the movies shared with the Actor.
	    public HashMap<Actor, Movie> coActors;
	    
	    /* 	Variables used for shortest path algorithms */
	    public int distance; 			// Keeps track of its distance from root in BFS.
	    public Actor previousInPath; 	// Used for backtracking.
	    
	    /* 	Default constructor, initializes variables. */
	    public Actor (String name){
	    	actorName = name;
	    	coActors = new HashMap<Actor, Movie>();
	    	movies = new HashSet<String>();
	    	distance = Integer.MAX_VALUE;
	    	previousInPath = null;
	    }
	}
	
	/* Just an abstraction to make the HashMaps more intuitive. */
	class Movie {
		public String movieName;
		
		public Movie(String name) {
			movieName = name;
		}
	}
	
	
	class Graph {
		
		/* 	Adds to an Actor's coActor list. */
		public void addCoActor(String source, String destination, String movie) {
			Actor s = getActor(source);
			Actor d = getActor(destination);
			s.coActors.put(d, new Movie(movie));
		}
		
		/* 	Creates new Actor objects. */
		public void addActor(String actor, String movie) {
			Actor s = getActor(actor);
			s.movies.add(movie); // Duplications should be taken care of by HashSet
			updateMovieList(actor, movie);
		}
		
		/* 	Given a movie, adds an actor to the list of actors for that movie. If the movie
		 	hasn't been added yet, adds it and initializes the list with the given actor. */
		public void updateMovieList(String actor, String movie) {
			HashSet<String> cast = (HashSet<String>) movieMap.get(movie);
			if (cast == null) {
				cast = new HashSet<String>();
				movieMap.put(movie, cast);
			}
			cast.add(actor);
		}
		
		/* 	Lookup and return a Actor by name - also creates one if it doesn't exist. */
		public Actor getActor(String actorName) {
			Actor v = (Actor) actorMap.get(actorName);
			if (v == null) {
				v = new Actor(actorName);
				actorMap.put(actorName, v);
			}
			return v;
		}
	}
	
	/* 	Reads actors and movies from fileIn and constructs a graph. */ 
	public Graph buildVerticesFromFile() {
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
	
	/* 	If two actors have been together in a given movie, builds an edge between them.
	 	Note - pressure point for speed. Could use tweaking. Short lists, but O(n^2) for actors. */
	public void buildEdges (Graph g) {
		System.out.println("Building edges...");
		
		for (Entry<String, HashSet<String>> s : movieMap.entrySet()) {
			String key = s.getKey();
			HashSet<String> actors = s.getValue();
			for (String w : actors) {
				for (String u : actors) {
					 if ( u != w) g.addCoActor(u, w, key);
				}
			}
		}
		
		System.out.println("Completed.");
	}
	
	public void getShortestPath(Graph g, String actor1, String actor2) {
		Actor A = g.getActor(actor1);
		Actor B = g.getActor(actor2);
		A.distance = 0;
		
		BFS(g, A, B);
		printPath(g,A,B);
	}
	
	/*	Visits all nodes in a breadth-first search according to:
	 * 
	 * 	1: Enqueue Actor A.
	 * 	2: Dequeue a node and examine.
	 * 		2a: If Actor B is found, return.
	 * 		2b: Otherwise, enqueue coActors.
	 * 	3: Repeat until queue is empty.
	 */
	public void BFS(Graph g, Actor A, Actor B) {
		System.out.println("Finding shortest path..");
		
		int timer = 0;
		long begin = System.currentTimeMillis();
		Queue<Actor> q = new LinkedList<Actor>();
		HashSet<Actor> visitedActors = new HashSet<Actor>();

		q.add(A); // Enqueue the root node.
		while (!q.isEmpty()) {
			
			// Dequeue and examine. *(poll throws null instead of an exception)
			Actor current = q.poll(); 
			visitedActors.add(current);
			if (timer++ % 10000 == 0)
				System.out.print("#");
			if (timer % 100000 == 0)
				System.out.print("\n");
			
			for (Actor a_i : current.coActors.keySet()) {
				if (!visitedActors.contains(a_i)) {
					q.add(a_i);
					a_i.distance = current.distance + 1;
					a_i.previousInPath = current;
				}
				//if (a_i == B) return;
			}
		}	
		
		System.out.print("\n");
		System.out.println("Traversal: Time elapsed " + 
				(System.currentTimeMillis() - begin)/1000.0 + "seconds.");
	} 	// End BFS
	
	public void printPath (Graph g, Actor A, Actor B) {
		Actor start = A;
		Actor destination = B;
		
		System.out.println(B.actorName + " has a Bacon number of " + B.distance);
		while (destination != start) {
			System.out.println(destination.actorName + " was in " +
					destination.coActors.get(destination.previousInPath.actorName) 
					+ "with " +
					destination.previousInPath.actorName);
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





