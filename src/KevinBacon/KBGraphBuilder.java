package KevinBacon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

class KBGraphBuilder {
	
	// File containing 156,467 actor/movie pairs. 
	static String fileIn = "actorfile.csv";
	
	// Hashes actor names to vertices
	private HashMap<String, Actor> actorMap = new HashMap<String, Actor>();
	
	// Hashes movies to lists of their actors
	private HashMap<String, HashSet<String>> movieMap = new HashMap<String, HashSet<String>>();
		
	// Each vertex represents an actor, and has a set of edges.
	class Actor {
		public String actorName;
		public HashSet<String> movies; 
	    public ArrayList<Edge> coActors;
	    
	    /* Variables used for shortest path algorithms */
	    public int distance; // Keeps track of its distance from root in BFS.
	    public boolean visited;
	    public Actor previousInPath;

	    public Actor (String name){
	    	actorName = name;
	    	coActors = new ArrayList<Edge>();
	    	movies = new HashSet<String>();
	    	
	    	distance = Integer.MAX_VALUE;
	    	visited = false;
	    	previousInPath = null;
	    }
	    
	    public String getName() {
	    	return actorName;
	    }
	    
	    public Actor getPreviousActor () {
	    	return previousInPath;
	    }
	}
		
	/* 	Edges connect two actors that have been in the same movie.
	 	Each vertex has an edge, so an edge only needs to track its destination. */
	class Edge {
		public Actor nextActor; 	
		public String movieName;
		
		public Edge(String name, Actor destination) {
			nextActor = destination;
			movieName = name;
		}
	}
	
	
	class Graph {
		
		public void addEdge(String source, String destination, String movie) {
			Actor s = getActor(source);
			Actor d = getActor(destination);
			s.coActors.add(new Edge(movie, d));
		}
		
		public void addActor(String actor, String movie) {
			Actor s = getActor(actor);
			s.movies.add(movie); // Duplications should be taken care of by HashSet
			updateMovieList(actor, movie);
		}
		
		// Given a movie, adds an actor to the list of actors for that movie. If the movie
		// hasn't been added yet, adds it and initializes the list with the given actor.
		public void updateMovieList(String actor, String movie) {
			HashSet<String> cast = (HashSet<String>) movieMap.get(movie);
			if (cast == null) {
				cast = new HashSet<String>();
				movieMap.put(movie, cast);
			}
			cast.add(actor);
		}
		
		// Lookup and return a Actor by name - also creates one if it doesn't exist.
		public Actor getActor(String actorName) {
			Actor v = (Actor) actorMap.get(actorName);
			if (v == null) {
				v = new Actor(actorName);
				actorMap.put(actorName, v);
			}
			return v;
		}
	}
	
	// Reads actors and movies from fileIn and constructs a graph. 
	public Graph buildVertices() {
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
					 if ( u != w) g.addEdge(u,w,key);
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
	
	public void printPath (Graph g, Actor A, Actor B) {
		Actor start = A;
		Actor destination = B;
		
		System.out.println(B.getName() + " has a Bacon number of " + B.distance);
		while (destination != start) {
			System.out.println(destination.getName() + " was in a movie with " +
					destination.getPreviousActor().getName());
			destination = destination.previousInPath;
		}
		
	}
	
	public void BFS(Graph g, Actor A, Actor B) {
		int timer = 0;
		Queue<Actor> q = new LinkedList<Actor>();
		HashSet<Actor> visitedActors = new HashSet<Actor>();

		q.add(A); // Enqueue the root node.
		
		while (!q.isEmpty()) {
			Actor current = q.poll(); // Dequeue and examine. *(poll throws null instead of an exception)
			visitedActors.add(current);
			if (timer++ % 1000 == 0)
				System.out.print("#");
			if (timer % 10000 == 0)
				System.out.print("\n");
			for (Edge a : current.coActors) {
				Actor a_i = a.nextActor;
				if (!visitedActors.contains(a_i)) {
					q.add(a_i);
					a_i.distance = current.distance + 1;
					a_i.previousInPath = current;
				}
			}
			System.out.print("\n");
		}		
	}
	
	public void printActorsByMovie (Graph g) {
		for (Entry<String, HashSet<String>> s : movieMap.entrySet()) {
			String key = s.getKey();
			System.out.println("Movie: " + key + "\nActors:");
			HashSet<String> actors = s.getValue();
			for (String w : actors) {
				System.out.println(w);
			}
			System.out.println("---------------");
		}
	}
	
	public static void main (String[] args)
	{
		String actor1 = "Muse Watson";
		String actor2 = "Adam Hendershott";
		
		KBGraphBuilder builder = new KBGraphBuilder();
		Graph graph = builder.buildVertices();	
		builder.buildEdges(graph);
		System.out.println("Finding shortest path..");
		builder.getShortestPath(graph, actor1, actor2);
	}
}

class KBGraph {
		//g.displayGraphStats();
		//g.displayResults();
	
}





